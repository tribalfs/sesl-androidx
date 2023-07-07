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

package androidx.credentials.provider

import android.content.pm.SigningInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

 @SdkSuppress(minSdkVersion = 28)
 @RunWith(AndroidJUnit4::class)
 @SmallTest
 class CallingAppInfoTest {

     @Test
     fun constructor_success() {
         CallingAppInfo("name", SigningInfo())
     }

     @Test
     fun constructor_success_withOrigin() {
         val origin = "origin"
         val callingAppInfo = CallingAppInfo("name", SigningInfo(), origin)

         assertThat(callingAppInfo.getOrigin("{\"key\":\"value\"}")).isNull()
         assertThat(callingAppInfo.origin).isEqualTo(origin)
     }

     @Test
     fun constructor_fail_emptyPackageName() {
         Assert.assertThrows(
            "Expected exception from no package name",
            IllegalArgumentException::class.java
        ) {
            CallingAppInfo("", SigningInfo(), "origin")
        }
     }

     @Test
     fun getOrigin_emptyPrivilegedAllowlist_throwsException() {
         val origin = "origin"
         val callingAppInfo = CallingAppInfo("name", SigningInfo(), origin)

         assertThat(callingAppInfo.origin).isEqualTo(origin)
         Assert.assertThrows(
             "Expected exception from emptyPrivilegedAllowList",
             IllegalArgumentException::class.java
         ) {
             callingAppInfo.getOrigin("")
         }
     }

     @Test
     fun getOrigin_invalidJSON_throwsException() {
         val origin = "origin"
         val callingAppInfo = CallingAppInfo("name", SigningInfo(), origin)

         assertThat(callingAppInfo.origin).isEqualTo(origin)
         Assert.assertThrows(
             "Expected exception from emptyPrivilegedAllowList",
             IllegalArgumentException::class.java
         ) {
             callingAppInfo.getOrigin("invalid_json")
         }
     }
 }
