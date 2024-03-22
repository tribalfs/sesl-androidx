/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.camera.camera2.pipe.integration.adapter

import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.params.InputConfiguration
import android.hardware.camera2.params.StreamConfigurationMap
import android.os.Build
import android.util.Size
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import androidx.camera.camera2.pipe.CameraMetadata.Companion.supportsPrivateReprocessing
import androidx.camera.camera2.pipe.core.Log
import androidx.camera.camera2.pipe.integration.config.CameraScope
import androidx.camera.camera2.pipe.integration.impl.CameraProperties
import androidx.camera.camera2.pipe.integration.impl.area
import androidx.camera.core.ImageProxy
import androidx.camera.core.MetadataImageReader
import androidx.camera.core.SafeCloseImageReaderProxy
import androidx.camera.core.impl.CameraCaptureCallback
import androidx.camera.core.impl.DeferrableSurface
import androidx.camera.core.impl.ImmediateSurface
import androidx.camera.core.impl.SessionConfig
import androidx.camera.core.impl.utils.executor.CameraXExecutors
import javax.inject.Inject

interface ZslControl {

    /**
     * Adds zero-shutter lag config to [SessionConfig].
     *
     * @param sessionConfigBuilder session config builder.
     */
    fun addZslConfig(sessionConfigBuilder: SessionConfig.Builder)

    /**
     * Determines whether the provided [DeferrableSurface] belongs to ZSL.
     *
     * @param surface The deferrable Surface to check.
     * @param sessionConfig The session configuration where its input configuration will be used to
     * determine whether the deferrable Surface belongs to ZSL.
     */
    fun isZslSurface(surface: DeferrableSurface, sessionConfig: SessionConfig): Boolean

    /**
     * Sets the flag if zero-shutter lag needs to be disabled by user case config.
     *
     *
     *  Zero-shutter lag will be disabled when any of the following conditions:
     *
     *  *  Extension is ON
     *  *  VideoCapture is ON
     *
     *
     * @param disabled True if zero-shutter lag should be disabled. Otherwise, should not be
     * disabled. However, enabling zero-shutter lag needs other conditions e.g.
     * flash mode OFF, so setting to false doesn't guarantee zero-shutter lag to
     * be always ON.
     */
    fun setZslDisabledByUserCaseConfig(disabled: Boolean)

    /**
     * Checks if zero-shutter lag is disabled by user case config.
     *
     * @return True if zero-shutter lag should be disabled. Otherwise, returns false.
     */
    fun isZslDisabledByUserCaseConfig(): Boolean

    /**
     * Sets the flag if zero-shutter lag needs to be disabled by flash mode.
     *
     *
     *  Zero-shutter lag will be disabled when flash mode is not OFF.
     *
     * @param disabled True if zero-shutter lag should be disabled. Otherwise, should not be
     * disabled. However, enabling zero-shutter lag needs other conditions e.g.
     * Extension is OFF and VideoCapture is OFF, so setting to false doesn't
     * guarantee zero-shutter lag to be always ON.
     */
    fun setZslDisabledByFlashMode(disabled: Boolean)

    /**
     * Checks if zero-shutter lag is disabled by flash mode.
     *
     * @return True if zero-shutter lag should be disabled. Otherwise, returns false.
     */
    fun isZslDisabledByFlashMode(): Boolean

    /**
     * Dequeues [ImageProxy] from ring buffer.
     *
     * @return [ImageProxy].
     */
    fun dequeueImageFromBuffer(): ImageProxy?
}

