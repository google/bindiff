// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.settings;

public interface IDisplaySettings {
  void addListener(IDisplaySettingsListener listener);

  int getAnimationSpeed();

  boolean getMagnifyingGlassMode();

  void removeListener(IDisplaySettingsListener listener);

  void setMagnifyingGlassMode(boolean value);
}
