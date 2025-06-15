package androidx.picker.features.composable

import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.Keep
import androidx.picker.adapter.AbsAdapter
import androidx.picker.model.viewdata.ViewData
import kotlin.jvm.internal.Intrinsics

/**
 * This abstract class design to encapsulate the view holder delegate functions
 * to work with different view types.
 *
 * @param frameView The root [View] of the associated ViewHolder.
 */
@Keep
abstract class ComposableViewHolder(@JvmField val frameView: View) {

    open fun bindAdapter(adapter: AbsAdapter) {
    }

    abstract fun bindData(viewData: ViewData)

    @CallSuper
    open fun onBind(itemView: View) {}

    @CallSuper
    open fun onViewRecycled(itemView: View) {}
}