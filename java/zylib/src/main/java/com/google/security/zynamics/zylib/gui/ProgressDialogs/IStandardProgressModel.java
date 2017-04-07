// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.ProgressDialogs;

public interface IStandardProgressModel {
  void addProgressListener(IStandardProgressListener listener);

  void closeRequested();

  void removeProgressListener(IStandardProgressListener listener);
}
