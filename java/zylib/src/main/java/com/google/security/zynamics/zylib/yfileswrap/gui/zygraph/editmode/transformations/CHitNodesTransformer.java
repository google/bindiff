// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.transformations;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.CStateChange;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseStateChange;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.CStateFactory;

import y.base.Node;
import y.view.HitInfo;

import java.awt.event.MouseEvent;

/**
 * Utility class to encapsulate state changes for nodes.
 */
public final class CHitNodesTransformer {
  /**
   * Changes the state of the node depending on the current hitInfo.
   * 
   * @param m_factory The state factory for all states.
   * @param event The mouse event that caused the state change.
   * @param hitInfo The information about what was hit.
   * @param oldNode The node which we come from.
   * 
   * @return The state object that describes the mouse state.
   */
  public static CStateChange changeNode(final CStateFactory<?, ?> m_factory,
      final MouseEvent event, final HitInfo hitInfo, final Node oldNode) {
    final Node node = hitInfo.getHitNode();

    if (node == oldNode) {
      // TODO @Nils please check if this change does not break BinDiff behavior
      // It fixes case 4207.
      return new CStateChange(m_factory.createNodeHoverState(node, event), false);
    } else {
      m_factory.createNodeExitState(oldNode, event);

      return new CStateChange(m_factory.createNodeEnterState(node, event), true);
    }
  }

  /**
   * Changes the state to node enter state.
   * 
   * @param m_factory The state factory for all states.
   * @param event The mouse event that caused the state change.
   * @param hitInfo The information about what was hit.
   * 
   * @return The state object that describes the mouse state.
   */
  public static IMouseStateChange enterNode(final CStateFactory<?, ?> m_factory,
      final MouseEvent event, final HitInfo hitInfo) {
    final Node n = hitInfo.getHitNode();

    return new CStateChange(m_factory.createNodeEnterState(n, event), true);
  }

  /**
   * Changes the state depending on the hit information from a node state to a non node state.
   * 
   * @param m_factory The state factory for all states.
   * @param event The mouse event that caused the state change.
   * @param hitInfo The information about what was hit.
   * @param state The state which we come from.
   * 
   * @return The state object that describes the mouse state.
   */
  public static IMouseStateChange exitNode(final CStateFactory<?, ?> m_factory,
      final MouseEvent event, final HitInfo hitInfo, final IMouseState state) {
    if (hitInfo.hasHitNodeLabels()) {
      throw new IllegalStateException();
    } else if (hitInfo.hasHitEdges()) {
      return CHitEdgesTransformer.enterEdge(m_factory, event, hitInfo);
    } else if (hitInfo.hasHitEdgeLabels()) {
      return CHitEdgeLabelsTransformer.enterEdgeLabel(m_factory, event, hitInfo);
    } else if (hitInfo.hasHitBends()) {
      return CHitBendsTransformer.enterBend(m_factory, event, hitInfo);
    } else if (hitInfo.hasHitPorts()) {
      return new CStateChange(state, true);
    } else {
      // TODO @Nils please check if this change does not break BinDiff behavior
      // It fixes case 4207.
      // return new CStateChange(m_factory.createDefaultState(), true);
      return new CStateChange(m_factory.createDefaultState(), false);
    }
  }
}
