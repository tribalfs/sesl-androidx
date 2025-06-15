package androidx.picker.adapter.viewholder

import android.view.View
import androidx.picker.adapter.AbsAdapter
import androidx.picker.helper.setEnabledDeeply
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.ViewData
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.DisposableHandle

/**
 * A generic [RecyclerView.ViewHolder] for picker items.
 *
 * This class provides a base implementation for binding data to views and handling view recycling.
 * Subclasses can override methods to provide specific behavior for different item types.
 *
 * @param view The view for this ViewHolder.
 */
open class PickerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private var disposable: DisposableHandle? = null

    /**
     * Binds the adapter to this ViewHolder.
     *
     * This method can be overridden by subclasses to perform specific actions when the adapter is
     * bound, such as setting up click listeners or accessing adapter-specific data.
     *
     * @param adapter The [AbsAdapter] to bind.
     */
    open fun bindAdapter(adapter: AbsAdapter) {
        // Optionally overridden by subclasses
    }

    /**
     * Updates the enabled/disabled state of this ViewHolder
     * if the [ViewData] is an instance of [AppInfoViewData].
     *
     * @param data The [ViewData] to bind.
     */
    open fun bindData(data: ViewData) {
        if (data is AppInfoViewData) {
            disposable = data.dimmedItem.bind { dimmed ->
                setViewEnableState(dimmed != true)
            }
        }
    }

    /**
     * Called when the view is recycled.
     *
     * This method is responsible for cleaning up any resources held by the ViewHolder, such as
     * click listeners or subscriptions. It removes any click listeners from the `itemView` and
     * disposes of the `disposable` if it exists.
     */
    open fun onViewRecycled() {
        if (itemView.hasOnClickListeners()) {
            itemView.setOnClickListener(null)
        }
        disposable?.dispose()
        disposable = null
    }

    /**
     * Sets the enabled state of the `itemView` and its children.
     *
     * @param enable `true` to enable the view and its children, `false` to disable them.
     */
    open fun setViewEnableState(enable: Boolean) {
        itemView.setEnabledDeeply(enable)
    }
}