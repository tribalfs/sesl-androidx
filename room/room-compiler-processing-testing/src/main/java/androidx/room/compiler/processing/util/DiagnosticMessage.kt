/*
 * Copyright 2020 The Android Open Source Project
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

package androidx.room.compiler.processing.util

import javax.tools.Diagnostic

/** Holder for diagnostics messages */
data class DiagnosticMessage(
    val kind: Diagnostic.Kind,
    val msg: String,
    val location: DiagnosticLocation? = null,
)

/**
 * Location of a diagnostic message. Note that, when run with KAPT this location might be on the
 * stubs or may not exactly match the kotlin source file (KAPT's stub to source mapping is not very
 * fine grained)
 */
data class DiagnosticLocation(
    val source: Source?,
    val line: Int,
)
