package androidx.picker.widget

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.picker.R
import androidx.picker.adapter.AbsAdapter
import androidx.picker.adapter.HeaderFooterAdapter
import androidx.picker.adapter.ListAdapter
import androidx.picker.common.log.debug
import androidx.picker.common.log.info
import androidx.picker.decorator.ListDividerItemDecoration
import androidx.picker.decorator.ListSpacingItemDecoration
import androidx.picker.decorator.RoundedCornerDecoration
import androidx.picker.features.composable.ComposableStrategy
import androidx.picker.features.composable.DefaultComposableStrategy
import androidx.picker.helper.SeslAppInfoDataHelper
import androidx.picker.model.AppData.ListCheckBoxAppDataBuilder
import androidx.picker.model.GroupTitleStyleData
import androidx.recyclerview.widget.LinearLayoutManager

/**
 * SeslAppPickerListView is a view for selecting applications, displayed as a list.
 *
 * It extends [SeslAppPickerView] and uses a [LinearLayoutManager] to arrange items.
 *
 * The appearance and behavior of items can be customized using a [ComposableStrategy].
 * If no custom strategy is provided via the `customStrategyClass` attribute,
 * it defaults to [DefaultComposableStrategy].
 *
 * This view also applies specific item decorations:
 * - [ListSpacingItemDecoration] for spacing between items.
 * - [ListDividerItemDecoration] for dividers between items.
 * - [RoundedCornerDecoration] for rounded corners, coordinated with the header/footer.
 *
 * @param context The Context the view is running in, through which it can
 *        access the current theme, resources, etc.
 * @param attrs The attributes of the XML tag that is inflating the view.
 * @param defStyleAttr An attribute in the current theme that contains a
 *        reference to a style resource that supplies default values for
 *        the view. Can be 0 to not look for defaults.
 */
class SeslAppPickerListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SeslAppPickerView(context, attrs, defStyleAttr) {

    @VisibleForTesting
    var composableStrategy: ComposableStrategy
        private set

    init {
        viewType = TYPE_LIST
        var strategyClassName: String? = null
        context.withStyledAttributes(attrs, R.styleable.SeslAppPickerListView, defStyleAttr) {
            strategyClassName = getString(R.styleable.SeslAppPickerListView_customStrategyClass)
        }

        composableStrategy = if (strategyClassName == null) {
            DefaultComposableStrategy()
        } else {
            try {
                val clazz = Class.forName(strategyClassName)
                val ctor = clazz.getConstructor()
                ctor.newInstance() as ComposableStrategy
            } catch (e: Throwable) {
                info("used DefaultComposableStrategy")
                debug(e)
                DefaultComposableStrategy()
            }
        }

        debug("use ComposableStrategy: $composableStrategy")
        initialize()

        if (isInEditMode) {
            val packages = SeslAppInfoDataHelper(context, ListCheckBoxAppDataBuilder::class.java).getPackages()
            submitList(packages)
        }
    }

    /**
     * Retrieves an adapter for the app picker.
     *
     * This method creates and configures a [ListAdapter] with the current context and
     * the defined [ComposableStrategy]. It also sets `hasStableIds` to true for
     * better performance with `RecyclerView`.
     *
     * @param viewType An integer representing the type of view for which the adapter is needed.
     *                 Currently, this parameter is not used in the implementation but is part of
     *                 the overridden method signature.
     * @return An instance of [AbsAdapter] (specifically, a [ListAdapter]) configured for the
     *         app picker.
     */
    override fun getAppPickerAdapter(@AppPickerType viewType: Int): AbsAdapter {
        val listAdapter = ListAdapter(context, composableStrategy)
        listAdapter.setHasStableIds(true)
        return listAdapter
    }

    /**
     * Retrieves the LayoutManager for the RecyclerView.
     * This implementation always returns a [LinearLayoutManager] for a vertical list.
     *
     * @param viewType The type of the view, not used in this implementation.
     * @return A [LinearLayoutManager] instance.
     */
    override fun getLayoutManager(@AppPickerType viewType: Int): LayoutManager = LinearLayoutManager(context)

    override fun setItemDecoration(i: Int, headerFooterAdapter: HeaderFooterAdapter) {
        super.setItemDecoration(i, headerFooterAdapter)
        addItemDecoration(ListSpacingItemDecoration(context))
        addItemDecoration(ListDividerItemDecoration(context))
        addItemDecoration(
            RoundedCornerDecoration(
                context,
                headerFooterAdapter,
                ContextCompat.getColor(context, GroupTitleStyleData.SOLID.backgroundColorId)
            )
        )
    }
}