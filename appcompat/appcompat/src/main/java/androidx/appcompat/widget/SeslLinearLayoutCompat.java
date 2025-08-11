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

package androidx.appcompat.widget;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_ENTER;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.R;
import androidx.appcompat.animation.SeslRecoilAnimator;
import androidx.appcompat.graphics.drawable.SeslRecoilDrawable;
import androidx.appcompat.util.SeslRoundedCorner;
import androidx.core.view.ViewCompat;

/*
 * Original code by Samsung, all rights reserved to the original author.
 */

//Added in sesl7
/**
 * Extension of {@link LinearLayoutCompat} that adds support for rounded corners and
 * recoil animation. The rounded corners can be configured using the {@code seslLayoutRoundedCorner} attribute.
 * The recoil animation effect that provides visual feedback during user interactions such as touch events or key presses.
 *
 * <p><b>Key Features:</b>
 * <ul>
 *     <li><b>Rounded Corners:</b> Supports customizable rounded corners via the
 *         {@link SeslRoundedCorner} utility. The degree of rounding
 *         can be specified through XML attributes.</li>
 *     <li><b>Recoil Animation:</b> Implements a recoil animation using
 *         {@link SeslRecoilAnimator} to provide dynamic feedback
 *         on interaction with child views. This animation is triggered by touch events
 *         (including S Pen events) and key events (specifically the Enter key).</li>
 *     <li><b>Interaction Handling:</b> Manages touch and key events to appropriately trigger
 *         animations and update the state of child views. It identifies the clickable child
 *         view under the touch event and applies the press/release states accordingly.</li>
 * </ul>
 *
 * <p><b>Usage:</b>
 * This layout can be used as a direct replacement for {@link LinearLayoutCompat} in XML layouts
 * where rounded corners and recoil animations are desired.
 *
 * <pre>
 * &lt;androidx.appcompat.widget.SeslLinearLayoutCompat
 *     xmlns:android="http://schemas.android.com/apk/res/android"
 *     xmlns:app="http://schemas.android.com/apk/res-auto"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     android:orientation="vertical"
 *     app:seslLayoutRoundedCorner="all"&gt;
 *
 *     &lt;!-- Child views --&gt;
 *
 * &lt;/androidx.appcompat.widget.SeslLinearLayoutCompat&gt;
 * </pre>
 *
 */
@RequiresApi(29)
public class SeslLinearLayoutCompat extends LinearLayoutCompat {
    private static final int MOTION_EVENT_ACTION_PEN_DOWN = 211;
    private static final int MOTION_EVENT_ACTION_PEN_UP = 212;
    private final ItemBackgroundHolder mItemBackgroundHolder;
    private final SeslRecoilAnimator.Holder mRecoilAnimatorHolder;
    private final SeslRoundedCorner mRoundedCorner;

    public static class ItemBackgroundHolder {
        Drawable activeBg = null;

        public ItemBackgroundHolder() {
        }

        public void setCancel() {
            if (activeBg != null) {
                if (activeBg instanceof SeslRecoilDrawable) {
                    ((SeslRecoilDrawable) activeBg).setCancel();
                } else {
                    activeBg.setState(new int[0]);
                }
                this.activeBg = null;
            }
        }

        public void setPress(@NonNull View view) {
            setRelease();
            Drawable background = view.getBackground();
            this.activeBg = background;
            if (background != null) {
                background.setState(new int[]{android.R.attr.state_pressed});
            }
        }

        public void setRelease() {
            if (activeBg != null) {
                activeBg.setState(new int[0]);
                activeBg = null;
            }
        }
    }

    public SeslLinearLayoutCompat(@NonNull Context context) {
        this(context, null);
    }

    public SeslLinearLayoutCompat(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SeslLinearLayoutCompat(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs, R.styleable.SeslLayout, defStyleAttr, 0);
        ViewCompat.saveAttributeDataForStyleable(this, context, R.styleable.SeslLayout, attrs, a.getWrappedTypeArray(), defStyleAttr, 0);
        int roundedCorner = a.getInt(R.styleable.SeslLayout_seslLayoutRoundedCorner, 0);
        a.recycle();

        mRoundedCorner =  new SeslRoundedCorner(context);
        mRoundedCorner.setRoundedCorners(roundedCorner);
        mItemBackgroundHolder = new ItemBackgroundHolder();
        mRecoilAnimatorHolder = new SeslRecoilAnimator.Holder(context);
    }

    private View findChildViewUnder(View view, int x, int y) {
        View foundView = null;

        if (view instanceof ViewGroup viewGroup) {
            int[] xy = transformCoordinate(viewGroup, x, y);
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                if (isPointInsideView(xy[0], xy[1], child) && (foundView = findChildViewUnder(child, x, y)) != null){
                    break;
                }
            }
        }

        if (foundView == null && view.isClickable() && view.getVisibility() == View.VISIBLE && view.isEnabled()) {
            return view;
        }

        return foundView;
    }

    private int[] transformCoordinate(View child, int x, int y) {
        return new int[]{x - child.getLeft(), y - child.getTop()};
    }

    private boolean isPointInsideView(int x, int y, View childView) {
        return new Rect(
                childView.getLeft(),
                childView.getTop(),
                childView.getRight(),
                childView.getBottom()
        ).contains(x, y);
    }

    private View findClickableChildUnder(MotionEvent event) {
        View child = null;

        for (int i = 0; i < getChildCount(); i++) {
            child = getChildAt(i);
            if (isPointInsideView((int) event.getX(), (int) event.getY(), child)) {
                break;
            }
        }

        if (child == null) {
            return null;
        }

        View childUnder = findChildViewUnder(child, (int) event.getX(), (int) event.getY());
        if (childUnder != null && childUnder != child) {
            if (childUnder.getHeight() * childUnder.getWidth() < child.getHeight() * child.getWidth() * 0.5f) {
                return null;
            }
        }

        return childUnder;
    }

    @Override
    public void dispatchDraw(@NonNull Canvas canvas) {
        super.dispatchDraw(canvas);
        this.mRoundedCorner.drawRoundedCorner(canvas);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (keyEvent.getKeyCode() == KEYCODE_ENTER) {
            if (keyEvent.getAction() == ACTION_DOWN) {
                View focusedChild = getFocusedChild();
                if (focusedChild != null) {
                    this.mRecoilAnimatorHolder.setPress(focusedChild);
                }
            } else {
                this.mRecoilAnimatorHolder.setRelease();
            }
        }
        return super.dispatchKeyEvent(keyEvent);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MOTION_EVENT_ACTION_PEN_DOWN:
                View clickableChild = findClickableChildUnder(motionEvent);
                if (clickableChild != null) {
                    mItemBackgroundHolder.setPress(clickableChild);
                    mRecoilAnimatorHolder.setPress(clickableChild);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MOTION_EVENT_ACTION_PEN_UP:
                mItemBackgroundHolder.setRelease();
                mRecoilAnimatorHolder.setRelease();
                break;
            case MotionEvent.ACTION_CANCEL:
                mItemBackgroundHolder.setCancel();
                mRecoilAnimatorHolder.setRelease();
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    @NonNull
    public SeslRoundedCorner getRoundedCorner() {
        return this.mRoundedCorner;
    }


}