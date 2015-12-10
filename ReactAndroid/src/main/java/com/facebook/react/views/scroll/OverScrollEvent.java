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

    public OverScrollEvent(int viewTag, long timestampMs, float overScrollX, float overScrollY) {
        super(viewTag, timestampMs);
        mOverScrollX = overScrollX;
        mOverScrollY = overScrollY;
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
        rctEventEmitter.receiveEvent(getViewTag(), getEventName(), eventData);
    }
}
