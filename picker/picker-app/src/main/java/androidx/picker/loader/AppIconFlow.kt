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

package androidx.picker.loader

import android.graphics.drawable.Drawable
import androidx.picker.features.observable.UpdateMutableState
import androidx.picker.model.AppInfoData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

/**
 * A [Flow] that provides an application icon.
 *
 * It first attempts to retrieve the icon from the [base] state. If the icon is not available in the
 * [base] state, it falls back to the [defaultIconFlow]. Once the icon is retrieved from the
 * [defaultIconFlow], it updates the [base] state with the new icon.
 *
 * @property base The [UpdateMutableState] holding the application information ([AppInfoData]), which may include
 *   the icon.
 * @property defaultIconFlow A [Flow] that provides a default icon if it's not available in [base].
 */
class AppIconFlow(
    val base: UpdateMutableState<AppInfoData, Drawable>,
    private val defaultIconFlow: Flow<Drawable?>
) : Flow<Drawable?> {

    override suspend fun collect(collector: FlowCollector<Drawable?>) {
        val icon = base.getValue(null, AppIconFlow::base)
        if (icon != null) {
            collector.emit(icon)
        } else {
            // Fallback to default icon flow and update base when received
            defaultIconFlow.collect { drawable ->
                base.setValue(null, AppIconFlow::base, drawable)
                collector.emit(drawable)
            }
        }
    }
}