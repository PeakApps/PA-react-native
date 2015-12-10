package com.facebook.react.views.scroll;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

/**
 * Created by sagiantebi on 24/11/15.
 */
public class OverScrollEndedEvent extends Event<OverScrollEndedEvent> {

    public OverScrollEndedEvent(int viewTag, long timestampMs) {
        super(viewTag, timestampMs);
    }

    /* package */ static final String EVENT_NAME = "topOverScrollEnded";

    @Override
    public String getEventName() {
        return EVENT_NAME;
    }

    @Override
    public void dispatch(RCTEventEmitter rctEventEmitter) {
        WritableMap eventData = Arguments.createMap();
        eventData.putInt("target", getViewTag());
        rctEventEmitter.receiveEvent(getViewTag(), getEventName(), eventData);
    }
}
