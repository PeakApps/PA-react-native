package com.facebook.react.views.image;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

/**
 * Created by sagiantebi on 24/11/15.
 */
public class OnErrorEvent extends Event<OnErrorEvent> {

    /* package */ static final String EVENT_NAME = "topImageLoadError";

    private String mError;

    public OnErrorEvent(int viewTag, long timestampMs, String error) {
        super(viewTag, timestampMs);
        mError = error;
    }

    @Override
    public String getEventName() {
        return EVENT_NAME;
    }

    @Override
    public void dispatch(RCTEventEmitter rctEventEmitter) {
        WritableMap eventData = Arguments.createMap();
        eventData.putInt("target", getViewTag());
        eventData.putString("error", mError);
        rctEventEmitter.receiveEvent(getViewTag(), getEventName(), eventData);
    }
}
