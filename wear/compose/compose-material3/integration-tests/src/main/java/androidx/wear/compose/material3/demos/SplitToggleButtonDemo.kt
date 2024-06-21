/*
 * Copyright 2023 The Android Open Source Project
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

package androidx.wear.compose.material3.demos

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.SplitSwitchButton
import androidx.wear.compose.material3.Text

@Composable
fun SplitSwitchButtonDemo() {
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item { ListHeader { Text("Switch") } }
        item { DemoSplitSwitchButton(enabled = true, initiallyChecked = true) }
        item { DemoSplitSwitchButton(enabled = true, initiallyChecked = false) }
        item { ListHeader { Text("Disabled Switch") } }
        item { DemoSplitSwitchButton(enabled = false, initiallyChecked = true) }
        item { DemoSplitSwitchButton(enabled = false, initiallyChecked = false) }
        item { ListHeader { Text("Multi-line") } }
        item {
            DemoSplitSwitchButton(
                enabled = true,
                initiallyChecked = true,
                primary = "8:15AM",
                secondary = "Monday"
            )
        }
        item {
            DemoSplitSwitchButton(
                enabled = true,
                initiallyChecked = true,
                primary = "Primary Label with 3 lines of content max"
            )
        }
        item {
            DemoSplitSwitchButton(
                enabled = true,
                initiallyChecked = true,
                primary = "Primary Label with 3 lines of content max",
                secondary = "Secondary label with 2 lines"
            )
        }
    }
}

@Composable
private fun DemoSplitSwitchButton(
    enabled: Boolean,
    initiallyChecked: Boolean,
    primary: String = "Primary label",
    secondary: String? = null,
) {
    var checked by remember { mutableStateOf(initiallyChecked) }
    val context = LocalContext.current
    SplitSwitchButton(
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(
                primary,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis
            )
        },
        secondaryLabel =
            secondary?.let {
                {
                    Text(
                        secondary,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        textAlign = TextAlign.Start,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
        checked = checked,
        onCheckedChange = { checked = it },
        toggleContentDescription = primary,
        onContainerClick = {
            val toastText = if (checked) "Checked" else "Not Checked"
            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
        },
        containerClickLabel = "click",
        enabled = enabled,
    )
}
