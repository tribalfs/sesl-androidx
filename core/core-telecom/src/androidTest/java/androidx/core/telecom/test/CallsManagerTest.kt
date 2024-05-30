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

package androidx.core.telecom.test

import android.os.Build.VERSION_CODES
import android.telecom.CallException
import android.telecom.DisconnectCause
import android.telecom.PhoneAccount.CAPABILITY_SELF_MANAGED
import android.telecom.PhoneAccount.CAPABILITY_SUPPORTS_CALL_STREAMING
import android.telecom.PhoneAccount.CAPABILITY_SUPPORTS_TRANSACTIONAL_OPERATIONS
import android.telecom.PhoneAccount.CAPABILITY_SUPPORTS_VIDEO_CALLING
import android.telecom.PhoneAccount.CAPABILITY_VIDEO_CALLING
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.telecom.CallAttributesCompat
import androidx.core.telecom.CallEndpointCompat
import androidx.core.telecom.CallsManager
import androidx.core.telecom.internal.AddCallResult
import androidx.core.telecom.internal.ConnectionServiceProduction
import androidx.core.telecom.internal.TelecomManagerProduction
import androidx.core.telecom.internal.utils.Utils
import androidx.core.telecom.test.utils.BaseTelecomTest
import androidx.core.telecom.test.utils.ConnectionServiceFailPlatformSide
import androidx.core.telecom.test.utils.TelecomManagerFailPlatformSide
import androidx.core.telecom.test.utils.TestUtils
import androidx.core.telecom.util.ExperimentalAppActions
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.filters.SmallTest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@SdkSuppress(minSdkVersion = VERSION_CODES.O /* api=26 */)
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@RequiresApi(VERSION_CODES.O)
@RunWith(AndroidJUnit4::class)
class CallsManagerTest : BaseTelecomTest() {
    private val mTestClassName = "androidx.core.telecom.test"

    companion object {
        val TAG = CallsManagerTest::class.java.simpleName
    }

    @SmallTest
    @Test
    fun testGetPhoneAccountWithUBuild() {
        try {
            Utils.setUtils(TestUtils.mV2Build)
            val account = mCallsManager.getPhoneAccountHandleForPackage()
            assertEquals(mTestClassName, account.componentName.className)
        } finally {
            Utils.resetUtils()
        }
    }

    @SmallTest
    @Test
    fun testGetPhoneAccountWithUBuildWithTminusBuild() {
        try {
            Utils.setUtils(TestUtils.mBackwardsCompatBuild)
            val account = mCallsManager.getPhoneAccountHandleForPackage()
            assertEquals(CallsManager.CONNECTION_SERVICE_CLASS, account.componentName.className)
        } finally {
            Utils.resetUtils()
        }
    }

    @SmallTest
    @Test
    fun testGetPhoneAccountWithInvalidBuild() {
        try {
            Utils.setUtils(TestUtils.mInvalidBuild)
            assertThrows(UnsupportedOperationException::class.java) {
                mCallsManager.getPhoneAccountHandleForPackage()
            }
        } finally {
            Utils.resetUtils()
        }
    }

    @SmallTest
    @Test
    fun testRegisterPhoneAccount() {
        Utils.resetUtils()

        if (Utils.hasInvalidBuildVersion()) {
            assertThrows(UnsupportedOperationException::class.java) {
                mCallsManager.registerAppWithTelecom(CallsManager.CAPABILITY_BASELINE)
            }
        } else {

            mCallsManager.registerAppWithTelecom(CallsManager.CAPABILITY_BASELINE)
            val account = mCallsManager.getBuiltPhoneAccount()!!
            assertNotNull(account.extras)
            if (Utils.hasPlatformV2Apis()) {
                assertTrue(
                    Utils.hasCapability(
                        CAPABILITY_SUPPORTS_TRANSACTIONAL_OPERATIONS,
                        account.capabilities
                    )
                )
            } else {
                assertTrue(
                    account.capabilities and CAPABILITY_SELF_MANAGED == CAPABILITY_SELF_MANAGED
                )
            }
        }
    }

