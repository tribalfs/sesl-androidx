package androidx.picker.adapter

import android.widget.Filterable
import androidx.picker.adapter.viewholder.PickerViewHolder
import androidx.picker.model.viewdata.ViewData
import androidx.picker.widget.SeslAppPickerView

/**
 * Adapter interface for the app picker, supporting filtering and binding.
 */
interface AppPickerAdapter : Filterable {

    /**
     * Listener for binding view holders with view data.
     */
    fun interface OnBindListener {
        fun onBindViewHolder(viewHolder: PickerViewHolder, viewData: ViewData)
    }

    /**
     * Returns the [ViewData] at the given position, or null if not available.
     */
    fun getAppInfo(position: Int): ViewData?

    /**
     * The filtered data set backing the adapter.
     */
    fun getDataSetFiltered(): List<ViewData>

    /**
     * Sets the listener to be invoked when a view holder is bound.
     */
    fun setOnBindListener(listener: OnBindListener)

    /**
     * Sets the search filter listener for the picker view.
     */
    fun setOnSearchFilterListener(listener: SeslAppPickerView.OnSearchFilterListener)

    /**
     * Submits a new list of items to the adapter.
     */
    fun submitList(itemList: List<ViewData>)

    /**
     * Updates the given item in the adapter.
     */
    fun updateItem(viewData: ViewData)
}