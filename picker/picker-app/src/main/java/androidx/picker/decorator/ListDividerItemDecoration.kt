package androidx.picker.decorator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.core.content.withStyledAttributes
import androidx.core.view.children
import androidx.picker.R
import androidx.picker.adapter.viewholder.AppListItemViewHolder
import androidx.picker.adapter.viewholder.FrameViewHolder
import androidx.picker.adapter.viewholder.GroupTitleViewHolder
import androidx.picker.helper.isRTL
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * A [RecyclerView.ItemDecoration] that draws a divider between items in a list.
 *
 * This decoration takes into account the padding of the divider drawable and the layout direction
 * (RTL or LTR) to draw the divider correctly.
 *
 * It also considers different view holder types ([AppListItemViewHolder], [FrameViewHolder],
 * [GroupTitleViewHolder]) to determine whether a divider should be drawn and how it should be
 * positioned.
 *
 * @param context The context to use for accessing resources and styled attributes.
 */
class ListDividerItemDecoration(context: Context) : RecyclerView.ItemDecoration() {

    private var divider: Drawable? = null
    private val dividerPaddingStart: Int
    private val iconFrameWidth: Int
    private val leftFrameWidth: Int

    init {
        context.withStyledAttributes(null, intArrayOf(android.R.attr.listDivider)){
            divider = getDrawable(0)
        }

        val rect = Rect().also { divider?.getPadding(it) }
        dividerPaddingStart = if (context.isRTL()) rect.right else rect.left

        with(context.resources) {
            iconFrameWidth = getDimensionPixelSize(R.dimen.picker_app_list_icon_frame_width)
            leftFrameWidth = getDimensionPixelSize(R.dimen.picker_app_list_left_frame_width)
        }
    }

    override fun seslOnDispatchDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.seslOnDispatchDraw(c, parent, state)
        val divider = this.divider ?: return

        val children = parent.children.toList()
        for (view in children.subList(0, max(parent.childCount - 1, 0))) {
            val childViewHolder = parent.getChildViewHolder(view)
            val nextViewHolder =
                parent.findViewHolderForAdapterPosition(parent.getChildAdapterPosition(view) + 1)

            if (childViewHolder !is FrameViewHolder &&
                childViewHolder !is GroupTitleViewHolder &&
                nextViewHolder !is GroupTitleViewHolder &&
                nextViewHolder !is FrameViewHolder
            ) {
                val width = if (childViewHolder is AppListItemViewHolder) {
                    val leftFrame =
                        if (childViewHolder.composableType.leftFrame != null) leftFrameWidth else 0
                    val iconFrame =
                        if (childViewHolder.composableType.iconFrame != null) iconFrameWidth else 0
                    (view.paddingStart + leftFrame + iconFrame) - dividerPaddingStart
                } else {
                    0
                }

                val left = view.left
                val right = view.right
                val layoutParams = view.layoutParams as? RecyclerView.LayoutParams
                val bottomMargin = layoutParams?.bottomMargin ?: 0
                val top = view.translationY.roundToInt() + view.bottom + bottomMargin
                val height = divider.intrinsicHeight + top

                val isRtl = view.context.isRTL()
                if (isRtl) {
                    divider.setBounds(left, top, right - width, height)
                } else {
                    divider.setBounds(left + width, top, right, height)
                }
                divider.draw(c)
            }
        }
    }
}