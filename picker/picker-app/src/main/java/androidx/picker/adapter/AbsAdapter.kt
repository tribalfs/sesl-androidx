/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.picker.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.icu.text.AlphabeticIndex
import android.os.Build
import android.os.LocaleList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.SectionIndexer
import androidx.annotation.RestrictTo
import androidx.picker.R
import androidx.picker.adapter.viewholder.PickerViewHolder
import androidx.picker.common.log.LogTag
import androidx.picker.common.log.info
import androidx.picker.features.observable.UpdateMutableState
import androidx.picker.features.search.InitialSearchUtils.getMatchedStringOffset
import androidx.picker.features.search.InitialSearchUtils.getSearchResultFromSCS
import androidx.picker.loader.AppIconFlow
import androidx.picker.model.AppData
import androidx.picker.model.AppInfo
import androidx.picker.model.AppInfoData
import androidx.picker.model.AppInfoDataImpl
import androidx.picker.model.GroupTitleStyleData
import androidx.picker.model.Highlightable
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.AppSideViewData
import androidx.picker.model.viewdata.CategoryViewData
import androidx.picker.model.viewdata.GroupTitleViewData
import androidx.picker.model.viewdata.SearchableViewData
import androidx.picker.model.viewdata.ViewData
import androidx.picker.widget.SeslAppPickerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale
import java.util.StringTokenizer
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.set
import kotlin.reflect.KProperty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

