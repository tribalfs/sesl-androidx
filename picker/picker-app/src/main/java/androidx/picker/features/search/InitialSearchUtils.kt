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

package androidx.picker.features.search

import android.content.Context
import android.database.CharArrayBuffer
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.picker.model.AppInfo
import java.util.regex.Pattern
import java.util.regex.Matcher

object InitialSearchUtils {
    private const val APPLICATION_ID_BIXBY = "com.samsung.android.bixby.service.bixbysearch"
    private const val APPLICATION_ID_SCS = "com.samsung.android.scs"
    private const val HANGEUL_CODE_CHOSUNG_HIEUH = 4370
    private const val HANGEUL_CODE_CHOSUNG_KIYEOK = 4352
    private const val HANGEUL_CODE_LETTER_HIEUH = 12622
    private const val HANGEUL_CODE_LETTER_KIYEOK = 12593
    private const val HANGEUL_CODE_SYLLABLE_GA = 44032
    private const val HANGEUL_CODE_SYLLABLE_HIH = 44032
    private const val INVALID_IDX = -1
    private const val KEY_COMPONENT_NAME = "componentName"
    private const val KEY_LABEL = "label"
    private const val KEY_PACKAGE_NAME = "packageName"
    private const val KEY_USER = "user"
    private const val MODULE_CATEGORY_ID = "ai"
    private const val MODULE_ID = "search"
    private const val TAG = "InitialSearchUtils"
    private val KOREAN_RANGE_PATTERN = arrayOf(
        "[\\uAC00-\\uAE4A]", "[\\uAE4C-\\uB091]", "", "[\\uB098-\\uB2E2]", "", "", "[\\uB2E4-\\uB52A]",
        "[\\uB530-\\uB775]", "[\\uB77C-\\uB9C1]", "", "", "", "", "", "", "", "[\\uB9C8-\\uBC11]",
        "[\\uBC14-\\uBE5B]", "[\\uBE60-\\uC0A5]", "", "[\\uC0AC-\\uC2F6]", "[\\uC2F8-\\uC53D]",
        "[\\uC544-\\uC78E]", "[\\uC790-\\uC9DA]", "[\\uC9DC-\\uCC27]", "[\\uCC28-\\uCE6D]",
        "[\\uCE74-\\uD0B9]", "[\\uD0C0-\\uD305]", "[\\uD30C-\\uD551]", "[\\uD558-\\uD79D]"
    )
    internal const val AUTHORITY_SCS = "com.samsung.android.scs.ai.search"
    internal const val AUTH_VERSION = "v1"
    private const val APPLICATION = "application"
    private val SCS_PROVIDER_URI: Uri = Uri.Builder().scheme("content").authority(AUTHORITY_SCS).appendPath(AUTH_VERSION).appendPath(APPLICATION).build()
    const val AUTHORITY_BIXBY = "com.samsung.android.bixby.service.bixbysearch.ai.search"
    private val BIXBY_PROVIDER_URI: Uri = Uri.Builder().scheme("content").authority(AUTHORITY_BIXBY).appendPath(AUTH_VERSION).appendPath(APPLICATION).build()

    private fun createPattern(str: String?): Pattern {
        return Pattern.compile("(" + extractPattern(str) + ")")
    }

    @JvmStatic
    fun extractPattern(str: String?): String {
        var s = str
        if (s == null || !s.matches(Regex("[0-9|a-z|A-Z|ㄱ-ㅎ|ㅏ-ㅣ|가-힣| ]*"))) {
            s = s?.let { Pattern.quote(it) } ?: ""
        }
        val length = s.length
        val sb = StringBuilder()
        var i = 0
        sb.setLength(0)
        while (true) {
            val i2 = i + 1
            val codePointAt = s.codePointAt(i)
            if (!isKoreanCharacter(codePointAt)) {
                sb.appendCodePoint(codePointAt)
            } else if (isKoreanConsonantCharacter(codePointAt)) {
                sb.append(getRegexPatternOfKoreanCharacter(codePointAt))
            } else {
                sb.appendCodePoint(codePointAt)
            }
            if (i2 >= length) {
                return sb.toString()
            }
            i = i2
        }
    }

