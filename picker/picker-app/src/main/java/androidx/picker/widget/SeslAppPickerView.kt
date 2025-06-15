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

package androidx.picker.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.annotation.IntDef
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.appcompat.util.SeslRoundedCorner.ROUNDED_CORNER_ALL
import androidx.appcompat.util.SeslRoundedCorner.ROUNDED_CORNER_NONE
import androidx.core.content.withStyledAttributes
import androidx.core.util.Supplier
import androidx.picker.R
import androidx.picker.adapter.AbsAdapter
import androidx.picker.adapter.AppPickerAdapter
import androidx.picker.adapter.HeaderFooterAdapter
import androidx.picker.adapter.viewholder.AppListItemViewHolder
import androidx.picker.adapter.viewholder.Inducible
import androidx.picker.adapter.viewholder.PickerViewHolder
import androidx.picker.common.log.LogTag
import androidx.picker.common.log.debug
import androidx.picker.common.log.info
import androidx.picker.controller.ViewDataController
import androidx.picker.controller.order.ReverseOrder
import androidx.picker.controller.order.StrengthOrder
import androidx.picker.controller.strategy.Strategy
import androidx.picker.decorator.RecyclerViewCornerDecoration
import androidx.picker.loader.select.SelectStateLoader
import androidx.picker.loader.select.SelectableItem
import androidx.picker.model.AppData
import androidx.picker.model.AppInfo
import androidx.picker.model.GroupTitleStyleData
import androidx.picker.model.Selectable
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.ViewData
import androidx.picker.repository.AppDataRepository
import androidx.picker.widget.SeslAppPickerView.Companion.ORDER_ASCENDING
import androidx.picker.widget.SeslAppPickerView.Companion.ORDER_ASCENDING_IGNORE_CASE
import androidx.picker.widget.SeslAppPickerView.Companion.ORDER_DESCENDING
import androidx.picker.widget.SeslAppPickerView.Companion.ORDER_DESCENDING_IGNORE_CASE
import androidx.picker.widget.SeslAppPickerView.Companion.ORDER_NONE
import androidx.picker.widget.SeslAppPickerView.Companion.TYPE_GRID
import androidx.picker.widget.SeslAppPickerView.Companion.TYPE_LIST
import androidx.recyclerview.widget.RecyclerView
import java.text.Collator
import kotlinx.coroutines.DisposableHandle

/**
 * An abstract base class for views that display a list or grid of applications,
 * allowing users to pick one or more.
 *
 * This class extends [RecyclerView] and implements several interfaces to manage
 * application data, selection state, and user interactions.
 *
 * Key features:
 * - Supports both list and grid layouts.
 * - Handles different ordering options for the application list.
 * - Allows customization of subheader appearance (solid or transparent).
 * - Provides methods for adding, removing, and updating application items.
 * - Manages item selection state.
 * - Integrates with [AppDataRepository] for data fetching and [SelectStateLoader]
 *   for managing selection.
 * - Uses strategy pattern for customizing data handling and presentation logic.
 *
 * Subclasses must implement [getAppPickerAdapter] and [getLayoutManager] to provide
 * the specific adapter and layout manager for the chosen view type (list or grid).
 *
 * XML attributes:
 * - `strategy`: (Optional) The fully qualified class name of the [Strategy] implementation
 *   to use. Defaults to `androidx.picker.controller.strategy.AppItemStrategy`.
 * - `appPickerContextClass`: (Optional) The fully qualified class name of the
 *   `androidx.picker.di.AppPickerContext` implementation to use.
 *   Defaults to `androidx.picker.di.AppPickerContext`.
 * - `seslRoundedCorner`: (Optional) Integer value to set the rounded corner radius for items.
 *   Defaults to 15.
 * - `pickerApp_subHeaderType`: (Optional) Defines the subheader style. Can be
 *   `SUBHEADER_TYPE_SOLID` (0) or `SUBHEADER_TYPE_TRANSPARENT` (1). Defaults to `SUBHEADER_TYPE_SOLID`.
 */
