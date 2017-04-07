// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseStateChange;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.CStateFactory;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import y.base.Node;

import java.awt.event.MouseEvent;

public class CNodeEditEnterState implements IMouseState {
  /**
   * State factory that creates new state objects when necessary.
   */
  private final CStateFactory<?, ?> m_factory;

  /**
   * The graph the entered node belongs to.
   */
  private final AbstractZyGraph<?, ?> m_graph;

  /**
   * The entered node.
   */
  private final Node m_node;

  /**
   * Creates a new state object.
   * 
   * @param factory State factory that creates new state objects when necessary.
   * @param graph The graph the entered node belongs to.
   * @param node The entered node.
   */
  public CNodeEditEnterState(final CStateFactory<?, ?> factory, final AbstractZyGraph<?, ?> graph,
      final Node node) {
    m_factory = factory;
    m_graph = graph;
    m_node = node;
  }

  /**
   * Returns the graph the entered node belongs to.
   * 
   * @return The graph the entered node belongs to.
   */
  public AbstractZyGraph<?, ?> getGraph() {
    return m_graph;
  }

  /**
   * Returns the entered node.
   * 
   * @return The entered node.
   */
  public Node getNode() {
    return m_node;
  }

  @Override
  public CStateFactory<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>> getStateFactory() {
    return m_factory;
  }

  @Override
  public IMouseStateChange mouseDragged(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public IMouseStateChange mouseMoved(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public IMouseStateChange mousePressed(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public IMouseStateChange mouseReleased(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    throw new IllegalStateException("Not yet implemented");
  }
}
