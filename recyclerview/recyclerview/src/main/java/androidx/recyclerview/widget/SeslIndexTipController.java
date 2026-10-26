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

package androidx.recyclerview.widget;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroupOverlay;
import android.widget.SectionIndexer;

//sesl9
/**
 * Controller managing the lifecycle, states, and text provider of the index tip overlay
 * shown over RecyclerView during fast-scrolling or section navigation.
 */
class SeslIndexTipController {
    private boolean mAttached;
    private IndexTipState mCurrentState;
    private final IndexTipState mFadingOutState;
    private final IndexTipState mHiddenState;
    private boolean mImmersivePositionDirty;
    private final SeslIndexTipScrollContext mScrollContext;
    private final SectionIndexerTextProvider mTextProvider;
    private final IndexTipState mTimedVisibleState;
    private final SeslIndexTipView mView;
    private final IndexTipState mVisibleState;

    public final class FadingOutState extends BaseState {
        private FadingOutState() {
            super();
        }

        @Override
        public void enter(SeslIndexTipController controller) {
            controller.fadeOutDelayed();
        }

        @Override
        public void exit(SeslIndexTipController controller) {
            controller.cancelPendingFadeOut();
        }

        @Override
        public void onFadeOutFinished(SeslIndexTipController controller) {
            controller.transitionTo(controller.mHiddenState);
        }

        @Override
        public void onScroll(SeslIndexTipController controller, int dx, int dy, boolean isActionScroll) {
            if (controller.shouldShow(dx, dy) || isActionScroll) {
                if (isActionScroll) {
                    controller.transitionTo(controller.mTimedVisibleState);
                } else {
                    controller.transitionTo(controller.mVisibleState);
                }
            }
        }

        @Override
        public void onTimerExpired(SeslIndexTipController controller) {
            controller.fadeOut();
        }
    }

    public final class HiddenState extends BaseState {
        private HiddenState() {
            super();
        }

        @Override
        public void enter(SeslIndexTipController controller) {
            controller.hideImmediate();
        }

        @Override
        public void onScroll(SeslIndexTipController controller, int dx, int dy, boolean isActionScroll) {
            if (isActionScroll) {
                controller.transitionTo(controller.mTimedVisibleState);
            } else if (controller.shouldShow(dx, dy)) {
                controller.transitionTo(controller.mVisibleState);
            }
        }
    }

    public interface IndexTipState {
        void enter(SeslIndexTipController controller);

        void exit(SeslIndexTipController controller);

        void onFadeOutFinished(SeslIndexTipController controller);

        void onHideRequested(SeslIndexTipController controller);

        void onIdle(SeslIndexTipController controller);

        void onScroll(SeslIndexTipController controller, int dx, int dy, boolean isActionScroll);

        void onTimerExpired(SeslIndexTipController controller);
    }

    public static final class SectionIndexerTextProvider {
        private SectionIndexer mSectionIndexer;
        private Object[] mSections = new Object[0];

        public SectionIndexerTextProvider(SectionIndexer sectionIndexer) {
            update(sectionIndexer);
        }

        public void refresh() {
            Object[] sections = mSectionIndexer.getSections();
            if (sections == null) {
                throw new IllegalStateException("SectionIndexer.getSections() must not return null.");
            }
            mSections = sections;
        }

        public String resolveText(int position) {
            if (position != -1) {
                int section = mSectionIndexer.getSectionForPosition(position);
                if (section >= mSections.length) {
                    //custom - helpful for paged list
                    //mSections is stale, refresh.
                    refresh();
                }
                if (section >= 0 && section < mSections.length) {
                    Object obj = mSections[section];
                    if (obj != null) {
                        return obj.toString();
                    }
                }
            }
            return null;
        }

        public void update(SectionIndexer sectionIndexer) {
            mSectionIndexer = sectionIndexer;
            refresh();
        }
    }

    public final class TransientVisibleState extends BaseState {
        private TransientVisibleState() {
            super();
        }

        @Override
        public void enter(SeslIndexTipController controller) {
            controller.show();
            controller.startHideTimer();
        }

        @Override
        public void exit(SeslIndexTipController controller) {
            controller.cancelHideTimer();
        }

        @Override
        public void onHideRequested(SeslIndexTipController controller) {
            controller.transitionTo(controller.mFadingOutState);
        }

        @Override
        public void onIdle(SeslIndexTipController controller) {
            controller.transitionTo(controller.mFadingOutState);
        }

        @Override
        public void onScroll(SeslIndexTipController controller, int dx, int dy, boolean isActionScroll) {
            if (isActionScroll) {
                controller.resetHideTimer();
                controller.show();
            } else if (controller.shouldShow(dx, dy)) {
                controller.transitionTo(controller.mVisibleState);
            }
        }

        @Override
        public void onTimerExpired(SeslIndexTipController controller) {
            controller.transitionTo(controller.mFadingOutState);
        }
    }

    public final class VisibleState extends BaseState {
        private VisibleState() {
            super();
        }

        @Override
        public void enter(SeslIndexTipController controller) {
            controller.show();
        }

        @Override
        public void onHideRequested(SeslIndexTipController controller) {
            controller.transitionTo(mFadingOutState);
        }

