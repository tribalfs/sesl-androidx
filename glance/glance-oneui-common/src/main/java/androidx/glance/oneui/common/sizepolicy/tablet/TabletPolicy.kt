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

package androidx.glance.oneui.common.sizepolicy.tablet

import android.content.Context
import android.util.Size
import android.util.SizeF
import androidx.glance.oneui.common.AppWidgetSize
import androidx.glance.oneui.common.GridSpanInfo
import androidx.glance.oneui.common.isPortrait
import androidx.glance.oneui.common.sizepolicy.SizeRatioPolicy

abstract class TabletPolicy : SizeRatioPolicy() {
    companion object {
        private const val ORIENTATION_STATE_KEY = 0
        private const val GRID_6_10_MODE_STATE_KEY = 1

        private val policyMap = mapOf(
            0 to PortraitPolicy,
            1 to LandscapePolicy,
            2 to PortraitPolicyWith6x10Grid,
            3 to LandscapePolicyWith6x10Grid
        )

        private fun makeStateKey(isPortrait: Boolean, is6x10Grid: Boolean): Int {
            val i = if (isPortrait) 0 else 1
            return if (is6x10Grid) i or 2 else i
        }

        fun convertDpToSize(
            context: Context,
            width: Float,
            height: Float,
            gridSpanInfo: GridSpanInfo,
            screenSize: Size
        ): AppWidgetSize {
            val isPortrait = context.resources.configuration.isPortrait()
            val policy = policyMap[makeStateKey(isPortrait, gridSpanInfo.is6x10Grid())]
            return policy?.convertDpToSize(width, height, screenSize) ?: AppWidgetSize.Unknown
        }

        fun convertSizeToDp(
            context: Context,
            size: AppWidgetSize,
            gridSpanInfo: GridSpanInfo,
            screenSize: Size
        ): Pair<SizeF, SizeF> {
            val isPortrait = context.resources.configuration.isPortrait()
            val policy = policyMap[makeStateKey(isPortrait, gridSpanInfo.is6x10Grid())]
            return policy?.convertSizeToDp(size, screenSize) ?: Pair(SizeF(0f, 0f), SizeF(0f, 0f))
        }
    }

    object PortraitPolicy : TabletPolicy() {
        override val hRatioLevel0 = 0.0f
        override val hRatioLevel1 = 0.095f
        override val hRatioLevel2 = 0.286f
        override val hRatioLevel3 = 0.476f
        override val hRatioLevel4 = 1.0f
        override val wRatioLevel0 = 0.0f
        override val wRatioLevel1 = 0.13f
        override val wRatioLevel2 = 0.38f
        override val wRatioLevel3 = 0.78f
    }

    object LandscapePolicy : TabletPolicy() {
        override val hRatioLevel0 = 0.0f
        override val hRatioLevel1 = 0.126f
        override val hRatioLevel2 = 0.378f
        override val hRatioLevel3 = 0.65f
        override val hRatioLevel4 = 1.0f
        override val wRatioLevel0 = 0.0f
        override val wRatioLevel1 = 0.1f
        override val wRatioLevel2 = 0.26f
        override val wRatioLevel3 = 0.45f
    }

    object PortraitPolicyWith6x10Grid : TabletPolicy() {
        override val hRatioLevel0 = 0.0f
        override val hRatioLevel1 = 0.095f
        override val hRatioLevel2 = 0.26f
        override val hRatioLevel3 = 0.41f
        override val hRatioLevel4 = 1.0f
        override val wRatioLevel0 = 0.0f
        override val wRatioLevel1 = 0.13f
        override val wRatioLevel2 = 0.43f
        override val wRatioLevel3 = 0.78f
    }

    object LandscapePolicyWith6x10Grid : TabletPolicy() {
        override val hRatioLevel0 = 0.0f
        override val hRatioLevel1 = 0.128f
        override val hRatioLevel2 = 0.42f
        override val hRatioLevel3 = 0.63f
        override val hRatioLevel4 = 1.0f
        override val wRatioLevel0 = 0.0f
        override val wRatioLevel1 = 0.1f
        override val wRatioLevel2 = 0.26f
        override val wRatioLevel3 = 0.52f
    }
}

private fun GridSpanInfo.is6x10Grid(): Boolean = w == 6 && h == 10
