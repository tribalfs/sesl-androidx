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

package androidx.picker.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.util.SeslRoundedCorner
import androidx.appcompat.util.SeslRoundedCorner.ROUNDED_CORNER_ALL
import androidx.appcompat.util.SeslSubheaderRoundedCorner
import androidx.core.graphics.Insets
import androidx.core.view.children
import androidx.picker.R
import androidx.picker.adapter.HeaderFooterAdapter
import androidx.picker.adapter.layoutmanager.AutoFitGridLayoutManager
import androidx.picker.adapter.viewholder.FrameViewHolder
import androidx.picker.adapter.viewholder.GroupTitleViewHolder
import androidx.picker.common.log.LogTag
import androidx.picker.common.log.debug
import androidx.picker.decorator.RecyclerViewCornerDecoration
import androidx.picker.features.gridComposable.GridStrategy
import androidx.picker.features.gridComposable.IconOnlyGridStrategy
import androidx.picker.helper.SeslSelectLayoutFooterHelper
import androidx.picker.model.SpanData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * A [SeslAppPickerGridView] specialized for the selected-app area of
 * [SeslAppPickerSelectLayout].
 *
 * Manages its own keyboard/system-bar-aware bottom footer, orientation-based layout
 * (horizontal list in portrait, auto-fit grid in landscape), and sesl rounded corner
 * decorations including a system bar rounded corner decoration.
 */
//sesl9
class SeslSelectLayoutSelectedListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SeslAppPickerGridView(context, attrs, defStyleAttr), LogTag {

    override val logTag: String = "SeslSelectLayoutSelectedListView"

    init {
        scrollBarStyle = 0
        setNestedScrollingEnabled(false)
        appListOrder = ORDER_NONE
        seslSetGoToTopEnabled(false)
        seslSetFastScrollerEnabled(false)
    }

    private val mFooterHelper = SeslSelectLayoutFooterHelper(context)

    private val mSystemBarCornerDecoration =
        SelectedSystemBarRoundedCornerDecoration(context, mFooterHelper.getBackgroundColor())

    private var mIsFooterPresent = false

    private var mSavedAdapter: RecyclerView.Adapter<*>? = null

    /**
     * Shows the keyboard/system-bar aware footer in the selected list view if the content
     * can scroll vertically, otherwise clears it.
     */
    fun updateSelectedListViewFooter(orientation: Int, isBottomSearchVisible: Boolean) {
        if (canVerticallyScroll(orientation, isBottomSearchVisible)) {
            addSelectedListViewFooter(orientation, isBottomSearchVisible)
        } else {
            clearSelectedListViewFooter(orientation)
        }
    }

    private fun addSelectedListViewFooter(orientation: Int, isBottomSearchVisible: Boolean) {
        val footerView = mFooterHelper.getOrCreateFooterView()
        val targetHeight = mFooterHelper.computeTargetFooterHeight(orientation, isBottomSearchVisible)

        if (!mIsFooterPresent) {
            mIsFooterPresent = true
            mSystemBarCornerDecoration.updateHeight(0)
            clearFooters()
            (footerView.parent as? ViewGroup)?.removeView(footerView)
            addFooter(footerView, ROUNDED_CORNER_ALL)
            seslSetFillBottomEnabled(true)
        }

        setScrollBarVerticalPadding(targetHeight)

        val animate = !isInLayout() && footerView.parent != null
        mFooterHelper.updateFooterHeight(footerView, targetHeight, animate)
    }

    private fun clearSelectedListViewFooter(orientation: Int) {
        val footerView = mFooterHelper.getOrCreateFooterView()
        mIsFooterPresent = false
        mSystemBarCornerDecoration.updateHeight(mFooterHelper.getBottomSystemBarHeight(orientation))
        clearFooters()
        (footerView.parent as? ViewGroup)?.removeView(footerView)
        setScrollBarVerticalPadding(0)
        seslSetFillBottomEnabled(false)
    }

    private fun canVerticallyScroll(orientation: Int, isBottomSearchVisible: Boolean): Boolean {
        val targetHeight = mFooterHelper.computeTargetFooterHeight(orientation, isBottomSearchVisible)
        val selectedListHeight = height - paddingTop - paddingBottom - targetHeight

        var contentHeight = computeVerticalScrollRange()
        if (mIsFooterPresent) {
            contentHeight -= getVisibleFooterHeight()
        }

        val canScroll = contentHeight > selectedListHeight
        debug(
            "canVerticallyScroll: $canScroll (ContentHeight: $contentHeight > " +
                "SelectedListHeight: $selectedListHeight)"
        )
        return canScroll
    }