        @Override
        public void onIdle(SeslIndexTipController controller) {
            controller.transitionTo(mFadingOutState);
        }

        @Override
        public void onScroll(SeslIndexTipController controller, int dx, int dy, boolean isActionScroll) {
            if (!controller.shouldShow(dx, dy)) {
                controller.transitionTo(mFadingOutState);
            } else if (isActionScroll) {
                controller.transitionTo(controller.mTimedVisibleState);
            } else {
                controller.show();
            }
        }
    }

    public SeslIndexTipController(View view, SectionIndexer sectionIndexer, SeslIndexTipScrollContext scrollContext) {
        HiddenState hiddenState = new HiddenState();
        mHiddenState = hiddenState;
        mVisibleState = new VisibleState();
        mTimedVisibleState = new TransientVisibleState();
        mFadingOutState = new FadingOutState();
        mCurrentState = hiddenState;
        mAttached = false;
        mImmersivePositionDirty = false;
        mView = new SeslIndexTipView(view.getContext(), view);
        mTextProvider = new SectionIndexerTextProvider(sectionIndexer);
        mScrollContext = scrollContext;
    }

    /* package */ void transitionTo(IndexTipState newState) {
        if (mCurrentState == newState) {
            return;
        }
        mCurrentState.exit(this);
        mCurrentState = newState;
        newState.enter(this);
    }

    public void attach(ViewGroupOverlay overlay) {
        if (mAttached) {
            return;
        }
        overlay.add(mView);
        mAttached = true;
    }

    public void cancelHideTimer() {
        mView.cancelHideTimer();
    }

    public void cancelPendingFadeOut() {
        mView.cancelPendingFadeOut();
    }

    public void detach(ViewGroupOverlay overlay) {
        if (mAttached) {
            transitionTo(mHiddenState);
            overlay.remove(mView);
            mAttached = false;
        }
    }

    public void fadeOut() {
        mView.fadeOut(() -> mCurrentState.onFadeOutFinished(SeslIndexTipController.this));
    }

    public void fadeOutDelayed() {
        mView.fadeOutDelayed(() -> mCurrentState.onFadeOutFinished(SeslIndexTipController.this));
    }

    public int getPaddingLeft() {
        return mView.getHorizontalPaddingLeft();
    }

    public int getPaddingRight() {
        return mView.getHorizontalPaddingRight();
    }

    public void hideImmediate() {
        mView.cancelHideTimer();
        mView.cancelFadeAnimation();
        mView.hideImmediate();
    }

    public void onAvailableBoundsChanged(boolean isAvailable) {
        onScroll(2, 1, isAvailable, true);
    }

    public void onIdle() {
        mCurrentState.onIdle(this);
    }

    public void onImmersivePositionChanged(boolean dirty) {
        mImmersivePositionDirty = dirty;
        mView.onImmersivePositionChanged(dirty);
    }

    public void onScroll(int dx, int dy, boolean isScrollable, boolean isActionScroll) {
        String text = mTextProvider.resolveText(mScrollContext.getFirstVisibleItemPosition());
        if (TextUtils.isEmpty(text)) {
            transitionTo(mHiddenState);
            return;
        }
        if (!isScrollable) {
            transitionTo(mHiddenState);
        } else {
            if (mScrollContext.isNestedScrollSuppressed(dx, dy)) {
                mScrollContext.consumeNestedScrollRange();
                return;
            }
            mView.updateText(text);
            mCurrentState.onScroll(this, dx, dy, isActionScroll);
            mView.invalidateIfNeed();
        }
    }

    public void refreshSections() {
        mTextProvider.refresh();
        transitionTo(mHiddenState);
    }

    public void resetHideTimer() {
        mView.cancelHideTimer();
        startHideTimer();
    }

    public void setHorizontalPadding(int left, int right) {
        mView.setHorizontalPadding(left, right);
    }

    public void setSectionIndexer(SectionIndexer sectionIndexer) {
        mTextProvider.update(sectionIndexer);
    }

    public void setTopMargin(int topMargin) {
        mView.setTopMargin(topMargin);
    }

    public boolean shouldShow(int dx, int dy) {
        return dx != 0 && dy != 0 && mScrollContext.canScrollUp();
    }

    public void show() {
        mView.fadeIn();
    }

    public void startHideTimer() {
        mView.startHideTimer(() -> mCurrentState.onTimerExpired(SeslIndexTipController.this));
    }

    public void updateLayout(int width, int topOffset, int leftPadding, int rightPadding) {
        mView.applyLayout(width, topOffset, leftPadding, rightPadding, mImmersivePositionDirty);
    }

    static abstract class BaseState implements IndexTipState {
        protected BaseState() {
        }

        @Override
        public void enter(SeslIndexTipController controller) {
        }

        @Override
        public void exit(SeslIndexTipController controller) {
        }

        @Override
        public void onFadeOutFinished(SeslIndexTipController controller) {
        }

        @Override
        public void onHideRequested(SeslIndexTipController controller) {
        }

        @Override
        public void onIdle(SeslIndexTipController controller) {
        }

        @Override
        public void onTimerExpired(SeslIndexTipController controller) {
        }

        @Override
        public void onScroll(SeslIndexTipController controller, int dx, int dy, boolean isActionScroll) {
        }
    }
}
