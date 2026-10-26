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

package androidx.glance.oneui.common

import android.R
import android.content.res.ColorStateList
import android.graphics.BlendMode
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView

internal object TraverseViewColorApplier {
    private const val TAG = "ViewColorApplier"
    private const val MONOTONE_TEXT_SHADOW_COLOR = 218103808
    private const val MONOTONE_TEXT_SHADOW_RADIUS = 5.0f

    fun traverse(view: View, baseWidgetColor: Int, updateColor: Boolean, showShadow: Boolean) {
        SeslAppWidgetLog.i(TAG, "traverse view=$view baseWidgetColor=$baseWidgetColor updateColor=$updateColor showShadow=$showShadow")
        traverseInner(view, baseWidgetColor, updateColor, showShadow)
    }

    private fun traverseInner(view: View, baseWidgetColor: Int, updateColor: Boolean, showShadow: Boolean) {
        if (Build.VERSION.SDK_INT < 30) {
            return
        }

        val isKeepColor = view.getTag(-369098752) == "true" || (view.tag as? String) == "keepColor"

        when (view) {
            is ListView -> {
                if (updateColor) {
                    setMonotoneColor(view, baseWidgetColor)
                }
            }
            is ViewGroup -> {
                if (updateColor && !isKeepColor) {
                    val background = view.background
                    if (background is GradientDrawable) {
                        val color = background.color
                        val defaultColor = color?.defaultColor ?: -1
                        if (defaultColor != -1 && defaultColor != 0) {
                            if (view.id == R.id.background) {
                                view.background = null
                            } else {
                                val mutated = background.mutate() as GradientDrawable
                                mutated.setColor(ColorStateList.valueOf(newColor(baseWidgetColor, alpha(defaultColor))))
                                view.background = mutated
                            }
                        }
                    } else if (background is ColorDrawable) {
                        val color = background.color
                        if (color != -1 && color != 0) {
                            if (view.id == R.id.background) {
                                view.background = null
                            } else {
                                view.setBackgroundColor(newColor(baseWidgetColor, alpha(color)))
                            }
                        }
                    }
                }
                for (i in 0 until view.childCount) {
                    traverseInner(view.getChildAt(i), baseWidgetColor, updateColor, showShadow)
                }
            }
            is TextView -> {
                if (updateColor && !isKeepColor) {
                    view.setTextColor(newColor(baseWidgetColor, 255))
                    view.setShadowLayer(if (showShadow) MONOTONE_TEXT_SHADOW_RADIUS else 0.0f, 0.0f, 0.0f, MONOTONE_TEXT_SHADOW_COLOR)
                }
                if (view.textSizeUnit == TypedValue.COMPLEX_UNIT_SP && view.tag is String) {
                    val tag = view.tag as String
                    try {
                        tag.split(";").forEach {
                            if (it.isNotEmpty()) {
                                val pairs = it.split("=")
                                if (pairs.size == 2 && pairs[0] == "maxFontScale") {
                                    applyMaxFontScale(view, pairs[1].toFloat())
                                }
                            }
                        }
                    } catch (e: Exception) {
                        SeslAppWidgetLog.e(TAG, "traverse: ex=$e")
                    }
                }
            }
            is ImageView -> {
                if (updateColor && !isKeepColor) {
                    if (view is ImageButton) {
                        val background = view.background
                        if (background is GradientDrawable) {
                            val color = background.color
                            val defaultColor = color?.defaultColor ?: -1
                            if (defaultColor != -1) {
                                background.setTint(newColor(baseWidgetColor, alpha(defaultColor)))
                                background.setTintBlendMode(BlendMode.SRC)
                            }
                        } else if (background is ColorDrawable) {
                            val color = background.color
                            view.setBackgroundColor(newColor(baseWidgetColor, alpha(color)))
                        }
                    }
                    if (view.imageTintList != null) {
                        view.imageTintList = null
                    }
                    val drawable = view.drawable
                    when (drawable) {
                        is VectorDrawable -> {
                            setPathColor(drawable, "all", baseWidgetColor)
                            view.invalidate()
                        }
                        is AnimatedVectorDrawable -> {
                            setPathColor(drawable, baseWidgetColor)
                            view.invalidate()
                        }
                        is BitmapDrawable -> {
                            view.setColorFilter(newColor(baseWidgetColor, 255))
                            view.invalidate()
                        }
                        else -> {
                            val colorFilter = view.colorFilter
                            if (colorFilter is PorterDuffColorFilter) {
                                view.setColorFilter(newColor(baseWidgetColor, alpha(getColor(colorFilter))))
                                view.invalidate()
                            } else if (colorFilter != null) {
                                SeslAppWidgetLog.d(TAG, "traverse: image colorFilter=$colorFilter")
                            }
                        }
                    }
                }
            }
            is ProgressBar -> {
                if (updateColor && !isKeepColor) {
                    val progressTintList = view.progressTintList
                    val defaultColor = progressTintList?.defaultColor ?: 872415231
                    view.progressTintList = ColorStateList.valueOf(newColor(baseWidgetColor, (defaultColor shr 24) and 255))
                    val progressBackgroundTintList = view.progressBackgroundTintList
                    val backgroundDefaultColor = progressBackgroundTintList?.defaultColor ?: 452984831
                    view.progressBackgroundTintList = ColorStateList.valueOf(newColor(baseWidgetColor, (backgroundDefaultColor shr 24) and 255))
                    view.invalidate()
                }
            }
        }
    }

