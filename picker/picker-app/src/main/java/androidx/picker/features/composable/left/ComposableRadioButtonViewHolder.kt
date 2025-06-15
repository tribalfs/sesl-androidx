package androidx.picker.features.composable.left

import android.view.View
import android.widget.RadioButton
import androidx.annotation.Keep
import androidx.core.util.Supplier
import androidx.picker.features.composable.ActionableComposableViewHolder
import androidx.picker.features.composable.widget.ComposableActionViewHolder
import androidx.picker.helper.setAccessibilityFocusable
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.ViewData
import kotlinx.coroutines.DisposableHandle

/**
 * ViewHolder for a composable view that displays a RadioButton.
 *
 * This ViewHolder is responsible for binding data to a RadioButton and handling user interactions.
 * It extends [ActionableComposableViewHolder] to support actions when the view is clicked.
 *
 * @param frameView The view to be used by this [ComposableActionViewHolder]
 * which is expected to be a RadioButton.
 */
@Keep
class ComposableRadioButtonViewHolder(
    frameView: View
) : ActionableComposableViewHolder(frameView) {

    private var disposableHandle: DisposableHandle? = null
    private val radioButton: RadioButton = frameView as RadioButton

    override fun bindData(viewData: ViewData) {
        val appInfoViewData = viewData as? AppInfoViewData
        val selectableItem = appInfoViewData?.selectableItem ?: return

        disposableHandle?.dispose()
        disposableHandle = selectableItem.bind{ isChecked ->
            radioButton.isChecked = isChecked
        }

        radioButton.setOnClickListener {
            selectableItem.setValue(radioButton.isChecked)
        }

        doAction = Supplier {
            selectableItem.setValue(!radioButton.isChecked)
            true
        }
    }

    override fun onBind(itemView: View) {
        radioButton.setAccessibilityFocusable(itemView.hasOnClickListeners())
        super.onBind(itemView)
    }

    override fun onViewRecycled(itemView: View) {
        super.onViewRecycled(itemView)
        radioButton.setOnClickListener(null)
        disposableHandle?.dispose()
    }
}