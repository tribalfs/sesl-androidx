/*
 * Copyright 2026 The Android Open Source Project
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

/**
 * Global project properties and versions metadata for SESL AndroidX modules.
 */
object SeslManifest {
    val moduleInfo: Map<String, List<Any>> = mapOf(
        // [<vanilla version or sesl if purely sesl>, <sesl version>, <rN>, <minsdk>, <targetSdk>]
        "androidx.core" to listOf("1.19.0", "1.0.7-sesl8", "rev0", 23, 37),
        "androidx.core-ktx" to listOf("1.19.0", "1.0.0-sesl8", "rev0", 23, 37),
        "androidx.customview" to listOf("1.2.0-rc01", "1.0.0-sesl8", "rev0", 21, 35),
        "androidx.drawerlayout" to listOf("1.2.0", "1.0.0-sesl8", "rev0", 21, 35),
        "androidx.viewpager" to listOf("1.1.0-beta01", "1.0.0-sesl8", "rev0", 21, 35),
        "androidx.coordinatorlayout" to listOf("1.3.0", "1.0.0-sesl8", "rev0", 21, 35),
        "androidx.appcompat" to listOf("1.7.1", "1.0.21-sesl8", "rev8", 21, 35),
        "androidx.fragment" to listOf("1.9.0", "1.0.5-sesl8", "rev0", 23, 37),
        "androidx.recyclerview" to listOf("1.4.0", "1.0.12-sesl8", "rev3", 21, 35),
        "androidx.preference" to listOf("1.2.1", "1.0.0-sesl8", "rev1", 21, 35),
        "androidx.slidingpanelayout" to listOf("1.2.0", "1.0.4-sesl8", "rev1", 21, 35),
        "androidx.viewpager2" to listOf("1.1.0", "1.0.0-sesl8", "rev0", 21, 35),
        "androidx.swiperefreshlayout" to listOf("1.2.0-alpha01", "1.0.0-sesl8", "rev0", 21, 35),
        "androidx.indexscroll" to listOf("1.0.0", "1.0.0-sesl8", "rev0", 21, 35),
        "androidx.picker-basic" to listOf("1.0.7", "1.0.7-sesl8", "rev2", 21, 35),
        "androidx.picker-color" to listOf("1.0.2", "1.0.3-sesl8", "rev0", 21, 35),
        "androidx.picker-app" to listOf("1.0.6", "1.0.6-sesl8", "rev0", 21, 35),
        "androidx.apppickerview" to listOf("1.0.1", "1.0.1-sesl7", "rev1", 21, 35),
    )

    val pomInfo: Map<String, String> = mapOf(
        "inceptionYear" to "2024",
        "packaging" to "aar",
        "url" to "https://github.com/tribalfs/sesl-androidx",
        "scmUrl" to "https://github.com/tribalfs/sesl-androidx",
        "scmConnection" to "scm:git@github.com:tribalfs/sesl-androidx.git",
        "devConnection" to "scm:git@github.com:tribalfs/sesl-androidx.git",
        "licenceName" to "Apache-2.0 License",
        "licenseUrl" to "https://github.com/tribalfs/sesl-androidx/blob/sesl-androidx-main/LICENSE.txt",
        "licenceDist" to "repo",
        "developerId" to "tribalfs",
        "developName" to "Tribalfs",
        "developerUrl" to "https://github.com/tribalfs",
    )
}