/**
 * Abstract base class for RecyclerView adapters used in the [SeslAppPickerView].
 * This class provides common functionality for handling data, filtering, and section indexing.
 *
 * @param context The context used for accessing resources and other system services.
 * @param groupTitleStyleData The style data for group titles. Defaults to [GroupTitleStyleData.SOLID].
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
abstract class AbsAdapter @JvmOverloads constructor(
    protected val context: Context,
    protected val groupTitleStyleData: GroupTitleStyleData = GroupTitleStyleData.SOLID
) : RecyclerView.Adapter<PickerViewHolder>(), AppPickerAdapter, SectionIndexer, LogTag {

    private val dataListUnfiltered: MutableList<ViewData> = ArrayList()
    private val dataListFiltered: MutableList<ViewData> = ArrayList()
    private var _filter: Filter? = null
    private var searchText: String = ""

    private var onBindListener: AppPickerAdapter.OnBindListener? = null
    private var onSearchFilterListener: SeslAppPickerView.OnSearchFilterListener? = null
    private var positionToSectionIndex = intArrayOf()
    private val sectionMap = HashMap<String, Int>()
    private var _sections = arrayOf<Any>()

    private fun convertCategoryViewData2AppInfoViewData(categoryViewData: CategoryViewData): AppInfoViewData {
        val checkBoxAppInfoData = AppData.ListCheckBoxAppDataBuilder(categoryViewData.appData.appInfo)
            .setLabel(categoryViewData.title)
            .setIcon(categoryViewData.icon)
            .setSelected(categoryViewData.selectableItem.isSelected)
            .build() as AppInfoDataImpl

        return AppInfoViewData(
            appInfoData = checkBoxAppInfoData,
            iconFlow = AppIconFlow(
                base = object : UpdateMutableState<AppInfoData, Drawable>(checkBoxAppInfoData) {
                    override fun setValue(thisRef: Any?, prop: KProperty<*>, value: Drawable?) {
                        checkBoxAppInfoData.icon = value
                    }
                    override fun getValue(thisRef: Any?, prop: KProperty<*>): Drawable? =
                        checkBoxAppInfoData.icon
                },
                defaultIconFlow = object : Flow<Drawable?> {
                    override suspend fun collect(collector: FlowCollector<Drawable?>) {
                        collector.emit(categoryViewData.icon)
                    }
                }
            ),
            selectableItem = categoryViewData.selectableItem,
            spanCount = -1,
            onActionClick = null
        )
    }

    private fun isFilterMatch(searchText: String, searchable: String?): Boolean {
        if (searchable.isNullOrEmpty()) return false
        val tokens = StringTokenizer(searchText.lowercase())
        val searchableLower = searchable.lowercase().trim().replace(" ", "")
        val pattern = this@AbsAdapter.searchText.trim().replace(" ", "")
        while (tokens.hasMoreTokens()) {
            val token = tokens.nextToken()
            if (searchableLower.contains(token) || getMatchedStringOffset(searchableLower, pattern) > -1) return true
        }
        return false
    }

    private fun refreshSectionMap(list: List<ViewData>) {
        sectionMap.clear()
        positionToSectionIndex = IntArray(list.size)
        val sectionsLocal = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= 24) {
            var locales = context.resources.configuration.locales.takeIf { !it.isEmpty }
                ?: LocaleList(Locale.ENGLISH)
            val alphabeticIndex = AlphabeticIndex<Locale>(locales[0])

            for (i in 1 until locales.size()) {
                alphabeticIndex.addLabels(locales[i])
            }

            alphabeticIndex.addLabels(Locale.ENGLISH)
            val buildImmutableIndex = alphabeticIndex.buildImmutableIndex()

            for (i in list.indices) {
                val viewData = list[i]
                if (viewData is AppInfoViewData) {
                    var label = viewData.label ?: ""
                    val sectionLabel = buildImmutableIndex.getBucket(buildImmutableIndex.getBucketIndex(label)).label
                    if (!sectionMap.containsKey(sectionLabel)) {
                        sectionsLocal.add(sectionLabel)
                        sectionMap[sectionLabel] = i
                    }
                    positionToSectionIndex[i] = sectionsLocal.size - 1
                }
            }
        } else {
            for (i in list.indices) {
                val viewData = list[i]
                if (viewData is AppInfoViewData) {
                    var label = viewData.label ?: ""
                    val sectionLabel = label.first().toString()
                    if (!sectionMap.containsKey(label)) {
                        sectionsLocal.add(sectionLabel)
                        sectionMap[sectionLabel] = i
                    }
                    positionToSectionIndex[i] = sectionsLocal.size - 1
                }
            }
        }
        _sections = sectionsLocal.toTypedArray()
    }

    private fun replace(list: MutableList<ViewData>, old: ViewData, new: ViewData) {
        val index = list.indexOf(old)
        if (index == -1) return
        list.removeAt(index)
        list.add(index, new)
    }

    /**
     * Appends a list of [ViewData] to the current dataset and updates the adapter.
     *
     * @param list The list of [ViewData] to append.
     */
    fun appendItemList(list: List<ViewData>) {
        val arrayList = ArrayList(dataListUnfiltered)
        arrayList.addAll(list)
        submitList(arrayList)
    }

    /**
     * Processes a list of [ViewData] to handle invisible children of [CategoryViewData].
     *
     * This function iterates through the input `viewDataList`.
     * - If a [CategoryViewData] is encountered, its `invisibleChildren` are added to a temporary list.
     * - If an [AppInfoViewData] is encountered and it's present in the temporary list of
     *   invisible children, its `highlightText` is set to the current `searchText`.
     *
     * This is used to ensure that even if an app is an invisible child of a category (meaning it
     * might not be directly displayed under that category due to filtering or other logic),
     * it still gets highlighted if it matches the search query.
     *
     * @param viewDataList The list of [ViewData] to process.
     * @return A new list of [ViewData] with highlighting applied to relevant invisible children.
     */
    fun applyInvisibleChildrenOfCategoryAppData(viewDataList: List<ViewData>): List<ViewData> {
        val processedViewDataList = ArrayList<ViewData>()
        val invisibleChildrenFromCategories = ArrayList<ViewData>()
        for (viewData in viewDataList) {
            if (viewData is CategoryViewData) {
                invisibleChildrenFromCategories.addAll(viewData.invisibleChildren)
            } else if (viewData is AppInfoViewData && invisibleChildrenFromCategories.contains(
                    viewData
                )
            ) {
                viewData.getHighlightText().setValue(searchText)
            }
            processedViewDataList.add(viewData)
        }
        return processedViewDataList
    }

    /**
     * Converts a list of [CategoryViewData] objects into a list of [ViewData] objects,
     * specifically by transforming each [CategoryViewData] into an [AppInfoViewData].
     * This is typically used when displaying categories as app-like items in a filtered list.
     *
     * @param list The list of [CategoryViewData] to be converted.
     * @return A new list of [ViewData] containing the converted [AppInfoViewData] objects.
     */
    fun generateCategoryFilterResult(list: List<CategoryViewData>): List<ViewData> {
        val arrayList = ArrayList<ViewData>()
        for (cat in list) {
            arrayList.add(convertCategoryViewData2AppInfoViewData(cat))
        }
        return arrayList
    }

    /**
     * Generates a header for a filtered list of app side view data.
     *
     * @param str The title string for the header.
     * @param list The list of AppSideViewData items to be included under this header.
     * @return A ViewData object representing the group title header.
     */
    fun generateFilterHeader(str: String, list: List<AppSideViewData>): ViewData {
        val map = list.map { sideViewData -> sideViewData.appData }
        return GroupTitleViewData(
            AppData.GroupAppDataBuilder(str)
                .setLabel(str)
                .setSubLabel(map.size.toString())
                .setAppDatas(map)
                .build()
        )
    }

    override fun getAppInfo(position: Int): ViewData = dataListFiltered[position]

    /**
     * Filters a list of [SearchableViewData] based on the current [searchText] and a provided list of [AppInfo].
     *
     * This function iterates through each [SearchableViewData] in the [searchableViewDataList] list.
     * For each item, it checks if any of its `searchable` terms match the [searchText] using the [isFilterMatch] function.
     * If no direct match is found and the `key` of the [SearchableViewData] is an [AppInfo] instance,
     * it then checks if this [AppInfo] is present in the [appInfoList].
     * If either of these conditions is true, the [SearchableViewData] item is added to the result list.
     *
     * @param T The type of [SearchableViewData] being filtered.
     * @param searchableViewDataList The list of [SearchableViewData] items to filter.
     * @param appInfoList A list of [AppInfo] objects to check against if a direct search term match is not found.
     * @return A new list containing only the [SearchableViewData] items that matched the filter criteria.
     */
    fun <T : SearchableViewData> getAppInfoFilterResult(
        searchableViewDataList: List<T>,
        appInfoList: List<AppInfo>
    ): List<T> {
        val arrayList = ArrayList<T>()
        for (searchableData in searchableViewDataList) {
            var isMatch = false
            for (searchable in searchableData.searchable) {
                if (isFilterMatch(searchText, searchable)) {
                    isMatch = true
                    break
                }
            }
            if (!isMatch && searchableData.key is AppInfo) {
                isMatch = appInfoList.contains(searchableData.key as AppInfo)
            }
            if (isMatch) {
                arrayList.add(searchableData)
            }
        }
        return arrayList
    }

    override fun getDataSetFiltered(): MutableList<ViewData> = dataListFiltered

    override fun getFilter(): Filter {
        if (_filter != null) return _filter!!
        _filter = object : Filter() {
            override fun performFiltering(currentSearchText: CharSequence?): FilterResults {
                searchText = currentSearchText?.toString() ?: ""
                val results = FilterResults()

                val originalList = dataListUnfiltered
                val filteredList = mutableListOf<ViewData>()

                if (searchText.isEmpty()) {
                    filteredList.addAll(applyInvisibleChildrenOfCategoryAppData(originalList))
                } else {
                    val resultsFromSCS = getSearchResultFromSCS(context, searchText)
                    val catViewDataList = mutableListOf<CategoryViewData>()
                    val appInfoViewDataList = mutableListOf<AppInfoViewData>()

                    originalList.forEach {
                        if (it is CategoryViewData) {
                            catViewDataList.add(it)
                        } else if (it is AppInfoViewData) {
                            appInfoViewDataList.add(it)
                        }
                    }

                    val catViewDataListFiltered = getAppInfoFilterResult(catViewDataList, resultsFromSCS)
                    if (catViewDataListFiltered.isNotEmpty()) {
                        filteredList.add(generateFilterHeader(categoriesTitle, catViewDataListFiltered))
                        filteredList.addAll(generateCategoryFilterResult(catViewDataListFiltered))
                    }

                    val appInfoViewDataListFiltered = getAppInfoFilterResult(appInfoViewDataList, resultsFromSCS)
                    if (appInfoViewDataListFiltered.isNotEmpty()) {
                        if (catViewDataList.isNotEmpty()) {
                            filteredList.add(generateFilterHeader(appTitle, appInfoViewDataListFiltered))
                        }
                        filteredList.addAll(appInfoViewDataListFiltered)
                    }
                }
                results.values = filteredList
                return results
            }

            override fun publishResults(
                charSequence: CharSequence?,
                filterResults: FilterResults?
            ) {
                @Suppress("UNCHECKED_CAST")
                val results = filterResults?.values as? ArrayList<ViewData> ?: return
                for (viewData in results) {
                    if (viewData is Highlightable) {
                        viewData.getHighlightText().setValue(searchText)
                    }
                }
                onUpdateFilteredList(results)
                onSearchFilterListener?.onSearchFilterCompleted(itemCount)
            }
        }
        return _filter!!
    }

    override fun getItemCount(): Int = dataListFiltered.size

    override fun getItemId(i: Int): Long = dataListFiltered[i].key.hashCode().toLong()

    override fun getPositionForSection(i: Int): Int {
        val sections = _sections
        if (i < sections.size) {
            val position = sectionMap[sections[i]]
            if (position != null) return position
        }
        return 0
    }

    override fun getSectionForPosition(position: Int): Int {
        val positionToSectionIndex = positionToSectionIndex
        if (position >= positionToSectionIndex.size) return 0
        return positionToSectionIndex[position]
    }

    override fun getSections(): Array<Any> = _sections

    fun inflate(viewGroup: ViewGroup, layoutRes: Int): View {
        return LayoutInflater.from(viewGroup.context).inflate(layoutRes, viewGroup, false)
    }

    fun onUpdateFilteredList(list: List<ViewData>) {
        val calculateDiff = DiffUtil.calculateDiff(DiffUtilCallback(dataListFiltered, list))
        dataListFiltered.clear()
        dataListFiltered.addAll(list)
        refreshSectionMap(list)
        calculateDiff.dispatchUpdatesTo(NearbyListUpdateCallback(this))
    }

    override fun setOnBindListener(listener: AppPickerAdapter.OnBindListener) {
        onBindListener = listener
    }

    override fun setOnSearchFilterListener(listener: SeslAppPickerView.OnSearchFilterListener) {
        onSearchFilterListener = listener
    }

    override fun submitList(itemList: List<ViewData>) {
        info("submitList list=${itemList.size}")
        dataListUnfiltered.clear()
        dataListUnfiltered.addAll(itemList)
        getFilter().filter(searchText)
    }

    override fun updateItem(viewData: ViewData) {
        val arrayList = ArrayList(dataListUnfiltered)
        for (next in dataListUnfiltered) {
            if (next.key == viewData.key) {
                replace(arrayList, next, viewData)
                break
            }
        }
        submitList(arrayList)
    }

    override fun onBindViewHolder(holder: PickerViewHolder, position: Int) {
        val viewData = dataListFiltered[position]
        onBindListener?.onBindViewHolder(holder, viewData)
        holder.bindData(viewData)
        holder.bindAdapter(this)
    }

    override fun onBindViewHolder(holder: PickerViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            onBindViewHolder(holder, position)
        }
    }

    internal inline val categoriesTitle get() = context.resources.getString(R.string.title_categories)
    internal inline val appTitle get() = context.resources.getString(R.string.title_apps)

    override val logTag: String = "AppPickerViewAdapter"
}