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

package androidx.compose.runtime

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable

internal actual fun createSnapshotMutableLongState(value: Long): MutableLongState =
    ParcelableSnapshotMutableLongState(value)

@SuppressLint("BanParcelableUsage")
private class ParcelableSnapshotMutableLongState(value: Long) :
    SnapshotMutableLongStateImpl(value), Parcelable {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(longValue)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR =
            object : Parcelable.Creator<ParcelableSnapshotMutableLongState> {
                override fun createFromParcel(parcel: Parcel): ParcelableSnapshotMutableLongState {
                    return ParcelableSnapshotMutableLongState(value = parcel.readLong())
                }

                override fun newArray(size: Int) =
                    arrayOfNulls<ParcelableSnapshotMutableLongState>(size)
            }
    }
}
