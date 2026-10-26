package androidx.viewpager2.rune

import kotlin.annotation.AnnotationRetention.RUNTIME

@Retention(RUNTIME)
annotation class BooleanDef(vararg val value: Boolean = [])
