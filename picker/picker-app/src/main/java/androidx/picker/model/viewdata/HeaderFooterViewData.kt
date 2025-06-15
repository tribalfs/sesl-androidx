package androidx.picker.model.viewdata

import android.view.View
import kotlin.jvm.internal.Intrinsics

/**
 * The ui model for a header or footer view managed by
 * [androidx.picker.adapter.HeaderFooterAdapter]
 *
 * This class is used to represent the data associated with a header or footer view in a list or
 * grid. It contains a reference to the [View] that should be displayed as the header or footer.
 *
 * @property view The [View] to be displayed as the header or footer.
 */
class HeaderFooterViewData(@JvmField val view: View) : ViewData {

    /**
     * Creates a copy of this [HeaderFooterViewData] with optional replacement view.
     *
     * @param view The new [View] to be used. Defaults to the current instance's [view] if null.
     * @return A new [HeaderFooterViewData] instance with the updated [view].
     */
    @JvmOverloads
    fun copy(view: View = this.view): HeaderFooterViewData {
        return HeaderFooterViewData(view)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        return (other is HeaderFooterViewData) && Intrinsics.areEqual(this.view, other.view)
    }

    override fun hashCode(): Int {
        return this.view.hashCode()
    }

    override fun toString(): String {
        return "HeaderFooterViewData(view=" + this.view + ")"
    }
}