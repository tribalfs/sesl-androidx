package androidx.picker.model

import androidx.annotation.ColorRes
import androidx.picker.R


/**
 * Represents the styling data for a group title.
 *
 * This enum class defines different styles for group titles, specifying the background and
 * text colors for each style.
 *
 * @property backgroundColorId The resource ID of the background color.
 * @property textColorId The resource ID of the text color.
 */
enum class GroupTitleStyleData(
    @JvmField @param:ColorRes val backgroundColorId: Int,
    @JvmField @param:ColorRes val textColorId: Int
) {
    /** This style uses a solid background color and a contrasting text color for group titles. */
    SOLID(
        R.color.picker_app_list_subheader_background_color,
        R.color.picker_app_list_subheader_text_color
    ),

    /** This style uses a transparent background and a specific text color.*/
    TRANSPARENT(
        R.color.picker_app_list_transparent_subheader_background_color,
        R.color.picker_app_list_transparent_subheader_text_color
    )
}