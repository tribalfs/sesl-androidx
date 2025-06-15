package androidx.picker.common.log
import android.os.Build
import android.util.Log
import java.util.Locale

const val PREFIX: String = "SeslAppPicker"

/** Provides a log tag to use for logging messages. */
interface LogTag { val logTag: String }

val IS_DEBUG_DEVICE: Boolean by lazy {
    val type = Build.TYPE ?: ""
    val lowerType = type.lowercase(Locale.ROOT)
    lowerType.contains("debug") || lowerType == "eng"
}


fun LogTag.debug(msg: String) {
    if (IS_DEBUG_DEVICE) {
        Log.d("$PREFIX.${this.logTag}", msg)
    }
}

fun LogTag.debug(e: Throwable, enforcePrintStackTrace: Boolean = false) {
    val message = e.message ?: "Unknown error"
    debug(message)
    if (IS_DEBUG_DEVICE || enforcePrintStackTrace) {
        e.printStackTrace()
    }
}

fun LogTag.error(msg: String) {
    Log.e("$PREFIX.${this.logTag}", msg)
}

fun LogTag.info(msg: String) {
    Log.i("$PREFIX.${this.logTag}", msg)
}

fun LogTag.verbose(msg: String) {
    if (IS_DEBUG_DEVICE) {
        Log.v("$PREFIX.${this.logTag}", msg)
    }
}

fun LogTag.warn(msg: String) {
    Log.w("$PREFIX.${this.logTag}", msg)
}