package androidx.picker.controller.order

import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.ViewData
import java.text.Collator
import java.util.Locale


/**
 * An [Order] implementation that compares [ViewData] objects based on their labels,
 * using a specified strength level for the [Collator].
 *
 * This class is typically used to sort lists of applications or other items that have
 * display labels, allowing for locale-sensitive and strength-aware sorting.
 *
 * @param newStrength The strength level to be used by the [Collator] for comparison.
 *                    This determines the sensitivity of the comparison (e.g., ignoring case, accents).
 *                    Valid values are constants defined in [Collator], such as [Collator.PRIMARY],
 *                    [Collator.SECONDARY], [Collator.TERTIARY], or [Collator.IDENTICAL].
 */
class StrengthOrder(private val newStrength: Int) : Order<ViewData> {

    override fun compare(first: ViewData, second: ViewData): Int {
        var firstLabel: String? = null
        var secondLabel: String? = null
        val firstAppInfo = first as? AppInfoViewData
        var secondLabelOrDefault: String? = ""
        if (firstAppInfo == null || (firstAppInfo.label.also { firstLabel = it }) == null) {
            firstLabel = ""
        }
        val secondAppInfo = second as? AppInfoViewData
        if (secondAppInfo != null && (secondAppInfo.label.also { secondLabel = it }) != null) {
            secondLabelOrDefault = secondLabel
        }
        val collator = Collator.getInstance(Locale.getDefault())
        collator.setStrength(this.newStrength)
        return collator.compare(firstLabel, secondLabelOrDefault)
    }
}