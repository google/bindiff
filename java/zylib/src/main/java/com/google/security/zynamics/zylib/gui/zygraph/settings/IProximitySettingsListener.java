// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.settings;

public interface IProximitySettingsListener {
  /**
   * Invoked after the proximity browsing setting changed.
   * 
   * @param value The new value of the proximity browsing setting.
   */
  void changedProximityBrowsing(boolean value);

  void changedProximityBrowsingDepth(int children, int parents);

  /**
   * Invoked after the proximity browsing frozen setting changed.
   * 
   * @param value The new value of the proximity browsing frozen setting.
   */
  void changedProximityBrowsingFrozen(boolean value);

  /**
   * Invoked after the proximity browsing preview setting changed.
   * 
   * @param value The new value of the proximity browsing preview setting.
   */
  void changedProximityBrowsingPreview(boolean value);
}
