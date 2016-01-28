/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.react.views.scroll;

import javax.annotation.Nullable;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ScrollView;

import com.facebook.react.uimanager.MeasureSpecAssertions;
import com.facebook.react.uimanager.events.NativeGestureUtil;
import com.facebook.react.views.view.ReactClippingViewGroup;
import com.facebook.react.views.view.ReactClippingViewGroupHelper;
import com.facebook.infer.annotation.Assertions;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple subclass of ScrollView that doesn't dispatch measure and layout to its children and has
 * a scroll listener to send scroll events to JS.
 *
 * <p>ReactScrollView only supports vertical scrolling. For horizontal scrolling,
 * use {@link ReactHorizontalScrollView}.
 */
public class ReactScrollView extends ScrollView implements ReactClippingViewGroup {

  private final OnScrollDispatchHelper mOnScrollDispatchHelper = new OnScrollDispatchHelper();

  private boolean mRemoveClippedSubviews;
  private @Nullable Rect mClippingRect;
  private boolean mSendMomentumEvents;
  private boolean mDragging;
  private boolean mFlinging;
  private boolean mDoneFlinging;
  private List<Rect> mCachedChildFrames;

  private float mInitialY;
  private float mInitialX;
  private float mStartY = -1f;
  private float mStartX = -1f;
  private float mOverScrollDistanceY;
  private float mOverScrollDistanceX;

  private float mTouchSlop;

  private boolean mOverScrollY = true;
  private boolean mOverScrollX = false;

  private boolean mCurrentTouchPassedYThreshold = false;
  private boolean mCurrentTouchPassedXThreshold = false;

  private boolean mOverscrollXStarted = false;

  public ReactScrollView(Context context) {
    super(context);
    mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    mCachedChildFrames = new ArrayList<Rect>();
  }

