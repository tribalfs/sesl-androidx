package androidx.picker.model.viewdata

import android.view.View
import androidx.picker.model.SpanData

/**
 * The ui model for custom view use to item view managed by [androidx.picker.adapter.AbsAdapter].
 *
 * @property view The custom [View] instance, or `null` if no view is set.
 * @property spanCount The number of columns this view data should span in a grid layout.
 * Defaults to 1.
 *
 * Implements [ViewData] to indicate it's view-related data.
 * Implements [SpanData] to provide information about column spanning.
 */
data class CustomViewData(
    val view: View? = null,
    override val spanCount: Int = 1
) : ViewData, SpanData