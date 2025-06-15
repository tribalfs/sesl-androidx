package androidx.picker.helper

import android.graphics.drawable.Drawable

/**
 * Returns a new mutated copy of the drawable, or null if not possible.
 */
fun Drawable?.newMutateDrawable(): Drawable? {
    val constantState = this?.constantState ?: return null
    val newDrawable = constantState.newDrawable() ?: return null
    return newDrawable.mutate()
}