    private fun getVisibleFooterHeight(): Int {
        val gridLayoutManager = layoutManager as? AutoFitGridLayoutManager ?: return 0
        val headerFooterAdapter = adapter as? HeaderFooterAdapter ?: return 0
        if (headerFooterAdapter.footersCount <= 0) return 0

        val footerView = gridLayoutManager.findViewByPosition(
            headerFooterAdapter.itemCount - headerFooterAdapter.footersCount
        ) ?: return 0

        return max(0, min(footerView.bottom, height - paddingBottom) - max(footerView.top, 0))
    }

    private fun setScrollBarVerticalPadding(bottomSpaceHeight: Int) {
        val offset = resources.getDimensionPixelOffset(R.dimen.picker_app_selected_layout_bottom_footer_offset)
        seslSetScrollbarVerticalPadding(offset, bottomSpaceHeight + offset)
    }

    /**
     * Reconfigures item decorations and the layout manager for the given orientation.
     */
    fun configureViewBasedOnOrientation(orientation: Int, paddingHorizontal: Int) {
        setItemDecoration(orientation, paddingHorizontal)

        layoutManager = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            LinearLayoutManager(context).apply {
                setOrientation(LinearLayoutManager.HORIZONTAL)
            }
        } else {
            AutoFitGridLayoutManager(context).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        val headerFooterAdapter =
                            this@SeslSelectLayoutSelectedListView.adapter as? HeaderFooterAdapter
                        if (headerFooterAdapter != null && position >= 0 &&
                            position < headerFooterAdapter.itemCount
                        ) {
                            val item = headerFooterAdapter.getItem(position)
                            if (item is SpanData) {
                                val itemSpanCount = item.spanCount
                                if (itemSpanCount != -1) return itemSpanCount
                            }
                            return spanCount
                        }
                        return 1
                    }
                }
            }
        }
    }

    fun setItemDecoration(orientation: Int, paddingHorizontal: Int) {
        clearItemDecoration()

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            addItemDecoration(SelectedHorizontalItemDecoration())
            addItemDecoration(RecyclerViewCornerDecoration(context))
            seslSetFillBottomEnabled(false)
        } else {
            layoutParams.height = 0
            addItemDecoration(
                SelectedVerticalItemDecoration(
                    resources.getDimensionPixelOffset(R.dimen.picker_app_selected_item_view_interval_vertical_on_land)
                )
            )
            addItemDecoration(mSystemBarCornerDecoration)
        }

        if (paddingHorizontal > 0) {
            seslSetFillHorizontalPaddingEnabled(true)
        }
    }

    override fun setGridStrategy(strategy: GridStrategy) {
        super.setGridStrategy(strategy)
        invalidateItemDecorations()
        requestLayout()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (adapter == null) {
            mSavedAdapter?.let {
                adapter = it
            }
        }
        mSavedAdapter = null
    }

    override fun onDetachedFromWindow() {
        mSavedAdapter = adapter
        super.onDetachedFromWindow()
        mFooterHelper.cancelAnimation()
    }

    //sesl9
    inner class SelectedHorizontalItemDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)

            val adapter = parent.adapter ?: return

            val position = parent.getChildAdapterPosition(view)
            val res = parent.context.resources

            val horizontalPadding =
                res.getDimensionPixelSize(R.dimen.picker_app_selected_layout_horizontal_padding)
            val horizontalInterval =
                res.getDimensionPixelSize(R.dimen.picker_app_selected_item_view_interval_horizontal_on_port)

            val isIconOnly = gridStrategy is IconOnlyGridStrategy

            outRect.left = if (position == 0) horizontalPadding else horizontalInterval
            outRect.right =
                if (position == adapter.itemCount - 1) horizontalPadding else horizontalInterval

            outRect.top = res.getDimensionPixelSize(
                if (isIconOnly) R.dimen.picker_app_grid_item_view_icon_only_top_padding
                else R.dimen.picker_app_grid_item_view_item_top_padding
            )
            outRect.bottom = res.getDimensionPixelSize(
                if (isIconOnly) R.dimen.picker_app_grid_item_view_icon_only_bottom_padding
                else R.dimen.picker_app_grid_item_view_item_bottom_padding
            )

            view.findViewById<View>(R.id.item)?.let { item ->
                item.layoutParams.width =
                    res.getDimensionPixelOffset(R.dimen.picker_app_grid_item_view_title_width)

                val iconTopMargin = if (isIconOnly) {
                    res.getDimension(R.dimen.picker_app_grid_item_view_icon_only_layout_margin_top_bottom)
                } else {
                    res.getDimension(R.dimen.picker_app_grid_item_view_icon_layout_margin_top)
                }
                val iconBottomMargin = if (isIconOnly) {
                    res.getDimension(R.dimen.picker_app_grid_item_view_icon_only_layout_margin_top_bottom)
                } else {
                    res.getDimension(R.dimen.picker_app_grid_item_view_icon_layout_margin_bottom)
                }
                val titleSize = if (isIconOnly) {
                    0f
                } else {
                    res.getDimension(R.dimen.picker_app_grid_icon_title_size) * 2f
                }

                item.layoutParams.height = ceil(
                    res.getDimension(R.dimen.picker_app_grid_icon_size) +
                        iconTopMargin + iconBottomMargin + titleSize -
                        res.getDimension(R.dimen.picker_app_grid_item_view_remove_icon_layout_margin)
                ).toInt()
            }
        }
    }

    //sesl9
    inner class SelectedVerticalItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)

            val position = parent.getChildAdapterPosition(view)
            if (position == -1) return

            val viewHolder = parent.getChildViewHolder(view)
            if (viewHolder is GroupTitleViewHolder || viewHolder is FrameViewHolder) return

            if (parent.adapter == null) return

            val gridLayoutManager = parent.layoutManager as? GridLayoutManager ?: return

            val spanCount = gridLayoutManager.spanCount
            outRect.top = spacing / 2
            outRect.bottom = spacing / 2

            val horizontalInterval =
                view.context.resources.getDimensionPixelOffset(R.dimen.picker_app_selected_layout_horizontal_interval) / 2

            val columnIndex = position % spanCount
            outRect.left = if (columnIndex == 0) 0 else horizontalInterval
            outRect.right = if (columnIndex == spanCount - 1) 0 else horizontalInterval

            view.findViewById<View>(R.id.item)?.layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    //sesl9
    inner class SelectedSystemBarRoundedCornerDecoration(
        context: Context,
        backgroundColor: Int,
        private var bottomSystemBarHeight: Int = 0
    ) : RecyclerView.ItemDecoration() {

        private val paint = Paint().apply {
            color = backgroundColor
            style = Paint.Style.FILL
        }

        private val mListRoundedCorner = SeslRoundedCorner(context).apply {
            roundedCorners = ROUNDED_CORNER_ALL
        }

        private val mFooterRoundedCorner = SeslSubheaderRoundedCorner(context).apply {
            roundedCorners = ROUNDED_CORNER_ALL
            setRoundedCornerColor(ROUNDED_CORNER_ALL, backgroundColor)
        }

        override fun seslOnDispatchDraw(
            c: Canvas,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.seslOnDispatchDraw(c, parent, state)

            if (bottomSystemBarHeight > 0) {
                c.drawRect(
                    parent.left.toFloat(),
                    (parent.bottom - bottomSystemBarHeight).toFloat(),
                    parent.right.toFloat(),
                    parent.bottom.toFloat(),
                    paint
                )
            }

            val paddingLeft = parent.paddingLeft
            val paddingRight = parent.paddingRight
            if (paddingLeft <= 0 && paddingRight <= 0 && bottomSystemBarHeight <= 0) {
                mListRoundedCorner.drawRoundedCorner(c, Insets.NONE)
            } else {
                mListRoundedCorner.drawRoundedCorner(
                    c,
                    Insets.of(paddingLeft, 0, paddingRight, bottomSystemBarHeight)
                )
            }

            val headerFooterAdapter =
                this@SeslSelectLayoutSelectedListView.adapter as? HeaderFooterAdapter
            if (headerFooterAdapter != null && headerFooterAdapter.footersCount > 0) {
                val footerPosition =
                    headerFooterAdapter.itemCount - headerFooterAdapter.footersCount
                parent.children.forEach { child ->
                    if (parent.getChildViewHolder(child).bindingAdapterPosition == footerPosition) {
                        mFooterRoundedCorner.roundedCorners = SeslRoundedCorner.ROUNDED_CORNER_TOP_RIGHT or
                            SeslRoundedCorner.ROUNDED_CORNER_BOTTOM_RIGHT
                        mFooterRoundedCorner.drawRoundedCorner(child, c)
                    }
                }
            }
        }

        fun updateHeight(height: Int) {
            if (bottomSystemBarHeight != height) {
                bottomSystemBarHeight = height
                this@SeslSelectLayoutSelectedListView.invalidateItemDecorations()
            }
        }
    }
}
