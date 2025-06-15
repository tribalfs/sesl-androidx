package androidx.picker.adapter.layoutmanager

import android.content.Context
import androidx.picker.R
import androidx.picker.common.log.LogTag
import androidx.picker.common.log.debug
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max

/**
 * A [GridLayoutManager] that automatically adjusts the number of columns to fit the available
 * width.
 *
 * This layout manager calculates the optimal number of columns based on the item width and the
 * available width of the RecyclerView. It also takes into account the horizontal interval between
 * items.
 *
 * The `columnWidth` can be updated dynamically, and the layout manager will recalculate the
 * span count accordingly. The span count can also be forced to a specific value.
 */
class AutoFitGridLayoutManager(context: Context) : GridLayoutManager(context, 1), LogTag {
    private val resources = context.resources
    private var columnWidth: Int = resources.getDimensionPixelOffset(R.dimen.picker_app_grid_item_view_item_width_land)
    private var columnWidthChanged: Boolean = true
    private var forcedSpanCount: Boolean = false
    private val horizontalInterval: Int = resources.getDimensionPixelOffset(R.dimen.picker_app_selected_layout_horizontal_interval)
    private var prevWidth: Int = 0

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        if (!forcedSpanCount && (prevWidth != width || (columnWidthChanged && columnWidth > 0))) {
            val availableWidth = width - paddingStart - paddingEnd
            val coerceAtLeast = max(1, (availableWidth + horizontalInterval) / (columnWidth + horizontalInterval))
            debug("onLayoutChildren $spanCount -> $coerceAtLeast, availableWidth=$availableWidth")
            spanCount = coerceAtLeast
            columnWidthChanged = false
            prevWidth = width
        }
        super.onLayoutChildren(recycler, state)
    }

    /**
     * Sets the number of columns in the grid.
     *
     * @param spanCount The number of columns.
     * @param forced If true, the span count will be forced to the given value, overriding any
     *               automatic calculation.
     */
    fun setSpanCount(spanCount: Int, forced: Boolean) {
        debug("setSpanCount $spanCount -> $spanCount")
        forcedSpanCount = forced
        super.setSpanCount(spanCount)
    }

    override val logTag: String = "AutoFitGridLayoutManager"
}