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

package androidx.glance.oneui.common.sizepolicy.flip

import android.content.Context
import android.util.Size
import android.util.SizeF
import androidx.glance.oneui.common.AppWidgetSize
import androidx.glance.oneui.common.isPortrait
import androidx.glance.oneui.common.sizepolicy.SizeRatioPolicy

abstract class FlipPolicy : SizeRatioPolicy() {
    companion object {
        private const val ORIENTATION_STATE_KEY = 0
        private const val EASY_MODE_STATE_KEY = 1

        private val policyMap = mapOf(
            0 to PortraitPolicy,
            1 to LandscapePolicy,
            2 to PortraitPolicyWithEasyMode,
            3 to LandscapePolicyWithEasyMode
        )

        private fun makeStateKey(isPortrait: Boolean, isEasyMode: Boolean): Int {
            val i = if (isPortrait) 0 else 1
            return if (isEasyMode) i or 2 else i
        }

        fun convertDpToSize(
            context: Context,
            width: Float,
            height: Float,
            isEasyMode: Boolean,
            screenSize: Size
        ): AppWidgetSize {
            val isPortrait = context.resources.configuration.isPortrait()
            val policy = policyMap[makeStateKey(isPortrait, isEasyMode)]
            return policy?.convertDpToSize(width, height, screenSize) ?: AppWidgetSize.Unknown
        }

        fun convertSizeToDp(
            context: Context,
            size: AppWidgetSize,
            isEasyMode: Boolean,
            screenSize: Size
        ): Pair<SizeF, SizeF> {
            val isPortrait = context.resources.configuration.isPortrait()
            val policy = policyMap[makeStateKey(isPortrait, isEasyMode)]
            return policy?.convertSizeToDp(size, screenSize) ?: Pair(SizeF(0f, 0f), SizeF(0f, 0f))
        }
    }

    object PortraitPolicy : FlipPolicy() {
        override val hRatioLevel0 = 0.0f
        override val hRatioLevel1 = 0.13f
        override val hRatioLevel2 = 0.39f
        override val hRatioLevel3 = 0.63f
        override val hRatioLevel4 = 1.0f
        override val wRatioLevel0 = 0.0f
        override val wRatioLevel1 = 0.21f
        override val wRatioLevel2 = 0.64f
        override val wRatioLevel3 = 1.0f
    }

    object LandscapePolicy : FlipPolicy() {
        override val hRatioLevel0 = 0.0f
        override val hRatioLevel1 = 0.2f
        override val hRatioLevel2 = 0.68f
        override val hRatioLevel3 = 1.0f
        override val hRatioLevel4 = 1.0f
        override val wRatioLevel0 = 0.0f
        override val wRatioLevel1 = 0.07f
        override val wRatioLevel2 = 0.328f
        override val wRatioLevel3 = 0.7f
    }

    object PortraitPolicyWithEasyMode : FlipPolicy() {
        override val hRatioLevel0 = 0.0f
        override val hRatioLevel1 = 0.145f
        override val hRatioLevel2 = 0.34f
        override val hRatioLevel3 = 0.58f
        override val hRatioLevel4 = 1.0f
        override val wRatioLevel0 = 0.0f
        override val wRatioLevel1 = 0.24f
        override val wRatioLevel2 = 0.55f
        override val wRatioLevel3 = 1.0f
    }

    object LandscapePolicyWithEasyMode : FlipPolicy() {
        override val hRatioLevel0 = 0.0f
        override val hRatioLevel1 = 0.31f
        override val hRatioLevel2 = 0.61f
        override val hRatioLevel3 = 1.0f
        override val hRatioLevel4 = 1.0f
        override val wRatioLevel0 = 0.0f
        override val wRatioLevel1 = 0.11f
        override val wRatioLevel2 = 0.36f
        override val wRatioLevel3 = 0.76f
    }
}