    private fun alpha(i: Int): Int = (i shr 24) and 255

    private fun newColor(i: Int, i5: Int): Int = (i5 shl 24) or (i and 0x00FFFFFF)

    private fun applyMaxFontScale(textView: TextView, maxFontScale: Float) {
        if (Build.VERSION.SDK_INT < 30) return
        val resources = textView.context.resources
        val currFontScale = resources.configuration.fontScale
        SeslAppWidgetLog.d(TAG, "applyMaxFontScale: currFontScale=$currFontScale maxFontScale=$maxFontScale")
        if (maxFontScale != 0.0f && currFontScale > maxFontScale && textView.textSizeUnit == TypedValue.COMPLEX_UNIT_SP) {
            val displayMetrics = resources.displayMetrics
            val originTextSize = if (Build.VERSION.SDK_INT < 34) {
                textView.textSize / displayMetrics.scaledDensity
            } else {
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textView.textSize, displayMetrics)
            }
            SeslAppWidgetLog.i(TAG, "applyMaxFontScale: originTextSize=$originTextSize currTextSize=${textView.textSize} scaledDensity=${displayMetrics.scaledDensity}")
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, originTextSize * maxFontScale)
        }
    }

    private fun getColor(porterDuffColorFilter: PorterDuffColorFilter): Int {
        return try {
            val method = PorterDuffColorFilter::class.java.getDeclaredMethod("hidden_getColor")
            method.isAccessible = true
            method.invoke(porterDuffColorFilter) as Int
        } catch (e: Exception) {
            -1
        }
    }

    private fun setMonotoneColor(listView: ListView, color: Int) {
        try {
            val method = listView.javaClass.getDeclaredMethod("semSetMonotoneColor", Int::class.javaPrimitiveType)
            method.isAccessible = true
            method.invoke(listView, color)
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun setPathColor(vectorDrawable: VectorDrawable, key: String, color: Int) {
        try {
            val method = VectorDrawable::class.java.getDeclaredMethod("setPathColor", String::class.java, Int::class.javaPrimitiveType)
            method.isAccessible = true
            method.invoke(vectorDrawable, key, color)
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun setPathColor(animatedVectorDrawable: AnimatedVectorDrawable, color: Int) {
        try {
            val method = AnimatedVectorDrawable::class.java.getDeclaredMethod("hidden_semSetPathColor", Int::class.javaPrimitiveType)
            method.isAccessible = true
            method.invoke(animatedVectorDrawable, color)
        } catch (e: Exception) {
            // ignore
        }
    }
}
