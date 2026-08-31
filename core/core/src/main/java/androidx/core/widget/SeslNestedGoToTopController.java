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

package androidx.core.widget;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

import android.os.Build;
import android.util.Log;
import android.util.StateSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;

import androidx.annotation.RestrictTo;

import org.jspecify.annotations.NonNull;

//sesl9
/**
 * {@link SeslGoToTopController} variant used by SESL nested scrolling widgets, driving
 * the button image states directly and using a longer auto-hide delay.
 */
@RestrictTo(LIBRARY_GROUP_PREFIX)
public final class SeslNestedGoToTopController extends SeslGoToTopController {

    private static final int MOTION_EVENT_ACTION_PEN_DOWN = 211;
    private static final int MOTION_EVENT_ACTION_PEN_MOVE = 213;
    private static final int MOTION_EVENT_ACTION_PEN_UP = 212;
    private static final int NSV_AUTO_HIDE_DELAY_MS = 2500;
    private static final String TAG = "SeslNestedGoToTopController";

    private boolean mIsSupportGoToTop = false;

    /** Builder for {@link SeslNestedGoToTopController}. */
    public static final class Builder extends SeslGoToTopControllerBuilder<SeslNestedGoToTopController, Builder> {
        @NonNull
        @Override
        public SeslNestedGoToTopController build() {
            validate();
            return new SeslNestedGoToTopController(this.host, this.config);
        }
    }

    /** Creates a new {@link SeslNestedGoToTopController} for the given host and config. */
    public SeslNestedGoToTopController(@NonNull Host host, @NonNull SeslGoToTopConfig config) {
        super(host, config);
        this.mHost = host;
    }

    @Override
    public int getAutoHideDelayMs() {
        return NSV_AUTO_HIDE_DELAY_MS;
    }

    /** Returns the scroll-to-top duration in milliseconds. */
    public int getScrollToTopDurationMs() {
        return this.mConfig.getScrollToTopDurationMs();
    }

    @Override
    public boolean isAvailable() {
        return Build.VERSION.SDK_INT >= 33 && isSupportGoToTop() && super.isAvailable();
    }

    /** Returns whether GoToTop feature is supported by the host widget. */
    public boolean isSupportGoToTop() {
        return this.mIsSupportGoToTop;
    }

    /** Enables or disables GoToTop feature support on the host widget. */
    public void setSupportGoToTop(boolean support) {
        this.mIsSupportGoToTop = support;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isAvailable()) {
            return false;
        }

        int actionMasked = event.getActionMasked();
        int x = (int) (event.getX() + 0.5f);
        int y = (int) (event.getY() + 0.5f);

        if (actionMasked == MotionEvent.ACTION_DOWN) {
            setScrollRunning(false);
            if (this.mGoToTopView != null && getState() != GTT_STATE_PRESSED && this.mGoToTopRect.contains(x, y)) {
                applyState(GTT_STATE_PRESSED);
                this.mGoToTopImage.setHotspot(x, y);
                this.mGoToTopImage.setState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled, android.R.attr.state_selected});
                return true;
            }
        } else if (actionMasked == MotionEvent.ACTION_UP) {
            if (getState() == GTT_STATE_PRESSED) {
                if (this.mHost.canScrollUp()) {
                    OnGoToTopClickListener listener = this.mOnClickListener;
                    if (listener == null || !listener.onGoToTopClick()) {
                        this.mHost.smoothScrollToTop();
                    }
                    return true;
                }
                setState(GTT_STATE_SHOWN);
                autoHide(AUTO_HIDE_REASON_SHOWN_OR_UPDATE);
                this.mGoToTopImage.setState(StateSet.NOTHING);
                this.mHost.playSoundEffect(SoundEffectConstants.CLICK);
                return true;
            }
        } else if (actionMasked == MotionEvent.ACTION_MOVE) {
            if (getState() == GTT_STATE_PRESSED) {
                if (!this.mGoToTopRect.contains(x, y)) {
                    setState(GTT_STATE_SHOWN);
                    this.mGoToTopImage.setState(StateSet.NOTHING);
                    autoHide(AUTO_HIDE_REASON_SHOWN_OR_UPDATE);
                    return true;
                }
                return true;
            }
        } else if (actionMasked == MotionEvent.ACTION_CANCEL) {
            if (this.mGoToTopState != GTT_STATE_NONE) {
                this.mGoToTopImage.setState(StateSet.NOTHING);
                return false;
            }
        }

        return false;
    }

    /** Handles S-Pen stylus touch events on the GoToTop button. */
    public boolean onTouchPenEvent(MotionEvent event) {
        if (!isAvailable()) {
            return false;
        }

        int actionMasked = event.getActionMasked();
        int x = (int) (event.getX() + 0.5f);
        int y = (int) (event.getY() + 0.5f);

        switch (actionMasked) {
            case MOTION_EVENT_ACTION_PEN_DOWN:
                if (this.mGoToTopState != GTT_STATE_PRESSED && this.mGoToTopRect.contains(x, y)) {
                    applyState(GTT_STATE_PRESSED);
                    this.mGoToTopImage.setHotspot(x, y);
                    this.mGoToTopImage.setState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled, android.R.attr.state_selected});
                    return true;
                }
                return false;

            case MOTION_EVENT_ACTION_PEN_UP:
                if (this.mGoToTopState == GTT_STATE_PRESSED) {
                    Log.d(TAG, "pen up false GOTOTOP");
                    if (this.mHost.canScrollUp()) {
                        this.mHost.smoothScrollToTop();
                        this.mHost.showTopEdgeEffect();
                    }
                    applyState(GTT_STATE_NONE);
                    this.mGoToTopImage.setState(StateSet.NOTHING);
                    return true;
                }
                return false;

            case MOTION_EVENT_ACTION_PEN_MOVE:
                if (this.mGoToTopState == GTT_STATE_PRESSED && !this.mGoToTopRect.contains(x, y)) {
                    this.mGoToTopState = GTT_STATE_SHOWN;
                    this.mGoToTopImage.setState(StateSet.NOTHING);
                    return true;
                }
                return false;

            default:
                return false;
        }
    }
}
