// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.ProgressDialogs;

public interface IEndlessProgressModel {
  void addProgressListener(IEndlessProgressListener listener);

  void closeRequested();

  void removeProgressListener(IEndlessProgressListener listener);
}
