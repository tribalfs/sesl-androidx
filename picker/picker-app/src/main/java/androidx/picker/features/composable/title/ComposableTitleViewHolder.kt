package androidx.picker.features.composable.title

import android.R.attr.handle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.picker.R
import androidx.picker.features.composable.ComposableViewHolder
import androidx.picker.helper.getPrimaryDarkColor
import androidx.picker.helper.getTextSecondaryColor
import androidx.picker.helper.setHighLightText
import androidx.picker.model.AppData.Companion.TYPE_ITEM_SWITCH
import androidx.picker.model.Highlightable
import androidx.picker.model.viewdata.AllAppsViewData
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.CategoryViewData
import androidx.picker.model.viewdata.ViewData
import kotlinx.coroutines.DisposableHandle
import kotlin.Lazy
import kotlin.LazyThreadSafetyMode

/**
 * ViewHolder for displaying a title item. This class is responsible for binding data
 * to the title view, summary view, and extra title view. It also handles layout adjustments
 * based on whether a sub-label is present.
 *
 * This ViewHolder supports different types of [ViewData]:
 * - [AppInfoViewData]: Displays app-specific information, including label, sub-label, and extra label.
 *                      It dynamically adjusts the layout if a sub-label is present and visible.
 * - [CategoryViewData]: Displays a category title.
 * - [AllAppsViewData]: Displays a generic "All Apps" title.
 *
 * If the [ViewData] implements the [Highlightable] interface, the title text will be highlighted
 * based on the provided highlight information.
 *
 * The ViewHolder manages [DisposableHandle]s to clean up resources (like listeners) when the
 * view is recycled or when new data is bound.
 *
 * @param frameView The root view of the item layout.
 */
@Keep
class ComposableTitleViewHolder(
    frameView: View
) : ComposableViewHolder(frameView) {

    private var currentLayoutId: Int = R.layout.picker_app_composable_frame_title_single
    private var disposableHandle: DisposableHandle? = null

    private val titleView: TextView = frameView.findViewById(R.id.title)
    private val summaryView: TextView = frameView.findViewById(R.id.summary)
    private val extraTitleView: TextView = frameView.findViewById(R.id.extra_label)

    private val highlightColor: Lazy<Int> = lazy(LazyThreadSafetyMode.NONE) {
        frameView.context.getPrimaryDarkColor()
    }
    private val subLabelValueColor: Lazy<Int> = lazy(LazyThreadSafetyMode.NONE) {
        frameView.context.getPrimaryDarkColor()
    }
    private val subLabelDescriptionColor: Lazy<Int> = lazy(LazyThreadSafetyMode.NONE) {
        frameView.context.getTextSecondaryColor()
    }

    private fun adjustLayout(hasSubLabel: Boolean) {
        if (frameView is ConstraintLayout) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(frameView.context, currentLayoutId)
            constraintSet.applyTo(frameView)
            frameView.layoutParams.height = getLayoutHeight(hasSubLabel)
        }
    }

    private fun getHighlightColor(): Int = highlightColor.value
    private fun getSubLabelValueColor(): Int = subLabelValueColor.value
    private fun getSubLabelDescriptionColor(): Int = subLabelDescriptionColor.value

    private fun getLayoutHeight(showSubLabel: Boolean): Int {
        return if (showSubLabel) {
            frameView.resources.getDimensionPixelOffset(R.dimen.picker_app_list_sub_label_height)
        } else {
            frameView.resources.getDimensionPixelOffset(R.dimen.picker_app_list_single_line_height)
        }
    }

    private fun getLayoutId(showSubLabel: Boolean): Int {
        return if (showSubLabel) {
            R.layout.picker_app_composable_frame_title_sublabel
        } else {
            R.layout.picker_app_composable_frame_title_single
        }
    }

    private fun getSubLabelShowState(viewData: ViewData): Boolean {
        if (viewData !is AppInfoViewData) return false
        return !(viewData.itemType == TYPE_ITEM_SWITCH && viewData.isValueInSubLabel && !viewData.selected)
    }

    override fun bindData(viewData: ViewData) {
        disposableHandle?.dispose()
        val disposableHandleList = mutableListOf<DisposableHandle>()

        when (viewData) {
            is AppInfoViewData -> {
                val hasSubLabel = !TextUtils.isEmpty(viewData.subLabel) && getSubLabelShowState(viewData)
                val layoutId = getLayoutId(hasSubLabel)
                if (currentLayoutId != layoutId) {
                    currentLayoutId = layoutId
                    adjustLayout(hasSubLabel)
                }
                titleView.text = viewData.label
                summaryView.text = viewData.subLabel
                extraTitleView.text = viewData.extraLabel
                summaryView.setTextColor(
                    if (viewData.isValueInSubLabel) getSubLabelValueColor() else getSubLabelDescriptionColor()
                )
                viewData.selectableItem?.let { selectableItem ->
                    val handle = selectableItem.registerAfterChangeUpdateListener { _ ->
                    val hasSubLabel2 = !TextUtils.isEmpty(viewData.subLabel) && getSubLabelShowState(viewData)
                    val layoutId2 = getLayoutId(hasSubLabel2)
                    if (currentLayoutId != layoutId2) {
                        currentLayoutId = layoutId2
                        adjustLayout(hasSubLabel2)
                    }
                }
                    disposableHandleList.add(handle)
                }
            }
            is CategoryViewData -> {
                titleView.text = viewData.title
            }
            is AllAppsViewData -> {
                titleView.text = titleView.resources.getString(R.string.title_all_apps)
            }
        }

        if (viewData is Highlightable) {
            val highlightable = viewData.getHighlightText()
            titleView.setHighLightText(highlightable.getState(), getHighlightColor())
            disposableHandleList.add(
                highlightable.bind { text -> titleView.setHighLightText(text, getHighlightColor()) }
            )
        }

        disposableHandle = object : DisposableHandle {
            override fun dispose() {
                disposableHandleList.forEach { it.dispose() }
            }
        }
    }

    override fun onViewRecycled(itemView: View) {
        super.onViewRecycled(itemView)
        disposableHandle?.dispose()
    }
}