package androidx.picker.features.composable

import androidx.picker.features.composable.icon.IconFrame
import androidx.picker.features.composable.left.LeftFrame
import androidx.picker.features.composable.title.TitleFrame
import androidx.picker.features.composable.widget.WidgetFrame

/**
 * Defines the different types of composable views that can be used.
 *
 * Each type defines which frames (left, icon, title, widget) are displayed.
 *
 * @property leftFrame The type of frame to display on the left side of the composable.
 * @property iconFrame The type of frame to display for the icon.
 * @property titleFrame The type of frame to display for the title.
 * @property widgetFrame The type of frame to display on the right side of the composable (widget).
 */
enum class ComposableTypeSet(
    override val leftFrame: LeftFrame? = null,
    override val iconFrame: IconFrame? = null,
    override val titleFrame: TitleFrame? = null,
    override val widgetFrame: WidgetFrame? = null
) : ComposableType {
    /**
     * A [ComposableType] that displays an icon (iconFrame) and a title (titleFrame).
     */
    TextOnly(
        leftFrame = null,
        iconFrame = IconFrame.Icon,
        titleFrame = TitleFrame.Title,
        widgetFrame = null
    ),
    /**
     * A [ComposableType] that displays an icon (iconFrame), a title (titleFrame),
     * and a switch(widgetFrame).
     */
    Switch(
        leftFrame = null,
        iconFrame = IconFrame.Icon,
        titleFrame = TitleFrame.Title,
        widgetFrame = WidgetFrame.Switch
    ),
    /**
     * A [ComposableType] that displays a title (titleFrame) and a switch (widgetFrame).
     */
    AllSwitch(
        leftFrame = null,
        iconFrame = null,
        titleFrame = TitleFrame.Title,
        widgetFrame = WidgetFrame.AllAppsSwitch
    ),
    /**
     * A [ComposableType] that displays a checkbox (leftFrame), an icon (iconFrame),
     * and a title (titleFrame).
     */
    CheckBox(
        leftFrame = LeftFrame.CheckBox,
        iconFrame = IconFrame.Icon,
        titleFrame = TitleFrame.Title,
        widgetFrame = null
    ),
    /**
     * A [ComposableType] that displays a checkbox (leftFrame), an icon (iconFrame),
     * a title (titleFrame), and an action button (widgetFrame).
     */
    CheckBoxAction(
        leftFrame = LeftFrame.CheckBox,
        iconFrame = IconFrame.Icon,
        titleFrame = TitleFrame.Title,
        widgetFrame = WidgetFrame.Action
    ),
    /**
     * A [ComposableType] that displays a checkbox (leftFrame), an icon (iconFrame),
     * a title (titleFrame), and an expander button (widgetFrame).
     */
    CheckBoxExpander(
        leftFrame = LeftFrame.CheckBox,
        iconFrame = IconFrame.Icon,
        titleFrame = TitleFrame.Title,
        widgetFrame = WidgetFrame.Expander
    ),
    /**
     * A [ComposableType] that displays a radio button (leftFrame), an icon (iconFrame),
     * and a title (titleFrame).
     */
    Radio(
        leftFrame = LeftFrame.Radio,
        iconFrame = IconFrame.Icon,
        titleFrame = TitleFrame.Title,
        widgetFrame = null
    ),
    /**
     * A [ComposableType] that displays a radio button (leftFrame), an icon (iconFrame),
     * a title (titleFrame), and an action button (widgetFrame).
     */
    RadioAction(
        leftFrame = LeftFrame.Radio,
        iconFrame = IconFrame.Icon,
        titleFrame = TitleFrame.Title,
        widgetFrame = WidgetFrame.Action
    )
}