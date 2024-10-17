/*
 * Copyright 2018 The Android Open Source Project
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

package androidx.core.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.BaseInstrumentationTestCase;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;

import androidx.core.graphics.Insets;
import androidx.core.test.R;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
@MediumTest
public class ViewGroupCompatTest extends BaseInstrumentationTestCase<ViewCompatActivity> {

    private ViewGroup mViewGroup;

    public ViewGroupCompatTest() {
        super(ViewCompatActivity.class);
    }

    @Before
    public void setUp() {
        final Activity activity = mActivityTestRule.getActivity();
        mViewGroup = activity.findViewById(R.id.container);
    }

    @Test
    public void isTransitionGroup() {
        assertFalse(ViewGroupCompat.isTransitionGroup(mViewGroup));
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                mViewGroup.setBackground(new ColorDrawable(Color.GRAY));
            }
        });
        assertTrue(ViewGroupCompat.isTransitionGroup(mViewGroup));
    }

    @Test
    public void setTransitionGroup() {
        assertFalse(ViewGroupCompat.isTransitionGroup(mViewGroup));
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                ViewGroupCompat.setTransitionGroup(mViewGroup, true);
            }
        });
        assertTrue(ViewGroupCompat.isTransitionGroup(mViewGroup));
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                ViewGroupCompat.setTransitionGroup(mViewGroup, false);
            }
        });
        assertFalse(ViewGroupCompat.isTransitionGroup(mViewGroup));
    }

    @Test
    public void installCompatInsetsDispatch() {
        final Insets[] insetsRoot = new Insets[1];
        final Insets[] insetsA = new Insets[1];
        final Insets[] insetsA1 = new Insets[1];
        final Insets[] insetsA2 = new Insets[1];
        final Insets[] insetsB = new Insets[1];
        final Insets[] insetsB1 = new Insets[1];
        final Insets[] insetsB2 = new Insets[1];

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            final Context context = mViewGroup.getContext();
            final FrameLayout viewA = new FrameLayout(context);
            final FrameLayout viewA1 = new FrameLayout(context);
            final FrameLayout viewA2 = new FrameLayout(context);
            final FrameLayout viewB = new FrameLayout(context);
            final FrameLayout viewB1 = new FrameLayout(context);
            final FrameLayout viewB2 = new FrameLayout(context) {

                @Override
                public WindowInsets onApplyWindowInsets(WindowInsets insets) {
                    insetsB2[0] = WindowInsetsCompat.toWindowInsetsCompat(insets)
                            .getSystemWindowInsets();
                    return insets;
                }
            };

            // mViewGroup --+-- viewA --+-- viewA1
            //              |           |
            //              |           +-- viewA2
            //              |
            //              +-- viewB --+-- viewB1
            //                          |
            //                          +-- viewB2
            viewA.addView(viewA1);
            viewA.addView(viewA2);
            viewB.addView(viewB1);
            viewB.addView(viewB2);
            mViewGroup.addView(viewA);
            mViewGroup.addView(viewB);

            ViewCompat.setOnApplyWindowInsetsListener(mViewGroup, (v, insets) -> {
                insetsRoot[0] = insets.getSystemWindowInsets();
                return insets;
            });
            ViewCompat.setOnApplyWindowInsetsListener(viewA, (v, insets) -> {
                insetsA[0] = insets.getSystemWindowInsets();
                return WindowInsetsCompat.CONSUMED;
            });
            ViewCompat.setOnApplyWindowInsetsListener(viewA1, (v, insets) -> {
                insetsA1[0] = insets.getSystemWindowInsets();
                return insets;
            });
            ViewCompat.setOnApplyWindowInsetsListener(viewA2, (v, insets) -> {
                insetsA2[0] = insets.getSystemWindowInsets();
                return insets;
            });
            ViewCompat.setOnApplyWindowInsetsListener(viewB, (v, insets) -> {
                insetsB[0] = insets.getSystemWindowInsets();
                return insets.replaceSystemWindowInsets(5, 5, 5, 5);
            });
            ViewCompat.setOnApplyWindowInsetsListener(viewB1, (v, insets) -> {
                insetsB1[0] = insets.getSystemWindowInsets();
                return insets;
            });

            ViewGroupCompat.installCompatInsetsDispatch(mViewGroup);

            ViewCompat.dispatchApplyWindowInsets(mViewGroup, new InsetsObtainer(context).obtain(
                    10, 10, 10, 10));
        });

        assertEquals(Insets.of(10, 10, 10, 10), insetsRoot[0]);
        assertEquals(Insets.of(10, 10, 10, 10), insetsA[0]);
        assertNull(insetsA1[0]);
        assertNull(insetsA2[0]);
        assertEquals(Insets.of(10, 10, 10, 10), insetsB[0]);
        assertEquals(Insets.of(5, 5, 5, 5), insetsB1[0]);
        assertEquals(Insets.of(5, 5, 5, 5), insetsB2[0]);
    }

    private static class InsetsObtainer extends View {

        private WindowInsets mWindowInsets = null;

        InsetsObtainer(Context context) {
            super(context);
        }

        @Override
        public WindowInsets onApplyWindowInsets(WindowInsets insets) {
            mWindowInsets = insets;
            return insets;
        }

        public WindowInsetsCompat obtain(int left, int top, int right, int bottom) {
            // Before API 28, there is no other way to create an unconsumed WindowInsets instance.
            // Calling fitSystemWindows here makes the framework dispatch a WindowInsets with the
            // given system window insets to this view.
            fitSystemWindows(new Rect(left, top, right, bottom));
            return WindowInsetsCompat.toWindowInsetsCompat(mWindowInsets);
        }
    }

}
