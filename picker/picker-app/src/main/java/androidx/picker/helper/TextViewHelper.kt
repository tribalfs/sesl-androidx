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

package androidx.picker.helper

import android.text.SpannableString
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.picker.features.search.InitialSearchUtils
import androidx.reflect.text.SeslTextUtilsReflector
import java.util.Locale
import java.util.StringTokenizer
import kotlin.math.min

private const val FONT_SCALE_LARGE = 1.3f
private const val MAX_OFFSET = 200

/**
 * Limits the font size of the TextView if the system font scale is larger than a predefined threshold.
 *
 * This function first resets the TextView's text size to 0 and then sets it again,
 * ensuring the size is adjusted based on the [limitFontScale] function. This is often used
 * to prevent text from becoming excessively large due to accessibility settings, maintaining
 * a reasonable layout.
 */
fun TextView.limitFontLarge() {
    setTextSize(TypedValue.COMPLEX_UNIT_PX, limitFontScale(this, textSize))
}

private fun limitFontScale(textView: TextView, pixel: Float): Float {
    val fontScale = textView.resources.configuration.fontScale
    return if (fontScale <= FONT_SCALE_LARGE) pixel else (pixel / fontScale) * FONT_SCALE_LARGE
}

/**
 * Highlights occurrences of a search string within the TextView's text.
 *
 * This function iterates through tokens in the `search` string and applies a
 * [ForegroundColorSpan] and a bold [StyleSpan] to matching segments in the
 * TextView's current text.
 *
 * If the `search` string is empty, the TextView's text is reset to its original string form
 * without any highlighting.
 *
 * The matching is case-insensitive and considers locale-specific lowercase transformations.
 * It also leverages `SeslTextUtilsReflector.semGetPrefixCharForSpan` to potentially adjust
 * the search token based on prefix characters for better matching.
 *
 * There's a `MAX_OFFSET` limit (200) to prevent excessive processing if a token appears
 * too many times.
 *
 * @param search The string to search for within the TextView's text.
 * @param foregroundColor The color to use for highlighting the matched text.
 *                        This should be a color integer (e.g., `Color.RED`).
 */
fun TextView.setHighLightText(search: String, @ColorInt foregroundColor: Int) {
    if (search.isEmpty()) {
        text = text.toString()
        return
    }
    val originalText = text.toString()
    val spannableString = SpannableString(originalText)
    val stringTokenizer = StringTokenizer(search)
    while (stringTokenizer.hasMoreTokens()) {
        var token = stringTokenizer.nextToken()
        var str = originalText
        var offset = 0
        while (true) {
            val paint: TextPaint = paint
            val charArray = token.toCharArray()
            val semPrefix = SeslTextUtilsReflector.semGetPrefixCharForSpan(paint, str, charArray)
            if (semPrefix != null && semPrefix.isNotEmpty()) {
                token = semPrefix.joinToString("")
            }
            val locale = Locale.getDefault()
            val lowerStr = str.lowercase(locale)
            val lowerToken = token.lowercase(locale)
            val index = if (str.length == lowerStr.length) {
                InitialSearchUtils.getMatchedStringOffset(lowerStr, lowerToken)
            } else {
                str.indexOf(token)
            }
            if (index < 0) break
            val length = token.length + index
            val start = index + offset
            offset += length
            val end = min(offset, spannableString.length)
            spannableString.setSpan(ForegroundColorSpan(foregroundColor), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(StyleSpan(android.graphics.Typeface.BOLD), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            str = str.substring(min(length, str.length))
            val lowerStr2 = str.lowercase(locale)
            if (!lowerStr2.contains(lowerToken) || offset >= MAX_OFFSET) break
        }
    }
    text = spannableString
}