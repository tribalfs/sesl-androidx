package androidx.picker.features.composable.custom

import androidx.picker.features.composable.ComposableFrame
import androidx.picker.model.AppData

/**
 * A custom frame that can be used to display custom content in the picker.
 *
 * This interface extends [ComposableFrame] and adds a predicate function that can be used to
 * determine whether the frame should be displayed for a given [AppData].
 * @see CustomStrategy
 * @see CustomViewHolder
 */
interface CustomFrame : ComposableFrame {
    /**
     * The class of the view holder that will be used to display the frame.
     *
     * This class must be a subclass of [CustomViewHolder].
     */
    override val viewHolderClass: Class<out CustomViewHolder>
    /**
     * Determines whether the frame should be displayed for the given [AppData].
     *
     * @param appData The [AppData] to check.
     * @return `true` if the frame should be displayed, `false` otherwise.
     */
    fun predicate(appData: AppData): Boolean
}