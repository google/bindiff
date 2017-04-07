// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.ProgressDialogs;

public interface IStandardProgressListener {
  void changedDescription(String description);

  void changedMaximum(int maximum);

  void finished();

  void next();

  void reset();
}
