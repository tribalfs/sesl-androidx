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
import androidx.glance.oneui.common.GridSpanInfo
import androidx.glance.oneui.common.isPortrait

abstract class MultiFoldMainPolicy : MultiFoldablePolicy() {
    companion object {
        private val policyMap = mapOf(
            0 to PortraitPolicy,
            1 to LandscapePolicy,
            2 to PortraitPolicyWith6x10Grid,
            3 to LandscapePolicyWith6x10Grid
        )

        fun convertDpToSize(
            context: Context,
            width: Float,
            height: Float,
            screenSize: Size,
            gridSpanInfo: GridSpanInfo
        ): AppWidgetSize {
            val isPortrait = context.resources.configuration.isPortrait()
            val policy = policyMap[makeStateKey(isPortrait, gridSpanInfo.is6x10Grid())]
            return policy?.convertDpToSize(width, height, screenSize) ?: AppWidgetSize.Unknown
        }

        fun convertSizeToDp(
            context: Context,
            size: AppWidgetSize,
            screenSize: Size,
            gridSpanInfo: GridSpanInfo
        ): Pair<SizeF, SizeF> {
            val isPortrait = context.resources.configuration.isPortrait()
            val policy = policyMap[makeStateKey(isPortrait, gridSpanInfo.is6x10Grid())]
            return policy?.convertSizeToDp(size, screenSize) ?: Pair(SizeF(0f, 0f), SizeF(0f, 0f))
        }
    }

    object PortraitPolicy : MultiFoldMainPolicy() {
        override val hRatioLevel0 = 0.0f
        override val hRatioLevel1 = 0.08f
        override val hRatioLevel2 = 0.17f
        override val hRatioLevel3 = 0.37f
        override val hRatioLevel4 = 1.0f
        override val wRatioLevel0 = 0.0f
        override val wRatioLevel1 = 0.1f
        override val wRatioLevel2 = 0.37f
        override val wRatioLevel3 = 0.78f
    }

    object LandscapePolicy : MultiFoldMainPolicy() {
        override val hRatioLevel0 = 0.0f
        override val hRatioLevel1 = 0.1f
        override val hRatioLevel2 = 0.23f
        override val hRatioLevel3 = 0.48f
        override val hRatioLevel4 = 1.0f
        override val wRatioLevel0 = 0.0f
        override val wRatioLevel1 = 0.07f
        override val wRatioLevel2 = 0.27f
        override val wRatioLevel3 = 0.57f
    }

    object PortraitPolicyWith6x10Grid : MultiFoldMainPolicy() {
        override val hRatioLevel0 = 0.0f
        override val hRatioLevel1 = 0.07f
        override val hRatioLevel2 = 0.15f
        override val hRatioLevel3 = 0.31f
        override val hRatioLevel4 = 1.0f
        override val wRatioLevel0 = 0.0f
        override val wRatioLevel1 = 0.08f
        override val wRatioLevel2 = 0.33f
        override val wRatioLevel3 = 0.71f
    }

    object LandscapePolicyWith6x10Grid : MultiFoldMainPolicy() {
        override val hRatioLevel0 = 0.0f
        override val hRatioLevel1 = 0.09f
        override val hRatioLevel2 = 0.22f
        override val hRatioLevel3 = 0.47f
        override val hRatioLevel4 = 1.0f
        override val wRatioLevel0 = 0.0f
        override val wRatioLevel1 = 0.07f
        override val wRatioLevel2 = 0.24f
        override val wRatioLevel3 = 0.5f
    }
}

private fun GridSpanInfo.is6x10Grid(): Boolean = w == 6 && h == 10
