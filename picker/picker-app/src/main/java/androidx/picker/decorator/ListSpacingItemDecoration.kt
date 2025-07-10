package androidx.picker.decorator

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.picker.R
import androidx.picker.adapter.HeaderFooterAdapter
import androidx.picker.adapter.viewholder.AppListItemViewHolder
import androidx.picker.features.composable.ComposableType
import androidx.picker.features.composable.ComposableTypeSet
import androidx.picker.helper.isRTL
import androidx.picker.model.viewdata.CategoryViewData
import androidx.recyclerview.widget.RecyclerView

/**
 * An [RecyclerView.ItemDecoration] that adds spacing to the start or end of items in a list,
 * specifically for items that represent categories.
 *
 * This decoration is used to visually indent category items within a list,
 * providing a clear visual hierarchy. It checks if the underlying data item is a
 * [CategoryViewData] and applies a predefined spacing.
 *
 * The spacing is applied to the left for LTR layouts and to the right for RTL layouts.
 * Certain item types, identified by `isIgnoreType`, are excluded from this spacing.
 *
 * @param context The context used to access resources, such as dimension values.
 */
class ListSpacingItemDecoration(
    val context: Context
) : RecyclerView.ItemDecoration() {

    private val spacing: Int = context.resources.getDimensionPixelOffset(R.dimen.picker_app_list_category_margin_left)

    private fun isIgnoreType(composableType: ComposableType): Boolean {
        return ComposableType.isSame(composableType, ComposableTypeSet.CheckBoxExpander) ||
            ComposableType.isSame(composableType, ComposableTypeSet.AllSwitch)
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val adapter = parent.adapter
        val headerFooterAdapter = adapter as? HeaderFooterAdapter ?: return
        val childViewHolder = parent.getChildViewHolder(view)
        if (childViewHolder is AppListItemViewHolder && !isIgnoreType(childViewHolder.composableType)) {
            val dataSetFiltered = headerFooterAdapter.getDataSetFiltered()
            if (dataSetFiltered.isNotEmpty()) {
                for (item in dataSetFiltered) {
                    if (item is CategoryViewData) {
                        if (context.isRTL()) {
                            outRect.set(0, 0, spacing, 0)
                        } else {
                            outRect.set(spacing, 0, 0, 0)
                        }
                        return
                    }
                }
            }
            outRect.set(0, 0, 0, 0)
        }
    }
}