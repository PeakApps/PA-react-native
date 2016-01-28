/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.react.views.image;

import javax.annotation.Nullable;

import java.util.Map;

import android.graphics.Color;

import android.graphics.drawable.Animatable;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.AbstractDraweeControllerBuilder;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.*;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewProps;

import java.lang.ref.WeakReference;
import java.util.Map;

public class ReactImageManager extends SimpleViewManager<ReactImageView> {

  public static final String REACT_CLASS = "RCTImageView";

  @Override
  public String getName() {
    return REACT_CLASS;
  }

  private @Nullable AbstractDraweeControllerBuilder mDraweeControllerBuilder;
  private final @Nullable Object mCallerContext;

  public ReactImageManager(
      AbstractDraweeControllerBuilder draweeControllerBuilder,
      Object callerContext) {
    mDraweeControllerBuilder = draweeControllerBuilder;
    mCallerContext = callerContext;
  }

  public ReactImageManager() {
    // Lazily initialize as FrescoModule have not been initialized yet
    mDraweeControllerBuilder = null;
    mCallerContext = null;
  }

  public AbstractDraweeControllerBuilder getDraweeControllerBuilder() {
    if (mDraweeControllerBuilder == null) {
      mDraweeControllerBuilder = Fresco.newDraweeControllerBuilder();
    }
    return mDraweeControllerBuilder;
  }

  public Object getCallerContext() {
    return mCallerContext;
  }

  @Override
  public ReactImageView createViewInstance(ThemedReactContext context) {
    return new ReactImageView(
        context,
        getDraweeControllerBuilder(),
        getCallerContext());
  }

  @Override
  protected void addEventEmitters(final ThemedReactContext reactContext, final ReactImageView view) {
    super.addEventEmitters(reactContext, view);
    view.setControllerListener(new ImageControllerListener(reactContext, view));
  }

  // ** PA **
  // Possible merge issue:

//
//  @Override
//  public @Nullable
//  Map getExportedCustomDirectEventTypeConstants() {
//    return MapBuilder.builder()
//            .put(OnLoadStartEvent.EVENT_NAME, MapBuilder.of("registrationName", "onLoadStart"))
//            .put(OnLoadEvent.EVENT_NAME, MapBuilder.of("registrationName", "onLoad"))
//            .put(OnErrorEvent.EVENT_NAME, MapBuilder.of("registrationName", "onError"))
//            .build();
//  }

  // In JS this is Image.props.source.uri
  @ReactProp(name = "src")
  public void setSource(ReactImageView view, @Nullable String source) {
    view.setSource(source);
  }

  // In JS this is Image.props.loadingIndicatorSource.uri
  @ReactProp(name = "loadingIndicatorSrc")
  public void setLoadingIndicatorSource(ReactImageView view, @Nullable String source) {
    view.setLoadingIndicatorSource(source);
  }

  @ReactProp(name = "borderColor", customType = "Color")
  public void setBorderColor(ReactImageView view, @Nullable Integer borderColor) {
    if (borderColor == null) {
      view.setBorderColor(Color.TRANSPARENT);
    } else {
      view.setBorderColor(borderColor);
    }
  }

  @ReactProp(name = "overlayColor")
  public void setOverlayColor(ReactImageView view, @Nullable Integer overlayColor) {
    if (overlayColor == null) {
      view.setOverlayColor(Color.TRANSPARENT);
    } else {
      view.setOverlayColor(overlayColor);
    }
  }

  @ReactProp(name = "borderWidth")
  public void setBorderWidth(ReactImageView view, float borderWidth) {
    view.setBorderWidth(borderWidth);
  }

  @ReactProp(name = "borderRadius")
  public void setBorderRadius(ReactImageView view, float borderRadius) {
    view.setBorderRadius(borderRadius);
  }

  @ReactProp(name = ViewProps.RESIZE_MODE)
  public void setResizeMode(ReactImageView view, @Nullable String resizeMode) {
    view.setScaleType(ImageResizeMode.toScaleType(resizeMode));
  }

  @ReactProp(name = "tintColor", customType = "Color")
  public void setTintColor(ReactImageView view, @Nullable Integer tintColor) {
    if (tintColor == null) {
      view.clearColorFilter();
    } else {
      view.setColorFilter(tintColor);
    }
  }

  @ReactProp(name = "progressiveRenderingEnabled")
  public void setProgressiveRenderingEnabled(ReactImageView view, boolean enabled) {
    view.setProgressiveRenderingEnabled(enabled);
  }

  @ReactProp(name = "fadeDuration")
  public void setFadeDuration(ReactImageView view, int durationMs) {
    view.setFadeDuration(durationMs);
  }

  @ReactProp(name = "shouldNotifyLoadEvents")
  public void setLoadHandlersRegistered(ReactImageView view, boolean shouldNotifyLoadEvents) {
    view.setShouldNotifyLoadEvents(shouldNotifyLoadEvents);
  }

  @Override
  public @Nullable Map getExportedCustomDirectEventTypeConstants() {
    return MapBuilder.of(
      ImageLoadEvent.eventNameForType(ImageLoadEvent.ON_LOAD_START),
        MapBuilder.of("registrationName", "onLoadStart"),
      ImageLoadEvent.eventNameForType(ImageLoadEvent.ON_LOAD),
        MapBuilder.of("registrationName", "onLoad"),
      ImageLoadEvent.eventNameForType(ImageLoadEvent.ON_LOAD_END),
        MapBuilder.of("registrationName", "onLoadEnd")
    );
  }

  @Override
  protected void onAfterUpdateTransaction(ReactImageView view) {
    super.onAfterUpdateTransaction(view);
    view.maybeUpdateView();
  }

  private static class ImageControllerListener implements ControllerListener {

    private WeakReference<ReactImageView> mImageViewWeakReference;
    private WeakReference<ReactContext> mReactContextWeakReference;

    private ImageControllerListener(ReactContext context, ReactImageView imageView) {
      mImageViewWeakReference = new WeakReference<ReactImageView>(imageView);
      mReactContextWeakReference = new WeakReference<ReactContext>(context);
    }

    @Override
    public void onSubmit(String id, Object callerContext) {
      ReactContext reactContext = mReactContextWeakReference.get();
      ReactImageView view = mImageViewWeakReference.get();
      if (reactContext != null && view != null) {
        reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher().dispatchEvent(new OnLoadStartEvent(view.getId(), System.currentTimeMillis()));
      }
    }

    @Override
    public void onFinalImageSet(String id, @Nullable Object imageInfo, @Nullable Animatable animatable) {
      ReactContext reactContext = mReactContextWeakReference.get();
      ReactImageView view = mImageViewWeakReference.get();
      if (reactContext != null && view != null) {
        reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher().dispatchEvent(new OnLoadEvent(view.getId(), System.currentTimeMillis()));
      }
    }

    @Override
    public void onIntermediateImageSet(String id, @Nullable Object imageInfo) {

    }

    @Override
    public void onIntermediateImageFailed(String id, Throwable throwable) {

    }

    @Override
    public void onFailure(String id, Throwable throwable) {
      ReactContext reactContext = mReactContextWeakReference.get();
      ReactImageView view = mImageViewWeakReference.get();
      if (reactContext != null && view != null) {
        reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher().dispatchEvent(new OnErrorEvent(view.getId(), System.currentTimeMillis(), throwable.getLocalizedMessage()));
      }
    }

    @Override
    public void onRelease(String id) {

    }

  }

}
