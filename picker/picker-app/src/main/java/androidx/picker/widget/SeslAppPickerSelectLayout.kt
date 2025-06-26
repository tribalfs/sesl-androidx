package androidx.picker.widget

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.ACCESSIBILITY_SERVICE
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Rect
import android.os.Build
import android.text.TextUtils
import android.transition.ChangeBounds
import android.transition.Transition
import android.transition.TransitionManager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.withStyledAttributes
import androidx.core.view.isInvisible
import androidx.picker.R
import androidx.picker.adapter.HeaderFooterAdapter
import androidx.picker.adapter.layoutmanager.AutoFitGridLayoutManager
import androidx.picker.common.log.LogTag
import androidx.picker.common.log.debug
import androidx.picker.common.log.error
import androidx.picker.common.log.warn
import androidx.picker.decorator.RecyclerViewCornerDecoration
import androidx.picker.helper.newMutateDrawable
import androidx.picker.model.AppData
import androidx.picker.model.AppInfo
import androidx.picker.model.AppInfoData
import androidx.picker.model.SpanData
import androidx.picker.model.appdata.CategoryAppData
import androidx.picker.model.appdata.GroupAppData
import androidx.picker.widget.SeslAppPickerSelectLayout.SelectLayoutType.AUTO
import androidx.picker.widget.SeslAppPickerSelectLayout.SelectLayoutType.LAND
import androidx.picker.widget.SeslAppPickerSelectLayout.SelectLayoutType.PORT
import androidx.picker.widget.SeslAppPickerView.Companion.ORDER_NONE
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible

/**
 * A custom layout class for displaying a list of apps and that also supports showing
 * a secondary list for displaying list of selected items either at the top or at
 * the side which is enabled using[enableSelectedAppPickerView].
 *
 * ### Key Features:
 * - **Dynamic Layout:** Automatically adjusts layout between portrait and landscape,
 *   and based on whether the selected apps view is visible.
 * - **Selected Apps View:** Can display a separate list of currently selected apps,
 *   either horizontally at the top (portrait) or vertically on the side (landscape).
 * - **Header Support:** Allows for a custom header view to be displayed above the
 *   selected apps view.
 * - **Search Filtering:** Supports filtering the main app list via [setSearchFilter].
 * - **State Management:** Manages the selection state of apps through its internal
 *   `CheckStateManager` and interfaces with the underlying [appPickerStateView].
 * - **Customization:**
 *     - Main view title can be customized using [setMainViewTitle].
 *     - Selected view title can be set using [setSelectedViewTitle].
 *     - Layout orientation can be forced using the `app:layoutType` XML attribute
 *       or programmatically via `selectLayoutType`.
 *
 * ### Custom XML Attributes:
 * - `app:layoutType` (enum): Defines the layout orientation behavior for the selected app list.
 *   Can be `auto`, `port`, or `land`. Defaults to `auto`.
 *   See [SelectLayoutType] for more details.
 *
 * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
 * @param attrs The attributes of the XML tag that is inflating the view.
 * @param defStyleAttr An attribute in the current theme that contains a reference to a style resource that supplies default values for the view. Can be 0 to not look for defaults.
 */
open class SeslAppPickerSelectLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes), AppPickerState, AppPickerEvent, LogTag {

    override val logTag: String = SeslAppPickerSelectLayout::class.java.simpleName

    /**
     * Enum representing the different layout configurations for the [SeslAppPickerSelectLayout].
     *
     * Each layout type is associated with a specific layout resource ID.
     * The appropriate layout is chosen based on the device orientation, whether a "selected apps"
     * view is enabled and visible, and whether a header view is present.
     *
     * @property LAND Landscape layout without a selected apps view or header.
     * @property LAND_HEADER_ONLY Landscape layout with only a header view, no selected apps view.
     * @property LAND_SELECTED Landscape layout with a selected apps view.
     * @property PORT Portrait layout without a selected apps view.
     * @property PORT_SELECTED Portrait layout with a selected apps view.
     * @property layoutResId The resource ID of the layout file for this type.
     */
    enum class LayoutType(val layoutResId: Int) {
        LAND(R.layout.picker_app_list_selectlayout_template_land),
        LAND_HEADER_ONLY(R.layout.picker_app_list_selectlayout_template_land_header_only),
        LAND_SELECTED(R.layout.picker_app_list_selectlayout_template_land_with_selected),
        PORT(R.layout.picker_app_list_selectlayout_template_portrait),
        PORT_SELECTED(R.layout.picker_app_list_selectlayout_template_portrait_with_selected);

        companion object {
            /**
             * Determines the [LayoutType] based on orientation, selection state, and header presence.
             *
             * @param orientation The current device orientation ([ORIENTATION_PORTRAIT] or other for landscape).
             * @param hasSelected Whether there are any items selected in the app picker.
             * @param hasHeader Whether a header view is currently displayed.
             * @return The appropriate [LayoutType] for the given parameters.
             */
            fun getType(orientation: Int, hasSelected: Boolean, hasHeader: Boolean): LayoutType {
                return when (orientation) {
                    ORIENTATION_PORTRAIT -> if (hasSelected) PORT_SELECTED else PORT
                    else -> if (hasSelected) LAND_SELECTED else if (hasHeader) LAND_HEADER_ONLY else LAND
                }
            }
        }
    }

