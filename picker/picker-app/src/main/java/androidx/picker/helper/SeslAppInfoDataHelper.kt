package androidx.picker.helper

import android.content.ComponentName
import android.content.Context
import androidx.picker.common.log.LogTag
import androidx.picker.common.log.error
import androidx.picker.common.log.info
import androidx.picker.features.scs.AbstractAppDataListFactory
import androidx.picker.model.AppData
import androidx.picker.model.AppData.AppDataBuilder
import androidx.picker.model.AppData.AppDataBuilderInfo
import androidx.picker.model.AppData.ItemType
import androidx.picker.model.AppInfo
import androidx.picker.model.AppInfoData
import androidx.picker.model.AppInfoDataImpl
import androidx.reflect.os.SeslUserHandleReflector
import kotlin.jvm.java

/**
 * Helper class for creating and managing AppInfoData objects.
 *
 * This class provides methods to create lists of [AppInfoData] from component names or package names.
 * It uses an [AbstractAppDataListFactory] to retrieve data based on the item type.
 *
 * @constructor Creates an instance of SeslAppInfoDataHelper.
 * @param context The application context.
 * @param builderClass The class of the [AppData.AppDataBuilder] to be used.
 * Defaults to [AppData.ListAppDataBuilder] when not provided.
 *
 */
class SeslAppInfoDataHelper @JvmOverloads constructor(
    context: Context,
    builderClass: Class<out AppDataBuilder<*>> = AppData.ListAppDataBuilder::class.java
) : LogTag {
    /** The factory used to create lists of app data.*/
    private val appDataListFactory: AbstractAppDataListFactory = AbstractAppDataListFactory.getFactory(context)

    /** The type of items to be retrieved, determined by the [AppData.AppDataBuilderInfo] */
    @ItemType
    private val itemType: Int

    /** The ID of the current user. */
    private val userId: Int = SeslUserHandleReflector.myUserId()

    init {
        val appDataBuilderInfo = builderClass.getAnnotation(AppDataBuilderInfo::class.java)
        if (appDataBuilderInfo != null) {
            this.itemType = appDataBuilderInfo.itemType
        } else {
            error("get wrong itemType using Builder class")
            this.itemType = AppData.TYPE_ITEM_TEXT
        }
    }

    /**
     * Converts a list of [ComponentName] objects to a list of [AppInfoData] objects.
     *
     * Each [ComponentName] is used to create an [AppInfo] object, which is then wrapped in an
     * [AppInfoDataImpl] object along with the current `itemType`.
     *
     * @param list A list of [ComponentName] objects.
     * @return A list of [AppInfoData] objects corresponding to the provided component names.
     */
    fun getComponents(list: List<ComponentName>): List<AppInfoData> {
        val arrayList = ArrayList<AppInfoData>()
        for (componentName in list) {
            arrayList.add(
                AppInfoDataImpl(
                    AppInfo(componentName.packageName, componentName.className, this.userId),
                    this.itemType
                )
            )
        }
        info("getComponents(${list.size})=${arrayList.size}")
        return arrayList
    }

    override val logTag: String = "SeslAppInfoDataHelper"

    /**
     * Retrieves a list of [AppInfoData] for all packages.
     *
     * This method uses the [appDataListFactory] to get the data list based on the `mItemType`.
     *
     * @return A list of [AppInfoData] objects.
     */
    fun getPackages(): List<AppInfoData> {
        val dataList = this.appDataListFactory.getDataList(this.itemType)
        info("getPackages=${dataList.size}")
        return dataList
    }

    /**
     * Retrieves a list of [AppInfoData] objects for the specified package names.
     *
     * @param list A list of package names.
     * @return A list of [AppInfoData] objects corresponding to the provided package names.
     * Each [AppInfoData] is created using the current `userId` and `mItemType`.
     */
    fun getPackages(list: List<String>): List<AppInfoData> {
        val arrayList = ArrayList<AppInfoData>()
        for (pkg in list) {
            arrayList.add(
                AppInfoDataImpl(
                    AppInfo(pkg, "", this.userId),
                    this.itemType
                )
            )
        }
        info("getPackages(${list.size})=${arrayList.size}")
        return arrayList
    }
}