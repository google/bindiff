// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.editmode;

/**
 * Interface for objects that want to be used to describe mouse state changes.
 */
public interface IMouseStateChange {
  /**
   * Returns the next state.
   * 
   * @return The next state.
   */
  IMouseState getNextState();

  /**
   * Determines whether the event should be changed to yFiles.
   * 
   * @return True, if the event should be chained to yFiles. False, otherwise.
   */
  boolean notifyYFiles();
}
