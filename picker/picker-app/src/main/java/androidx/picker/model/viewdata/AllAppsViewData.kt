package androidx.picker.model.viewdata

import androidx.picker.loader.select.SelectableItem
import androidx.picker.model.Selectable

/**
 * The ui model for the "All Apps" item managed by [androidx.picker.adapter.AbsAdapter].
 * This uses the [ComposableTypeSet.AllSwitch][androidx.picker.features.composable.ComposableTypeSet.AllSwitch]
 *
 * This data class holds information needed to display the "All Apps" entry, which typically
 * allows the user to view and select from a complete list of applications.
 *
 * @property selectableItem The underlying [SelectableItem] associated with the "All Apps" entry.
 *                          This provides the core selection behavior and identity for this view.
 */
data class AllAppsViewData(
    override val selectableItem: SelectableItem
) : ViewData, Selectable