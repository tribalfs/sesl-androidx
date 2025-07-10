package androidx.picker.decorator

import android.graphics.Rect
import android.view.View
import androidx.picker.adapter.viewholder.FrameViewHolder
import androidx.picker.adapter.viewholder.GroupTitleViewHolder
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION

/**
 * A [RecyclerView.ItemDecoration] that adds spacing around items in a [GridLayoutManager].
 *
 * This decoration applies horizontal and vertical spacing to each item.
 * It skips adding spacing for items whose ViewHolder is an instance of [GroupTitleViewHolder]
 * or [FrameViewHolder].
 *
 * The spacing is distributed such that the outer edges of the grid have half the spacing,
 * and the inner gaps between items have the full spacing amount.
 *
 * @param itemWidth The width of the grid item.
 * @param itemBottomSpacing The amount of total top and bottom spacing to apply in pixels.
 */
class GridSpacingItemDecoration(private val itemWidth: Int, private val itemBottomSpacing: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        rect: Rect,
        view: View,
        recyclerView: RecyclerView,
        state: RecyclerView.State
    ) {
        val childAdapterPosition = recyclerView.getChildAdapterPosition(view)
        if (childAdapterPosition == NO_POSITION || recyclerView.adapter == null) {
            return
        }
        val childViewHolder = recyclerView.getChildViewHolder(view)
        if ((childViewHolder is GroupTitleViewHolder) || (childViewHolder is FrameViewHolder)) {
            return
        }
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is GridLayoutManager) {
            val spanCount = layoutManager.spanCount
            val layoutParams = view.layoutParams
            val spanIndex =
                if (layoutParams is GridLayoutManager.LayoutParams) layoutParams.spanIndex else childAdapterPosition % spanCount
            val availableWidth =  with(recyclerView) { width - paddingStart - paddingEnd }
            val itemLeftRightSpacing = (availableWidth - (itemWidth * spanCount)) / (spanCount + 1)
            rect.left = itemLeftRightSpacing - ((spanIndex * itemLeftRightSpacing) / spanCount)
            rect.right = ((spanIndex + 1) * itemLeftRightSpacing) / spanCount
            (itemBottomSpacing / 2).let{
                rect.top = it
                rect.bottom = it
            }
        }
    }
}