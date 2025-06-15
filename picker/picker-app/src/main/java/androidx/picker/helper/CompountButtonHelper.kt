package androidx.picker.helper

import android.content.Context.ACCESSIBILITY_SERVICE
import android.view.accessibility.AccessibilityManager
import android.widget.CompoundButton

fun CompoundButton.setAccessibilityFocusable(focusable: Boolean) {
    val systemService = context.getSystemService(ACCESSIBILITY_SERVICE)
    val accessibilityManager = systemService as? AccessibilityManager
    if (accessibilityManager == null || !accessibilityManager.isEnabled) {
        return
    }
    if (focusable) {
        setFocusable(true)
        isClickable = true
    } else {
        setFocusable(false)
        isClickable = false
    }
}
