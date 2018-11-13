// Generated code from Butter Knife. Do not modify!
package com.maxlife.customvideorecorder;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.Injector;

public class CameraActivity$$ViewInjector<T extends com.maxlife.customvideorecorder.CameraActivity> implements Injector<T> {
  @Override public void inject(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131558531, "field 'capture'");
    target.capture = finder.castView(view, 2131558531, "field 'capture'");
    view = finder.findRequiredView(source, 2131558530, "field 'switchCamera'");
    target.switchCamera = finder.castView(view, 2131558530, "field 'switchCamera'");
    view = finder.findRequiredView(source, 2131558525, "field 'cameraPreview'");
    target.cameraPreview = finder.castView(view, 2131558525, "field 'cameraPreview'");
    view = finder.findRequiredView(source, 2131558526, "field 'buttonQuality'");
    target.buttonQuality = finder.castView(view, 2131558526, "field 'buttonQuality'");
    view = finder.findRequiredView(source, 2131558529, "field 'listOfQualities'");
    target.listOfQualities = finder.castView(view, 2131558529, "field 'listOfQualities'");
    view = finder.findRequiredView(source, 2131558532, "field 'buttonFlash'");
    target.buttonFlash = finder.castView(view, 2131558532, "field 'buttonFlash'");
    view = finder.findRequiredView(source, 2131558528, "field 'chronoRecordingImage'");
    target.chronoRecordingImage = finder.castView(view, 2131558528, "field 'chronoRecordingImage'");
    view = finder.findRequiredView(source, 2131558527, "field 'chrono'");
    target.chrono = finder.castView(view, 2131558527, "field 'chrono'");
  }

  @Override public void reset(T target) {
    target.capture = null;
    target.switchCamera = null;
    target.cameraPreview = null;
    target.buttonQuality = null;
    target.listOfQualities = null;
    target.buttonFlash = null;
    target.chronoRecordingImage = null;
    target.chrono = null;
  }
}
