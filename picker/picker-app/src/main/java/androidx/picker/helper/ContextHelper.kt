package androidx.picker.helper

import android.content.Context
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.appcompat.R
import androidx.core.content.ContextCompat

@ColorInt
fun Context.getPrimaryColor(): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
    return if (typedValue.resourceId != 0) {
        ContextCompat.getColor(this, typedValue.resourceId)
    } else {
        typedValue.data
    }
}

@ColorInt
fun Context.getPrimaryDarkColor(): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true)
    return if (typedValue.resourceId != 0) {
        ContextCompat.getColor(this, typedValue.resourceId)
    } else {
        typedValue.data
    }
}

@ColorInt
fun Context.getTextSecondaryColor(): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(android.R.attr.textColorSecondary, typedValue, true)
    return if (typedValue.resourceId != 0) {
        ContextCompat.getColor(this, typedValue.resourceId)
    } else {
        typedValue.data
    }
}

fun Context.isRTL(): Boolean {
    return resources.configuration.layoutDirection == android.util.LayoutDirection.RTL
}