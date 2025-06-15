package androidx.picker.model.viewdata

/**
 * Interface used as base ui model for item views managed by
 * [androidx.picker.adapter.AbsAdapter].
 *
 * @property key The unique identifier for this item. This is used by diffing algorithms (like
 *   [androidx.recyclerview.widget.DiffUtil]) to compare items in the list.
 *   By default, the `key` is the object instance itself (`this`).
 */
interface ViewData {
    val key: Any get() = this
}