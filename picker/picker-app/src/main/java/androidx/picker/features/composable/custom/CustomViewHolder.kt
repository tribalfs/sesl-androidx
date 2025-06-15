package androidx.picker.features.composable.custom

import android.view.View
import androidx.annotation.Keep
import androidx.picker.features.composable.ComposableViewHolder
import androidx.picker.model.AppInfoData
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.ViewData

/**
 * Abstract class that extends [ComposableViewHolder] and provides a way to bind [AppInfoData]
 * to the view.
 *
 * @param frameView The root view of the ViewHolder.
 * @see CustomStrategy
 * @see CustomFrame
 */
@Keep
abstract class CustomViewHolder(frameView: View) : ComposableViewHolder(frameView) {

    /**
     * Binds the application data to the ViewHolder.
     *
     * This method is called to populate the views within the ViewHolder with the data
     * from the provided [AppInfoData] object.
     *
     * @param appData The application data to bind.
     */
    abstract fun bindData(appData: AppInfoData)

    /**
     * Binds the given [ViewData] to this ViewHolder.
     *
     * If the [ViewData] is an instance of [AppInfoViewData], it extracts the [AppInfoData]
     * and calls the abstract [bindData] method with it.
     *
     * @param viewData The [ViewData] to bind.
     */
    override fun bindData(viewData: ViewData) {
        if (viewData is AppInfoViewData) {
            bindData(viewData.appInfoData)
        }
    }
}