@RequiresApi(Build.VERSION_CODES.M)
@CameraScope
class ZslControlImpl @Inject constructor(
    private val cameraProperties: CameraProperties
) : ZslControl {
    private val cameraMetadata = cameraProperties.metadata
    private val streamConfigurationMap: StreamConfigurationMap by lazy {
        checkNotNull(cameraMetadata[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP])
    }

    @VisibleForTesting
    internal var reprocessingImageReader: SafeCloseImageReaderProxy? = null
    private var metadataMatchingCaptureCallback: CameraCaptureCallback? = null
    private var reprocessingImageDeferrableSurface: DeferrableSurface? = null

    override fun addZslConfig(sessionConfigBuilder: SessionConfig.Builder) {
        reset()

        // TODO: b/331256951 - Port ZslDisabledByUserCaseConfig
        // TODO: b/267559511 - Port ZslDisablerQuirk

        if (!cameraMetadata.supportsPrivateReprocessing) {
            Log.info { "ZslControlImpl: Private reprocessing isn't supported" }
            return
        }

        val size = streamConfigurationMap.getInputSizes(FORMAT).toList().maxBy { it.area() }
        if (size == null) {
            Log.warn { "ZslControlImpl: Unable to find a supported size for ZSL" }
            return
        }
        Log.debug { "ZslControlImpl: Selected ZSL size: $size" }

        val isJpegValidOutput =
            streamConfigurationMap.getValidOutputFormatsForInput(FORMAT).contains(ImageFormat.JPEG)
        if (!isJpegValidOutput) {
            Log.warn { "ZslControlImpl: JPEG isn't valid output for ZSL format" }
            return
        }

        val metadataImageReader = MetadataImageReader(
            size.width,
            size.height,
            FORMAT,
            MAX_IMAGES
        )
        val metadataCaptureCallback = metadataImageReader.cameraCaptureCallback
        val reprocImageReader = SafeCloseImageReaderProxy(metadataImageReader)
        metadataImageReader.setOnImageAvailableListener(
            { reader ->
                try {
                    val imageProxy = reader.acquireLatestImage()
                    // TODO: b/330404301 - Queue the image into the ZSL ring buffer instead.
                    imageProxy?.close()
                } catch (e: IllegalStateException) {
                    Log.error { "Failed to acquire latest image" }
                }
            }, CameraXExecutors.ioExecutor()
        )

        // Init the reprocessing image reader surface and add into the target surfaces of capture
        val reprocDeferrableSurface = ImmediateSurface(
            checkNotNull(reprocImageReader.surface),
            Size(reprocImageReader.width, reprocImageReader.height),
            FORMAT
        )

        reprocDeferrableSurface.terminationFuture.addListener(
            { reprocImageReader.safeClose() },
            CameraXExecutors.mainThreadExecutor()
        )
        sessionConfigBuilder.addSurface(reprocDeferrableSurface)

        // Init capture and session state callback and enqueue the total capture result
        sessionConfigBuilder.addCameraCaptureCallback(metadataCaptureCallback)

        // Set input configuration for reprocessing capture request
        sessionConfigBuilder.setInputConfiguration(
            InputConfiguration(
                reprocImageReader.width,
                reprocImageReader.height,
                reprocImageReader.imageFormat,
            )
        )

        metadataMatchingCaptureCallback = metadataCaptureCallback
        reprocessingImageReader = reprocImageReader
        reprocessingImageDeferrableSurface = reprocDeferrableSurface
    }

    override fun isZslSurface(surface: DeferrableSurface, sessionConfig: SessionConfig): Boolean {
        val inputConfig = sessionConfig.inputConfiguration
        return surface.prescribedStreamFormat == inputConfig?.format &&
            surface.prescribedSize.width == inputConfig.width &&
            surface.prescribedSize.height == inputConfig.height
    }

    override fun setZslDisabledByUserCaseConfig(disabled: Boolean) {
        TODO("b/331256951 - Port ZslDisabledByUserCaseConfig")
    }

    override fun isZslDisabledByUserCaseConfig(): Boolean {
        TODO("b/331256951 - Port ZslDisabledByUserCaseConfig")
    }

    override fun setZslDisabledByFlashMode(disabled: Boolean) {
        TODO("b/331256916 - Port ZslDisabledByFlashMode")
    }

    override fun isZslDisabledByFlashMode(): Boolean {
        TODO("b/331256916 - Port ZslDisabledByFlashMode")
    }

    override fun dequeueImageFromBuffer(): ImageProxy? {
        TODO("b/330405430 - Add ZSL ring buffer and build requests")
    }

    private fun reset() {
        // TODO: b/330404301 - Reset the ring buffer when we have it.
        val reprocImageDeferrableSurface = reprocessingImageDeferrableSurface
        if (reprocImageDeferrableSurface != null) {
            val reprocImageReaderProxy = reprocessingImageReader
            if (reprocImageReaderProxy != null) {
                reprocImageDeferrableSurface.terminationFuture.addListener(
                    { reprocImageReaderProxy.safeClose() },
                    CameraXExecutors.mainThreadExecutor()
                )
                reprocessingImageReader = null
            }
            reprocImageDeferrableSurface.close()
            reprocessingImageDeferrableSurface = null
        }
    }

    companion object {
        // Due to b/232268355 and feedback from pixel team that private format will have better
        // performance, we will use private only for zsl.
        private const val FORMAT = ImageFormat.PRIVATE

        @VisibleForTesting
        internal const val RING_BUFFER_CAPACITY = 3

        @VisibleForTesting
        internal const val MAX_IMAGES = RING_BUFFER_CAPACITY * 3
    }
}

/**
 * No-Op implementation for [ZslControl].
 */
class ZslControlNoOpImpl @Inject constructor() : ZslControl {
    override fun addZslConfig(sessionConfigBuilder: SessionConfig.Builder) {
    }

    override fun isZslSurface(surface: DeferrableSurface, sessionConfig: SessionConfig) = false

    override fun setZslDisabledByUserCaseConfig(disabled: Boolean) {
    }

    override fun isZslDisabledByUserCaseConfig() = false

    override fun setZslDisabledByFlashMode(disabled: Boolean) {
    }

    override fun isZslDisabledByFlashMode() = false

    override fun dequeueImageFromBuffer(): ImageProxy? {
        return null
    }
}
