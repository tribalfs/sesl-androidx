/*
 * Copyright 2023 The Android Open Source Project
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

package androidx.bluetooth

import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult as FwkScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.annotation.RestrictTo
import java.util.UUID
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Entry point for BLE related operations. This class provides a way to perform Bluetooth LE
 * operations such as scanning, advertising, and connection with a respective [BluetoothDevice].
 *
 */
class BluetoothLe(private val context: Context) {

    private companion object {
        private const val TAG = "BluetoothLe"
    }

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    /**
     * Returns a [Flow] to start Bluetooth LE Advertising. When the flow is successfully collected,
     * the operation status [AdvertiseResult] will be delivered via the
     * flow [kotlinx.coroutines.channels.Channel].
     *
     * @param advertiseParams [AdvertiseParams] for Bluetooth LE advertising.
     * @return A [Flow] with [AdvertiseResult] status in the data stream.
     */
    @RequiresPermission("android.permission.BLUETOOTH_ADVERTISE")
    fun advertise(advertiseParams: AdvertiseParams): Flow<Int> =
        callbackFlow {
            val callback = object : AdvertiseCallback() {
                override fun onStartFailure(errorCode: Int) {
                    Log.d(TAG, "onStartFailure() called with: errorCode = $errorCode")

                    when (errorCode) {
                        AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE ->
                            trySend(AdvertiseResult.ADVERTISE_FAILED_DATA_TOO_LARGE)

                        AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED ->
                            trySend(AdvertiseResult.ADVERTISE_FAILED_FEATURE_UNSUPPORTED)

                        AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR ->
                            trySend(AdvertiseResult.ADVERTISE_FAILED_INTERNAL_ERROR)

                        AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS ->
                            trySend(AdvertiseResult.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS)
                    }
                }

                override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                    Log.d(TAG, "onStartSuccess() called with: settingsInEffect = $settingsInEffect")

                    trySend(AdvertiseResult.ADVERTISE_STARTED)
                }
            }

            val bluetoothAdapter = bluetoothManager.adapter
            val bleAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser

            val advertiseSettings = with(AdvertiseSettings.Builder()) {
                advertiseParams.isConnectable.let { setConnectable(it) }
                advertiseParams.timeoutMillis.let { setTimeout(it) }
                // TODO(ofy) Add when AndroidX is targeting Android U
//                advertiseParams.isDiscoverable.let { setDiscoverable(it) }
                build()
            }

            val advertiseData = with(AdvertiseData.Builder()) {
                advertiseParams.shouldIncludeDeviceName.let { setIncludeDeviceName(it) }
                advertiseParams.serviceData.forEach {
                    addServiceData(ParcelUuid(it.key), it.value)
                }
                advertiseParams.manufacturerData.forEach {
                    addManufacturerData(it.key, it.value)
                }
                advertiseParams.serviceUuids.forEach {
                    addServiceUuid(ParcelUuid(it))
                }
                build()
            }

            Log.d(TAG, "bleAdvertiser.startAdvertising($advertiseSettings, $advertiseData) called")
            bleAdvertiser.startAdvertising(advertiseSettings, advertiseData, callback)

            awaitClose {
                Log.d(TAG, "bleAdvertiser.stopAdvertising() called")
                bleAdvertiser.stopAdvertising(callback)
            }
        }

    /**
     * Returns a cold [Flow] to start Bluetooth LE scanning. Scanning is used to
     * discover advertising devices nearby.
     *
     * @param filters [ScanFilter]s for finding exact Bluetooth LE devices.
     *
     * @return A cold [Flow] of [ScanResult] that matches with the given scan filter.
     */
    @RequiresPermission("android.permission.BLUETOOTH_SCAN")
    fun scan(filters: List<ScanFilter> = emptyList()): Flow<ScanResult> = callbackFlow {
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: FwkScanResult) {
                trySend(ScanResult(result))
            }

            override fun onScanFailed(errorCode: Int) {
                // TODO(b/270492198): throw precise exception
                cancel("onScanFailed() called with: errorCode = $errorCode")
            }
        }

        val bluetoothAdapter = bluetoothManager.adapter
        val bleScanner = bluetoothAdapter?.bluetoothLeScanner
        val scanSettings = ScanSettings.Builder().build()

        bleScanner?.startScan(filters, scanSettings, callback)

        awaitClose {
            Log.d(TAG, "awaitClose() called")
            bleScanner?.stopScan(callback)
        }
    }

    /**
     * Scope for operations as a GATT client role.
     *
     * @see connectGatt
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    interface GattClientScope {

        /**
         * Gets the services discovered from the remote device
         */
        fun getServices(): List<GattService>

        /**
         * Gets the service of the remote device by UUID.
         *
         * If multiple instances of the same service exist, the first instance of the service
         * is returned.
         */
        fun getService(uuid: UUID): GattService?

        /**
         * Reads the given remote characteristic.
         *
         * @param characteristic a remote [GattCharacteristic] to read
         * @return The value of the characteristic
         */
        suspend fun readCharacteristic(characteristic: GattCharacteristic):
            Result<ByteArray>

        /**
         * Writes the given value to the given remote characteristic.
         *
         * @param characteristic a remote [GattCharacteristic] to write
         * @param value a value to be written.
         * @param writeType [GattCharacteristic.WRITE_TYPE_DEFAULT],
         * [GattCharacteristic.WRITE_TYPE_NO_RESPONSE], or
         * [GattCharacteristic.WRITE_TYPE_SIGNED].
         * @return the result of the write operation
         */
        suspend fun writeCharacteristic(
            characteristic: GattCharacteristic,
            value: ByteArray,
            writeType: Int
        ): Result<Unit>

        /**
         * Returns a _cold_ [Flow] that contains the indicated value of the given characteristic.
         */
        fun subscribeToCharacteristic(characteristic: GattCharacteristic): Flow<ByteArray>

        /**
         * Suspends the current coroutine until the pending operations are handled and the connection
         * is closed, then it invokes the given [block] before resuming the coroutine.
         */
        suspend fun awaitClose(block: () -> Unit)
    }

    /**
     * Connects to the GATT server on the remote Bluetooth device and
     * invokes the given [block] after the connection is made.
     *
     * The block may not be run if connection fails.
     *
     * @param device a [BluetoothDevice] to connect to
     * @param block a block of code that is invoked after the connection is made.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    suspend fun <R> connectGatt(
        device: BluetoothDevice,
        block: suspend GattClientScope.() -> R
    ): R? {
        return GattClientImpl().connect(context, device, block)
    }
}
