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

package androidx.glance.oneui.common.sizepolicy.multifold

import android.content.Context
import android.util.Size
import android.util.SizeF
import androidx.glance.oneui.common.AppWidgetSize
import androidx.glance.oneui.common.isPortrait

abstract class MultiFoldCoverPolicy : MultiFoldablePolicy() {
    companion object {
        private val policyMap = mapOf(
            0 to PortraitPolicy,
            1 to LandscapePolicy,
            2 to PortraitPolicy,
            3 to LandscapePolicy
        )

        fun convertDpToSize(
            context: Context,
            width: Float,
            height: Float,
            screenSize: Size,
            isEasyMode: Boolean
        ): AppWidgetSize {
            val isPortrait = context.resources.configuration.isPortrait()
            val policy = policyMap[makeStateKey(isPortrait, false)]
            return policy?.convertDpToSize(width, height, screenSize) ?: AppWidgetSize.Unknown
        }

        fun convertSizeToDp(
            context: Context,
            size: AppWidgetSize,
            screenSize: Size,
            isEasyMode: Boolean = false
        ): Pair<SizeF, SizeF> {
            val isPortrait = context.resources.configuration.isPortrait()
            val policy = policyMap[makeStateKey(isPortrait, false)]
            return policy?.convertSizeToDp(size, screenSize) ?: Pair(SizeF(0f, 0f), SizeF(0f, 0f))
        }
    }

    object PortraitPolicy : MultiFoldCoverPolicy() {
        override val hRatioLevel0 = 0.0f
        override val hRatioLevel1 = 0.13f
        override val hRatioLevel2 = 0.39f
        override val hRatioLevel3 = 0.63f
        override val hRatioLevel4 = 1.0f
        override val wRatioLevel0 = 0.0f
        override val wRatioLevel1 = 0.19f
        override val wRatioLevel2 = 0.64f
        override val wRatioLevel3 = 1.0f
    }

    object LandscapePolicy : MultiFoldCoverPolicy() {
        override val hRatioLevel0 = 0.0f
        override val hRatioLevel1 = 0.13f
        override val hRatioLevel2 = 0.22f
        override val hRatioLevel3 = 0.68f
        override val hRatioLevel4 = 1.0f
        override val wRatioLevel0 = 0.0f
        override val wRatioLevel1 = 0.08f
        override val wRatioLevel2 = 0.328f
        override val wRatioLevel3 = 0.55f
    }
}
