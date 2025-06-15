package androidx.picker.model

import androidx.picker.loader.select.SelectableItem


/**
 * Defines a type that can have its item selected.
 *
 * It is expected to be implemented on a model type.
 *
 * @property selectableItem Member of [Selectable] interface that holds the state
 * and UI information about how its item should be selected.
 */
interface Selectable {
    val selectableItem: SelectableItem?
}