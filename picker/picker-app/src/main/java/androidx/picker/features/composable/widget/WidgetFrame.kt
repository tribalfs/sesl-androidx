package androidx.picker.features.composable.widget

import androidx.annotation.LayoutRes
import androidx.picker.R
import androidx.picker.features.composable.ComposableFrame
import androidx.picker.features.composable.ComposableViewHolder
import kotlin.jvm.java

/**
 * Defines the available frames for widgets within the picker.
 *
 * Each enum constant represents a distinct widget frame, specifying its layout resource ID
 * and the corresponding [ComposableViewHolder] class responsible for managing its view.
 *
 * @property layoutResId The layout resource ID for the widget frame.
 * @property viewHolderClass The class of the [ComposableViewHolder] associated with this widget
 *   frame.
 */
enum class WidgetFrame(
    @LayoutRes override val layoutResId: Int,
    override val viewHolderClass: Class<out ComposableViewHolder>
) : ComposableFrame {

    Switch(
        R.layout.picker_app_composable_frame_switch,
        ComposableSwitchViewHolder::class.java
    ),
    AllAppsSwitch(
        R.layout.picker_app_composable_frame_switch,
        ComposableAllAppSwitchViewHolder::class.java
    ),
    Action(
        R.layout.picker_app_composable_frame_actionbutton,
        ComposableActionViewHolder::class.java
    ),
    Expander(
        R.layout.picker_app_composable_frame_expander,
        ComposableExpanderViewHolder::class.java
    )
}