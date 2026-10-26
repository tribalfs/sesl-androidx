/*
 * Copyright (C) 2026 The Android Open Source Project
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

package androidx.appcompat.app;

import android.content.Context;
import android.content.ContextWrapper;

import androidx.appcompat.R;
import androidx.core.os.SeslConfigurationCompat;

//sesl9
/**
 * The dialog for suggestive UI.
 */
public class SuggestiveDialog {

    public static class Builder extends AlertDialog.Builder {
        public Builder(Context context) {
            this(context, 0);
        }

        public Builder(Context context, int themeResId) {
            super(resolveSuggestiveDialogTheme(context), themeResId);
        }

        public static Context resolveSuggestiveDialogTheme(Context context) {
            ContextWrapper wrapper = new ContextWrapper(context);
            wrapper.setTheme(SeslConfigurationCompat.isNightModeActive(
                    context.getResources().getConfiguration())
                    ? R.style.Theme_AppCompat_Suggestive
                    : R.style.Theme_AppCompat_Suggestive_Light);
            return wrapper;
        }
    }
}
