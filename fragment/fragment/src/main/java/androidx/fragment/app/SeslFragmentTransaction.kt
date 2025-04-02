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
package androidx.fragment.app

import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import androidx.annotation.RequiresApi

@RequiresApi(23)
fun FragmentTransaction.seslSetAnimations(context: Context): FragmentTransaction {
    if (isDefaultTheme(context)) {
        val isRtl = context.resources.configuration.layoutDirection == 1
        return seslSetAnimations(isRtl)
    }
    return this
}

@Deprecated("Use seslSetAnimations(context) instead")
@RequiresApi(23)
fun FragmentTransaction.seslSetAnimations(): FragmentTransaction =
    seslSetAnimations(false)

@RequiresApi(23)
private fun FragmentTransaction.seslSetAnimations(isRtl: Boolean): FragmentTransaction {
    val animationSet = if (isRtl) {
        SeslFragmentTransactionAnimationSet.HorizontalForRTL
    } else {
        SeslFragmentTransactionAnimationSet.Horizontal
    }
    setCustomAnimations(
        animationSet.enter,
        animationSet.exit,
        animationSet.popEnter,
        animationSet.popExit
    )
    return this
}

private fun isDefaultTheme(context: Context): Boolean =
    TextUtils.isEmpty(Settings.System.getString(context.contentResolver, "current_sec_active_themepackage"))