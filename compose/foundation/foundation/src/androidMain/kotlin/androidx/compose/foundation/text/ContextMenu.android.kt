/*
 * Copyright 2021 The Android Open Source Project
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

package androidx.compose.foundation.text

import androidx.compose.foundation.contextmenu.ContextMenuState
import androidx.compose.foundation.contextmenu.close
import androidx.compose.foundation.text.selection.SelectionManager
import androidx.compose.foundation.text.selection.TextFieldSelectionManager
import androidx.compose.foundation.text.selection.contextMenuBuilder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource

@Composable
internal actual fun ContextMenuArea(
    manager: TextFieldSelectionManager,
    content: @Composable () -> Unit
) {
    val state = remember { ContextMenuState() }
    androidx.compose.foundation.contextmenu.ContextMenuArea(
        state = state,
        onDismiss = { state.close() },
        contextMenuBuilderBlock = manager.contextMenuBuilder(state),
        enabled = manager.enabled,
        content = content,
    )
}

// TODO (b/269341173) remove inline once this is non-trivial
@Composable
internal actual inline fun ContextMenuArea(
    manager: SelectionManager,
    content: @Composable () -> Unit
) {
    content()
}

/**
 * The default text context menu items.
 *
 * @param stringId The android [android.R.string] id for the label of this item
 */
internal enum class TextContextMenuItems(private val stringId: Int) {
    Cut(android.R.string.cut),
    Copy(android.R.string.copy),
    Paste(android.R.string.paste),
    SelectAll(android.R.string.selectAll);

    @ReadOnlyComposable
    @Composable
    fun resolvedString(): String = stringResource(stringId)
}