    /**
     * Defines the layout orientation behavior of the selected app list.
     *
     * @property PORT
     * @property LAND
     * @property AUTO
     */
    enum class SelectLayoutType {
        /** The selected app list location is resolved  based on
         * the device's current orientation to either of [PORT] or [LAND]*/
        AUTO,
        /** Shows the selected app list on the top of the primary app list. */
        PORT,
        /** Shows the selected app list on the left of the primary app list. */
        LAND,
    }

    private fun SelectLayoutType.toOrientation(): Int {
        return when (this) {
            AUTO -> resources.configuration.orientation
            PORT -> ORIENTATION_PORTRAIT
            LAND -> ORIENTATION_LANDSCAPE
        }
    }

    private fun Int.indexToSelectLayoutType(): SelectLayoutType? {
        return try {
            SelectLayoutType.entries.toTypedArray().elementAt(this)
        } catch (_: Exception) {
            error("Index for AppPickerSelectLayout Type is wrong =$this")
            null
        }
    }

    private var curLayoutType: LayoutType? = null
    private val appPickerStateContainerView: FrameLayout
    private val rootAppPickerContainer: ConstraintLayout
    private val checkStateManager = CheckStateManager()
    private var headerHeight: Int = 0
    private var headerVisibility: Boolean = true
    private var isMainViewTitleCustomized: Boolean = false
    private var isSelectedViewEnabled: Boolean = false
    private val listItemHeight: Int
    private val mainViewTitleView: TextView
    private var onSearchFilterListener: SeslAppPickerView.OnSearchFilterListener? = null
    private var onSearchFilterListenerForLayout = SeslAppPickerView.OnSearchFilterListener {
        onSearchFilterListener?.onSearchFilterCompleted(it)
        if (!isMainViewTitleCustomized) {
            val stringRes = if (appPickerStateView.appDataList.size <= it) {
                R.string.title_all_apps
            } else {
                R.string.title_apps
            }
            mainViewTitleView.setText(stringRes)
        }
        searchNoResultFoundView.isInvisible = it != 0
    }

    private var onStateChangeListener: AppPickerState.OnStateChangeListener? = null
    private var paddingHorizontal: Int
    private val searchNoResultFoundView: View
    private var selectLayoutType: SelectLayoutType = AUTO
    /** The backing field for [appPickerStateView]*/
    private var _appPickerStateView: SeslAppPickerView
    /** The [SeslAppPickerGridView] for displaying the selected list of apps.*/
    private val selectedListView: SeslAppPickerGridView
    private val selectedViewHeader: FrameLayout
    private var selectedViewHeight: Int = 0
    private var selectedViewTitleHeight: Int = 0
    private val selectedViewTitleView: TextView
    /** Updated to true when activity is in multi-window mode (Android N and above)
     * or if the Samsung-specific "semIsPopOver" configuration is true. */
    private var shouldCheckHeaderVisibility: Boolean = false

    /**
     * The primary [SeslAppPickerView] instance used by this layout.
     *
     * This method allows direct access to the `SeslAppPickerView` which is responsible
     * for displaying the list of apps and handling their selection state.
     *
     * @return The [SeslAppPickerView] instance.
     */
    var appPickerStateView: SeslAppPickerView
        get() = _appPickerStateView
        set(value) {
            appPickerStateContainerView.removeView(_appPickerStateView)
            _appPickerStateView = value
            initializeAppPickerStateView()
            appPickerStateContainerView.addView(_appPickerStateView)
        }

    init {
        val res = resources
        listItemHeight = res.getDimensionPixelOffset(R.dimen.picker_app_list_single_line_height)
        paddingHorizontal = res.getDimensionPixelSize(R.dimen.picker_app_padding_horizontal)

        context.withStyledAttributes(
            attrs,
            R.styleable.SeslAppPickerSelectLayout,
            defStyleAttr,
            defStyleRes
        ) {
            val layoutTypeIdx = getInt(R.styleable.SeslAppPickerSelectLayout_layoutType, -1)
            layoutTypeIdx.indexToSelectLayoutType()?.let { selectLayoutType = it }
        }

        val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as android.view.LayoutInflater
        inflater.inflate(R.layout.picker_app_list_checkbox_container, this, true)
        rootAppPickerContainer = findViewById(R.id.root_app_picker_container)
        appPickerStateContainerView = findViewById(R.id.app_picker_state_view_container)
        mainViewTitleView = findViewById(R.id.main_view_title)

        selectedViewHeader = findViewById<FrameLayout>(R.id.selected_app_picker_header).also {
            it.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                if (headerVisibility) {
                    headerHeight = bottom - top
                    post { updateHeaderVisibility() }
                }
            }
        }

