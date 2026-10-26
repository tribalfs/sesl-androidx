/*
 * Copyright 2024 The Android Open Source Project
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
@file:Suppress("MemberVisibilityCanBePrivate")

package androidx.appcompat.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.R
import androidx.appcompat.util.theme.SeslThemeResourceHelper.getColorInt
import androidx.appcompat.util.theme.resource.SeslThemeResourceColor
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat

/**
 * SeslIndicator is a layout that displays a series of dots,
 * representing pages or items in a ViewPager or similar component.
 * It allows users to visually track their position and navigate between pages.
 *
 * This class was added in sesl7.
 *
 * @constructor Creates a new SeslIndicator.
 * @param context The Context the view is running in, through which it can
 *        access the current theme, resources, etc.
 * @param attrs The attributes of the XML tag that is inflating the view.
 *///Added in sesl7
class SeslIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    fun interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

    var defaultCircle: Drawable? = null
        set(value) {
            val it = indicator.iterator()
            while (it.hasNext()) {
                it.next().defaultCircle = value
            }
            field = value
        }

    var selectCircle: Drawable? = null
        set(value) {
            val it = indicator.iterator()
            while (it.hasNext()) {
                it.next().selectCircle = value
            }
            field = value
        }

    var selectedPosition: Int = -1
        set(value) {
            var i = value
            if (i < 0) {
                i = 0
            } else if (i >= indicator.size) {
                i = indicator.size - 1
            }
            field = i
            invalidateIndicator()
        }

    private var itemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(itemClickListener: OnItemClickListener) {
        this.itemClickListener = itemClickListener
        val it = indicator.iterator()
        while (it.hasNext()) {
            it.next().setOnClickListener { view ->
                itemClickListener.onItemClick(view, indicator.indexOf(view))
            }
        }
    }

    private val indicator: MutableList<PageIndicatorMarker> = ArrayList()

    fun removeIndicator(position: Int) {
        if (position < 0 || position >= indicator.size) {
            return
        }
        removeView(indicator.removeAt(position))
        if (this.selectedPosition >= indicator.size) {
            selectedPosition = this.selectedPosition - 1
        } else {
            invalidateIndicator()
        }
    }

    val size: Int
        get() = indicator.size

    private fun generateDotIndicator(sizeType: Int? = SIZE_TYPE_SMALL): PageIndicatorMarker {
        return PageIndicatorMarker(context, sizeType).apply {
            this.defaultCircle = this@SeslIndicator.defaultCircle
            this.selectCircle = this@SeslIndicator.selectCircle
        }
    }

    private fun invalidateIndicator() {
        val size = indicator.size
        var i = 0
        while (i < size) {
            indicator[i].isActive = (i == this.selectedPosition)
            i++
        }
    }

    @JvmOverloads
    fun addIndicator(sizeType: Int? = SIZE_TYPE_SMALL) {
        val dotIndicator = generateDotIndicator(sizeType)
        dotIndicator.setOnClickListener { view ->
            itemClickListener?.onItemClick(view, indicator.indexOf(view))
        }
        indicator.add(dotIndicator)

        dotIndicator.accessibilityDelegate = object : AccessibilityDelegate() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfo
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                val infoCompat = AccessibilityNodeInfoCompat.wrap(info)
                infoCompat.contentDescription = resources.getString(
                    R.string.sesl_appbar_suggest_pagination,
                    indicator.indexOf(dotIndicator) + 1,
                    size
                )
            }
        }

        val lp = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        val paddingRes = if (sizeType == SIZE_TYPE_LARGE) {
            R.dimen.sesl_viewpager_indicator_horizontal_padding_lg
        } else {
            R.dimen.sesl_viewpager_indicator_horizontal_padding_sm
        }
        val margin = context.resources.getDimensionPixelSize(paddingRes) / 2
        lp.setMargins(margin, 0, margin, 0)
        addView(dotIndicator, lp)

        if (this.selectedPosition == -1) {
            this.selectedPosition = 0
        }
    }

    init {
        val defaultDrawable = context.getDrawable(R.drawable.sesl_viewpager_indicator_on_off)?.mutate()
        (defaultDrawable as? GradientDrawable)?.setColor(
            ColorStateList.valueOf(getAppBarViewPagerIndicatorOffColor(context))
        )
        this.defaultCircle = defaultDrawable

        val selectDrawable = context.getDrawable(R.drawable.sesl_viewpager_indicator_on_off)?.mutate()
        (selectDrawable as? GradientDrawable)?.setColor(
            ColorStateList.valueOf(getAppBarViewPagerIndicatorOnColor(context))
        )
        this.selectCircle = selectDrawable

        this.selectedPosition = -1
    }

    private fun getAppBarViewPagerIndicatorOffColor(context: Context): Int {
        return getColorInt(
            context,
            SeslThemeResourceColor.OpenThemeResourceColor(
                SeslThemeResourceColor.ThemeResourceColor(
                    R.color.sesl_appbar_viewpager_indicator_off,
                    R.color.sesl_appbar_viewpager_indicator_off_dark
                ),
                SeslThemeResourceColor.ThemeResourceColor(
                    R.color.sesl_appbar_viewpager_indicator_off_for_theme,
                    R.color.sesl_appbar_viewpager_indicator_off_dark_for_theme
                )
            )
        )
    }

    private fun getAppBarViewPagerIndicatorOnColor(context: Context): Int {
        return getColorInt(
            context,
            SeslThemeResourceColor.OpenThemeResourceColor(
                SeslThemeResourceColor.ThemeResourceColor(R.color.sesl_appbar_viewpager_indicator_on),
                SeslThemeResourceColor.ThemeResourceColor(R.color.sesl_appbar_viewpager_indicator_on_for_theme)
            )
        )
    }

    class PageIndicatorMarker @JvmOverloads constructor(
        context: Context,
        sizeType: Int? = SIZE_TYPE_SMALL,
        attrs: AttributeSet? = null
    ) : FrameLayout(context, attrs) {

        private val imageView: ImageView = ImageView(context)

        var isActive: Boolean = false
            set(value) {
                imageView.setImageDrawable(if (value) selectCircle else defaultCircle)
                isSelected = value
                field = value
            }

        var defaultCircle: Drawable? = null
            set(value) {
                field = value
                isActive = isActive
            }

        var selectCircle: Drawable? = null
            set(value) {
                field = value
                isActive = isActive
            }

        init {
            imageView.setImageDrawable(selectCircle)
            addView(imageView)

            if (sizeType == SIZE_TYPE_LARGE) {
                val size = context.resources.getDimensionPixelSize(R.dimen.sesl_viewpager_indicator_size_lg)
                val lp = imageView.layoutParams
                lp.width = size
                lp.height = size
            }
        }
    }

    companion object {
        const val SIZE_TYPE_SMALL: Int = 0
        const val SIZE_TYPE_LARGE: Int = 1
    }
}