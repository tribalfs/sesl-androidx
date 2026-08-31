/*
 * Copyright 2026 The Android Open Source Project
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


import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.RestrictTo;
import androidx.appcompat.R;

import org.jspecify.annotations.NonNull;

import java.text.NumberFormat;
import java.util.Locale;

//Custom
/**
 * Wrapper FrameLayout that overlays a One UI badge layout over an {@link ActionMenuItemView}.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public class ActionMenuItemViewBadgedWrapper extends FrameLayout {

    private int defaultEndMargin;
    private NumberFormat mNumberFormat = NumberFormat.getInstance(Locale.getDefault());

    /** Creates a badged wrapper frame around the given {@link ActionMenuItemView}. */
    public ActionMenuItemViewBadgedWrapper(Context context, ActionMenuItemView menuItemView) {
        super(context);
        addView(menuItemView);
        addView(LayoutInflater.from(context).inflate(
                R.layout.sesl_action_menu_item_badge, ActionMenuItemViewBadgedWrapper.this, false));
        updateItemViewBadge(menuItemView.getItemData().getBadgeText());
    }

    private void updateItemViewBadge(String badgeText) {

        ViewGroup badgeView = (ViewGroup) getChildAt(1);

        if (badgeText == null) {
            badgeView.setVisibility(GONE);
            return;
        }

        String formattedTextBadge;
        int badgeWidth;
        int badgeHeight;
        int badgeTopMargin;
        FrameLayout.LayoutParams badgeLp = (FrameLayout.LayoutParams) badgeView.getLayoutParams();

        Resources res = getResources();
        try {
            final int badgeCount = Math.min(Integer.parseInt(badgeText), 99);
            formattedTextBadge = mNumberFormat.format(badgeCount);

            final float default_width = res.getDimension(R.dimen.sesl_badge_default_width);
            final float additionalWidth = res.getDimension(R.dimen.sesl_badge_additional_width);
            badgeWidth = (int) (default_width + (formattedTextBadge.length() * additionalWidth));
            badgeHeight = (int) (default_width + additionalWidth);
            badgeTopMargin = (int) res.getDimension(R.dimen.sesl_menu_item_number_badge_top_margin);
            defaultEndMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2,
                    getResources().getDisplayMetrics());
        } catch (NumberFormatException e) {

            //This means `badgeText` is not a number
            //We will show dot badge instead
            formattedTextBadge = "";

            final int badgeSize = (int) res.getDimension(R.dimen.sesl_menu_item_badge_size);
            badgeWidth = badgeSize;
            badgeHeight = badgeSize;
            badgeTopMargin = (int) res.getDimension(R.dimen.sesl_menu_item_badge_top_margin);
            defaultEndMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6,
                    getResources().getDisplayMetrics());
        }

        ((TextView) badgeView.getChildAt(0)).setText(formattedTextBadge);
        badgeLp.setMarginEnd(defaultEndMargin);
        badgeLp.topMargin = badgeTopMargin;
        badgeLp.width = badgeWidth;
        badgeLp.height = badgeHeight;
        badgeView.setLayoutParams(badgeLp);
        badgeView.setVisibility(VISIBLE);
    }

    /** Returns the wrapped inner {@link ActionMenuItemView}. */
    @NonNull
    public ActionMenuItemView getInnerItemView() {
        return (ActionMenuItemView) getChildAt(0);
    }

    /** Adjusts the end margin of the badge overlay. */
    public void adjustBadgeEndMargin(int additionalMargin) {
        View badgeView = getChildAt(1);
        FrameLayout.LayoutParams badgeLp = (FrameLayout.LayoutParams) badgeView.getLayoutParams();
        int adjustedEndMargin = defaultEndMargin + additionalMargin;
        if (badgeLp.getMarginEnd() == adjustedEndMargin) return;
        badgeLp.setMarginEnd(adjustedEndMargin);
    }
}
