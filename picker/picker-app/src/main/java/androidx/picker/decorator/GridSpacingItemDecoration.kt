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
 * @param spacing The amount of spacing to apply in pixels.
 */
class GridSpacingItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {

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
            val spacing = this.spacing
            rect.left = spacing - ((spanIndex * spacing) / spanCount)
            rect.right = ((spanIndex + 1) * spacing) / spanCount
            rect.top = spacing / 2
            rect.bottom = spacing / 2
        }
    }
}