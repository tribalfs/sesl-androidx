package androidx.picker.controller.strategy.task


/**
 * An interface for converting a type to another type.
 *
 * @param T The type of the input to the task.
 * @param U The type of the output from the task.
 */
interface Task<T, U> {
    operator fun invoke(input: T): U
}