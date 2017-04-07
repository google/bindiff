// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.transformations;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.CStateChange;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseStateChange;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.CStateFactory;

import y.view.EdgeLabel;
import y.view.HitInfo;

import java.awt.event.MouseEvent;

/**
 * Utility class to encapsulate state changes for edge labels.
 */
public class CHitEdgeLabelsTransformer {
  /**
   * Changes the state of the edge label depending on the current hitInfo.
   * 
   * @param m_factory The state factory for all states.
   * @param event The mouse event that caused the state change.
   * @param hitInfo The information about what was hit.
   * @param oldLabel The edge label which we come from.
   * 
   * @return The state object that describes the mouse state.
   */
  public static CStateChange changeEdgeLabel(final CStateFactory<?, ?> m_factory,
      final MouseEvent event, final HitInfo hitInfo, final EdgeLabel oldLabel) {
    final EdgeLabel label = hitInfo.getHitEdgeLabel();

    if (label == oldLabel) {
      return new CStateChange(m_factory.createEdgeLabelHoverState(label, event), true);
    } else {
      m_factory.createEdgeLabelExitState(oldLabel, event);

      return new CStateChange(m_factory.createEdgeLabelEnterState(label, event), true);
    }
  }

  /**
   * Changes the state to edge label enter state.
   * 
   * @param m_factory The state factory for all states.
   * @param event The mouse event that caused the state change.
   * @param hitInfo The information about what was hit.
   * 
   * @return The state object that describes the mouse state.
   */
  public static IMouseStateChange enterEdgeLabel(final CStateFactory<?, ?> m_factory,
      final MouseEvent event, final HitInfo hitInfo) {
    final EdgeLabel l = hitInfo.getHitEdgeLabel();

    return new CStateChange(m_factory.createEdgeLabelEnterState(l, event), true);
  }
}
