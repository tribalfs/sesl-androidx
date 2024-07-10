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

package androidx.pdf.viewer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.graphics.Rect;
import android.view.View;

import androidx.pdf.data.Range;
import androidx.pdf.find.FindInFileView;
import androidx.pdf.util.ObservableValue;
import androidx.pdf.util.Observables;
import androidx.pdf.widget.FastScrollView;
import androidx.pdf.widget.ZoomView;
import androidx.test.filters.SmallTest;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;


@SmallTest
@RunWith(RobolectricTestRunner.class)
public class ZoomScrollValueObserverTest {
    private static final ObservableValue<Viewer.ViewState> VIEW_STATE_EXPOSED_VALUE =
            Observables.newExposedValueWithInitialValue(Viewer.ViewState.NO_VIEW);
    private static final Rect RECT = new Rect(0, 0, 100, 100);
    private static final ZoomView.ZoomScroll OLD_POSITION = new ZoomView.ZoomScroll(1.0f, 0, 0,
            false);
    private static final Range PAGE_RANGE = new Range(0, 100);

    private final PaginatedView mMockPaginatedView = mock(PaginatedView.class);
    private final ZoomView mMockZoomView = mock(ZoomView.class);
    private final PaginationModel mMockPaginationModel = mock(PaginationModel.class);
    private final LayoutHandler mMockLayoutHandler = mock(LayoutHandler.class);
    private final FloatingActionButton mMockAnnotationButton = mock(FloatingActionButton.class);
    private final FindInFileView mMockFindInFileView = mock(FindInFileView.class);
    private final PageIndicator mMockPageIndicator = mock(PageIndicator.class);
    private final FastScrollView mMockFastScrollView = mock(FastScrollView.class);
    private final PageRangeHandler mPageRangeHandler = mock(PageRangeHandler.class);

    private boolean mIsAnnotationIntentResolvable;
    private ZoomView.ZoomScroll mNewPosition;


    @Before
    public void setUp() {
        mIsAnnotationIntentResolvable = false;
        mNewPosition = new ZoomView.ZoomScroll(1.0f, 0, 0, false);

        when(mMockPaginatedView.getPageRangeHandler()).thenReturn(mPageRangeHandler);
        when(mMockPaginatedView.getPaginationModel()).thenReturn(mMockPaginationModel);
        when(mMockZoomView.getHeight()).thenReturn(100);
        when(mPageRangeHandler.computeVisibleRange(0, 1.0f, 100, false)).thenReturn(PAGE_RANGE);
        when(mMockPageIndicator.setRangeAndZoom(PAGE_RANGE, 1.0f, true)).thenReturn(false);
        when(mMockZoomView.getStableZoom()).thenReturn(1.0f);
        when(mMockZoomView.getVisibleAreaInContentCoords()).thenReturn(RECT);
        when(mMockPaginatedView.createPageViewsForVisiblePageRange()).thenReturn(false);
        when(mPageRangeHandler.getVisiblePages()).thenReturn(PAGE_RANGE);
    }

    @Test
    public void onChange_loadPageAssets_stablePosition() {
        mNewPosition = new ZoomView.ZoomScroll(1.0f, 0, 0, true);

        ZoomScrollValueObserver zoomScrollValueObserver = new ZoomScrollValueObserver(mMockZoomView,
                mMockPaginatedView, mMockLayoutHandler, mMockAnnotationButton,
                mMockFindInFileView, mMockPageIndicator, mMockFastScrollView,
                mIsAnnotationIntentResolvable, VIEW_STATE_EXPOSED_VALUE);
        zoomScrollValueObserver.onChange(OLD_POSITION, mNewPosition);

        verify(mMockZoomView).setStableZoom(1.0f);
        verify(mMockPaginationModel).setViewArea(RECT);
        verify(mMockPaginatedView).refreshPageRangeInVisibleArea(mNewPosition, 100);
        verify(mMockPaginatedView).handleGonePages(false);
        verify(mMockPaginatedView).loadInvisibleNearPageRange(1.0f);
        verify(mMockPaginatedView).refreshVisiblePages(false, Viewer.ViewState.NO_VIEW, 1.0f);
        verify(mMockPaginatedView).handleGonePages(true);
        verify(mMockLayoutHandler).maybeLayoutPages(100);
    }

    @Test
    public void onChange_loadPageAssets_stableZoom() {
        mNewPosition = new ZoomView.ZoomScroll(2.0f, 0, 0, false);
        when(mMockZoomView.getStableZoom()).thenReturn(2.0f);

        ZoomScrollValueObserver zoomScrollValueObserver = new ZoomScrollValueObserver(mMockZoomView,
                mMockPaginatedView, mMockLayoutHandler, mMockAnnotationButton,
                mMockFindInFileView, mMockPageIndicator, mMockFastScrollView,
                mIsAnnotationIntentResolvable, VIEW_STATE_EXPOSED_VALUE);
        zoomScrollValueObserver.onChange(OLD_POSITION, mNewPosition);

        verify(mMockPaginatedView).refreshVisibleTiles(false, Viewer.ViewState.NO_VIEW);
    }

    @Test
    public void onChange_showFastScrollView() {
        when(mMockPageIndicator.setRangeAndZoom(PAGE_RANGE, 1.0f, false)).thenReturn(true);

        ZoomScrollValueObserver zoomScrollValueObserver = new ZoomScrollValueObserver(mMockZoomView,
                mMockPaginatedView, mMockLayoutHandler, mMockAnnotationButton,
                mMockFindInFileView, mMockPageIndicator, mMockFastScrollView,
                mIsAnnotationIntentResolvable, VIEW_STATE_EXPOSED_VALUE);
        zoomScrollValueObserver.onChange(OLD_POSITION, mNewPosition);

        verify(mMockFastScrollView).setVisible();
    }

    @Test
    public void onChange_showAnnotationButton() {
        mIsAnnotationIntentResolvable = true;
        when(mMockAnnotationButton.getVisibility()).thenReturn(View.GONE);
        when(mMockFindInFileView.getVisibility()).thenReturn(View.GONE);

        ZoomScrollValueObserver zoomScrollValueObserver = new ZoomScrollValueObserver(mMockZoomView,
                mMockPaginatedView, mMockLayoutHandler, mMockAnnotationButton,
                mMockFindInFileView, mMockPageIndicator, mMockFastScrollView,
                mIsAnnotationIntentResolvable, VIEW_STATE_EXPOSED_VALUE);
        zoomScrollValueObserver.onChange(OLD_POSITION, mNewPosition);

        verify(mMockAnnotationButton).setVisibility(View.VISIBLE);
    }

    @Test
    public void onChange_hideAnnotationButton() {
        mIsAnnotationIntentResolvable = true;
        mNewPosition = new ZoomView.ZoomScroll(1.0f, 0, 1, false);

        ZoomScrollValueObserver zoomScrollValueObserver = new ZoomScrollValueObserver(mMockZoomView,
                mMockPaginatedView, mMockLayoutHandler, mMockAnnotationButton,
                mMockFindInFileView, mMockPageIndicator, mMockFastScrollView,
                mIsAnnotationIntentResolvable, VIEW_STATE_EXPOSED_VALUE);
        zoomScrollValueObserver.onChange(OLD_POSITION, mNewPosition);

        verify(mMockAnnotationButton).setVisibility(View.GONE);
    }

}
