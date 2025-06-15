package androidx.picker.features.scs

import android.content.Context
import androidx.annotation.RequiresApi
import androidx.annotation.RestrictTo
import androidx.picker.features.search.InitialSearchUtils.AUTHORITY_SCS
import androidx.picker.features.search.InitialSearchUtils.AUTH_VERSION

/**
 * Factory for creating AppDataList instances using the SCS (Samsung Content Service) provider
 * using `com.samsung.android.scs.ai.search/v1` in querying the apps.
 *
 * This factory is specifically for devices running Android API level 30 and above.
 * It provides the authority string required to interact with the SCS content provider.
 *
 * @param context The application context.
 */
@RequiresApi(api = 30)
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class AppDataListSCSFactory(context: Context) : AppDataListBixbyFactory(context) {
    override fun getAuthority(): String =  "$AUTHORITY_SCS/$AUTH_VERSION"
    override val logTag: String = "AppDataListSCSFactory"
}