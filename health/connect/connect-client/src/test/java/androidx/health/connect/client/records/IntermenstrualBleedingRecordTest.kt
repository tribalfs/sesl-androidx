/*
 * Copyright 2022 The Android Open Source Project
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

package androidx.health.connect.client.records

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IntermenstrualBleedingRecordTest {

    @Test
    fun validRecord_equals() {
        assertThat(
                IntermenstrualBleedingRecord(
                    time = Instant.ofEpochMilli(1234L),
                    zoneOffset = null,
                )
            )
            .isEqualTo(
                IntermenstrualBleedingRecord(
                    time = Instant.ofEpochMilli(1234L),
                    zoneOffset = null,
                )
            )
    }

    @Test
    fun toString_containsMembers() {
        assertThat(
                IntermenstrualBleedingRecord(
                        time = Instant.ofEpochMilli(1234L),
                        zoneOffset = null,
                    )
                    .toString()
            )
            .isEqualTo(
                "IntermenstrualBleedingRecord(time=1970-01-01T00:00:01.234Z, zoneOffset=null, metadata=Metadata(id='', dataOrigin=DataOrigin(packageName=''), lastModifiedTime=1970-01-01T00:00:00Z, clientRecordId=null, clientRecordVersion=0, device=null, recordingMethod=0))"
            )
    }
}
