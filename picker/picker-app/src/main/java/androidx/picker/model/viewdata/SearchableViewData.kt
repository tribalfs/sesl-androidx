package androidx.picker.model.viewdata


/**
 * Interface for the UI model of an item view managed by [AbsAdapter][androidx.picker.adapter.AbsAdapter]
 * to make the item searchable.
 *
 * @property searchable This property provides a list of strings that can be used to search the data.
 * The strings should be representative of the data and should be able to be matched by the user's search
 * query.
 */
interface SearchableViewData : ViewData {
    val searchable: List<String>
}