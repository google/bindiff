// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.editmode;

/**
 * Describes a single state change from one mouse state to another mouse state.
 */
public final class CStateChange implements IMouseStateChange {
  /**
   * The next mouse state.
   */
  private final IMouseState m_nextState;

  /**
   * True, to chain the event to yFiles.
   */
  private final boolean m_yfiles;

  /**
   * Creates a new state change object.
   * 
   * @param nextState The next mouse state.
   * @param yfiles True, to chain the event to yFiles.
   */
  public CStateChange(final IMouseState nextState, final boolean yfiles) {
    m_nextState = nextState;
    m_yfiles = yfiles;
  }

  @Override
  public IMouseState getNextState() {
    return m_nextState;
  }

  @Override
  public boolean notifyYFiles() {
    return m_yfiles;
  }
}
