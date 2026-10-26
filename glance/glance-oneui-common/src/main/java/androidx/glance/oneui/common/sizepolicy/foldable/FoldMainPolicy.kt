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

package androidx.glance.oneui.common.sizepolicy.foldable

import android.content.Context
import android.util.Size
import android.util.SizeF
import androidx.glance.oneui.common.AppWidgetSize
import androidx.glance.oneui.common.isPortrait

abstract class FoldMainPolicy : FoldablePolicy() {
    companion object {
        private val policyMap = mapOf(
            0 to PortraitPolicy,
            1 to LandscapePolicy,
            2 to PortraitPolicyWithEasyMode,
            3 to LandscapePolicyWithEasyMode
        )

        fun convertDpToSize(
            context: Context,
            width: Float,
            height: Float,
            screenSize: Size,
            isEasyMode: Boolean
        ): AppWidgetSize {
            val isPortrait = context.resources.configuration.isPortrait()
            val policy = policyMap[makeStateKey(isPortrait, isEasyMode)]
            return policy?.convertDpToSize(width, height, screenSize) ?: AppWidgetSize.Unknown
        }

        fun convertSizeToDp(
            context: Context,
            size: AppWidgetSize,
            screenSize: Size,
            isEasyMode: Boolean = false
        ): Pair<SizeF, SizeF> {
            val isPortrait = context.resources.configuration.isPortrait()
            val policy = policyMap[makeStateKey(isPortrait, isEasyMode)]
            return policy?.convertSizeToDp(size, screenSize) ?: Pair(SizeF(0f, 0f), SizeF(0f, 0f))
        }
    }

    object PortraitPolicy : FoldMainPolicy() {
        override val hRatioLevel0 = 0.0f
        override val hRatioLevel1 = 0.13f
        override val hRatioLevel2 = 0.39f
        override val hRatioLevel3 = 0.65f
        override val hRatioLevel4 = 1.0f
        override val wRatioLevel0 = 0.0f
        override val wRatioLevel1 = 0.115f
        override val wRatioLevel2 = 0.376f
        override val wRatioLevel3 = 0.64f
    }

    object LandscapePolicy : FoldMainPolicy() {
        override val hRatioLevel0 = 0.0f
        override val hRatioLevel1 = 0.13f
        override val hRatioLevel2 = 0.39f
        override val hRatioLevel3 = 0.65f
        override val hRatioLevel4 = 1.0f
        override val wRatioLevel0 = 0.0f
        override val wRatioLevel1 = 0.12f
        override val wRatioLevel2 = 0.34f
        override val wRatioLevel3 = 0.56f
    }

    object PortraitPolicyWithEasyMode : FoldMainPolicy() {
        override val hRatioLevel0 = 0.0f
        override val hRatioLevel1 = 0.19f
        override val hRatioLevel2 = 0.37f
        override val hRatioLevel3 = 0.71f
        override val hRatioLevel4 = 1.0f
        override val wRatioLevel0 = 0.0f
        override val wRatioLevel1 = 0.18f
        override val wRatioLevel2 = 0.38f
        override val wRatioLevel3 = 0.805f
    }

    object LandscapePolicyWithEasyMode : FoldMainPolicy() {
        override val hRatioLevel0 = 0.0f
        override val hRatioLevel1 = 0.19f
        override val hRatioLevel2 = 0.37f
        override val hRatioLevel3 = 0.71f
        override val hRatioLevel4 = 1.0f
        override val wRatioLevel0 = 0.0f
        override val wRatioLevel1 = 0.16f
        override val wRatioLevel2 = 0.36f
        override val wRatioLevel3 = 0.76f
    }
}
