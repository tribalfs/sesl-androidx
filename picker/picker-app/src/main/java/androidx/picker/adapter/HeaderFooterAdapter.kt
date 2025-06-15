package androidx.picker.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.SectionIndexer
import androidx.appcompat.util.SeslRoundedCorner.ROUNDED_CORNER_NONE
import androidx.picker.R
import androidx.picker.adapter.viewholder.FrameViewHolder
import androidx.picker.adapter.viewholder.PickerViewHolder
import androidx.picker.helper.seslSetRoundedCorner
import androidx.picker.model.AppInfo
import androidx.picker.model.viewdata.HeaderFooterViewData
import androidx.picker.model.viewdata.ViewData
import androidx.picker.widget.SeslAppPickerView
import androidx.recyclerview.widget.RecyclerView

/**
 * A RecyclerView.Adapter that allows for adding header and footer views to a wrapped adapter.
 *
 * This adapter acts as a wrapper around another [AbsAdapter] and provides functionality
 * to prepend header views and append footer views to the list displayed by the RecyclerView.
 *
 * @param wrappedAdapter The [AbsAdapter] that this adapter will wrap.
 *                       This adapter will display the items from the wrapped adapter
 *                       along with any added headers and footers.
 */
class HeaderFooterAdapter(
    private val wrappedAdapter: AbsAdapter
) : RecyclerView.Adapter<PickerViewHolder>(), AppPickerAdapter, SectionIndexer {

    companion object {
        private const val HEADER_VIEW_TYPE = 1000
        private const val FOOTER_VIEW_TYPE = 1001
    }

    private val headerViewInfoList = mutableListOf<HeaderFooterViewData>()
    private val footerViewInfoList = mutableListOf<HeaderFooterViewData>()

    private val observer = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            @SuppressLint("NotifyDataSetChanged")
            this@HeaderFooterAdapter.notifyDataSetChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            notifyItemRangeChanged(headerViewInfoList.size + positionStart, itemCount, payload)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            notifyItemRangeInserted(headerViewInfoList.size + positionStart, itemCount)
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            val size = headerViewInfoList.size
            for (i in 0 until itemCount) {
                notifyItemMoved(fromPosition + size + i, toPosition + size + i)
            }
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            notifyItemRangeRemoved(headerViewInfoList.size + positionStart, itemCount)
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            notifyItemRangeChanged(headerViewInfoList.size + positionStart, itemCount)
        }
    }

    init {
        setHasStableIds(wrappedAdapter.hasStableIds())
    }

    /**
     * Adds a header view to the adapter.
     *
     * @param view The view to add as a header.
     * @param roundedCorners The rounded corners to apply to the header view. Default is 0.
     */
    @JvmOverloads
    fun addHeader(view: View, roundedCorners: Int = ROUNDED_CORNER_NONE) {
        view.seslSetRoundedCorner(roundedCorners, null)
        headerViewInfoList.add(HeaderFooterViewData(view))
        notifyItemInserted(headerViewInfoList.size - 1)
    }

    /**
     * Adds a footer view to the adapter.
     *
     * @param view The view to be added as a footer.
     * @param roundedCorners The rounded corners to be applied to the view.
     *                      See [androidx.picker.helper.SeslViewReflector.semSetRoundedCorners]
     *                      for possible values.
     */
    @JvmOverloads
    fun addFooter(view: View, roundedCorners: Int = ROUNDED_CORNER_NONE) {
        view.seslSetRoundedCorner(roundedCorners, null)
        footerViewInfoList.add(HeaderFooterViewData(view))
        notifyItemInserted(headersCount + wrappedAdapter.itemCount + footersCount - 1)
    }

    /**
     * Remove all headers from the list.
     */
    fun clearHeaders() {
        val count = headersCount
        if (count > 0) {
            headerViewInfoList.clear()
            notifyItemRangeRemoved(0, count)
        }
    }

    /**
     * Clears all footer views from the adapter.
     *
     * This function removes all previously added footer views.
     * If there are footers present, it clears the internal list of footers
     * and notifies the RecyclerView that the items have been removed.
     */
    fun clearFooters() {
        val count = footersCount
        if (count > 0) {
            footerViewInfoList.clear()
            notifyItemRangeRemoved(headersCount + wrappedAdapter.itemCount, count)
        }
    }

    val headersCount: Int get() = headerViewInfoList.size
    val footersCount: Int get() = footerViewInfoList.size

    override fun getItemCount(): Int = headersCount + wrappedAdapter.itemCount + footersCount

    override fun getItemId(position: Int): Long {
        if (position >= itemCount) return -1L
        return when (getItemViewType(position)) {
            HEADER_VIEW_TYPE -> headerViewInfoList[position].hashCode().toLong()
            FOOTER_VIEW_TYPE -> footerViewInfoList[footersCount + (position - itemCount)].hashCode().toLong()
            else -> wrappedAdapter.getItemId(position - headersCount)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position < headersCount -> HEADER_VIEW_TYPE
            position >= itemCount - footersCount -> FOOTER_VIEW_TYPE
            else -> wrappedAdapter.getItemViewType(position - headersCount)
        }
    }

    /**
     * Retrieves the [ViewData] associated with the item at the specified [position].
     *
     * This function takes into account headers, footers, and the wrapped adapter's data.
     *
     * @param position The position of the item to retrieve.
     * @return The [ViewData] at the specified position, or null if the position is out of bounds
     * or if no item exists at that position.
     */
    fun getItem(position: Int): ViewData? {
        if (position >= itemCount) return null
        return when (getItemViewType(position)) {
            HEADER_VIEW_TYPE -> headerViewInfoList[position]
            FOOTER_VIEW_TYPE -> footerViewInfoList[footersCount + (position - itemCount)]
            else -> wrappedAdapter.getDataSetFiltered().getOrNull(position - headersCount)
        }
    }

    override fun getAppInfo(position: Int): ViewData? =
        wrappedAdapter.getAppInfo(position)

    override fun getDataSetFiltered(): MutableList<ViewData> =
        wrappedAdapter.getDataSetFiltered()

    override fun getFilter(): Filter = wrappedAdapter.filter

    fun getPosition(appInfo: AppInfo): Int {
        val idx = wrappedAdapter.getDataSetFiltered().indexOfFirst { it.key == appInfo }
        return if (idx == -1) -1 else headersCount + idx
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        wrappedAdapter.registerAdapterDataObserver(observer)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        wrappedAdapter.unregisterAdapterDataObserver(observer)
    }

    override fun setOnBindListener(listener: AppPickerAdapter.OnBindListener) {
        wrappedAdapter.setOnBindListener(listener)
    }

    override fun setOnSearchFilterListener(listener: SeslAppPickerView.OnSearchFilterListener) {
        wrappedAdapter.setOnSearchFilterListener(listener)
    }

    override fun submitList(itemList: List<ViewData>) {
        wrappedAdapter.submitList(itemList)
    }

    override fun updateItem(viewData: ViewData) {
        wrappedAdapter.updateItem(viewData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickerViewHolder {
        return when (viewType) {
            HEADER_VIEW_TYPE, FOOTER_VIEW_TYPE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.picker_app_frame, parent, false)
                FrameViewHolder(view)
            }
            else -> wrappedAdapter.onCreateViewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: PickerViewHolder, position: Int) {
        when (getItemViewType(position)) {
            HEADER_VIEW_TYPE -> {
                val vg = holder.itemView as ViewGroup
                vg.removeAllViews()
                vg.addView(headerViewInfoList[position].view)
            }
            FOOTER_VIEW_TYPE -> {
                val idx = footersCount + (position - itemCount)
                val vg = holder.itemView as ViewGroup
                vg.removeAllViews()
                vg.addView(footerViewInfoList[idx].view)
            }
            else -> wrappedAdapter.onBindViewHolder(holder, position - headersCount)
        }
    }


    /**
     * This method is similar to [onBindViewHolder] but ensures that partial bind with payload won't be
     * applied for headers and footers.
     *
     * @param holder The [PickerViewHolder] to update.
     * @param position The position of the item within the adapter's data set.
     * @param payloads A non-null list of merged payloads. If not empty and the item is not a header or footer,
     *                 the wrapped adapter's partial bind is invoked; otherwise, a full bind is performed.
     */
    fun onBindViewHolder2(holder: PickerViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.isNotEmpty() && getItemViewType(position) != HEADER_VIEW_TYPE && getItemViewType(position) != FOOTER_VIEW_TYPE) {
            wrappedAdapter.onBindViewHolder(holder, position - headersCount, payloads)
        } else {
            @Suppress("DEPRECATION")
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getSections(): Array<Any> = wrappedAdapter.sections

    override fun getPositionForSection(sectionIndex: Int): Int {
        val offset = headersCount
        return wrappedAdapter.getPositionForSection(sectionIndex) + offset
    }

    override fun getSectionForPosition(position: Int): Int {
        val positionAdjusted = position - headersCount
        if (positionAdjusted < 0) return 0
        return wrappedAdapter.getSectionForPosition(positionAdjusted)
    }
}