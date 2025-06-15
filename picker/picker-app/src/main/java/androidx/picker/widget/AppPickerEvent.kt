package androidx.picker.widget

import android.view.View
import androidx.picker.model.AppInfo


/**
 * An interface for handling app picker events.
 */
interface AppPickerEvent {
    /**
     * Interface definition for a callback to be invoked when an item in this
     * view has been clicked.
     */
    fun interface OnItemClickEventListener {
        /**
         * Called when an item in this view has been clicked.
         *
         * @param view The view that was clicked.
         * @param appInfo The information about the app that was clicked.
         * @return True if the event was handled, false otherwise.
         */
        fun onClick(view: View?, appInfo: AppInfo): Boolean
    }

    /**
     * Sets the listener to be called when an item in the list is clicked.
     *
     * @param listener The listener to be called when an item is clicked, or null to remove the listener.
     */
    fun setOnItemClickEventListener(listener: OnItemClickEventListener?)

    /**
     * Sets the listener to be called when an action button in an item is clicked.
     *
     * The action button is typically a secondary action associated with the list item,
     * such as "info" or "settings".
     *
     * @param listener The listener to be called when an action button is clicked,
     *                                or null to remove the listener.
     */
    fun setOnItemActionClickEventListener(listener: OnItemClickEventListener?)
}