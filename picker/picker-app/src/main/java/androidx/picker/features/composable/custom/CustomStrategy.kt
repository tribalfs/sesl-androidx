package androidx.picker.features.composable.custom

import androidx.annotation.Keep
import androidx.picker.features.composable.ComposableFrame
import androidx.picker.features.composable.ComposableType
import androidx.picker.features.composable.ComposableTypeImpl
import androidx.picker.features.composable.DefaultComposableStrategy
import androidx.picker.features.composable.icon.IconFrame
import androidx.picker.features.composable.left.LeftFrame
import androidx.picker.features.composable.title.TitleFrame
import androidx.picker.features.composable.widget.WidgetFrame
import androidx.picker.model.AppData
import androidx.picker.model.AppData.Companion.TYPE_ITEM_CHECKBOX
import androidx.picker.model.AppData.Companion.TYPE_ITEM_RADIOBUTTON
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.ViewData
import kotlin.collections.plus

/**
 * An abstract class that extends [DefaultComposableStrategy] to provide a custom strategy
 * for selecting composable types. This class allows for the addition of custom frames
 * to the widget frame list and custom logic for selecting composable types based on view data.
 *
 * @see CustomViewHolder
 * @see CustomFrame
 */
@Keep
abstract class CustomStrategy : DefaultComposableStrategy() {

    private val customWidgetList by lazy { getCustomFrameList() }

    override val widgetFrameList by lazy { WidgetFrame.entries + customWidgetList }

    /**
     * Abstract method to be implemented by subclasses to provide a list of custom frames.
     * This method is called lazily to initialize the [customWidgetList].
     *
     * @return A list of [CustomFrame] objects.
     */
    abstract fun getCustomFrameList(): List<CustomFrame>

    override fun selectComposableType(viewData: ViewData): ComposableType? {
        if (viewData !is AppInfoViewData) {
            return super.selectComposableType(viewData)
        }
        val customFrame = customWidgetList.firstOrNull { it.predicate(viewData as AppData) }
        if (customFrame == null) {
            return super.selectComposableType(viewData)
        }
        val titleFrame = TitleFrame.Title
        val leftFrame = when (viewData.itemType) {
            TYPE_ITEM_CHECKBOX -> LeftFrame.CheckBox
            TYPE_ITEM_RADIOBUTTON -> LeftFrame.Radio
            else -> null
        }
        return ComposableTypeImpl(leftFrame, IconFrame.Icon, titleFrame, customFrame)
    }
}