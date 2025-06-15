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

import androidx.picker.features.composable.icon.IconFrame
import androidx.picker.features.composable.left.LeftFrame
import androidx.picker.features.composable.title.TitleFrame
import androidx.picker.features.composable.widget.WidgetFrame
import androidx.picker.model.AppData.Companion.TYPE_ITEM_CHECKBOX
import androidx.picker.model.AppData.Companion.TYPE_ITEM_RADIOBUTTON
import androidx.picker.model.AppData.Companion.TYPE_ITEM_SWITCH
import androidx.picker.model.viewdata.AllAppsViewData
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.CategoryViewData
import androidx.picker.model.viewdata.ViewData

/**
 * Default concrete implementation of [ComposableStrategy].
 *
 * This class provides a default strategy for selecting the appropriate [ComposableType] based on
 * the given [ViewData]. It defines the lists of [ComposableFrame]s for different parts of the UI
 * and implements the logic to choose a [ComposableType] based on the type of [ViewData] and its
 * properties.
 *
 * @see androidx.picker.features.composable.custom.CustomStrategy
 */
open class DefaultComposableStrategy : ComposableStrategy {

    override val leftFrameList: List<ComposableFrame> = LeftFrame.entries
    override val iconFrameList: List<ComposableFrame> = IconFrame.entries
    override val titleFrameList: List<ComposableFrame> = TitleFrame.entries
    override val widgetFrameList: List<ComposableFrame> = WidgetFrame.entries

    override fun selectComposableType(viewData: ViewData): ComposableType? {
        return when (viewData) {
            is AllAppsViewData -> ComposableTypeSet.AllSwitch
            is CategoryViewData -> ComposableTypeSet.CheckBoxExpander
            is AppInfoViewData -> {
                when (viewData.itemType) {
                    TYPE_ITEM_CHECKBOX -> {
                        if (viewData.actionIcon != null) ComposableTypeSet.CheckBoxAction else ComposableTypeSet.CheckBox
                    }
                    TYPE_ITEM_RADIOBUTTON -> {
                        if (viewData.actionIcon != null) ComposableTypeSet.RadioAction else ComposableTypeSet.Radio
                    }
                    TYPE_ITEM_SWITCH -> ComposableTypeSet.Switch
                    else -> ComposableTypeSet.TextOnly
                }
            }
            else -> null
        }
    }
}