    /**
     * Register all the capabilities currently exposed by the CallsManager class and verify they are
     * re-mapped to the correct platform capabilities.
     */
    @SdkSuppress(minSdkVersion = VERSION_CODES.UPSIDE_DOWN_CAKE)
    @SmallTest
    @Test
    fun testRegisterAllCapabilities() {
        setUpV2Test()
        mCallsManager.registerAppWithTelecom(
            CallsManager.CAPABILITY_SUPPORTS_VIDEO_CALLING or
                CallsManager.CAPABILITY_SUPPORTS_CALL_STREAMING
        )

        val phoneAccount = mCallsManager.getBuiltPhoneAccount()!!
        assertTrue(phoneAccount.hasCapabilities(CAPABILITY_SELF_MANAGED))
        assertTrue(phoneAccount.hasCapabilities(CAPABILITY_SUPPORTS_TRANSACTIONAL_OPERATIONS))
        assertTrue(phoneAccount.hasCapabilities(CAPABILITY_SUPPORTS_VIDEO_CALLING))
        assertTrue(phoneAccount.hasCapabilities(CAPABILITY_VIDEO_CALLING))
        assertTrue(phoneAccount.hasCapabilities(CAPABILITY_SUPPORTS_CALL_STREAMING))
    }

    /**
     * Ensure all backwards compat builds can register video capabilities and the values are
     * re-mapped to the correct platform capabilities.
     */
    @SdkSuppress(minSdkVersion = VERSION_CODES.O)
    @SmallTest
    @Test
    fun testRegisterVideoCapabilitiesOnly() {
        setUpBackwardsCompatTest()
        mCallsManager.registerAppWithTelecom(CallsManager.CAPABILITY_SUPPORTS_VIDEO_CALLING)

        val phoneAccount = mCallsManager.getBuiltPhoneAccount()!!
        assertTrue(phoneAccount.hasCapabilities(CAPABILITY_SELF_MANAGED))
        assertTrue(phoneAccount.hasCapabilities(CAPABILITY_SUPPORTS_VIDEO_CALLING))
        assertTrue(phoneAccount.hasCapabilities(CAPABILITY_VIDEO_CALLING))
    }

    /**
     * Verify that calls starting in the video state that are originally initialized with the
     * earpiece route are switched to the speaker phone audio route. This test creates VoIP calls
     * using the APIs introduced in Android U.
     */
    @SdkSuppress(minSdkVersion = VERSION_CODES.UPSIDE_DOWN_CAKE)
    @SmallTest
    @Test
    fun testAddOutgoingVideoCall_CallEndpointShouldBeSpeaker_Transactional() {
        setUpV2Test()
        runBlocking { assertVideoCallStartsWithSpeakerEndpoint() }
    }

    /**
     * Verify that calls starting in the video state that are originally initialized with the
     * earpiece route are switched to the speaker phone audio route. This test creates VoIP calls
     * using the legacy ConnectionService method.
     */
    @SdkSuppress(minSdkVersion = VERSION_CODES.O)
    @SmallTest
    @Test
    fun testAddOutgoingVideoCall_CallEndpointShouldBeSpeaker_BackwardsCompat() {
        setUpBackwardsCompatTest()
        runBlocking { assertVideoCallStartsWithSpeakerEndpoint() }
    }

    @SdkSuppress(minSdkVersion = VERSION_CODES.UPSIDE_DOWN_CAKE)
    @OptIn(ExperimentalAppActions::class)
    @SmallTest
    @Test
    fun testPauseExecutionOrThrow_Transactional() {
        setUpV2Test()
        runBlocking {
            val cd = CompletableDeferred<AddCallResult>()
            cd.complete(AddCallResult.SuccessCallSession())
            mCallsManager.pauseExecutionUntilCallIsReadyOrTimeout(cd)
        }
    }

    @SdkSuppress(minSdkVersion = VERSION_CODES.UPSIDE_DOWN_CAKE)
    @SmallTest
    @Test
    fun testFailPlatformSide_Transactional() {
        setUpV2Test()
        val telecomManagerFailPlatformSide = TelecomManagerFailPlatformSide()
        val expectedCallExceptionCode = CallException.CODE_CALL_NOT_PERMITTED_AT_PRESENT_TIME
        telecomManagerFailPlatformSide.setCallException(expectedCallExceptionCode)
        mCallsManager.setTelecomManagerAdapter(telecomManagerFailPlatformSide)
        try {
            runBlocking {
                val waitForCallException =
                    CompletableDeferred<androidx.core.telecom.CallException>()
                try {
                    mCallsManager.addCall(
                        TestUtils.INCOMING_CALL_ATTRIBUTES,
                        TestUtils.mOnAnswerLambda,
                        TestUtils.mOnDisconnectLambda,
                        TestUtils.mOnSetActiveLambda,
                        TestUtils.mOnSetInActiveLambda,
                    ) {}
                } catch (e: androidx.core.telecom.CallException) {
                    waitForCallException.complete(e)
                }
                waitForCallException.await()
                assertEquals(expectedCallExceptionCode, waitForCallException.getCompleted().code)
            }
        } finally {
            mCallsManager.setTelecomManagerAdapter(TelecomManagerProduction(mContext))
        }
    }

