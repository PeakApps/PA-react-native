package com.facebook.react.views.scroll;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

/**
 * Created by sagiantebi on 23/11/15.
 */
public class OverScrollEvent extends Event<OverScrollEvent> {

    /*package*/ static final String EVENT_NAME = "topOverScroll";

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
}
