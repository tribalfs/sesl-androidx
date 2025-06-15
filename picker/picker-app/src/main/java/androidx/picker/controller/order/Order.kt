package androidx.picker.controller.order


/**
 * Interface representing an order of elements.
 *
 * It is a [Comparator] which compares two elements and returns an integer value based on their
 * order.
 *
 * @param T The type of elements being compared.
 */
interface Order<T> : Comparator<T> {
    override fun compare(first: T, second: T): Int
}