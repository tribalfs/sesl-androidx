package androidx.picker.model


/**
 * Defines a type that can be grouped into an arbitrary [String] value.
 *
 * It is up to the implementor to define the value and semantics of [group]. A null or empty string
 * is treated as "no group".
 *
 * @property group The group identifier for this object.
 */
interface Groupable { var group: String }