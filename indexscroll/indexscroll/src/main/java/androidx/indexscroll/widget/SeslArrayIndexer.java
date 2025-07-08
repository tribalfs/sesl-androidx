/*
 * Copyright 2024 The Android Open Source Project
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

package androidx.indexscroll.widget;

import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

import android.os.Bundle;

import androidx.annotation.RestrictTo;

import java.util.List;

/*
 * Original code by Samsung, all rights reserved to the original author.
 */

/**
 * A helper class for {@link SeslIndexScrollView} that alphabetically sorts and sections a list of items.
 *
 * <p>This class is designed to work with a {@link List} of {@link String} objects.
 * It provides the necessary methods for the {@link SeslIndexScrollView} to display an indexed
 * scrollbar and allow users to quickly navigate through the list.
 *
 * <p>Usage:
 * <pre>
 * {@code
 * List<String> myData = new ArrayList<>();
 * // Populate myData with string items
 *
 * CharSequence indexCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#"; // Or your custom index characters
 * SeslArrayIndexer arrayIndexer = new SeslArrayIndexer(myData, indexCharacters);
 *
 * // Pass the arrayIndexer to your SeslIndexScrollView or its adapter
 * }
 * </pre>
 */
public class SeslArrayIndexer extends SeslAbsIndexer {
    private final String TAG = "SeslArrayIndexer";
    private final boolean DEBUG = false;
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    protected @NonNull List<String> mData;

    public SeslArrayIndexer(@NonNull List<String> listData, @NonNull CharSequence indexCharacters) {
        super(indexCharacters);
        mData = listData;
    }

    @Override
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    protected int getItemCount() {
        return mData.size();
    }

    @Override
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    protected @NonNull String getItemAt(int pos) {
        return mData.get(pos);
    }

    @Override
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    protected @Nullable Bundle getBundle() {
        return null;
    }

    @Override
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    protected boolean isDataToBeIndexedAvailable() {
        return getItemCount() > 0;
    }
}
