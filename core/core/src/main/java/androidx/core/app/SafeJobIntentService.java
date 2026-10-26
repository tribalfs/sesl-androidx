/*
 * Copyright 2025 The Android Open Source Project
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

package androidx.core.app;

import android.util.Log;

import org.jspecify.annotations.Nullable;

//sesl9
/**
 * A {@link JobIntentService} that ignores {@link SecurityException} thrown while dequeuing
 * work, so that a revoked-permission state does not crash the service processing loop.
 */
abstract class SafeJobIntentService extends JobIntentService {
    private static final String TAG = "SamsungAccount";

    @Override
    public final @Nullable GenericWorkItem dequeueWork() {
        Log.i(TAG, "[" + getClass().getSimpleName() + "] " + " dequeueWork");
        try {
            return super.dequeueWork();
        } catch (SecurityException e) {
            Log.w(TAG, "[" + getClass().getSimpleName() + "] "
                    + " ignore JobIntentService.dequeueWork() SecurityException");
        }
        return null;
    }
}
