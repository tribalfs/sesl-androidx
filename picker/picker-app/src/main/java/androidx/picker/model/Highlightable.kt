package androidx.picker.model

import androidx.picker.features.observable.ObservableProperty


/**
 * Interface for picker items that can be highlighted.
 *
 * This interface is used by pickers that support highlighting of items as the user scrolls through
 * them.
 * @property highlightText property containing the text to highlight in the item.
 */
fun interface Highlightable {
    fun getHighlightText(): ObservableProperty<String>
}