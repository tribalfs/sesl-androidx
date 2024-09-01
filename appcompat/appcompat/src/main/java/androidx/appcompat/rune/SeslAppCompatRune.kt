package androidx.appcompat.rune

import androidx.annotation.RestrictTo
import kotlin.annotation.AnnotationRetention
import kotlin.annotation.Retention

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS)
annotation class BooleanDef(val value: BooleanArray = [])

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object SeslAppCompatRune {
    const val WIDGET_BASIC_INTERACTION: Boolean = true

    @BooleanDef([true])
    @Retention(AnnotationRetention.RUNTIME)
    annotation class RuneAppCompatType
}

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.ANNOTATION_CLASS)
annotation class SeslAppCompatRuneSupport(
    @SeslAppCompatRune.RuneAppCompatType val value: Boolean = false
)