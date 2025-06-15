package androidx.picker.helper

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.children


private const val DIMMED_OPACITY = 0.4f
private const val NORMAL_OPACITY = 1.0f

/**
 * Recursively sets the enabled state of this View and all its descendants.
 *
 * For ImageViews, also adjusts the alpha to indicate enabled/disabled state.
 *
 * @param enabled True to enable this View and its descendants, false to disable.
 */
fun View.setEnabledDeeply(enabled: Boolean) {
    val queue = ArrayDeque<View>()
    queue.add(this)
    while (queue.isNotEmpty()) {
        val view = queue.removeFirst()
        view.isEnabled = enabled
        if (view is ImageView) {
            view.alpha = if (enabled) NORMAL_OPACITY else DIMMED_OPACITY
        }
        if (view is ViewGroup) {
            queue.addAll(view.children)
        }
    }
}