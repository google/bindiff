// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.ProgressDialogs;

public interface IStandardDescriptionUpdater {
  void next();

  void reset();

  void setDescription(String description);

  void setMaximum(int size);
}
