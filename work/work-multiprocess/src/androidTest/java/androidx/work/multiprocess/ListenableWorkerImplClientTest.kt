/*
 * Copyright 2021 The Android Open Source Project
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

package androidx.work.multiprocess

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.concurrent.futures.CallbackToFutureAdapter.getFuture
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.work.impl.WorkManagerImpl
import androidx.work.impl.utils.SerialExecutorImpl
import androidx.work.impl.utils.taskexecutor.TaskExecutor
import java.util.concurrent.Executor
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
public class ListenableWorkerImplClientTest {
    private lateinit var mContext: Context
    private lateinit var mWorkManager: WorkManagerImpl
    private lateinit var mExecutor: Executor
    private lateinit var mClient: ListenableWorkerImplClient

    @Before
    public fun setUp() {
        if (Build.VERSION.SDK_INT <= 27) {
            // Exclude <= API 27, from tests because it causes a SIGSEGV.
            return
        }

        mContext = mock(Context::class.java)
        mWorkManager = mock(WorkManagerImpl::class.java)
        `when`(mContext.applicationContext).thenReturn(mContext)
        mExecutor = Executor { it.run() }

        val taskExecutor = mock(TaskExecutor::class.java)
        `when`(taskExecutor.serialTaskExecutor).thenReturn(SerialExecutorImpl(mExecutor))
        `when`(mWorkManager.workTaskExecutor).thenReturn(taskExecutor)
        mClient = ListenableWorkerImplClient(mContext, mExecutor)
    }

    @Test
    @MediumTest
    public fun failGracefullyWhenBindFails() {
        if (Build.VERSION.SDK_INT <= 27) {
            // Exclude <= API 27, from tests because it causes a SIGSEGV.
            return
        }
        `when`(
                mContext.bindService(
                    any(Intent::class.java),
                    any(ServiceConnection::class.java),
                    anyInt()
                )
            )
            .thenReturn(false)
        val componentName = ComponentName("packageName", "className")
        var exception: Throwable? = null
        try {
            mClient.getListenableWorkerImpl(componentName).get()
        } catch (throwable: Throwable) {
            exception = throwable
        }
        assertNotNull(exception)
        val message = exception?.cause?.message ?: ""
        assertTrue(message.contains("Unable to bind to service"))
    }

    @Test
    @MediumTest
    @Suppress("UNCHECKED_CAST")
    public fun cleanUpWhenDispatcherFails() {
        if (Build.VERSION.SDK_INT <= 27) {
            // Exclude <= API 27, from tests because it causes a SIGSEGV.
            return
        }

        val binder = mock(IBinder::class.java)
        val remoteDispatcher =
            mock(RemoteDispatcher::class.java) as RemoteDispatcher<IListenableWorkerImpl>
        val remoteStub = mock(IListenableWorkerImpl::class.java)
        val message = "Something bad happened"
        `when`(remoteDispatcher.execute(eq(remoteStub), any(IWorkManagerImplCallback::class.java)))
            .thenThrow(RuntimeException(message))
        `when`(remoteStub.asBinder()).thenReturn(binder)
        val session = getFuture { it.set(remoteStub) }
        var exception: Throwable? = null
        try {
            mClient.execute(session, remoteDispatcher).get()
        } catch (throwable: Throwable) {
            exception = throwable
        }
        assertNotNull(exception)
    }

    @Test
    @MediumTest
    @Suppress("UNCHECKED_CAST")
    public fun cleanUpWhenSessionIsInvalid() {
        if (Build.VERSION.SDK_INT <= 27) {
            // Exclude <= API 27, from tests because it causes a SIGSEGV.
            return
        }

        val remoteDispatcher =
            mock(RemoteDispatcher::class.java) as RemoteDispatcher<IListenableWorkerImpl>
        val session =
            getFuture<IListenableWorkerImpl> {
                it.setException(RuntimeException("Something bad happened"))
            }
        var exception: Throwable? = null
        try {
            mClient.execute(session, remoteDispatcher).get()
        } catch (throwable: Throwable) {
            exception = throwable
        }
        assertNotNull(exception)
    }

    @Test
    @MediumTest
    public fun cleanUpOnSuccessfulDispatch() {
        if (Build.VERSION.SDK_INT <= 27) {
            // Exclude <= API 27, from tests because it causes a SIGSEGV.
            return
        }

        val binder = mock(IBinder::class.java)
        val remoteDispatcher =
            RemoteDispatcher<IListenableWorkerImpl> { _, callback ->
                callback.onSuccess(ByteArray(0))
            }
        val remoteStub = mock(IListenableWorkerImpl::class.java)
        `when`(remoteStub.asBinder()).thenReturn(binder)
        val session = getFuture { it.set(remoteStub) }
        var exception: Throwable? = null
        try {
            mClient.execute(session, remoteDispatcher).get()
        } catch (throwable: Throwable) {
            exception = throwable
        }
        assertNull(exception)
    }
}