        selectedViewTitleView = findViewById<TextView>(R.id.selected_view_title).also {
            it.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                if (headerVisibility) {
                    selectedViewTitleHeight = bottom - top
                    post { updateHeaderVisibility() }
                }
            }
        }

        searchNoResultFoundView = findViewById<View>(R.id.no_results_found).also {
            @SuppressLint("ClickableViewAccessibility")
            it.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                        .hideSoftInputFromWindow(windowToken, 0)
                }
                false
            }
        }


        selectedListView = findViewById<SeslAppPickerGridView>(R.id.selected_app_picker_view).also {
            it.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                if (headerVisibility) {
                    selectedViewHeight = bottom - top
                    post { updateHeaderVisibility() }
                }
            }
            it.scrollBarStyle = SCROLLBARS_OUTSIDE_OVERLAY
            it.appListOrder = ORDER_NONE
            it.submitList(checkStateManager.getList())
            it.seslSetGoToTopEnabled(false)
            it.seslSetFastScrollerEnabled(false)
            it.setOnItemClickEventListener { view, appInfo ->
                for (appInfoData in checkStateManager.getList()) {
                    if (appInfoData.appInfo == appInfo) {
                        _appPickerStateView.setState(appInfo, false)
                        val uncheckText =
                            context.resources.getText(R.string.select_layout_unchecked_selected_app)
                                .toString()
                        if ((context.getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager).isEnabled) {
                            selectedListView.announceForAccessibility(
                                String.format(
                                    uncheckText,
                                    appInfoData.label
                                )
                            )
                        }
                    }
                }
                true
            }
        }

        shouldCheckHeaderVisibility = shouldCheckHeaderVisibility()
        updateLayout()

        appPickerStateContainerView.addView(SeslAppPickerListView(context).also {
            _appPickerStateView = it
        })
        initializeAppPickerStateView()
    }


    internal fun updateHeaderVisibility() {
        val visible = !shouldCheckHeaderVisibility || selectLayoutType.toOrientation() == ORIENTATION_LANDSCAPE || isVisibleHeight()
        if (headerVisibility != visible) {
            headerVisibility = visible
            post { updateLayout() }
        }
    }

    private fun updateLayout() {
        val orientation = selectLayoutType.toOrientation()
        setItemDecoration(orientation)
        selectedListView.layoutManager = getLayoutManager(orientation)
        refreshSelectedAppPickerView(false)
    }

    /**
     * Checks if there is enough vertical space to display the header and selected views.
     *
     * This function calculates the available height by subtracting the heights of the header,
     * selected view title, selected view, and main view title from the total height of the layout.
     * It then compares this available height with the height of a single list item.
     *
     * @return `true` if the available height is greater than the list item height, `false` otherwise.
     */
    private fun isVisibleHeight(): Boolean {
        val available =
            height - headerHeight - selectedViewTitleHeight - selectedViewHeight - mainViewTitleView.height
        return available > listItemHeight
    }

    /**
     * Determines whether the visibility of the header should be checked.
     *
     * This function checks two conditions:
     * 1. If the device is in multi-window mode (Android N and above).
     * 2. If the Samsung-specific "semIsPopOver" configuration is true.
     *
     * The header visibility check is necessary in these scenarios to ensure
     * the layout adjusts correctly when screen space is limited or when
     * the app is displayed in a pop-over window.
     *
     * @return `true` if the header visibility should be checked, `false` otherwise.
     */
    private fun shouldCheckHeaderVisibility(): Boolean {
        fun isMultiWindow() =
            Build.VERSION.SDK_INT >= 24 && (context as? Activity)?.isInMultiWindowMode == true
        return try {
            val config = resources.configuration
            config.javaClass.getMethod("semIsPopOver").invoke(config) as? Boolean == true or isMultiWindow()
        } catch (_: Exception) {
            warn("Failed to call semIsPopOver")
            isMultiWindow()
        }
    }

    private fun setItemDecoration(orientation: Int) {
        selectedListView.clearItemDecoration()
        if (orientation == ORIENTATION_PORTRAIT) {
            selectedListView.layoutParams.height = LayoutParams.WRAP_CONTENT
            selectedListView.addItemDecoration(SelectedHorizontalItemDecoration())
        } else {
            selectedListView.layoutParams.height = 0
            selectedListView.addItemDecoration(
                SelectedVerticalItemDecoration(
                    resources.getDimensionPixelOffset(R.dimen.picker_app_selected_item_view_interval_vertical_on_land)
                )
            )
        }
        if (paddingHorizontal > 0) {
            selectedListView.setPadding(paddingHorizontal, 0, paddingHorizontal, 0)
            selectedListView.seslSetFillHorizontalPaddingEnabled(true)
        }
        selectedListView.addItemDecoration(RecyclerViewCornerDecoration(context))
        selectedListView.seslSetFillBottomEnabled(false)
    }

    /**
     * Retrieves the appropriate [RecyclerView.LayoutManager] based on the provided orientation.
     *
     * - If the orientation is [ORIENTATION_PORTRAIT], a horizontal [LinearLayoutManager] is returned.
     * - Otherwise, an [AutoFitGridLayoutManager] is returned. The grid layout manager
     *   is configured with a custom [GridLayoutManager.SpanSizeLookup] to allow items to span
     *   multiple columns if specified by [SpanData].
     *
     * @param orientation The orientation to determine the layout manager for.
     *                    1 indicates horizontal layout, other values indicate grid layout.
     * @return The configured [RecyclerView.LayoutManager].
     */
    private fun getLayoutManager(orientation: Int): RecyclerView.LayoutManager {
        return if (orientation == ORIENTATION_PORTRAIT) {
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        } else {
            val gridLayoutManager = AutoFitGridLayoutManager(context)
            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val adapter = selectedListView.adapter as HeaderFooterAdapter
                    if (position < 0 || position >= adapter.itemCount) return 1
                    val item = adapter.getItem(position)
                    return if (item is SpanData && item.spanCount != -1) item.spanCount else gridLayoutManager.spanCount
                }
            }
            gridLayoutManager
        }
    }

    private fun initializeAppPickerStateView() {
        _appPickerStateView.setOnStateChangeListener(object : AppPickerState.OnStateChangeListener {
            override fun onStateAllChanged(isAllSelected: Boolean) {
                clearCheckedItemList()
                if (isAllSelected) {
                    updateCheckedAppList(_appPickerStateView.appDataList)
                }
                if (isSelectedViewEnabled) {
                    selectedListView.submitList(checkStateManager.getList())
                    post { refreshSelectedAppPickerView(true) }
                }
                onStateChangeListener?.onStateAllChanged(isAllSelected)
            }

            override fun onStateChanged(appInfo: AppInfo, isSelected: Boolean) {
                if (isSelected) {
                    addSelectedItem(appInfo)
                } else {
                    removeSelectedItem(appInfo)
                }
                post { refreshSelectedAppPickerView(true) }
                onStateChangeListener?.onStateChanged(appInfo, isSelected)
            }
        })
        if (paddingHorizontal > 0) {
            _appPickerStateView.setPadding(paddingHorizontal, 0, paddingHorizontal, 0)
            _appPickerStateView.seslSetFillHorizontalPaddingEnabled(true)
            _appPickerStateView.scrollBarStyle = SCROLLBARS_OUTSIDE_OVERLAY
        }
    }


    /**
     * Adds an item to the checked list.
     *
     * @param appInfoData The data of the app to be added.
     */
    fun addCheckedItem(appInfoData: AppInfoData) {
        if (appInfoData.dimmed) {
            checkStateManager.addFixedItem(appInfoData)
        } else {
            checkStateManager.add(appInfoData)
        }
    }

    /**
     * Adds an app to the selected items list.
     *
     * If the app is already selected, this method does nothing.
     * Otherwise, it retrieves the app's data and adds it to the selected items.
     * If the app belongs to a category and the category is selected, the category itself is added.
     *
     * @param appInfo The [AppInfo] of the app to add.
     */
    fun addSelectedItem(appInfo: AppInfo) {
        if (checkStateManager.exist(appInfo)) {
            return
        }
        val appData = _appPickerStateView.getAppData(appInfo)
        val categoryList = getCategoryAppDataList(_appPickerStateView.appDataList)
        when (appData) {
            is AppInfoData -> {
                val catAppData = getCategoryAppDataContainsAppInfo(categoryList, appInfo)
                if (catAppData == null || !catAppData.selected) {
                    addSelectItem(appData)
                } else {
                    addSelectItem(catAppData)
                }
            }

            is CategoryAppData -> addSelectItem(appData)
        }
    }

    /**
     * Clears the list of checked items.
     */
    fun clearCheckedItemList() = checkStateManager.clear()

    /**
     * Enables or disables showing the secondary app picker view for selected items.
     *
     * When enabled, a separate view displaying the selected apps will be shown.
     * When disabled, this view will be hidden.
     *
     * @param enabled True to enable the selected app picker view, false to disable it.
     */
    fun enableSelectedAppPickerView(enabled: Boolean) {
        isSelectedViewEnabled = enabled
        selectedListView.submitList(checkStateManager.getList())
        post { refreshSelectedAppPickerView(false) }
    }

    /**
     * Retrieves the [AppData] associated with the given [AppInfo].
     *
     * This method queries the underlying [appPickerStateView] to find the corresponding
     * [AppData] for the provided [AppInfo].
     *
     * @param appInfo The [AppInfo] for which to retrieve the [AppData].
     * @return The [AppData] associated with the `appInfo`, or `null` if no such data exists.
     */
    fun getAppData(appInfo: AppInfo): AppData? = _appPickerStateView.getAppData(appInfo)

    /**
     * Retrieves the list of [AppData] currently managed by the the [appPickerStateView]'s
     * ViewDataController.
     *
     * This list represents all the applications available for selection, including their current state.
     *
     * @return A list of [AppData] objects.
     */
    fun getAppDataList(): List<AppData> = _appPickerStateView.appDataList

    /**
     * Retrieves an [AppInfoData] object from a list based on its [AppInfo].
     *
     * @param list The list of [AppInfoData] to search within.
     * @param appInfo The [AppInfo] to match.
     * @return The matching [AppInfoData] object if found, otherwise null.
     */
    fun getAppInfoData(list: List<AppInfoData>, appInfo: AppInfo): AppInfoData? =
        list.find { it.appInfo == appInfo }

    /**
     * Finds a [CategoryAppData] in a list that contains a specific [AppInfo].
     *
     * @param list The list of [CategoryAppData] to search within.
     * @param appInfo The [AppInfo] to search for.
     * @return The [CategoryAppData] that contains the given [AppInfo], or null if not found.
     */
    fun getCategoryAppDataContainsAppInfo(
        list: List<CategoryAppData>,
        appInfo: AppInfo
    ): CategoryAppData? =
        list.find { getAppInfoData(it.appInfoDataList, appInfo) != null }

    /**
     * Extracts a list of [CategoryAppData] from a given list of [AppData].
     *
     * This function iterates through the input list and performs the following:
     * - If an item is a [GroupAppData], it flattens its `appDataList` and filters for [CategoryAppData].
     * - If an item is a [CategoryAppData], it's included in the result.
     * - Other types of [AppData] are ignored.
     *
     * @param list The list of [AppData] to process.
     * @return A list containing only [CategoryAppData] extracted from the input list.
     */
    fun getCategoryAppDataList(list: List<AppData>): List<CategoryAppData> =
        list.flatMap {
            when (it) {
                is GroupAppData -> it.appDataList.filterIsInstance<CategoryAppData>()
                is CategoryAppData -> listOf(it)
                else -> emptyList()
            }
        }


    override fun getState(appInfo: AppInfo): Boolean = _appPickerStateView.getState(appInfo)

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        shouldCheckHeaderVisibility = shouldCheckHeaderVisibility()
        if (selectLayoutType == AUTO) {
            updateLayout()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (selectedViewHeader.isNotEmpty() || isSelectedViewEnabled) {
            post { refreshSelectedAppPickerView(false) }
        }
    }

    /**
     * Refreshes the selected [appPickerStateView].
     *
     * This function determines the appropriate layout type based on the current orientation,
     * whether there are selected apps, and whether a header is present.
     *
     * If `withTransition` is true, a [ChangeBounds] transition is used to animate the
     * layout change. During the transition, the item animator of the `selectedListView`
     * is temporarily removed and restored to prevent animation conflicts.
     *
     * @param withTransition True to animate the layout change, false otherwise.
     */
    fun refreshSelectedAppPickerView(withTransition: Boolean) {
        val orientation = selectLayoutType.toOrientation()
        val hasSelected = isSelectedViewEnabled && checkStateManager.size() > 0 && headerVisibility
        val showHeader = selectedViewHeader.isNotEmpty() && headerVisibility
        val type = LayoutType.getType(orientation, hasSelected, showHeader)
        if (curLayoutType != type) {
            curLayoutType = type
            val visibility = searchNoResultFoundView.visibility
            val constraintSet = ConstraintSet()
            constraintSet.clone(context, type.layoutResId)
            constraintSet.applyTo(rootAppPickerContainer)
            searchNoResultFoundView.visibility = visibility
            if (withTransition) {
                val changeBounds = ChangeBounds()
                changeBounds.addListener(object : Transition.TransitionListener {
                    var rollback: Runnable? = null
                    override fun onTransitionStart(transition: Transition) {
                        val itemAnimator = selectedListView.itemAnimator
                        if (itemAnimator != null) {
                            debug("setItemAnimator = null")
                            selectedListView.clearAnimation()
                            selectedListView.itemAnimator = null
                            rollback = Runnable {
                                debug("setItemAnimator = $itemAnimator")
                                selectedListView.itemAnimator = itemAnimator
                            }
                        }
                    }

                    override fun onTransitionEnd(transition: Transition) {
                        rollback?.run()
                    }

                    override fun onTransitionCancel(transition: Transition) {}
                    override fun onTransitionPause(transition: Transition) {}
                    override fun onTransitionResume(transition: Transition) {}
                })
                rootAppPickerContainer.clearAnimation()
                TransitionManager.beginDelayedTransition(rootAppPickerContainer, changeBounds)
            } else {
                if (Build.VERSION.SDK_INT >= 23) {
                    TransitionManager.endTransitions(rootAppPickerContainer)
                }
            }
        }
        selectedViewHeader.isVisible = showHeader
    }

    /**
     * Smoothly scrolls the selected app list to the specified app.
     *
     * @param appInfo The [AppInfo] of the app to scroll to.
     * @param induceAnimation True to induce an animation during the scroll, false otherwise.
     *                    Defaults to false.
     */
    fun smoothScrollToAppInfo(appInfo: AppInfo, induceAnimation: Boolean = false) {
        selectedListView.smoothScrollToAppInfo(appInfo, induceAnimation)
    }

    /**
     * Sets a custom header view for the selected app picker.
     *
     * This method removes any existing header view and adds the provided view
     * to the `selectedViewHeader` FrameLayout. If the provided view is null,
     * the header will be cleared.
     *
     * After setting the header, `refreshSelectedAppPickerView(false)` is called
     * to update the layout based on the presence and size of the new header.
     *
     * @param view The custom [View] to set as the header. Can be null to remove the header.
     */
    fun setHeader(view: View?) {
        selectedViewHeader.removeAllViews()
        if (view != null) {
            selectedViewHeader.addView(view)
        }
        refreshSelectedAppPickerView(false)
    }

    /**
     * Sets the title of the main app list view.
     *
     * If a custom title is provided, it will be displayed.
     * Otherwise, the default title "All apps" (R.string.title_all_apps) will be used.
     * The visibility of the title view is also updated:
     * - If a custom title is set but is empty, the title view will be hidden (GONE).
     * - Otherwise, the title view will be visible (VISIBLE).
     *
     * @param title The custom title to set. If null, the default title will be used.
     */
    fun setMainViewTitle(title: String?) {
        isMainViewTitleCustomized = title != null
        mainViewTitleView.text = title ?: context.resources.getText(R.string.title_all_apps)
        mainViewTitleView.post {
            mainViewTitleView.visibility =
                if (isMainViewTitleCustomized && TextUtils.isEmpty(title)) GONE else VISIBLE
        }
    }

    override fun setOnItemClickEventListener(listener: AppPickerEvent.OnItemClickEventListener?) {
        _appPickerStateView.setOnItemClickEventListener(listener)
    }

    override fun setOnItemActionClickEventListener(listener: AppPickerEvent.OnItemClickEventListener?) {
        _appPickerStateView.setOnItemActionClickEventListener(listener)
    }

    override fun setOnStateChangeListener(listener: AppPickerState.OnStateChangeListener?) {
        onStateChangeListener = listener
    }

    /**
     * Sets a search filter for the app list.
     *
     * This method filters the list of apps displayed in the `SeslAppPickerView` based on the provided
     * filter string. The filtering is performed by the `appPickerView` itself.
     *
     * @param filter The string to filter the app list by. If `null` or empty, the filter is cleared.
     * @param onSearchFilterListener An optional listener to be notified when the search filter operation is completed.
     *                               The `onSearchFilterCompleted` method of this listener will be called.
     *                               If not provided, this uses the previously set listener if any.
     */
    @JvmOverloads
    fun setSearchFilter(filter: String, onSearchFilterListener: SeslAppPickerView.OnSearchFilterListener? = null) {
        onSearchFilterListener?.let { this.onSearchFilterListener = it }
        _appPickerStateView.setSearchFilter(filter, onSearchFilterListenerForLayout)
    }

    /**
     * Sets the title text for the selected view.
     *
     * If the provided title is empty or null, the selected view title will be hidden.
     *
     * @param title The title text to display.
     */
    fun setSelectedViewTitle(title: String) {
        selectedViewTitleView.visibility = if (TextUtils.isEmpty(title)) GONE else VISIBLE
        selectedViewTitleView.text = title
    }

    override fun setState(appInfo: AppInfo, isSelected: Boolean) {
        _appPickerStateView.setState(appInfo, isSelected)
    }

    override fun setStateAll(isAllSelected: Boolean) {
        _appPickerStateView.setStateAll(isAllSelected)
    }

    /**
     * Submits a new list of [AppData] to be displayed.
     *
     * If the provided [list] is `null` or not provided, the default list from [appPickerStateView]
     * will be used.
     *
     * @param list The new list of [AppData] to display, or `null` to use the default list.
     */
    @JvmOverloads
    fun submitList(list: List<AppData>? = null) {
        clearCheckedItemList()
        if (list != null) {
            updateCheckedAppList(list)
            if (isSelectedViewEnabled) {
                selectedListView.submitList(checkStateManager.getList())
                post { refreshSelectedAppPickerView(false) }
            }
            searchNoResultFoundView.visibility = if (list.isEmpty()) VISIBLE else INVISIBLE
        }
        _appPickerStateView.submitList(list)
    }

    /**
     * Updates the checked app list based on the provided list of [AppData].
     *
     * This function iterates through the input list and updates the checked state
     * for each type of [AppData] (AppInfoData, CategoryAppData, GroupAppData).
     *
     * @param list The list of [AppData] to process. If null, the function returns immediately.
     */
    fun updateCheckedAppList(list: List<AppData>) {
        for (appData in list) {
            when (appData) {
                is AppInfoData -> updateCheckedAppList(appData)
                is CategoryAppData -> updateCheckedAppList(appData)
                is GroupAppData -> updateCheckedAppList(appData)
            }
        }
    }

    /**
     * Updates an item in the app list with new data.
     *
     * This method is used to refresh the visual representation of an app item
     * if its underlying data has changed (e.g., selection state, icon, label).
     *
     * @param appInfoData The [AppInfoData] object containing the updated information for the item.
     */
    fun updateItem(appInfoData: AppInfoData) {
        _appPickerStateView.updateItem(appInfoData)
    }

    private fun addInternalSelectItems(list: List<AppData>) {
        if (isSelectedViewEnabled) {
            selectedListView.addItems(list)
            post {
                val size = checkStateManager.size()
                if (size > 0) {
                    selectedListView.smoothScrollToPosition(size - 1)
                }
            }
        }
    }

    private fun addSelectItem(appInfoData: AppInfoData) {
        val removeData = convertCheckBox2Remove(appInfoData)
        addCheckedItem(removeData)
        addInternalSelectItems(listOf(removeData))
    }

    /**
     * Adds a category app data item to the selected list.
     *
     * This method removes any existing items from the same category, converts the category
     * app data to a removable format, adds it to the checked items, and then adds it
     * to the internal list of selected items.
     *
     * @param categoryAppData The category app data to add.
     */
    fun addSelectItem(categoryAppData: CategoryAppData) {
        removeSelectItemInCategory(categoryAppData)
        val removeData = convertCheckBox2Remove(categoryAppData)
        addCheckedItem(removeData)
        addInternalSelectItems(listOf(removeData))
    }


    /**
     * Removes an item from the selected list.
     *
     * @param appInfoData The AppInfoData of the item to remove.
     */
    fun removeSelectItem(appInfoData: AppInfoData?) {
        if (appInfoData == null) return
        checkStateManager.remove(appInfoData.appInfo)
        if (isSelectedViewEnabled) {
            selectedListView.removeItem(appInfoData)
        }
    }

    /**
     * Removes an item from the selected list.
     *
     * This method handles the removal of an app from the selected list. If the app is
     * directly in the list, it's removed. If the app is part of a category, the category
     * itself is removed and then the other apps in that category are added back to the
     * selected list.
     *
     * @param appInfo The [AppInfo] of the item to remove.
     */
    fun removeSelectedItem(appInfo: AppInfo) {
        val appInfoData = checkStateManager.get(appInfo)
        val catAppDataList = getCategoryAppDataList(_appPickerStateView.appDataList)
        if (appInfoData != null) {
            removeSelectItem(appInfoData)
            return
        }
        val catAppData = getCategoryAppDataContainsAppInfo(catAppDataList, appInfo)
        val catAppInfoData = catAppData?.let { checkStateManager.get(it.appInfo) }
        if (catAppInfoData != null) {
            removeSelectItem(catAppInfoData)
            addSelectItemInCategory(catAppData)
        }
    }


    private fun addSelectItemInCategory(categoryAppData: CategoryAppData) {
        val list = categoryAppData.appInfoDataList
            .filter { it.selected }
            .map { convertCheckBox2Remove(it) }
        addInternalSelectItems(list)
    }

    private fun removeSelectItemInCategory(categoryAppData: CategoryAppData) {
        if (isSelectedViewEnabled) {
            val list = categoryAppData.appInfoDataList
                .mapNotNull { checkStateManager.get(it.appInfo) }
            selectedListView.removeItems(list)
        }
    }

    private fun updateCheckedAppList(appInfoData: AppInfoData) {
        if (appInfoData.selected) {
            addCheckedItem(convertCheckBox2Remove(appInfoData))
        }
    }

    private fun updateCheckedAppList(categoryAppData: CategoryAppData) {
        if (categoryAppData.selected) {
            removeSelectItemInCategory(categoryAppData)
            addCheckedItem(convertCategory2Remove(categoryAppData))
        } else {
            for (appInfoData in categoryAppData.appInfoDataList) {
                updateCheckedAppList(appInfoData)
            }
        }
    }

    private fun updateCheckedAppList(groupAppData: GroupAppData) {
        for (appData in groupAppData.appDataList) {
            when (appData) {
                is AppInfoData -> updateCheckedAppList(appData)
                is CategoryAppData -> updateCheckedAppList(appData)
            }
        }
    }

    /**
     * Adds items from a [GroupAppData] to the checked items list.
     *
     * This function iterates through the `appDataList` of the provided [GroupAppData].
     * - If an item is a [CategoryAppData], it's converted and added to the checked items.
     * - If an item is an [AppInfoData] and is not dimmed, it's converted and added to the checked items.
     *
     * @param groupAppData The [GroupAppData] containing items to be added.
     */
    fun addCheckedItem(groupAppData: GroupAppData) {
        for (appData in groupAppData.appDataList) {
            when (appData) {
                is CategoryAppData -> addCheckedItem(convertCategory2Remove(appData))
                is AppInfoData -> if (!appData.dimmed) addCheckedItem(convertCheckBox2Remove(appData))
            }
        }
    }

    fun convertCategory2Remove(categoryAppData: CategoryAppData): AppInfoData =
        AppData.GridRemoveAppDataBuilder(categoryAppData.appInfo)
            .setIcon(categoryAppData.icon.newMutateDrawable())
            .setLabel(categoryAppData.label)
            .build()

    /**
     * Converts an [AppInfoData] object representing a checkbox item to an [AppInfoData]
     * object suitable for a "remove" action.
     *
     * This is typically used when an item is selected from a list with checkboxes,
     * and it needs to be displayed in a "selected items" view with a remove button.
     *
     * The returned [AppInfoData] will have its icon and sub-icon (if present)
     * set as new mutated drawables.
     *
     * @param appInfoData The [AppInfoData] object to convert.
     * @return A new [AppInfoData] object configured for a "remove" action.
     */
    fun convertCheckBox2Remove(appInfoData: AppInfoData): AppInfoData =
        AppData.GridRemoveAppDataBuilder(appInfoData)
            .setIcon(appInfoData.icon.newMutateDrawable())
            .setSubIcon(appInfoData.subIcon.newMutateDrawable())
            .build()

    /**
     * Converts a [CategoryAppData] object to an [AppInfoData] object suitable for the remove view.
     * This is typically used when a whole category of apps is selected.
     *
     * @param categoryAppData The [CategoryAppData] to convert.
     * @return An [AppInfoData] object representing the category for the remove view.
     */
    private fun convertCheckBox2Remove(categoryAppData: CategoryAppData): AppInfoData =
        AppData.GridRemoveAppDataBuilder(categoryAppData.appInfo)
            .setLabel(categoryAppData.label)
            .setIcon(categoryAppData.icon.newMutateDrawable())
            .setSelected(categoryAppData.selected)
            .build()


    /**
     * Manages the state of checked (selected) apps.
     *
     * This class keeps track of two types of checked items:
     * - **Fixed items**: These are items that are always considered checked and cannot be unchecked by the user through the UI (e.g., dimmed items).
     * - **Checked items**: These are items that the user has selected.
     *
     * It provides methods to add, remove, and query the state of checked apps.
     */
    class CheckStateManager : LogTag {
        private val fixedAppMap = LinkedHashMap<AppInfo, AppInfoData>()
        private val checkedMap = LinkedHashMap<AppInfo, AppInfoData>()

        fun add(appInfoData: AppInfoData) {
            val appInfo = appInfoData.appInfo
            if (!checkedMap.containsKey(appInfo)) {
                checkedMap[appInfo] = appInfoData
            } else {
                warn("$appInfoData is already added")
            }
        }

        fun addFixedItem(appInfoData: AppInfoData) {
            val appInfo = appInfoData.appInfo
            if (!fixedAppMap.containsKey(appInfo)) {
                fixedAppMap[appInfo] = appInfoData
            } else {
                warn("$appInfoData is already added")
            }
        }

        fun clear() {
            val toRemove = checkedMap.values.filter { !it.dimmed }
            for (appInfoData in toRemove) {
                checkedMap.remove(appInfoData.appInfo)
            }
        }

        fun exist(appInfo: AppInfo): Boolean =
            checkedMap.containsKey(appInfo) || fixedAppMap.containsKey(appInfo)

        fun get(appInfo: AppInfo): AppInfoData? =
            checkedMap[appInfo] ?: fixedAppMap[appInfo]

        fun getList(): List<AppInfoData> =
            fixedAppMap.values.toList() + checkedMap.values.toList()

        override val logTag: String = "CheckStateManager"

        fun remove(appInfo: AppInfo) {
            checkedMap.remove(appInfo)
            fixedAppMap.remove(appInfo)
        }

        fun size(): Int = checkedMap.size + fixedAppMap.size
    }

    class SelectedHorizontalItemDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            val adapter = parent.adapter ?: return
            val position = parent.getChildAdapterPosition(view)
            val resources = parent.context.resources
            val padding =
                resources.getDimensionPixelSize(R.dimen.picker_app_selected_layout_horizontal_padding)
            val interval =
                resources.getDimensionPixelSize(R.dimen.picker_app_selected_item_view_interval_horizontal_on_port)
            outRect.left = if (position == 0) padding else interval
            outRect.right = if (position != adapter.itemCount - 1) interval else padding
            outRect.top =
                resources.getDimensionPixelSize(R.dimen.picker_app_grid_item_view_item_top_padding)
            outRect.bottom =
                resources.getDimensionPixelSize(R.dimen.picker_app_grid_item_view_item_bottom_padding)
            val itemView = view.findViewById<View>(R.id.item)
            itemView.layoutParams.width =
                resources.getDimensionPixelOffset(R.dimen.picker_app_grid_item_view_title_width)
            itemView.layoutParams.height =
                ((resources.getDimension(R.dimen.picker_app_grid_icon_title_size) * 2.0f
                    + resources.getDimension(R.dimen.picker_app_grid_item_view_icon_layout_margin_bottom)
                    + resources.getDimension(R.dimen.picker_app_grid_item_view_icon_layout_margin_top)
                    + resources.getDimension(R.dimen.picker_app_grid_icon_size))
                    - resources.getDimension(R.dimen.picker_app_grid_item_view_remove_icon_layout_margin)).toInt()
        }
    }

    class SelectedVerticalItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            val position = parent.getChildAdapterPosition(view)
            if (position == -1 || parent.adapter == null) return
            val layoutManager = parent.layoutManager
            if (layoutManager is GridLayoutManager) {
                val spanCount = layoutManager.spanCount
                outRect.top = spacing / 2
                outRect.bottom = spacing / 2
                val hInterval =
                    view.context.resources.getDimensionPixelOffset(R.dimen.picker_app_selected_layout_horizontal_interval) / 2
                val col = position % spanCount
                outRect.left = if (col == 0) 0 else hInterval
                outRect.right = if (col == spanCount - 1) 0 else hInterval
                view.findViewById<View>(R.id.item).layoutParams.width = -1
            }
        }
    }


}