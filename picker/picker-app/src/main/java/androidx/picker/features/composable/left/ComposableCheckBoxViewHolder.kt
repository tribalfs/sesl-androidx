package androidx.picker.features.composable.left

import android.view.View
import android.widget.CheckBox
import androidx.annotation.Keep
import androidx.core.util.Supplier
import androidx.picker.features.composable.ActionableComposableViewHolder
import androidx.picker.features.composable.widget.ComposableActionViewHolder
import androidx.picker.helper.setAccessibilityFocusable
import androidx.picker.model.Selectable
import androidx.picker.model.viewdata.ViewData
import kotlinx.coroutines.DisposableHandle

/**
 * ViewHolder for Composable Items with Checkbox.
 *
 * This ViewHolder is responsible for binding data to a Checkbox view and handling user
 * interactions. This is a concrete implementation of [ActionableComposableViewHolder]  to provide action handling
 * capabilities.
 *
 * @param frameView The view to be used by this [ComposableActionViewHolder]
 * which is expected to be a CheckBox.
 */
@Keep
class ComposableCheckBoxViewHolder(
    frameView: View
) : ActionableComposableViewHolder(frameView) {

    private val checkBox: CheckBox = frameView as CheckBox
    private var disposableHandle: DisposableHandle? = null

    override fun bindData(viewData: ViewData) {
        val selectable = viewData as? Selectable
        val selectableItem = selectable?.selectableItem ?: return

        disposableHandle?.dispose()
        disposableHandle = selectableItem.bind { isChecked ->
            checkBox.isChecked = isChecked
        }

        checkBox.setOnClickListener {
            selectableItem.setValue(checkBox.isChecked)
        }

        doAction = Supplier {
            selectableItem.setValue(!checkBox.isChecked)
            true
        }
    }

    override fun onBind(itemView: View) {
        checkBox.setAccessibilityFocusable(itemView.hasOnClickListeners())
        super.onBind(itemView)
    }

    override fun onViewRecycled(itemView: View) {
        super.onViewRecycled(itemView)
        checkBox.setOnClickListener(null)
        disposableHandle?.dispose()
        disposableHandle = null
    }
}