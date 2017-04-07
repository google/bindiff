// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.editmode;

import java.awt.event.MouseEvent;

/**
 * Interface for all objects to be used as default actions that are executed as soon as a state
 * change was triggered.
 * 
 * @param <T> The type of the state change object.
 */
public interface IStateAction<T> {
  /**
   * Executes an action.
   * 
   * @param state The new state.
   * @param event The event that led to the new state.
   */
  void execute(T state, MouseEvent event);
}
