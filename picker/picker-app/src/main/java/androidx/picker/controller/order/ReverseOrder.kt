package androidx.picker.controller.order


/**
 * A decorator class that reverses the order of a given [Order].
 *
 * This class takes an existing [Order] instance and provides a new [Order]
 * that sorts elements in the opposite direction.
 *
 * @param T The type of objects that this [Order] can compare.
 * @property base The underlying [Order] whose order will be reversed.
 */
class ReverseOrder<T>(private val base: Order<T>) : Order<T> {
    override fun compare(first: T, second: T): Int {
        return base.compare(second, first)
    }
}