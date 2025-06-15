package androidx.picker.helper

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import kotlin.jvm.internal.Intrinsics


/**
 * A helper class for rounding the corners of a BitmapDrawable.
 *
 * This class extends BitmapDrawable and overrides the draw method to clip the drawable with a
 * rounded rectangle.
 *
 * Example usage:
 * ```
 * val bitmap = BitmapFactory.decodeResource(resources, R.drawable.my_image)
 * val roundedDrawable = SeslRoundedLayerDrawable(resources, bitmap)
 * imageView.setImageDrawable(roundedDrawable)
 * ```
 *
 * @param res The resources object.
 * @param bitmap The bitmap to be drawn.
 */
class SeslRoundedLayerDrawable(res: Resources, bitmap: Bitmap) : BitmapDrawable(res, bitmap)