/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.react.views.scroll;

import android.graphics.Rect;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.PixelUtil;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.views.view.ReactViewGroup;

import java.util.List;

/**
 * Helper class that deals with emitting Scroll Events.
 */
public class ReactScrollViewHelper {

  public static final long MOMENTUM_DELAY = 20;

  /**
   * Shared by {@link ReactScrollView} and {@link ReactHorizontalScrollView}.
   */
  public static void emitScrollEvent(ViewGroup scrollView) {
    emitScrollEvent(scrollView, ScrollEventType.SCROLL);
  }

  public static void emitScrollBeginDragEvent(ViewGroup scrollView) {
    emitScrollEvent(scrollView, ScrollEventType.BEGIN_DRAG);
  }

  public static void emitScrollEndDragEvent(ViewGroup scrollView) {
    emitScrollEvent(scrollView, ScrollEventType.END_DRAG);
  }

  public static void emitScrollMomentumBeginEvent(ViewGroup scrollView) {
    emitScrollEvent(scrollView, ScrollEventType.MOMENTUM_BEGIN);
  }

  public static void emitScrollMomentumEndEvent(ViewGroup scrollView) {
    emitScrollEvent(scrollView, ScrollEventType.MOMENTUM_END);
  }

// ** PA **
// Possible merge issue:
 private static void emitScrollEvent(ViewGroup scrollView, ScrollEventType scrollEventType) 
{
	emitScrollEvent(scrollView, scrollEventType, null);
}

  private static void emitScrollEvent(ViewGroup scrollView, ScrollEventType scrollEventType, WritableMap userData) {
    View contentView = scrollView.getChildAt(0);
    ReactContext reactContext = (ReactContext) scrollView.getContext();
    reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher().dispatchEvent(
        ScrollEvent.obtain(
            scrollView.getId(),
            SystemClock.uptimeMillis(),
            scrollEventType,
            scrollView.getScrollX(),
            scrollView.getScrollY(),
            contentView.getWidth(),
            contentView.getHeight(),
            scrollView.getWidth(),
            scrollView.getHeight(),
            userData));
  }

    /* package */ static void emitOverScrollEvent(ReactScrollView scrollView, float overScrollX, float overScrollY) {
        ReactContext reactContext = (ReactContext) scrollView.getContext();
        reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher().dispatchEvent(new OverScrollEvent(scrollView.getId(), SystemClock.uptimeMillis(), overScrollX, overScrollY));
    }

    /* package */ static void emitOverScrollEndedEvent(ReactScrollView scrollView) {
        ReactContext reactContext = (ReactContext) scrollView.getContext();
        reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher().dispatchEvent(new OverScrollEndedEvent(scrollView.getId(), SystemClock.uptimeMillis()));
    }

    static private Rect locateView(View view) {
        Rect loc = new Rect();
        /*int[] location = new int[2];
        if (view == null) {

            return loc;
        }*/
        //view.getLocalVisibleRect(loc);

        /*loc.left = location[0];
        loc.top = location[1];
        loc.right = loc.left + view.getWidth();
        loc.bottom = loc.top + view.getHeight();*/
        loc.left = view.getLeft();
        loc.top = view.getTop();
        loc.right = view.getRight();
        loc.bottom = view.getBottom();
        return loc;
    }

    static private boolean RectEqualToRect(Rect rect1, Rect rect2)
    {
        return rect1.left == rect2.left &&
                rect1.right == rect2.right &&
                rect1.top == rect2.top &&
                rect1.bottom == rect2.bottom;
    }

    static WritableArray calculateChildFramesData(ViewGroup scrollView, List<Rect> cachedChildFrames) {
        if (scrollView.getChildCount() == 0)
            return null;

        WritableArray updatedChildFrames = Arguments.createArray();

        ReactViewGroup reactSubviews = (ReactViewGroup)scrollView.getChildAt(0);
        for (int idx = 0; idx < reactSubviews.getChildCount(); idx++)
        {
            View subview = reactSubviews.getChildAt(idx);

            // Check if new or changed
            Rect newFrame = locateView(subview);
            boolean frameChanged = false;
            if (cachedChildFrames.size() <= idx) {
                frameChanged = true;
                cachedChildFrames.add(newFrame);
            } else if (RectEqualToRect(newFrame, cachedChildFrames.get(idx))) {
                frameChanged = true;
                cachedChildFrames.set(idx, newFrame);
            }

            // Create JS frame object
            if (frameChanged) {
                WritableMap frame = Arguments.createMap();
                frame.putDouble("index", idx);
                frame.putDouble("x",  PixelUtil.toDIPFromPixel(newFrame.left));
                frame.putDouble("y",  PixelUtil.toDIPFromPixel(newFrame.top));
                frame.putDouble("width",  PixelUtil.toDIPFromPixel(newFrame.width()));
                frame.putDouble("height",  PixelUtil.toDIPFromPixel(newFrame.height()));
                updatedChildFrames.pushMap(frame);
            };
        }
        return updatedChildFrames;
    }


}
