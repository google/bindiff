// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.transformations;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.CStateChange;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseStateChange;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.CStateFactory;

import y.view.Bend;
import y.view.HitInfo;

import java.awt.event.MouseEvent;

/**
 * Utility class to encapsulate state changes for bends.
 */
public class CHitBendsTransformer {
  /**
   * Changes the state of the bend depending on the current hitInfo.
   * 
   * @param m_factory The state factory for all states.
   * @param event The mouse event that caused the state change.
   * @param hitInfo The information about what was hit.
   * @param oldBend The bend which we come from.
   * 
   * @return The state object that describes the mouse state.
   */
  public static CStateChange changeBend(final CStateFactory<?, ?> m_factory,
      final MouseEvent event, final HitInfo hitInfo, final Bend oldBend) {
    final Bend bend = hitInfo.getHitBend();

    if (bend == oldBend) {
      return new CStateChange(m_factory.createBendHoverState(bend, event), true);
    } else {
      m_factory.createBendExitState(oldBend, event);

      return new CStateChange(m_factory.createBendEnterState(bend, event), true);
    }
  }

  /**
   * Changes the state to bend enter state.
   * 
   * @param m_factory The state factory for all states.
   * @param event The mouse event that caused the state change.
   * @param hitInfo The information about what was hit.
   * 
   * @return The state object that describes the mouse state.
   */
  public static IMouseStateChange enterBend(final CStateFactory<?, ?> m_factory,
      final MouseEvent event, final HitInfo hitInfo) {
    final Bend b = hitInfo.getHitBend();

    return new CStateChange(m_factory.createBendEnterState(b, event), true);
  }
}
