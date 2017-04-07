// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.transformations;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.CStateChange;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseStateChange;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.CStateFactory;

import y.view.HitInfo;

import java.awt.event.MouseEvent;

/**
 * Utility class to encapsulate state changes for the background.
 */
public class CHitBackgroundTransformer {
  public static IMouseStateChange exitBackground(final CStateFactory<?, ?> m_factory,
      final MouseEvent event, final AbstractZyGraph<?, ?> graph, final IMouseState state) {
    final double x = graph.getEditMode().translateX(event.getX());
    final double y = graph.getEditMode().translateY(event.getY());

    final HitInfo hitInfo = graph.getGraph().getHitInfo(x, y);

    if (hitInfo.hasHitNodes()) {
      return CHitNodesTransformer.enterNode(m_factory, event, hitInfo);
    } else if (hitInfo.hasHitNodeLabels()) {
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
      // Nothing was hit at all. The current state remains unchanged.

      return new CStateChange(m_factory.createDefaultState(), true);
    }
  }
}
