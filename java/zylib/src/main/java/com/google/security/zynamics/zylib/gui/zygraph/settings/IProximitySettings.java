// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.settings;

public interface IProximitySettings {
  void addListener(IProximitySettingsListener listener);

  boolean getProximityBrowsing();

  int getProximityBrowsingChildren();

  boolean getProximityBrowsingFrozen();

  int getProximityBrowsingParents();

  void removeListener(IProximitySettingsListener listener);
}
