package com.facebook.react.views.scroll;

import android.support.v4.util.Pools;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

/**
 * Created by sagiantebi on 23/11/15.
 */
public class OverScrollEvent extends Event<OverScrollEvent> {

    /*package*/ static final String EVENT_NAME = "topOverScroll";

    private static final Pools.SynchronizedPool<OverScrollEvent> EVENTS_POOL =
            new Pools.SynchronizedPool<>(3);

    private float mOverScrollX;
    private float mOverScrollY;
    private boolean mDown;

    public OverScrollEvent(int viewTag, long timestampMs, float overScrollX, float overScrollY, boolean down) {
        super(viewTag, timestampMs);
        mOverScrollX = overScrollX;
        mOverScrollY = overScrollY;
        mDown = down;
    }

    @Override
    public String getEventName() {
        return EVENT_NAME;
    }

    @Override
    public void dispatch(RCTEventEmitter rctEventEmitter) {
        WritableMap eventData = Arguments.createMap();
        eventData.putInt("target", getViewTag());
        eventData.putDouble("overScrollX", mOverScrollX);
        eventData.putDouble("overScrollY", mOverScrollY);
        eventData.putBoolean("overScrollDown", mDown);
        rctEventEmitter.receiveEvent(getViewTag(), getEventName(), eventData);
    }

    public static OverScrollEvent obtain(int viewTag, long timestampMs, float overScrollX, float overScrollY, boolean down) {
        OverScrollEvent e = EVENTS_POOL.acquire();
        if (e == null) {
            e = new OverScrollEvent(viewTag, timestampMs, overScrollX, overScrollY, down);
        }
        e.init(viewTag, timestampMs);
        e.mOverScrollX = overScrollX;
        e.mOverScrollY = overScrollY;
        e.mDown = down;
        return e;
    }

    @Override
    public void onDispose() {
        super.onDispose();
        EVENTS_POOL.release(this);
    }

}
