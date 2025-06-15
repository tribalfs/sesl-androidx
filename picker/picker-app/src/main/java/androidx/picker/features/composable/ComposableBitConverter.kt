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

package androidx.picker.features.composable

import androidx.annotation.VisibleForTesting
import kotlin.math.ceil
import kotlin.math.log2

/**
 * A utility class for converting between [ComposableType] and an integer representation using bitwise
 * operations. This is used to efficiently represent different combinations of [ComposableFrame]s
 * as view types in a RecyclerView.
 *
 * Each [ComposableFrame] (Left, Icon, Title, Widget) is assigned a range of bits within the
 * integer. The number of bits allocated for each frame type depends on the number of possible
 * [ComposableFrame]s defined in the [ComposableStrategy].
 *
 * For example, if there are:
 * - 3 possible Left frames
 * - 5 possible Icon frames
 * - 2 possible Title frames
 * - 4 possible Widget frames
 *
 * The bit allocation would be:
 * - Left: `ceil(log2(3 + 1))` = 2 bits (can represent 0-3, where 0 means no Left frame)
 * - Icon: `ceil(log2(5 + 1))` = 3 bits (can represent 0-7, where 0 means no Icon frame)
 * - Title: `ceil(log2(2 + 1))` = 2 bits (can represent 0-3, where 0 means no Title frame)
 * - Widget: `ceil(log2(4 + 1))` = 3 bits (can represent 0-7, where 0 means no Widget frame)
 *
 * The final integer (view type) would be constructed by shifting and ORing these bits together.
 *
 * This class provides methods to:
 * - `encodeAsBits(composableType: ComposableType)`: Convert a [ComposableType] to its integer
 *   representation.
 * - `decodeAsType(viewType: Int)`: Convert an integer (view type) back to a [ComposableType].
 * - `getMaxBit()`: Get the maximum possible integer value that can be encoded.
 *
 * Caching is used to optimize repeated conversions.
 *
 * @param frameStrategy The [ComposableStrategy] defining the available [ComposableFrame]s for
 *   each slot.
 */
class ComposableBitConverter(
    frameStrategy: ComposableStrategy
) {
    companion object {
        const val BIT_NULL = 0
        const val ICON = 1
        const val LEFT = 0
        const val OFFSET_FOR_ZERO_AS_NULL = 1
        const val TITLE = 2
        const val WIDGET = 3

        @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
        @Retention(AnnotationRetention.RUNTIME)
        annotation class FrameSlot
    }

    private val cachedMapByComposableType = LinkedHashMap<ComposableType, Int>()
    private val cachedMapByViewType = LinkedHashMap<Int, ComposableType>()
    private val frameInfo: Array<List<ComposableFrame>> = arrayOf(
        frameStrategy.leftFrameList,
        frameStrategy.iconFrameList,
        frameStrategy.titleFrameList,
        frameStrategy.widgetFrameList
    )
    private val rangeList: Array<IntRange>
    internal val maxBit: Int

    init {

        val ranges = mutableListOf<IntRange>()
        var bitOffset = 0
        for (frames in frameInfo) {
            val bits = ceil(log2((frames.size + 1).toDouble())).toInt()
            val range = bitOffset until (bitOffset + bits)
            ranges.add(range)
            bitOffset += bits
        }
        rangeList = ranges.toTypedArray()
        maxBit = (1 shl bitOffset) - 1
    }

    @VisibleForTesting
    fun readBit(@FrameSlot position: Int, data: Int): Int {
        return getBitWithRange(data, rangeList[position])
    }

    private fun getBitWithRange(data: Int, range: IntRange): Int {
        val mask = ((1 shl (range.last + 1)) - (1 shl range.first))
        return (data and mask) shr range.first
    }

    private fun decodeAsFrame(@FrameSlot position: Int, data: Int): ComposableFrame? {
        val readBit = readBit(position, data)
        if (readBit == 0) return null
        return frameInfo[position][readBit - 1]
    }

    fun decodeAsType(viewType: Int): ComposableType {
        cachedMapByViewType[viewType]?.let { return it }
        val type = ComposableTypeImpl(
            decodeAsFrame(LEFT, viewType),
            decodeAsFrame(ICON, viewType),
            decodeAsFrame(TITLE, viewType),
            decodeAsFrame(WIDGET, viewType)
        )
        cachedMapByViewType[viewType] = type
        return type
    }

    fun encodeAsBits(composableType: ComposableType): Int {
        cachedMapByComposableType[composableType]?.let { return it }
        var result = 0
        val pairs = listOf(
            composableType.leftFrame to LEFT,
            composableType.iconFrame to ICON,
            composableType.titleFrame to TITLE,
            composableType.widgetFrame to WIDGET
        )
        for ((frame, position) in pairs) {
            if (frame != null) {
                result = result or encodeAsBits(position, frame)
            }
        }
        cachedMapByComposableType[composableType] = result
        return result
    }

    @VisibleForTesting
    private fun encodeAsBits(@FrameSlot position: Int, frame: ComposableFrame): Int {
        val index = frameInfo[position].indexOf(frame) + 1
        if (index == 0) return 0
        return index shl rangeList[position].first
    }

    fun getMaxBit(): Int = maxBit
}