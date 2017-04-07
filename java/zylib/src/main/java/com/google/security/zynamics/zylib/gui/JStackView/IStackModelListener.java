// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.JStackView;

/**
 * Interface for listener objects that want to be notified about changes in the stack data.
 */
public interface IStackModelListener {
  /**
   * Invoked after the data of the stack model changed.
   */
  void dataChanged();
}
