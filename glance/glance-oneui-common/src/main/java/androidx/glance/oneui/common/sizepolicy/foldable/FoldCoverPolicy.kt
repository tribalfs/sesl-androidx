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

abstract class FoldCoverPolicy : FoldablePolicy() {
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

    object PortraitPolicy : FoldCoverPolicy() {
        override val hRatioLevel0 = 0.0f
        override val hRatioLevel1 = 0.13f
        override val hRatioLevel2 = 0.39f
        override val hRatioLevel3 = 0.63f
        override val hRatioLevel4 = 1.0f
        override val wRatioLevel0 = 0.0f
        override val wRatioLevel1 = 0.205f
        override val wRatioLevel2 = 0.65f
        override val wRatioLevel3 = 1.0f
    }

    object LandscapePolicy : FoldCoverPolicy() {
        override val hRatioLevel0 = 0.0f
        override val hRatioLevel1 = 0.22f
        override val hRatioLevel2 = 0.68f
        override val hRatioLevel3 = 1.0f
        override val hRatioLevel4 = 1.0f
        override val wRatioLevel0 = 0.0f
        override val wRatioLevel1 = 0.07f
        override val wRatioLevel2 = 0.328f
        override val wRatioLevel3 = 0.7f
    }

    object PortraitPolicyWithEasyMode : FoldCoverPolicy() {
        override val hRatioLevel0 = 0.0f
        override val hRatioLevel1 = 0.145f
        override val hRatioLevel2 = 0.29f
        override val hRatioLevel3 = 0.58f
        override val hRatioLevel4 = 1.0f
        override val wRatioLevel0 = 0.0f
        override val wRatioLevel1 = 0.24f
        override val wRatioLevel2 = 0.55f
        override val wRatioLevel3 = 0.86f
    }

    object LandscapePolicyWithEasyMode : FoldCoverPolicy() {
        override val hRatioLevel0 = 0.0f
        override val hRatioLevel1 = 0.31f
        override val hRatioLevel2 = 0.61f
        override val hRatioLevel3 = 1.0f
        override val hRatioLevel4 = 1.0f
        override val wRatioLevel0 = 0.0f
        override val wRatioLevel1 = 0.11f
        override val wRatioLevel2 = 0.26f
        override val wRatioLevel3 = 0.55f
    }
}
