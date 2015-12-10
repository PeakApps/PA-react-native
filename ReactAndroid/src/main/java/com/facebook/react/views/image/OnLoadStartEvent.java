package com.facebook.react.views.image;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

/**
 * Created by sagiantebi on 24/11/15.
 */
public class OnLoadStartEvent extends Event<OnLoadStartEvent> {

    /* package */ static final String EVENT_NAME = "topImageLoadStarted";

    public OnLoadStartEvent(int viewTag, long timestampMs) {
        super(viewTag, timestampMs);
    }

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
