package androidx.viewpager2.rune

import kotlin.annotation.AnnotationRetention.SOURCE

@Retention(SOURCE)
annotation class SeslViewPager2RuneSupport(
    @SeslViewPager2Rune.RuneViewPager2Type val value: Boolean = false,
)
