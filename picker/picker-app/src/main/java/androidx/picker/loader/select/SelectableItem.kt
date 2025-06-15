package androidx.picker.loader.select

import androidx.annotation.Keep
import androidx.picker.features.observable.MutableState
import androidx.picker.features.observable.ObservableProperty
import kotlin.Unit
import kotlinx.coroutines.DisposableHandle

/**
 * Represents an item that can be selected or deselected.
 * This class extends {@link ObservableProperty} to provide observable behavior
 * for its selection state.
 *
 * @param mutableState The underlying {@link MutableState} that holds the selection state.
 * @param onUpdated An optional callback that is invoked when the selection state is updated.
 */
@Keep
open class SelectableItem(
    mutableState: MutableState<Boolean>,
    onUpdated: ((Boolean) -> Unit)? = null
) : ObservableProperty<Boolean>(mutableState, onUpdated) {

    /**
     * `true` if this item is selected, `false` otherwise.
     * This property reflects the current state of the item's selection.
     */
    val isSelected: Boolean get() = getState()

    /**
     * Registers a listener to be notified after the selection state changes.
     *
     * @param onValueUpdateListener A callback that will be invoked with the new selection state
     *                              after the change has occurred.
     * @return A [DisposableHandle] that can be used to unregister the listener.
     */
    fun registerAfterChangeUpdateListener(
        onValueUpdateListener: (isSelected: Boolean) -> Unit
    ): DisposableHandle {
        return registerAfterChangeUpdateListener { _, newValue ->
            onValueUpdateListener(newValue)
        }
    }

    /**
     * Registers a listener to be notified before the selection state changes.
     * This listener can prevent the change from occurring by returning `false`.
     *
     * @param onValueUpdateListener A callback that will be invoked with the proposed new selection
     *                              state. The callback should return `true` to allow the change,
     *                              or `false` to prevent it.
     * @return A [DisposableHandle] that can be used to unregister the listener.
     */
    fun registerBeforeChangeUpdateListener(
        onValueUpdateListener: (isSelected: Boolean) -> Boolean
    ): DisposableHandle {
        return registerBeforeChangeUpdateListener { _, newValue ->
            onValueUpdateListener(newValue)
        }
    }
}