abstract class SeslAppPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr), RecyclerView.RecyclerListener,
    AppPickerAdapter.OnBindListener, AppPickerEvent, AppPickerState, LogTag {

    companion object {
        const val ORDER_NONE = 0
        const val ORDER_ASCENDING = 1
        const val ORDER_ASCENDING_IGNORE_CASE = 2
        const val ORDER_DESCENDING = 3
        const val ORDER_DESCENDING_IGNORE_CASE = 4

        const val SUBHEADER_TYPE_SOLID = 0
        const val SUBHEADER_TYPE_TRANSPARENT = 1

        const val TYPE_LIST = 0
        const val TYPE_GRID = 1
    }

    @IntDef(
        ORDER_NONE,
        ORDER_ASCENDING,
        ORDER_ASCENDING_IGNORE_CASE,
        ORDER_DESCENDING,
        ORDER_DESCENDING_IGNORE_CASE
    )
    @Retention(AnnotationRetention.SOURCE)
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    annotation class AppPickerOrder

    @IntDef(TYPE_LIST, TYPE_GRID)
    @Retention(AnnotationRetention.SOURCE)
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    annotation class AppPickerType

    /**
     * Returns an [AbsAdapter] instance to be used for the given [viewType][AppPickerType] type.
     *
     * This method is responsible for providing the appropriate adapter (e.g., for list or grid view)
     * based on the [viewType] parameter. Subclasses must implement this method to return a
     * concrete adapter that can display the application items in the desired layout.
     *
     * The returned adapter will be wrapped by a [HeaderFooterAdapter] to support headers and footers.
     *
     * @param viewType An integer representing the type of view for which the adapter is required.
     *                 This will typically be one of [TYPE_LIST] or [TYPE_GRID].
     * @return The [AbsAdapter] to use for the specified view type.
     * @see AppPickerType
     * @see HeaderFooterAdapter
     * @see AppPickerAdapter
     */
    abstract fun getAppPickerAdapter(@AppPickerType viewType: Int): AbsAdapter

    override val logTag: String = this::class.java.simpleName

    /**
     * Interface definition for a callback to be invoked when a search filter operation is completed.
     */
    fun interface OnSearchFilterListener {
        /**
         * Part of the [OnSearchFilterListener] interface called by the adapter
         * when a search filter operation is completed.
         *
         * This method is invoked after the adapter has finished filtering its items
         * based on a search query. It provides the count of items that matched
         * the filter criteria.
         *
         * @param count The number of items that matched the search filter. If the
         *              filter was cleared (e.g., by providing a null or empty search string),
         *              this will be the total number of items in the adapter.
         */
        fun onSearchFilterCompleted(count: Int)
    }

    private val clearKeyboardListener: OnScrollListener
    private val scrollListener: OnScrollListener
    private var onClickEventListener: AppPickerEvent.OnItemClickEventListener? = null
    private var onActionClickEventListener: AppPickerEvent.OnItemClickEventListener? = null
    private var onStateChangeListener: AppPickerState.OnStateChangeListener? = null
    private var disposable: DisposableHandle? = null
    private var triggerAnimation: Supplier<DisposableHandle?>? = null
    private var selectStateLoader: SelectStateLoader

    @VisibleForTesting
    private var seslRoundedCorner: Int = ROUNDED_CORNER_ALL
    private val strategy: Strategy
    internal var viewDataController: ViewDataController
        private set

    /**
     * The repository responsible for providing application data.
     *
     * This repository is used to fetch the list of applications to be displayed
     * in the picker. It typically handles loading application information from
     * the system or other data sources.
     *
     * @see [androidx.picker.di.AppPickerContext.appDataRepository]
     */
    val appDataRepository: AppDataRepository

    /**
     * Data object holding the style information for group titles.
     *
     * This determines the appearance of the subheaders or group titles within
     * the application list, such as whether they have a solid background or
     * a transparent one.
     *
     * @see GroupTitleStyleData.SOLID
     * @see GroupTitleStyleData.TRANSPARENT
     */
    internal val groupTitleStyleData: GroupTitleStyleData

    /**
     * The adapter responsible for managing the header and footer views in the list.
     *
     * This adapter wraps the main content adapter (provided by [getAppPickerAdapter])
     * and allows for the addition of custom header and footer views.
     *
     * It is initialized during the [initialize] method.
     *
     * @see HeaderFooterAdapter
     * @see initialize
     * @see addHeader
     * @see addFooter
     */
    lateinit var headerFooterAdapter: HeaderFooterAdapter
        private set


    /**
     * The order in which the application list is sorted.
     *
     * This property controls how the applications are arranged in the list.
     * Setting this property will trigger a re-sorting of the list.
     *
     * Possible values are:
     * - [ORDER_NONE]: No specific order.
     * - [ORDER_ASCENDING]: Sort by application label in ascending order (case-sensitive).
     * - [ORDER_ASCENDING_IGNORE_CASE]: Sort by application label in ascending order (case-insensitive).
     * - [ORDER_DESCENDING]: Sort by application label in descending order (case-sensitive).
     * - [ORDER_DESCENDING_IGNORE_CASE]: Sort by application label in descending order (case-insensitive).
     *
     * Defaults to [ORDER_NONE].
     *
     * @see AppPickerOrder
     * @see ViewDataController.order
     * @see getAppLabelComparator
     */
    @AppPickerOrder
    var appListOrder: Int = ORDER_NONE
        set(value) {
            if (field == value) return
            field = value
            viewDataController.order = getAppLabelComparator(value)
        }


    /**
     * The current view type of the app picker.
     *
     * This property determines whether the applications are displayed in a list or a grid.
     * It can be set to either [TYPE_LIST] or [TYPE_GRID].
     *
     * Changing this value will typically involve re-initializing the layout manager and adapter
     * to reflect the new view type.
     *
     * Defaults to [TYPE_LIST].
     *
     * @see AppPickerType
     * @see getLayoutManager
     * @see getAppPickerAdapter
     */
    @AppPickerType
    var viewType: Int = TYPE_LIST
        internal set

    init {
        var strategyClassName: String? = null
        var appPickerContextClassName: String? = null
        var subHeaderType = 0

        context.withStyledAttributes(attrs, R.styleable.SeslAppPickerView, defStyleAttr, 0) {
            strategyClassName = getString(R.styleable.SeslAppPickerView_strategy)
            appPickerContextClassName =
                getString(R.styleable.SeslAppPickerView_appPickerContextClass)
            seslRoundedCorner =
                getInt(R.styleable.SeslAppPickerView_seslRoundedCorner, ROUNDED_CORNER_ALL)
            subHeaderType =
                getInt(R.styleable.SeslAppPickerView_pickerApp_subHeaderType, SUBHEADER_TYPE_SOLID)
        }

        debug("init strategy=$strategyClassName, roundedCorner=$seslRoundedCorner")

        groupTitleStyleData = if (subHeaderType == SUBHEADER_TYPE_TRANSPARENT) {
            GroupTitleStyleData.TRANSPARENT
        } else {
            GroupTitleStyleData.SOLID
        }

        val appPickerContextClass = try {
            if (appPickerContextClassName.isNullOrEmpty()) {
                Class.forName("androidx.picker.di.AppPickerContext")
            } else {
                Class.forName(appPickerContextClassName)
            }
        } catch (e: Exception) {
            androidx.picker.di.AppPickerContext::class.java
        }

        val appPickerContext = try {
            val ctor = appPickerContextClass.getConstructor(Context::class.java)
            ctor.newInstance(context) as androidx.picker.di.AppPickerContext
        } catch (_: Exception) {
            androidx.picker.di.AppPickerContext(context)
        }

        debug("used appPickerContext: $appPickerContext")

        @Suppress("DEPRECATION")
        setRecyclerListener(this)
        appDataRepository = appPickerContext.appDataRepository
        selectStateLoader = appPickerContext.selectStateLoader

        strategy = if (strategyClassName != null) {
            try {
                val strategyClass = Class.forName(strategyClassName)
                val ctor = strategyClass.getConstructor(appPickerContext::class.java)
                ctor.newInstance(appPickerContext) as Strategy
            } catch (e: Exception) {
                androidx.picker.controller.strategy.AppItemStrategy(appPickerContext)
            }
        } else {
            androidx.picker.controller.strategy.AppItemStrategy(appPickerContext)
        }

        viewDataController = ViewDataController(strategy)

        clearKeyboardListener = object : OnScrollListener() {
            private val inputManager
                get() =
                    context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_DRAGGING) {
                    inputManager.hideSoftInputFromWindow(windowToken, 0)
                }
            }
        }

        scrollListener = object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    SCROLL_STATE_IDLE -> {
                        if (triggerAnimation == null) return
                        if (disposable != null) {
                            disposable?.dispose()
                        } else {
                            disposable = triggerAnimation?.get()
                        }
                    }

                    SCROLL_STATE_DRAGGING -> {
                        disposable?.let {
                            it.dispose()
                            triggerAnimation = null
                        }
                    }
                }
            }
        }

        selectStateLoader.setOnSelectListener(
            object : SelectStateLoader.OnSelectListener {
                override fun onAllAppsSelected(isAllSelected: Boolean) {
                    onStateChangeListener?.onStateAllChanged(isAllSelected)
                }

                override fun onItemSelected(appInfo: AppInfo, isSelected: Boolean) {
                    onStateChangeListener?.onStateChanged(appInfo, isSelected)
                }
            })

        viewDataController.addOnDataEventListener { list ->
            headerFooterAdapter.submitList(list)
        }
    }

    private fun generateTextViewHolder(titleText: String): View {
        return inflate(context, R.layout.picker_app_text, null).apply {
            findViewById<TextView>(R.id.title).text = titleText
        }
    }

    private fun getAppLabelComparator(@AppPickerOrder order: Int): Comparator<ViewData>? {
        return when (order) {
            ORDER_ASCENDING -> StrengthOrder(Collator.TERTIARY)
            ORDER_ASCENDING_IGNORE_CASE -> StrengthOrder(Collator.PRIMARY)
            ORDER_DESCENDING -> ReverseOrder(StrengthOrder(Collator.TERTIARY))
            ORDER_DESCENDING_IGNORE_CASE -> ReverseOrder(StrengthOrder(Collator.PRIMARY))
            else -> null
        }
    }

    /**
     * Adds a footer view to the list with the given string as its text content.
     *
     * This method inflates a default text view layout (`R.layout.picker_app_text`),
     * sets its text to the provided [title], and then adds it as a footer.
     *
     * @param title The string to be displayed in the footer view.
     *
     * @see addFooter
     * @see generateTextViewHolder
     */
    fun addFooter(title: String) {
        addFooter(generateTextViewHolder(title))
    }

    /**
     * Adds a header view to the list with the given string as its text content.
     *
     * This method inflates a default text view layout (`R.layout.picker_app_text`),
     * sets its text to the provided [title], and then adds it as a header.
     *
     * @param title The string to display in the header view.
     * @see addHeader
     * @see generateTextViewHolder
     */
    fun addHeader(title: String) = addHeader(generateTextViewHolder(title))

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    internal fun addItem(appData: AppData) = addItems(listOf(appData))

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    internal fun addItems(list: List<AppData>) = viewDataController.addAppDataList(list)


    fun clearItemDecoration() {
        while (itemDecorationCount > 0) {
            removeItemDecorationAt(0)
        }
    }

    /** Convenience method for [ViewDataController.getAppData] */
    fun getAppData(appInfo: AppInfo): AppData? = viewDataController.getAppData(appInfo)

    /** Convenience property for [ViewDataController.appDataList] */
    val appDataList: List<AppData> get() = viewDataController.appDataList

    override fun getState(appInfo: AppInfo): Boolean {
        val viewData = viewDataController.getViewData(appInfo)
        return if (viewData is Selectable) {
            viewData.selectableItem?.isSelected ?: false
        } else {
            false
        }
    }

    /**
     * Returns a [LayoutManager] instance to be used for the given view type.
     *
     * @param viewType The integer representing the type of view for which the layout manager is required.
     * @return The [LayoutManager] to use for the specified view type, or null if not applicable.
     */
    abstract fun getLayoutManager(viewType: Int): LayoutManager?

    /**
     * Initializes the view and sets up any required resources or listeners.
     * This is be called inside init of the concrete subclasses after setting the [viewType].
     *
     * This method performs the following actions:
     * - Adds the appropriate layout manager based on the [viewType].
     * - Adds the [HeaderFooterAdapter] with the specific [AppPickerAdapter] for the [viewType].
     * - Sets the item decoration for the RecyclerView.
     * - Enables the "Go to top" button functionality.
     * - Enables the fast scroller.
     * - Enables the fill bottom feature.
     */
    open fun initialize() {
        layoutManager = getLayoutManager(viewType)
        adapter = HeaderFooterAdapter(getAppPickerAdapter(viewType)).also {
            headerFooterAdapter = it
            setItemDecoration(viewType, it)
            it.setOnBindListener(this)
        }
        seslSetGoToTopEnabled(true)
        seslSetFastScrollerEnabled(true)
        seslSetFillBottomEnabled(true)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        addOnScrollListener(scrollListener)
        addOnScrollListener(clearKeyboardListener)
    }

    override fun onDetachedFromWindow() {
        //adapter = null
        super.onDetachedFromWindow()
        removeOnScrollListener(scrollListener)
        removeOnScrollListener(clearKeyboardListener)
    }

    /**
     * Called to set and update the click listener for viewHolder's item view.
     *
     * If the [onClickEventListener] is set and the [viewData] is an [AppInfoViewData],
     * this method sets an [View.OnClickListener] on the [viewHolder] root view.
     * When clicked:
     * 1. It retrieves the [AppData] corresponding to the [viewData].
     * 2. If [AppData] is found:
     *    a. It invokes [AppPickerEvent.OnItemClickEventListener.onClick] with the view
     *       and the [AppInfo].
     *    b. If the listener handles the click (returns `true`), no further action is taken.
     *    c. Otherwise, if the [viewHolder] is an [AppListItemViewHolder] and its `doAction()`
     *       method returns `true`, or if there's no [onClickEventListener], the click
     *       listener on the view is removed.
     *
     * @param viewHolder The [PickerViewHolder] which should be updated to represent the
     *                   contents of the item at the given position in the data set.
     * @param viewData   The [ViewData] object containing the data for the item at the
     *                   specified position.
     * @see AppPickerAdapter.OnBindListener.onBindViewHolder
     * @see AppPickerEvent.OnItemClickEventListener
     * @see AppListItemViewHolder.doAction
     */
    override fun onBindViewHolder(viewHolder: PickerViewHolder, viewData: ViewData) {
        if ((onClickEventListener == null && onActionClickEventListener == null) || viewData !is AppInfoViewData) return

        if (onActionClickEventListener != null) {
            viewData.onActionClick =  { onActionClickEventListener?.onClick(null, it.appInfo) }
        } else {
            viewData.onActionClick = null
        }

        viewHolder.itemView.apply {
            setOnClickListener { view ->
                val appData = getAppData(viewData.appInfo) ?: return@setOnClickListener
                val handledByListener = onClickEventListener?.onClick(view, appData.appInfo) == true
                if (handledByListener) return@setOnClickListener
                // If not handled, pass-down the click event down to the member `ActionableComposableViewHolder`s
                // who has `doAction` set. If still unhandled and onClickEventListener is null,
                // clear the itemView's click listener.
                if ((viewHolder as? AppListItemViewHolder)?.doAction() != true && onClickEventListener == null) {
                    setOnClickListener(null)
                }
            }
        }
    }

    override fun onViewRecycled(viewHolder: ViewHolder) = (viewHolder as PickerViewHolder).onViewRecycled()

    /**
     * Removes a specific application item from the list.
     *
     * This method removes the provided [AppData] object from the underlying data
     * managed by the [ViewDataController]. The list will be updated to reflect
     * this change.
     *
     * @param appData The [AppData] object representing the application item to be removed.
     * @return `true` if the item was found and removed, `false` otherwise.
     * @see ViewDataController.removeAppData
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    fun removeItem(appData: AppData) = viewDataController.removeAppData(appData)

    /**
     * Removes a list of [AppData] items from the view.
     *
     * This method delegates to the [ViewDataController] to remove the specified
     * application data items from the underlying data set. The view will then
     * be updated to reflect these changes.
     *
     * @param list The list of [AppData] objects to remove.
     * @see ViewDataController.removeAppDataList
     * @see addItem
     * @see addItems
     * @see removeItem
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    fun removeItems(list: List<AppData>) = viewDataController.removeAppDataList(list)

    @CallSuper
    open fun setItemDecoration(i: Int, headerFooterAdapter: HeaderFooterAdapter) {
        clearItemDecoration()
        addItemDecoration(RecyclerViewCornerDecoration(context, seslRoundedCorner))
    }


    override fun setOnItemClickEventListener(listener: AppPickerEvent.OnItemClickEventListener?) {
        val rebind = onClickEventListener == null && listener != null
        onClickEventListener = listener
        if (rebind) {
            post { @SuppressLint("NotifyDataSetChanged") headerFooterAdapter.notifyDataSetChanged() }
        }
    }


    override fun setOnItemActionClickEventListener(listener: AppPickerEvent.OnItemClickEventListener?) {
        val rebind = onActionClickEventListener == null && listener != null
        onActionClickEventListener = listener
        if (rebind) {
            post { @SuppressLint("NotifyDataSetChanged") headerFooterAdapter.notifyDataSetChanged() }
        }
    }


    /**
     * Sets a listener to be notified of changes in the selection state of items.
     *
     * This listener will be invoked when an individual item's selection state changes
     * (via [AppPickerState.OnStateChangeListener.onStateChanged]) or when the selection
     * state of all items changes (via [AppPickerState.OnStateChangeListener.onStateAllChanged]).
     *
     * @param onStateChangeListener The [AppPickerState.OnStateChangeListener] to set,
     *                              or `null` to remove the existing listener.
     * @see AppPickerState.OnStateChangeListener
     * @see setState
     * @see setStateAll
     */
    override fun setOnStateChangeListener(listener: AppPickerState.OnStateChangeListener?) {
        this@SeslAppPickerView.onStateChangeListener = listener
    }

    /**
     * Updates the selection state of the item associated with the given [appInfo].
     *
     * This will trigger any registered [AppPickerState.OnStateChangeListener]
     * if the selection state actually changes.
     *
     * @param appInfo The [AppInfo] of the item whose selection state needs to be set.
     * @param isSelected `true` to select the item, `false` to deselect it.
     * @see AppPickerState.OnStateChangeListener
     * @see SelectableItem.setValue
     */
    override fun setState(appInfo: AppInfo, isSelected: Boolean) {
        val viewData = viewDataController.getViewData(appInfo)
        if (viewData is Selectable) {
            viewData.selectableItem?.setValue(isSelected)
        }
    }

    /**
     * Updates the selection state of all applications currently displayed
     * in the list.
     *
     * @param isAllSelected `true` to select all items, `false` to deselect all items.
     * @see SelectStateLoader.setStateAll
     * @see AppPickerState.setStateAll
     */
    override fun setStateAll(isAllSelected: Boolean) =
        selectStateLoader.setStateAll(viewDataController.currentList, isAllSelected)

    /**
     * Submits a new list of [AppData] to be displayed.
     *
     * If the provided [list] is `null` or not provided, the default list from [appDataRepository]
     * will be used.
     *
     * @param list The new list of [AppData] to display, or `null` to use the default list.
     */
    @JvmOverloads
    fun submitList(list: List<AppData>? = null) {
        info("submitList=${list?.size}")
        viewDataController.setAppDataList(
            (list ?: appDataRepository.getDefaultList()).toMutableList()
        )
    }

    /**
     * Updates an existing application item in the list with new data.
     *
     * If an item with the same [AppInfo] as the provided [appData] exists in the list,
     * it will be replaced with the new [appData]. Otherwise, the list remains unchanged.
     *
     * @param appData The [AppData] object containing the updated information for the item.
     */
    fun updateItem(appData: AppData) {
        val updateList = viewDataController.appDataList.map {
            if (it.appInfo == appData.appInfo) appData else it
        }
        viewDataController.setAppDataList(updateList.toMutableList())
    }

    /**
     * Adds a footer view to the list.
     *
     * This method allows you to append a custom view to the bottom of the application list.
     * The footer view will be displayed below all the application items.
     *
     * @param view The [View] to add as a footer.
     * @param roundedCorner An integer value representing the rounded corner style for the footer.
     *                      Defaults to [ROUNDED_CORNER_ALL], which applies rounding to all corners.
     *                      You can use other constants from `SeslRoundedCorner` like
     *                      [ROUNDED_CORNER_NONE] for no rounding, or specific corner constants.
     *                      This parameter controls the visual appearance of the footer's corners.
     *
     * @see HeaderFooterAdapter.addFooter
     * @see androidx.appcompat.util.SeslRoundedCorner
     */
    @JvmOverloads
    fun addFooter(view: View, roundedCorner: Int = ROUNDED_CORNER_ALL) =
        headerFooterAdapter.addFooter(view, roundedCorner)

    /**
     * Adds a header view to the top of the list.
     *
     * @param view The [View] to add as a header.
     * @param roundedCorner An integer constant defining the rounded corner style for the header.
     * Defaults to [ROUNDED_CORNER_NONE].
     */
    @JvmOverloads
    fun addHeader(view: View, roundedCorner: Int = ROUNDED_CORNER_NONE) =
        headerFooterAdapter.addHeader(view, roundedCorner)

    /**
     * Sets a search filter on the adapter.
     *
     * @param str The string to filter by. If null, the filter is cleared.
     * @param onSearchFilterListener An optional listener to be notified when the
     *                               filtering operation is complete.
     */
    @JvmOverloads
    fun setSearchFilter(str: String, onSearchFilterListener: OnSearchFilterListener? = null) {
        if (onSearchFilterListener != null) {
            headerFooterAdapter.setOnSearchFilterListener(onSearchFilterListener)
        }
        headerFooterAdapter.filter.filter(str)
    }

    /**
     * Smoothly scrolls the list to the item corresponding to the given [AppInfo].
     *
     * @param appInfo The application info to scroll to.
     * @param induce  True to induced animation on the item simulating a "press", false otherwise.
     * @return The item ID of the scrolled to application, or -1L if not found.
     */
    @JvmOverloads
    fun smoothScrollToAppInfo(appInfo: AppInfo, induce: Boolean = false) =
        scrollToAppInfo(appInfo, induce, true)

    /**
     * Scrolls the list to the item corresponding to the given [AppInfo].
     *
     * @param appInfo The application info to scroll to.
     * @param induce  True to induced animation on the item simulating a "press", false otherwise.
     * @param smoothScroll True to smoothly scroll to the item, false for an immediate jump.
     * @return The item ID of the scrolled to application, or -1L if not found.
     */
    @JvmOverloads
    fun scrollToAppInfo(
        appInfo: AppInfo,
        induce: Boolean = false,
        smoothScroll: Boolean = false
    ): Long {
        val headerFooterAdapter = headerFooterAdapter
        val position = headerFooterAdapter.getPosition(appInfo)
        if (position == NO_POSITION) {
            return -1L
        }
        if (induce) {
            val itemId = headerFooterAdapter.getItemId(position)
            val viewHolder = findViewHolderForItemId(itemId)
            if (viewHolder != null && viewHolder.absoluteAdapterPosition != NO_POSITION && viewHolder is Inducible) {
                disposable?.dispose()
                triggerAnimation = Supplier { (viewHolder as? Inducible)?.induce() }
                disposable = triggerAnimation?.get()
            }
        }
        if (smoothScroll) {
            post { smoothScrollToPosition(position) }
        } else {
            post { scrollToPosition(position) }
        }
        return headerFooterAdapter.getItemId(position)
    }

    /**
     * Updates the list of selectable items in the [SelectStateLoader].
     *
     * This function iterates through the current list of [ViewData] items in the
     * [viewDataController], extracts the [SelectableItem] from each [AppInfoViewData],
     * and then updates the [selectStateLoader] with this new list of selectable items.
     * This is typically called when the underlying data changes and the selection state
     * needs to be synchronized with the displayed items.
     */
    fun updateSelectableItemList() {
        val list = viewDataController.currentList.mapNotNull { (it as? AppInfoViewData)?.selectableItem }
        selectStateLoader.updateSelectableItemList(list)
    }
}