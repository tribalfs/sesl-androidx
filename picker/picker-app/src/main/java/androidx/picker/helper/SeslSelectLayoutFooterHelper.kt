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

package androidx.picker.helper

import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.picker.R

/**
 * Helper class for computing, creating, and animating footer view heights in [androidx.picker.widget.SeslAppPickerSelectLayout].
 */
class SeslSelectLayoutFooterHelper(private val context: Context) {
    private var footerAnimator: ValueAnimator? = null
    private val footerBackgroundColor: Int = context.getRoundedCornerColor()
    private var footerView: View? = null

    /**
     * Computes the target height of the footer based on orientation and search bar visibility.
     */
    fun computeTargetFooterHeight(orientation: Int, isBottomSearchVisible: Boolean): Int {
        val searchViewHeight = if (isBottomSearchVisible) {
            try {
                val resId = context.resources.getIdentifier(
                    "sesl_search_view_bottom_preferred_height",
                    "dimen",
                    context.packageName
                )
                if (resId != 0) context.resources.getDimensionPixelSize(resId) else 0
            } catch (_: Exception) { 0 }
        } else 0

        val offset = if (orientation != 2 || isBottomSearchVisible) {
            context.resources.getDimensionPixelSize(R.dimen.picker_app_selected_layout_bottom_footer_offset)
        } else {
            context.resources.getDimensionPixelSize(R.dimen.picker_app_selected_layout_landscape_bottom_footer_offset_without_search)
        }

        return getBottomSystemBarHeight(orientation) + searchViewHeight + offset
    }

    /**
     * Calculates the bottom system bar + display cutout insets for the given orientation.
     */
    fun getBottomSystemBarHeight(orientation: Int): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return 0
        if (Build.VERSION.SDK_INT >= 30) {
            try {
                val metrics = windowManager.currentWindowMetrics
                val insets = metrics.windowInsets.getInsets(
                    WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout()
                )
                return insets.bottom + (if (orientation == 2) insets.top else 0)
            } catch (_: Exception) {}
        }
        return 0
    }

    /**
     * Retrieves or creates the footer view instance.
     */
    fun getOrCreateFooterView(): View {
        if (footerView == null) {
            footerView = View(context).apply {
                setBackgroundColor(footerBackgroundColor)
            }
        }
        return footerView!!
    }

    //sesl9
    fun getBackgroundColor(): Int = footerBackgroundColor

    /**
     * Cancels any running footer height animation.
     */
    fun cancelAnimation() {
        footerAnimator?.takeIf { it.isRunning }?.cancel()
    }

    /**
     * Updates or animates the target footer view height.
     */
    fun updateFooterHeight(view: View, targetHeight: Int, animate: Boolean) {
        cancelAnimation()
        val layoutParams = view.layoutParams ?: FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, targetHeight
        ).also { view.layoutParams = it }

        val currentHeight = layoutParams.height
        if (currentHeight != targetHeight) {
            if (!animate) {
                layoutParams.height = targetHeight
                view.layoutParams = layoutParams
                return
            }
            footerAnimator = ValueAnimator.ofInt(currentHeight, targetHeight).apply {
                addUpdateListener { anim ->
                    val h = anim.animatedValue as Int
                    val lp = view.layoutParams
                    if (lp != null) {
                        lp.height = h
                        view.layoutParams = lp
                    }
                }
                start()
            }
        }
    }
}