  public void setSendMomentumEvents(boolean sendMomentumEvents) {
    mSendMomentumEvents = sendMomentumEvents;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    MeasureSpecAssertions.assertExplicitMeasureSpec(widthMeasureSpec, heightMeasureSpec);

    setMeasuredDimension(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec));
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    // Call with the present values in order to re-layout if necessary
    scrollTo(getScrollX(), getScrollY());
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    if (mRemoveClippedSubviews) {
      updateClippingRect();
    }
  }

  @Override
  protected void onScrollChanged(int x, int y, int oldX, int oldY) {
    super.onScrollChanged(x, y, oldX, oldY);

    if (mOnScrollDispatchHelper.onScrollChanged(x, y)) {
      if (mRemoveClippedSubviews) {
        updateClippingRect();
      }

//      WritableArray childFrames = ReactScrollViewHelper.calculateChildFramesData(this, mCachedChildFrames);
//      WritableMap userData = Arguments.createMap();
//      userData.putArray("updatedChildFrames", childFrames);

     // ReactScrollViewHelper.emitScrollEvent(this, x, y, null);

	// ** PA **
	// Possible merge/rebase issue:
      if (mFlinging) {
        mDoneFlinging = false;
    }

      ReactScrollViewHelper.emitScrollEvent(this);
  }
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    boolean superHandled = false;
    if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
      mInitialY = ev.getRawY();
      mInitialX = ev.getRawX();
    }
    if (super.onInterceptTouchEvent(ev)) {
      NativeGestureUtil.notifyNativeGestureStarted(this, ev);
      superHandled = true;
    } else if (mOverScrollX && ev.getActionMasked() == MotionEvent.ACTION_MOVE && Math.abs(mInitialX - ev.getRawX()) > mTouchSlop && !mCurrentTouchPassedYThreshold) {
      NativeGestureUtil.notifyNativeGestureStarted(this, ev);
      superHandled = true;
    }
    if (ev.getActionMasked() == MotionEvent.ACTION_UP || ev.getActionMasked() == MotionEvent.ACTION_CANCEL) {
      mStartY = -1f;
      mCurrentTouchPassedYThreshold = false;
      mCurrentTouchPassedXThreshold = false;
      mStartX = -1f;
    }

	// ** PA **
	// Possible merge issue:
	if ( superHandled )
	{
		mDragging = true;
	}
    return superHandled;
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {
      if (Math.abs(mInitialY - ev.getRawY()) > mTouchSlop && !mCurrentTouchPassedXThreshold) {
        mCurrentTouchPassedYThreshold = true;
      }

      if (Math.abs(mInitialX - ev.getRawX()) > mTouchSlop && !mCurrentTouchPassedYThreshold) {
        mCurrentTouchPassedXThreshold = true;
      }
      if (mOverScrollY) {
        boolean overScrollDown = (getChildAt(0).getHeight() - getScrollY()) == getHeight();
        if (!ViewCompat.canScrollVertically(this, -1) || getScrollY() == 0 || (overScrollDown)) {
          float dragDistance = mInitialY - ev.getRawY();
          if (dragDistance < -mTouchSlop || (overScrollDown && dragDistance > mTouchSlop)) {
            if (mStartY != -1f) {
              if (overScrollDown) {
                mOverScrollDistanceY += (mStartY - ev.getRawY());
              } else {
                mOverScrollDistanceY += (ev.getRawY() - mStartY);
              }
              ReactScrollViewHelper.emitOverScrollEvent(this, 0, mOverScrollDistanceY, overScrollDown);
            }
            mStartY = ev.getRawY();
            return true;
          }
        } else {
          mStartY = -1f;
          mOverScrollDistanceY = 0f;
        }
      }
      if (mOverScrollX && !mCurrentTouchPassedYThreshold) {
        float dragDistance = mInitialX - ev.getRawX();
        if (dragDistance < -mTouchSlop) {
          mOverscrollXStarted = true;
        }
        if (mOverscrollXStarted) {
          if (mStartX != -1f) {
            mOverScrollDistanceX += (ev.getRawX() - mStartX);
          }
          mStartX = ev.getRawX();
          ReactScrollViewHelper.emitOverScrollEvent(this, mOverScrollDistanceX, 0f, false);
          return true;
        }
      }
    }
    if (ev.getActionMasked() == MotionEvent.ACTION_UP || ev.getActionMasked() == MotionEvent.ACTION_CANCEL) {
      mStartY = -1f;
      mCurrentTouchPassedYThreshold = false;
      mCurrentTouchPassedXThreshold = false;
      mStartX = -1f;
      if (mOverScrollY && mOverScrollDistanceY > 0f) {
        ReactScrollViewHelper.emitOverScrollEndedEvent(this);
		mDragging = false;

        // ** PA **
        // Possible merge issue (ADDED "Y")
        mOverScrollDistanceY = 0f;
        return true;
      }
      if (mOverScrollX && mOverscrollXStarted) {
        ReactScrollViewHelper.emitOverScrollEndedEvent(this);
        mOverScrollDistanceX = 0f;
        mOverscrollXStarted = false;
        return true;
      }
    }
	//** PA **
	// Possible merge/rebase issue:
    int action = ev.getAction() & MotionEvent.ACTION_MASK;
    if (action == MotionEvent.ACTION_UP && mDragging) {
      ReactScrollViewHelper.emitScrollEndDragEvent(this);
      mDragging = false;
    }

    return super.onTouchEvent(ev);

  }


  @Override
  public void setRemoveClippedSubviews(boolean removeClippedSubviews) {
    if (removeClippedSubviews && mClippingRect == null) {
      mClippingRect = new Rect();
    }
    mRemoveClippedSubviews = removeClippedSubviews;
      updateClippingRect();
  }

  @Override
  public boolean getRemoveClippedSubviews() {
    return mRemoveClippedSubviews;
  }

  @Override
  public void updateClippingRect() {
    if (!mRemoveClippedSubviews) {
      return;
    }

    Assertions.assertNotNull(mClippingRect);

    ReactClippingViewGroupHelper.calculateClippingRect(this, mClippingRect);
    View contentView = getChildAt(0);
    if (contentView instanceof ReactClippingViewGroup) {
      ((ReactClippingViewGroup) contentView).updateClippingRect();
    }
  }

  @Override
  public void getClippingRect(Rect outClippingRect) {
    outClippingRect.set(Assertions.assertNotNull(mClippingRect));
  }

  public void setOverScrollY(boolean overScrollY) {
    mOverScrollY = overScrollY;
  }
  public void setOverScrollX(boolean overScrollY) {
    mOverScrollX = overScrollY;
  }

  @Override
  public void fling(int velocityY) {
    super.fling(velocityY);
    if (mSendMomentumEvents) {
      mFlinging = true;
      ReactScrollViewHelper.emitScrollMomentumBeginEvent(this);
      Runnable r = new Runnable() {
        @Override
        public void run() {
          if (mDoneFlinging) {
            mFlinging = false;
            ReactScrollViewHelper.emitScrollMomentumEndEvent(ReactScrollView.this);
          } else {
            mDoneFlinging = true;
            ReactScrollView.this.postOnAnimationDelayed(this, ReactScrollViewHelper.MOMENTUM_DELAY);
  }
      }};
      postOnAnimationDelayed(r, ReactScrollViewHelper.MOMENTUM_DELAY);
}
  }
}
