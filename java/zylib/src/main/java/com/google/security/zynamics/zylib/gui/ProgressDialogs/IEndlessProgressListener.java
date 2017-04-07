// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.ProgressDialogs;

public interface IEndlessProgressListener {
  void changedDescription(String description);

  void changedGeneralDescription(String description);

  void finished();
}
