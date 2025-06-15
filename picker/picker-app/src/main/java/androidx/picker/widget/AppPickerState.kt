package androidx.picker.widget

import androidx.picker.model.AppInfo


/**
 * Interface for managing the view state of an app picker.
 *
 * This interface defines methods for getting and setting the state of individual apps,
 * as well as setting the state of all apps at once. It also provides a way to listen
 * for changes to the state of the app picker.
 */
interface AppPickerState {
    /**
     * Interface for listening to state changes in the AppPicker.
     *
     * This interface provides callbacks for when the selection state of all apps changes
     * or when the selection state of an individual app changes.
     */
    interface OnStateChangeListener {
        /**
         * Called when the selection state of all apps changes.
         *
         * This function is a member of the [OnStateChangeListener] interface.
         *
         * @param isAllSelected `true` if all apps are now selected, `false` otherwise.
         */
        fun onStateAllChanged(isAllSelected: Boolean)

        /**
         * Called when the selection state of an individual application changes.
         *
         * @param appInfo The [AppInfo] object representing the application whose state has changed.
         * @param isSelected `true` if the application is now selected, `false` if it is now deselected.
         */
        fun onStateChanged(appInfo: AppInfo, isSelected: Boolean)
    }

    /**
     * Retrieves the current selection state of the specified application.
     *
     * @param appInfo The [AppInfo] object representing the application for which to get the state.
     * @return `true` if the application is selected, `false` otherwise.
     */
    fun getState(appInfo: AppInfo): Boolean

    /**
     * Sets the listener to be notified when the state of an app or all apps changes.
     *
     * This listener will receive callbacks when:
     * - The selection state of all apps changes via [setStateAll].
     * - The selection state of an individual app changes via [setState].
     *
     * @param listener The listener to set. Pass `null` to remove the current listener.
     */
    fun setOnStateChangeListener(listener: OnStateChangeListener?)

    /**
     * Sets the selection state of a specific application.
     *
     * This function is part of the [AppPickerState] interface.
     *
     * @param appInfo The [AppInfo] object representing the application whose state is to be set.
     * @param isSelected `true` to select the application, `false` to deselect it.
     */
    fun setState(appInfo: AppInfo, isSelected: Boolean)

    /**
     * Sets the selection state of all applications in the app picker.
     *
     * This function allows you to select or deselect all applications at once.
     * If an [OnStateChangeListener] is set, its `onStateAllChanged` method will be called
     * to notify about this change.
     *
     * @param isAllSelected `true` to select all applications, `false` to deselect all applications.
     */
    fun setStateAll(isAllSelected: Boolean)
}