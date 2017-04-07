// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.types.common;

public interface IIterableCollection<CallbackType> {
  void iterate(final CallbackType callback);
}
