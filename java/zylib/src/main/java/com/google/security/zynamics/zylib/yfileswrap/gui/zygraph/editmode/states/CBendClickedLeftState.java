// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.CStateChange;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseStateChange;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.CStateFactory;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.transformations.CHitBendsTransformer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.transformations.CHitNodesTransformer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import y.base.Edge;
import y.view.Bend;
import y.view.EdgeLabel;
import y.view.HitInfo;

import java.awt.event.MouseEvent;

public class CBendClickedLeftState implements IMouseState {
  private final CStateFactory<?, ?> m_factory;

  private final AbstractZyGraph<?, ?> m_graph;

  private final Bend m_bend;

  public CBendClickedLeftState(final CStateFactory<?, ?> factory,
      final AbstractZyGraph<?, ?> graph, final Bend bend) {
    m_factory = Preconditions.checkNotNull(factory, "Error: factory argument can not be null");
    m_graph = Preconditions.checkNotNull(graph, "Error: graph argument can not be null");
    m_bend = Preconditions.checkNotNull(bend, "Error: bend argument can not be null");
  }

  public AbstractZyGraph<?, ?> getGraph() {
    return m_graph;
  }

  @Override
  public CStateFactory<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>> getStateFactory() {
    return m_factory;
  }

  @Override
  public IMouseStateChange mouseDragged(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return new CStateChange(this, true);
  }

  @Override
  public IMouseStateChange mouseMoved(final MouseEvent event, final AbstractZyGraph<?, ?> g) {
    final double x = g.getEditMode().translateX(event.getX());
    final double y = g.getEditMode().translateY(event.getY());

    final HitInfo hitInfo = g.getGraph().getHitInfo(x, y);

    if (hitInfo.hasHitNodes()) {
      m_factory.createBendExitState(m_bend, event);

      return CHitNodesTransformer.enterNode(m_factory, event, hitInfo);
    } else if (hitInfo.hasHitNodeLabels()) {
      throw new IllegalStateException();
    } else if (hitInfo.hasHitEdges()) {
      m_factory.createBendExitState(m_bend, event);

      final Edge e = hitInfo.getHitEdge();

      return new CStateChange(m_factory.createEdgeEnterState(e, event), true);
    } else if (hitInfo.hasHitEdgeLabels()) {
      m_factory.createBendExitState(m_bend, event);

      final EdgeLabel l = hitInfo.getHitEdgeLabel();

      return new CStateChange(m_factory.createEdgeLabelEnterState(l, event), true);
    } else if (hitInfo.hasHitBends()) {
      return CHitBendsTransformer.changeBend(m_factory, event, hitInfo, m_bend);
    } else if (hitInfo.hasHitPorts()) {
      return new CStateChange(this, true);
    } else {
      m_factory.createBendExitState(m_bend, event);

      return new CStateChange(this, true);
    }
  }

  @Override
  public IMouseStateChange mousePressed(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return new CStateChange(m_factory.createDefaultState(), true);
  }

  @Override
  public IMouseStateChange mouseReleased(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return new CStateChange(m_factory.createDefaultState(), true);
  }
}
