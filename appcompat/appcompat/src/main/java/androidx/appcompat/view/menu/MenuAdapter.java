/*
 * Copyright (C) 2016 The Android Open Source Project
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
package androidx.appcompat.view.menu;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.RestrictTo;
import androidx.appcompat.R;

import java.util.ArrayList;

/**
 */
@RestrictTo(LIBRARY_GROUP_PREFIX)
public class MenuAdapter extends BaseAdapter {
    MenuBuilder mAdapterMenu;

    private int mExpandedIndex = -1;

    private boolean mForceShowIcon;
    private final boolean mOverflowOnly;
    private final LayoutInflater mInflater;
    private final int mItemLayoutRes;

    public int mInitPaddingTop;
    public int mInitPaddingBottom;

    public MenuAdapter(MenuBuilder menu, LayoutInflater inflater, boolean overflowOnly,
            int itemLayoutRes) {
        mOverflowOnly = overflowOnly;
        mInflater = inflater;
        mAdapterMenu = menu;
        mItemLayoutRes = itemLayoutRes;
        findExpandedIndex();
    }

    public boolean getForceShowIcon() {
        return mForceShowIcon;
    }

    public void setForceShowIcon(boolean forceShow) {
        mForceShowIcon = forceShow;
    }

    @Override
    public int getCount() {
        ArrayList<MenuItemImpl> items = mOverflowOnly ?
                mAdapterMenu.getNonActionItems() : mAdapterMenu.getVisibleItems();
        if (mExpandedIndex < 0) {
            return items.size();
        }
        return items.size() - 1;
    }

    public MenuBuilder getAdapterMenu() {
        return mAdapterMenu;
    }

    @Override
    public MenuItemImpl getItem(int position) {
        ArrayList<MenuItemImpl> items = mOverflowOnly ?
                mAdapterMenu.getNonActionItems() : mAdapterMenu.getVisibleItems();
        if (mExpandedIndex >= 0 && position >= mExpandedIndex) {
            position++;
        }
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        // Since a menu item's ID is optional, we'll use the position as an
        // ID for the item in the AdapterView
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(mItemLayoutRes, parent, false);
            mInitPaddingTop = convertView.getPaddingTop();
            mInitPaddingBottom = convertView.getPaddingBottom();
        }

        final int currGroupId = getItem(position).getGroupId();
        final int prevGroupId =
                position - 1 >= 0 ? getItem(position - 1).getGroupId() : currGroupId;
        // Show a divider if adjacent items are in different groups.
        ((ListMenuItemView) convertView)
                .setGroupDividerEnabled(mAdapterMenu.isGroupDividerEnabled()
                        && (currGroupId != prevGroupId));

        MenuView.ItemView itemView = (MenuView.ItemView) convertView;
        if (mForceShowIcon) {
            ((ListMenuItemView) convertView).setForceShowIcon(true);
        }
        itemView.initialize(getItem(position), 0);

        final int firstLastItemPadding = convertView.getResources()
                .getDimensionPixelSize(R.dimen.sesl_popup_menu_first_last_item_vertical_edge_padding);

        int paddingTop = mInitPaddingTop + firstLastItemPadding;
        int paddingBottom = mInitPaddingBottom + firstLastItemPadding;
        int paddingLeft = convertView.getPaddingLeft();
        if (position != 0) {
            paddingTop = mInitPaddingTop;
        }
        int paddingRight = convertView.getPaddingRight();
        if (position != getCount() - 1) {
            paddingBottom = mInitPaddingBottom;
        }
        convertView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

        return convertView;
    }

    void findExpandedIndex() {
        final MenuItemImpl expandedItem = mAdapterMenu.getExpandedItem();
        if (expandedItem != null) {
            final ArrayList<MenuItemImpl> items = mAdapterMenu.getNonActionItems();
            final int count = items.size();
            for (int i = 0; i < count; i++) {
                final MenuItemImpl item = items.get(i);
                if (item == expandedItem) {
                    mExpandedIndex = i;
                    return;
                }
            }
        }
        mExpandedIndex = -1;
    }

    @Override
    public void notifyDataSetChanged() {
        findExpandedIndex();
        super.notifyDataSetChanged();
    }
}