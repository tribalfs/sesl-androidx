package androidx.picker.model

import androidx.annotation.RestrictTo

/**
 * Interface to mark the data associated with a span in a grid-like structure.
 *
 * This interface provides information about the number of spans an item occupies.
 * It is intended for use within the library to manage layout and sizing of items
 * within a grid.
 *
 * @property spanCount The number of spans this item occupies. A value of
 *   [SPAN_FULL_SIZE] indicates that the item should occupy the full available width
 *   or height of the grid.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
interface SpanData {
    val spanCount: Int

    companion object {
        const val SPAN_FULL_SIZE: Int = -1
    }
}