/*
 * Copyright 2026 The Android Open Source Project
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
import android.app.Activity
import android.content.Context
import android.content.Context.ACCESSIBILITY_SERVICE
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Build
import android.text.TextUtils
import android.transition.ChangeBounds
import android.transition.Transition
import android.transition.TransitionManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewTreeObserver
import android.view.WindowInsets
import android.view.WindowManager
import android.view.accessibility.AccessibilityManager
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.withStyledAttributes
import androidx.core.view.isInvisible
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import androidx.picker.R
import androidx.picker.common.log.LogTag
import androidx.picker.common.log.debug
import androidx.picker.common.log.error
import androidx.picker.common.log.warn
import androidx.picker.helper.SeslSelectLayoutFooterHelper
import androidx.picker.helper.newMutateDrawable
import androidx.picker.model.AppData
import androidx.picker.model.AppInfo
import androidx.picker.model.AppInfoData
import androidx.picker.model.appdata.CategoryAppData
import androidx.picker.model.appdata.GroupAppData
import androidx.picker.widget.SeslAppPickerSelectLayout.SelectLayoutType.AUTO
import androidx.picker.widget.SeslAppPickerSelectLayout.SelectLayoutType.LAND
import androidx.picker.widget.SeslAppPickerSelectLayout.SelectLayoutType.PORT

open class SeslAppPickerSelectLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes), AppPickerState, AppPickerEvent, LogTag, ViewTreeObserver.OnGlobalLayoutListener {

    override val logTag: String = SeslAppPickerSelectLayout::class.java.simpleName

    enum class LayoutType(val layoutResId: Int) {
        LAND(R.layout.picker_app_list_selectlayout_template_land),
        LAND_HEADER_ONLY(R.layout.picker_app_list_selectlayout_template_land_header_only),
        LAND_SELECTED(R.layout.picker_app_list_selectlayout_template_land_with_selected),
        PORT(R.layout.picker_app_list_selectlayout_template_portrait),
        PORT_SELECTED(R.layout.picker_app_list_selectlayout_template_portrait_with_selected),
        PORT_HEADER(R.layout.picker_app_list_select_layout_header_portrait),
        PORT_HEADER_SELECTED(R.layout.picker_app_list_select_layout_header_portrait_with_selected);

        companion object {
            fun getType(orientation: Int, hasSelected: Boolean, hasHeader: Boolean): LayoutType {
                return when (orientation) {
                    ORIENTATION_PORTRAIT -> if (hasSelected) PORT_SELECTED else PORT
                    else -> if (hasSelected) LAND_SELECTED else if (hasHeader) LAND_HEADER_ONLY else LAND
                }
            }
        }
    }

    enum class SelectLayoutType {
        AUTO,
        PORT,
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
            error("Index for AppPickerSelectLayout Type is wrong =")
            null
        }
    }

    private var curLayoutType: LayoutType? = null
    private var curPortHeaderLayoutType: LayoutType? = null
    private val appPickerStateContainerView: FrameLayout
    private val rootAppPickerContainer: ConstraintLayout
    private val checkStateManager = CheckStateManager()
    private val footerHelper = SeslSelectLayoutFooterHelper(context)
    private var headerHeight: Int = 0
    private var headerVisibility: Boolean = true
    private var isBottomSearchVisible: Boolean = false
    private var isKeyboardVisible: Boolean = false
    private var isMainViewTitleCustomized: Boolean = false
    private var isSelectedViewEnabled: Boolean = false
    private var keyboardHeight: Int = 0
    private var keyboardObserver: ViewTreeObserver? = null
    private val listItemHeight: Int
    private var mainViewTitleText: String? = null
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
            val titleStr = context.resources.getString(stringRes)
            mainViewTitleText = titleStr
            mainViewTitleView.text = titleStr
            portMainViewTitleView.text = titleStr
        }
        searchNoResultFoundView.isInvisible = it != 0
        updateTitleViewVisibility()
    }

    private var onStateChangeListener: AppPickerState.OnStateChangeListener? = null
    private var paddingHorizontal: Int
    private val searchNoResultFoundView: View
    private var selectLayoutType: SelectLayoutType = AUTO

    private val portHeaderLayout: View
    private val portHeaderRootView: ConstraintLayout
    private val portSelectedViewHeader: FrameLayout
    private val portMainViewTitleView: TextView
    private val portSelectedViewTitleView: TextView
    private val portSelectedListView: SeslSelectLayoutSelectedListView

    private lateinit var _appPickerStateView: SeslAppPickerView
    private val selectedListView: SeslSelectLayoutSelectedListView
    private val selectedViewHeader: FrameLayout
    private var selectedViewHeight: Int = 0
    private var selectedViewTitleHeight: Int = 0
    private val selectedViewTitleView: TextView
    private var selectedViewTitleText: String? = null
    private var shouldCheckHeaderVisibility: Boolean = false

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

        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.picker_app_list_checkbox_container, this, true)
        rootAppPickerContainer = findViewById(R.id.root_app_picker_container)
        appPickerStateContainerView = findViewById(R.id.app_picker_state_view_container)
        mainViewTitleView = findViewById(R.id.main_view_title)

        portHeaderLayout = inflater.inflate(R.layout.picker_app_list_select_layout_header_container, null, false)
        portHeaderRootView = portHeaderLayout.findViewById(R.id.root_app_picker_container)
        portSelectedViewHeader = portHeaderLayout.findViewById(R.id.selected_app_picker_header)
        portMainViewTitleView = portHeaderLayout.findViewById(R.id.main_view_title)
        portSelectedViewTitleView = portHeaderLayout.findViewById(R.id.selected_view_title)
        portSelectedListView = portHeaderLayout.findViewById(R.id.selected_app_picker_view)

        selectedViewHeader = findViewById<FrameLayout>(R.id.selected_app_picker_header).also {
            it.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                if (headerVisibility) {
                    headerHeight = bottom - top
                    post { updateHeaderVisibility() }
                }
            }
        }

        portSelectedViewHeader.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (headerVisibility) {
                headerHeight = bottom - top
                post { updateHeaderVisibility() }
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

        portSelectedViewTitleView.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (headerVisibility) {
                selectedViewTitleHeight = bottom - top
                post { updateHeaderVisibility() }
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

        selectedListView = findViewById(R.id.selected_app_picker_view)
        setupSelectedListView(selectedListView)
        setupSelectedListView(portSelectedListView)

        appPickerStateContainerView.addView(SeslAppPickerListView(context).also {
            _appPickerStateView = it
        })
        initializeAppPickerStateView()

        shouldCheckHeaderVisibility = shouldCheckHeaderVisibility()
        updateLayout()
    }

    private fun setupSelectedListView(listView: SeslSelectLayoutSelectedListView) {
        listView.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (headerVisibility) {
                selectedViewHeight = bottom - top
                post { updateHeaderVisibility() }
            }
        }
        listView.submitList(checkStateManager.getList())
        listView.setOnItemClickEventListener { view, appInfo ->
            for (appInfoData in checkStateManager.getList()) {
                if (appInfoData.appInfo == appInfo) {
                    _appPickerStateView.setState(appInfo, false)
                    val uncheckText =
                        context.resources.getText(R.string.select_layout_unchecked_selected_app)
                            .toString()
                    if ((context.getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager).isEnabled) {
                        listView.announceForAccessibility(
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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setupKeyboardDetection()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeKeyboardDetection()
    }

    private fun setupKeyboardDetection() {
        if (keyboardObserver == null || !keyboardObserver!!.isAlive) {
            keyboardObserver = viewTreeObserver.also {
                if (it.isAlive) {
                    it.addOnGlobalLayoutListener(this)
                }
            }
        }
    }

    private fun removeKeyboardDetection() {
        keyboardObserver?.let {
            if (it.isAlive) {
                it.removeOnGlobalLayoutListener(this)
            }
            keyboardObserver = null
        }
    }

    override fun onGlobalLayout() {
        if (searchNoResultFoundView.visibility == VISIBLE) {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            if (windowManager != null && Build.VERSION.SDK_INT >= 30) {
                try {
                    val windowInsets = windowManager.currentWindowMetrics.windowInsets
                    val imeBottom = windowInsets.getInsets(WindowInsets.Type.ime()).bottom
                    val sysBottom = windowInsets.getInsets(WindowInsets.Type.systemBars()).bottom
                    val keyHeight = maxOf(0, imeBottom - sysBottom)
                    val keyVisible = keyHeight > 0
                    if (keyVisible != isKeyboardVisible || keyHeight != keyboardHeight) {
                        isKeyboardVisible = keyVisible
                        keyboardHeight = keyHeight
                        updateNoResultsMarginForKeyboard()
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun setBottomSearchVisible(visible: Boolean) {
        isBottomSearchVisible = visible
        updateNoResultsMarginForKeyboard()
        addAppPickerStateViewFooter(false)
        updateSelectedListViewFooter()
    }

    fun updateNoResultsMarginForKeyboard() {
        var margin = if (isBottomSearchVisible) {
            try {
                val resId = context.resources.getIdentifier(
                    "sesl_search_view_bottom_preferred_height",
                    "dimen",
                    context.packageName
                )
                if (resId != 0) context.resources.getDimensionPixelSize(resId) else 0
            } catch (_: Exception) { 0 }
        } else 0
        if (isKeyboardVisible) {
            margin += keyboardHeight
        }
        if (selectLayoutType.toOrientation() == ORIENTATION_PORTRAIT) {
            margin += try {
                val resId = context.resources.getIdentifier(
                    "sesl_action_bar_default_height",
                    "dimen",
                    context.packageName
                )
                if (resId != 0) context.resources.getDimensionPixelSize(resId) else 0
            } catch (_: Exception) { 0 }
        }
        val lp = searchNoResultFoundView.layoutParams as? MarginLayoutParams
        if (lp != null) {
            lp.bottomMargin = margin
            searchNoResultFoundView.layoutParams = lp
        }
    }

    fun updateSelectedListViewFooter() {
        selectedListView.post {
            selectedListView.updateSelectedListViewFooter(
                selectLayoutType.toOrientation(),
                isBottomSearchVisible
            )
        }
    }

    fun addAppPickerStateViewFooter(animate: Boolean = false) {
        if (!::_appPickerStateView.isInitialized) return
        _appPickerStateView.post {
            val footerView = footerHelper.getOrCreateFooterView()
            val targetHeight = footerHelper.computeTargetFooterHeight(
                selectLayoutType.toOrientation(),
                isBottomSearchVisible
            )
            if (animate) {
                _appPickerStateView.clearFooters()
                (footerView.parent as? ViewGroup)?.removeView(footerView)
                _appPickerStateView.addFooter(footerView)
            }
            footerHelper.updateFooterHeight(
                footerView,
                targetHeight,
                !animate && !_appPickerStateView.isInLayout()
            )
        }
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
        selectedListView.configureViewBasedOnOrientation(orientation, paddingHorizontal)
        portSelectedListView.configureViewBasedOnOrientation(orientation, paddingHorizontal)

        if (paddingHorizontal > 0) {
            selectedListView.setPadding(paddingHorizontal, 0, paddingHorizontal, 0)
            portSelectedListView.setPadding(paddingHorizontal, 0, paddingHorizontal, 0)
        }

        if (orientation == ORIENTATION_PORTRAIT) {
            _appPickerStateView.clearHeaders()
            _appPickerStateView.addHeader(portHeaderLayout)
        } else {
            _appPickerStateView.clearHeaders()
        }

        refreshSelectedAppPickerView(false)
        updateSelectedListViewFooter()
    }

    private fun isVisibleHeight(): Boolean {
        val available =
            height - headerHeight - selectedViewTitleHeight - selectedViewHeight - mainViewTitleView.height
        return available > listItemHeight
    }

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


    private fun initializeAppPickerStateView() {
        _appPickerStateView.setOnStateChangeListener(object : AppPickerState.OnStateChangeListener {
            override fun onStateAllChanged(isAllSelected: Boolean) {
                clearCheckedItemList()
                if (isAllSelected) {
                    updateCheckedAppList(_appPickerStateView.appDataList)
                }
                if (isSelectedViewEnabled) {
                    selectedListView.submitList(checkStateManager.getList())
                    portSelectedListView.submitList(checkStateManager.getList())
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

    fun addCheckedItem(appInfoData: AppInfoData) {
        if (appInfoData.dimmed) {
            checkStateManager.addFixedItem(appInfoData)
        } else {
            checkStateManager.add(appInfoData)
        }
    }

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

    fun clearCheckedItemList() = checkStateManager.clear()

    fun enableSelectedAppPickerView(enabled: Boolean) {
        isSelectedViewEnabled = enabled
        selectedListView.submitList(checkStateManager.getList())
        portSelectedListView.submitList(checkStateManager.getList())
        post { refreshSelectedAppPickerView(false) }
    }

    fun getAppData(appInfo: AppInfo): AppData? = _appPickerStateView.getAppData(appInfo)

    fun getAppDataList(): List<AppData> = _appPickerStateView.appDataList

    fun getAppInfoData(list: List<AppInfoData>, appInfo: AppInfo): AppInfoData? =
        list.find { it.appInfo == appInfo }

    fun getCategoryAppDataContainsAppInfo(
        list: List<CategoryAppData>,
        appInfo: AppInfo
    ): CategoryAppData? =
        list.find { getAppInfoData(it.appInfoDataList, appInfo) != null }

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
        addAppPickerStateViewFooter(false)
        updateSelectedListViewFooter()
        if (selectLayoutType == AUTO) {
            updateLayout()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (selectedViewHeader.isNotEmpty() || portSelectedViewHeader.isNotEmpty() || isSelectedViewEnabled) {
            post { refreshSelectedAppPickerView(false) }
        }
    }

    private fun updateSelectedViewVisibility(title: String?) {
        val isPortrait = selectLayoutType.toOrientation() == ORIENTATION_PORTRAIT
        if (isPortrait) {
            selectedViewTitleView.visibility = GONE
            selectedListView.visibility = GONE
            portSelectedViewTitleView.visibility = if (TextUtils.isEmpty(title)) GONE else VISIBLE
            portSelectedListView.visibility = if (isSelectedViewEnabled && checkStateManager.size() > 0) VISIBLE else GONE
        } else {
            portSelectedViewTitleView.visibility = GONE
            portSelectedListView.visibility = GONE
            selectedViewTitleView.visibility = if (curLayoutType == LayoutType.LAND || TextUtils.isEmpty(title)) GONE else VISIBLE
            selectedListView.visibility = if (isSelectedViewEnabled && checkStateManager.size() > 0) VISIBLE else GONE
        }
    }

    private fun updateTitleViewVisibility() {
        val isPortrait = selectLayoutType.toOrientation() == ORIENTATION_PORTRAIT
        if (isPortrait) {
            mainViewTitleView.visibility = GONE
            portMainViewTitleView.visibility = VISIBLE
        } else {
            portMainViewTitleView.visibility = GONE
            mainViewTitleView.visibility = VISIBLE
        }
    }

    fun refreshSelectedAppPickerView(withTransition: Boolean) {
        val orientation = selectLayoutType.toOrientation()
        val hasSelected = isSelectedViewEnabled && checkStateManager.size() > 0 && headerVisibility
        val showHeader = (selectedViewHeader.isNotEmpty() || portSelectedViewHeader.isNotEmpty()) && headerVisibility
        val type = LayoutType.getType(orientation, hasSelected, showHeader)
        val portHeaderType = if (hasSelected) LayoutType.PORT_HEADER_SELECTED else LayoutType.PORT_HEADER

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
                                debug("setItemAnimator = ")
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

        if (orientation == ORIENTATION_PORTRAIT && curPortHeaderLayoutType != portHeaderType) {
            curPortHeaderLayoutType = portHeaderType
            val portConstraintSet = ConstraintSet()
            portConstraintSet.clone(context, portHeaderType.layoutResId)
            portConstraintSet.applyTo(portHeaderRootView)
        }

        if (orientation == ORIENTATION_PORTRAIT) {
            selectedViewHeader.visibility = GONE
            portSelectedViewHeader.visibility = if (showHeader) VISIBLE else GONE
        } else {
            selectedViewHeader.visibility = if (showHeader) VISIBLE else GONE
            portSelectedViewHeader.visibility = GONE
        }

        updateSelectedViewVisibility(selectedViewTitleText)
        updateTitleViewVisibility()
    }

    fun smoothScrollToAppInfo(appInfo: AppInfo, induceAnimation: Boolean = false) {
        if (selectLayoutType.toOrientation() == ORIENTATION_PORTRAIT) {
            portSelectedListView.smoothScrollToAppInfo(appInfo, induceAnimation)
        } else {
            selectedListView.smoothScrollToAppInfo(appInfo, induceAnimation)
        }
    }

    fun setHeader(view: View?) {
        selectedViewHeader.removeAllViews()
        portSelectedViewHeader.removeAllViews()
        if (view != null) {
            if (selectLayoutType.toOrientation() == ORIENTATION_PORTRAIT) {
                portSelectedViewHeader.addView(view)
            } else {
                selectedViewHeader.addView(view)
            }
        }
        refreshSelectedAppPickerView(false)
    }

    fun setMainViewTitle(title: String?) {
        isMainViewTitleCustomized = title != null
        val titleText = title ?: context.resources.getText(R.string.title_all_apps).toString()
        mainViewTitleText = titleText
        mainViewTitleView.text = titleText
        portMainViewTitleView.text = titleText
        updateTitleViewVisibility()
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

    @JvmOverloads
    fun setSearchFilter(filter: String, onSearchFilterListener: SeslAppPickerView.OnSearchFilterListener? = null) {
        onSearchFilterListener?.let { this.onSearchFilterListener = it }
        _appPickerStateView.setSearchFilter(filter, onSearchFilterListenerForLayout)
    }

    fun setSelectedViewTitle(title: String) {
        selectedViewTitleText = title
        selectedViewTitleView.text = title
        portSelectedViewTitleView.text = title
        updateSelectedViewVisibility(title)
    }

    override fun setState(appInfo: AppInfo, isSelected: Boolean) {
        _appPickerStateView.setState(appInfo, isSelected)
    }

    override fun setStateAll(isAllSelected: Boolean) {
        _appPickerStateView.setStateAll(isAllSelected)
    }

    @JvmOverloads
    fun submitList(list: List<AppData>? = null) {
        clearCheckedItemList()
        if (list != null) {
            updateCheckedAppList(list)
            if (isSelectedViewEnabled) {
                selectedListView.submitList(checkStateManager.getList())
                portSelectedListView.submitList(checkStateManager.getList())
                post { refreshSelectedAppPickerView(false) }
            }
            searchNoResultFoundView.visibility = if (list.isEmpty()) VISIBLE else INVISIBLE
        }
        _appPickerStateView.submitList(list)
        addAppPickerStateViewFooter(true)
    }

    fun updateCheckedAppList(list: List<AppData>) {
        for (appData in list) {
            when (appData) {
                is AppInfoData -> updateCheckedAppList(appData)
                is CategoryAppData -> updateCheckedAppList(appData)
                is GroupAppData -> updateCheckedAppList(appData)
            }
        }
    }

    fun updateItem(appInfoData: AppInfoData) {
        _appPickerStateView.updateItem(appInfoData)
    }

    private fun addInternalSelectItems(list: List<AppData>) {
        if (isSelectedViewEnabled) {
            selectedListView.addItems(list)
            portSelectedListView.addItems(list)
            post {
                val size = checkStateManager.size()
                if (size > 0) {
                    selectedListView.smoothScrollToPosition(size - 1)
                    portSelectedListView.smoothScrollToPosition(size - 1)
                }
            }
        }
    }

    private fun addSelectItem(appInfoData: AppInfoData) {
        val removeData = convertCheckBox2Remove(appInfoData)
        addCheckedItem(removeData)
        addInternalSelectItems(listOf(removeData))
    }

    fun addSelectItem(categoryAppData: CategoryAppData) {
        removeSelectItemInCategory(categoryAppData)
        val removeData = convertCheckBox2Remove(categoryAppData)
        addCheckedItem(removeData)
        addInternalSelectItems(listOf(removeData))
    }

    fun removeSelectItem(appInfoData: AppInfoData?) {
        if (appInfoData == null) return
        checkStateManager.remove(appInfoData.appInfo)
        if (isSelectedViewEnabled) {
            selectedListView.removeItem(appInfoData)
            portSelectedListView.removeItem(appInfoData)
        }
    }

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
            portSelectedListView.removeItems(list)
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

    fun convertCheckBox2Remove(appInfoData: AppInfoData): AppInfoData =
        AppData.GridRemoveAppDataBuilder(appInfoData)
            .setIcon(appInfoData.icon.newMutateDrawable())
            .setSubIcon(appInfoData.subIcon.newMutateDrawable())
            .build()

    private fun convertCheckBox2Remove(categoryAppData: CategoryAppData): AppInfoData =
        AppData.GridRemoveAppDataBuilder(categoryAppData.appInfo)
            .setLabel(categoryAppData.label)
            .setIcon(categoryAppData.icon.newMutateDrawable())
            .setSelected(categoryAppData.selected)
            .build()

    class CheckStateManager : LogTag {
        private val fixedAppMap = LinkedHashMap<AppInfo, AppInfoData>()
        private val checkedMap = LinkedHashMap<AppInfo, AppInfoData>()

        fun add(appInfoData: AppInfoData) {
            val appInfo = appInfoData.appInfo
            if (!checkedMap.containsKey(appInfo)) {
                checkedMap[appInfo] = appInfoData
            } else {
                warn(" is already added")
            }
        }

        fun addFixedItem(appInfoData: AppInfoData) {
            val appInfo = appInfoData.appInfo
            if (!fixedAppMap.containsKey(appInfo)) {
                fixedAppMap[appInfo] = appInfoData
            } else {
                warn(" is already added")
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

}
