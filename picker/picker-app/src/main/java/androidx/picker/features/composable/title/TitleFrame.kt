package androidx.picker.features.composable.title

import androidx.annotation.LayoutRes
import androidx.picker.R
import androidx.picker.features.composable.ComposableFrame
import androidx.picker.features.composable.ComposableViewHolder
import kotlin.jvm.java

/**
 * Enum class that defines the available title frames in the picker.
 *
 * Each enum entry represents a specific title layout and its corresponding ViewHolder class.
 * This allows for easy management and selection of different title styles within the picker
 * component.
 *
 * @property layoutResId The layout resource ID for the title frame.
 * @property viewHolderClass The ViewHolder class responsible for binding data to the title frame.
 */
enum class TitleFrame(
    @LayoutRes override val layoutResId: Int,
    override val viewHolderClass: Class<out ComposableViewHolder>
) : ComposableFrame {
    Title(
        R.layout.picker_app_composable_frame_title_single,
        ComposableTitleViewHolder::class.java
    )
}