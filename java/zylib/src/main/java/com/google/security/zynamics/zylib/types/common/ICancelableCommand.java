// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.types.common;

public interface ICancelableCommand extends ICommand {
  void cancel() throws Exception;

  boolean wasCanceled();
}