    @JvmStatic
    fun getMatchedStringOffset(textToSearch: String, patternToMatch: String): Int {
        val textAsCharArray = toCharArrayBuffer(textToSearch, 128)
        val patternMatcher: Matcher = createPattern(patternToMatch).matcher(String(textAsCharArray.data, 0, textAsCharArray.sizeCopied))
        return if (patternMatcher.find()) patternMatcher.start() else -1
    }

    private fun getRegexPatternOfKoreanCharacter(i: Int): String {
        return KOREAN_RANGE_PATTERN[i - 12593]
    }

    @JvmStatic
    fun getSearchResultFromSCS(context: Context, str: String): List<AppInfo> {
        val i = Build.VERSION.SDK_INT
        val arrayList = ArrayList<AppInfo>()
        val bundle = Bundle()
        bundle.putString("android:query-arg-sql-selection", str)
        var query: Cursor? = null
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                query = context.contentResolver.query(
                    if (i >= 30) SCS_PROVIDER_URI else BIXBY_PROVIDER_URI,
                    null, bundle, null
                )
            }
        } catch (e: Exception) {
            Log.d(TAG, "Fail to get application query result: $e")
        }
        if (query == null) {
            query?.close()
            return arrayList
        }
        try {
            if (!query.moveToFirst()) {
                query.close()
                return arrayList
            }
            do {
                val columnIndex = query.getColumnIndex("label")
                val columnIndex2 = query.getColumnIndex(KEY_COMPONENT_NAME)
                val columnIndex3 = query.getColumnIndex("packageName")
                val columnIndex4 = query.getColumnIndex(KEY_USER)
                if (columnIndex != -1 && columnIndex2 != -1 && columnIndex3 != -1) {
                    arrayList.add(
                        AppInfo(
                            query.getString(columnIndex3),
                            query.getString(columnIndex2),
                            query.getString(columnIndex4).toInt()
                        )
                    )
                } else {
                    Log.e(
                        TAG,
                        String.format(
                            "Can't find columnIndex (%s : %d, %s : %d, %s : %d)",
                            "label", columnIndex, KEY_COMPONENT_NAME, columnIndex2, "packageName", columnIndex3
                        )
                    )
                }
            } while (query.moveToNext())
            query.close()
            return arrayList
        } finally {
            // No-op: Java finally block was empty
        }
    }

    private fun isKoreanCharacter(i: Int): Boolean {
        return (i in HANGEUL_CODE_CHOSUNG_KIYEOK..HANGEUL_CODE_CHOSUNG_HIEUH) ||
                (i in HANGEUL_CODE_LETTER_KIYEOK..HANGEUL_CODE_LETTER_HIEUH) ||
                (i in 44032..44032)
    }

    private fun isKoreanConsonantCharacter(i: Int): Boolean {
        return i in HANGEUL_CODE_LETTER_KIYEOK..HANGEUL_CODE_LETTER_HIEUH
    }

    @JvmStatic
    fun toCharArrayBuffer(str: String?, i: Int): CharArrayBuffer {
        if (i < 0) {
            return CharArrayBuffer(0)
        }
        val charArrayBuffer = CharArrayBuffer(i)
        if (str != null) {
            val cArr = charArrayBuffer.data
            if (cArr == null || cArr.size < str.length) {
                charArrayBuffer.data = str.toCharArray()
            } else {
                str.toCharArray(charArrayBuffer.data, 0, 0, str.length)
            }
            charArrayBuffer.sizeCopied = str.length
        } else {
            charArrayBuffer.sizeCopied = 0
        }
        return charArrayBuffer
    }
}