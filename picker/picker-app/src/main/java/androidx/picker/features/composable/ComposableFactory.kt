package androidx.picker.features.composable

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.picker.R
import androidx.picker.model.viewdata.ViewData
import java.security.InvalidParameterException
import kotlin.ranges.IntRange

/**
 * Factory for creating and managing composable views.
 *
 * This class handles the conversion between [ComposableType] and integer view types,
 * inflates composable views, and applies appropriate padding strategies.
 *
 * @property composableStrategy The strategy used to determine the [ComposableType] for a given [ViewData].
 */
class ComposableFactory(
    val composableStrategy: ComposableStrategy
) {
    private val converter: ComposableBitConverter = ComposableBitConverter(composableStrategy)
    val viewTypeRange: IntRange = 0..converter.maxBit

    /**
     * Returns the [ComposableType] for the given viewType integer.
     * Throws [InvalidParameterException] if viewType is not in the composable view type range.
     */
    fun getComposableType(viewType: Int): ComposableType {
        if (viewType in viewTypeRange) {
            return converter.decodeAsType(viewType)
        }
        throw InvalidParameterException("viewType must be in Composable View Type range $viewTypeRange")
    }

    /**
     * Returns the item type integer for the given [ViewData], or null if not composable.
     */
    fun getItemType(viewData: ViewData): Int? {
        val composableType = composableStrategy.selectComposableType(viewData)
        return composableType?.let { getItemType(it) }
    }

    /**
     * Inflates a composable view for the given viewType and applies the appropriate padding strategy.
     */
    fun inflateComposableView(parent: ViewGroup, viewType: Int): View {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.picker_app_composable_row_item_view, parent, false)
        val paddingStrategy = PaddingStrategy.get(getComposableType(viewType))
        paddingStrategy.applyToView(itemView)
        return itemView
    }

    /**
     * Encodes a [ComposableType] as an integer item type.
     */
    private fun getItemType(composableType: ComposableType): Int {
        return converter.encodeAsBits(composableType)
    }

    /**
     * Padding strategies for composable views.
     */
    enum class PaddingStrategy(
        val start: Int,
        val top: Int,
        val end: Int,
        val bottom: Int
    ) {
        IconFramePadding(
            R.dimen.picker_app_list_icon_padding_start,
            0,
            R.dimen.picker_app_list_padding_end,
            0
        ),
        LeftFramePadding(
            R.dimen.picker_app_list_radio_padding_start,
            0,
            R.dimen.picker_app_list_padding_end,
            0
        ),
        TitleFramePadding(
            R.dimen.picker_app_list_text_only_padding_start,
            0,
            R.dimen.picker_app_list_padding_end,
            0
        );

        /**
         * Applies the padding strategy to the given [view], resolving dimensions from resources.
         */
        fun applyToView(view: View) {
            val resources: Resources = view.context.resources
            fun getDimenOrZero(resId: Int): Int =
                if (resId == 0) 0 else resources.getDimensionPixelOffset(resId)
            view.setPaddingRelative(
                getDimenOrZero(start),
                getDimenOrZero(top),
                getDimenOrZero(end),
                getDimenOrZero(bottom)
            )
        }

        companion object {
            /**
             * Returns the appropriate [PaddingStrategy] for the given [ComposableType].
             */
            fun get(composableType: ComposableType): PaddingStrategy {
                return when {
                    composableType.leftFrame != null -> LeftFramePadding
                    composableType.iconFrame != null -> IconFramePadding
                    else -> TitleFramePadding
                }
            }
        }
    }
}