    @SdkSuppress(minSdkVersion = VERSION_CODES.O)
    @SmallTest
    @Test
    fun testFailPlatformSide_BackwardsCompat() {
        setUpBackwardsCompatTest()
        mCallsManager.setTelecomManagerAdapter(TelecomManagerFailPlatformSide())
        mCallsManager.setConnectionServiceAdapter(ConnectionServiceFailPlatformSide())
        try {
            runBlocking {
                val waitForCallException =
                    CompletableDeferred<androidx.core.telecom.CallException>()
                try {
                    mCallsManager.addCall(
                        TestUtils.INCOMING_CALL_ATTRIBUTES,
                        TestUtils.mOnAnswerLambda,
                        TestUtils.mOnDisconnectLambda,
                        TestUtils.mOnSetActiveLambda,
                        TestUtils.mOnSetInActiveLambda,
                    ) {}
                } catch (e: androidx.core.telecom.CallException) {
                    waitForCallException.complete(e)
                }
                waitForCallException.await()
                assertEquals(
                    androidx.core.telecom.CallException.ERROR_UNKNOWN,
                    waitForCallException.getCompleted().code
                )
            }
        } finally {
            mCallsManager.setTelecomManagerAdapter(TelecomManagerProduction(mContext))
            mCallsManager.setConnectionServiceAdapter(ConnectionServiceProduction())
        }
    }

    @SdkSuppress(minSdkVersion = VERSION_CODES.O)
    @OptIn(ExperimentalAppActions::class)
    @SmallTest
    @Test
    fun testPauseExecutionOrThrow_BackwardsCompat() {
        setUpBackwardsCompatTest()
        runBlocking {
            val cd = CompletableDeferred<AddCallResult>()
            cd.complete(AddCallResult.Error(CallException.CODE_ERROR_UNKNOWN))
            try {
                mCallsManager.pauseExecutionUntilCallIsReadyOrTimeout(cd)
                fail(
                    "failed to throw a CallException out to the client when the platform signaled" +
                        " it failed to add the call"
                )
            } catch (e: androidx.core.telecom.CallException) {
                Log.i(TAG, "callException=[$e] was thrown as expected")
            }
        }
    }

    suspend fun assertVideoCallStartsWithSpeakerEndpoint() {
        assertWithinTimeout_addCall(
            CallAttributesCompat(
                TestUtils.OUTGOING_NAME,
                TestUtils.TEST_PHONE_NUMBER_8985,
                CallAttributesCompat.DIRECTION_OUTGOING,
                CallAttributesCompat.CALL_TYPE_VIDEO_CALL
            )
        ) {
            launch {
                val waitUntilSpeakerEndpointJob = CompletableDeferred<CallEndpointCompat>()

                val flowsJob = launch {
                    val speakerFlow =
                        currentCallEndpoint.filter { it.type == CallEndpointCompat.TYPE_SPEAKER }

                    speakerFlow.collect {
                        Log.i(TAG, "speakerFlow.collect=[$it]")
                        waitUntilSpeakerEndpointJob.complete(it)
                    }
                }

                Log.i(TAG, "before await")
                waitUntilSpeakerEndpointJob.await()
                Log.i(TAG, "after await")

                // at this point, the CallEndpoint has been found
                val speakerEndpoint = waitUntilSpeakerEndpointJob.getCompleted()
                assertNotNull(speakerEndpoint)
                assertEquals(CallEndpointCompat.TYPE_SPEAKER, speakerEndpoint.type)

                // finally, terminate the call
                disconnect(DisconnectCause(DisconnectCause.LOCAL))
                // stop collecting flows so the test can end
                flowsJob.cancel()
                Log.i(TAG, " flowsJob.cancel()")
            }
        }
    }
}
