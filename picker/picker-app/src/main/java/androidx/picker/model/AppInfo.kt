package androidx.picker.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents information about an application, including its package name, activity name, and user ID.
 * This class is Parcelable, allowing it to be passed between components.
 *
 * @property packageName The package name of the application.
 * @property activityName The name of the activity within the application. Defaults to an empty string.
 * @property user The user ID associated with the application. Defaults to 0.
 */
@Parcelize
data class AppInfo @JvmOverloads constructor(
    val packageName: String,
    val activityName: String,
    val user: Int = 0
) : Parcelable