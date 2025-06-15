package androidx.picker.features.composable

import androidx.annotation.LayoutRes

/**
 * Represents a frame that can be composed within a view.
 *
 * This interface defines the essential properties for a composable frame, allowing it to be
 * inflated and managed within a larger view structure.
 *
 * @property layoutResId The resource ID of the layout file associated with this frame.
 * @property viewHolderClass The class of the [ComposableViewHolder] associated with this frame.
 */
interface ComposableFrame {
    @get:LayoutRes
    val layoutResId: Int
    val viewHolderClass: Class<out ComposableViewHolder>
}