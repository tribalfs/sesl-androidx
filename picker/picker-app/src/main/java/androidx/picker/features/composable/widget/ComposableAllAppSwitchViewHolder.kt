package androidx.picker.features.composable.widget

import android.annotation.SuppressLint
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityManager
import androidx.annotation.Keep
import androidx.appcompat.widget.SwitchCompat
import androidx.core.util.Supplier
import androidx.picker.R
import androidx.picker.features.composable.ActionableComposableViewHolder
import androidx.picker.model.viewdata.AllAppsViewData
import androidx.picker.model.viewdata.ViewData
import kotlinx.coroutines.DisposableHandle

/**
 * A concrete implementation of the [ActionableComposableViewHolder]
 * that displays a switch to toggle the selection state of all apps.
 *
 * This ViewHolder binds to [AllAppsViewData] and updates the switch's state based on the
 * `selectableItem`'s state. It also handles user interactions with the switch, updating the
 * `selectableItem` accordingly.
 *
 * Accessibility features are handled by making the switch non-focusable and non-clickable when
 * accessibility services are enabled, relying on the parent item's accessibility handling.
 *
 * @param frameView The view to be used by this [ComposableActionViewHolder]
 * which is expected to be or to contain a SwitchCompat (with the ID `R.id.switch_widget`)
 * and a View (with the ID `R.id.switch_divider_widget`).
 */
@Keep
class ComposableAllAppSwitchViewHolder(
    frameView: View
) : ActionableComposableViewHolder(frameView) {

    private var disposableHandle: DisposableHandle? = null
    private val divider = frameView.findViewById<View>(R.id.switch_divider_widget)
    private var fromUser: Boolean = false
    private val switch = frameView.findViewById<SwitchCompat>(R.id.switch_widget)

    override fun bindData(viewData: ViewData) {
        val allAppsViewData = viewData as AllAppsViewData
        val selectableItem = allAppsViewData.selectableItem

        disposableHandle?.dispose()
        disposableHandle = selectableItem.bind { checked ->
            fromUser = false
            switch.isChecked = checked
        }

        switch.isChecked = selectableItem.getState()

        switch.setOnClickListener { selectableItem.setValue(switch.isChecked) }

        @SuppressLint("ClickableViewAccessibility")
        switch.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                fromUser = true
            }
            false
        }

        switch.setOnCheckedChangeListener { _, isChecked ->
            if (fromUser) {
                selectableItem.setValue(isChecked)
            }
            fromUser = false
        }

        divider.visibility = View.GONE

        doAction = Supplier {
            selectableItem.setValue(!switch.isChecked)
            true
        }
    }

    override fun onBind(itemView: View) {
        super.onBind(itemView)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val accessibilityManager = itemView.context.getSystemService(AccessibilityManager::class.java)
            if (accessibilityManager?.isEnabled == true) {
                switch.isFocusable = false
                switch.isClickable = false
                itemView.contentDescription = null
            }
        }
    }

    override fun onViewRecycled(itemView: View) {
        super.onViewRecycled(itemView)
        switch.setOnCheckedChangeListener(null)
        @SuppressLint("ClickableViewAccessibility")
        switch.setOnTouchListener(null)
        fromUser = false
        disposableHandle?.dispose()
    }
}