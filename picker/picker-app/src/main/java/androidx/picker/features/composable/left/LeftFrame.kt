package androidx.picker.features.composable.left

import androidx.annotation.LayoutRes
import androidx.picker.R
import androidx.picker.features.composable.ComposableFrame
import androidx.picker.features.composable.ComposableViewHolder
import kotlin.jvm.java

/**
 * Defines the available left frames that can be used in the composable picker.
 *
 * Each enum constant represents a specific left frame type and provides the layout resource ID
 * and the corresponding view holder class.
 *
 * @property layoutResId The layout resource ID for the left frame.
 * @property viewHolderClass The class of the view holder associated with the left frame.
 */
enum class LeftFrame(
    @LayoutRes override val layoutResId: Int,
    override val viewHolderClass: Class<out ComposableViewHolder>
) : ComposableFrame {
    Radio(
        R.layout.picker_app_composable_frame_radiobutton,
        ComposableRadioButtonViewHolder::class.java
    ),
    CheckBox(
        R.layout.picker_app_composable_frame_checkbox,
        ComposableCheckBoxViewHolder::class.java
    );
}