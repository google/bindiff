// Copyright 2011-2024 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.CStateChange;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseStateChange;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.CStateFactory;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CMousePressedHandler;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.transformations.CHitNodesTransformer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.event.MouseEvent;
import y.base.Node;
import y.view.HitInfo;

/**
 * This class represents the mouse state that is reached as soon as the user moves the mouse out of
 * a node.
 */
public final class CNodeExitState<
        NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    implements IMouseState {
  /** State factory that creates new state objects when necessary. */
  private final CStateFactory<NodeType, EdgeType> m_factory;

  /** The graph the exited node belongs to. */
  private final AbstractZyGraph<NodeType, EdgeType> m_graph;

  /** The exited node. */
  private final Node m_node;

  /**
   * Creates a new state object.
   *
   * @param factory State factory that creates new state objects when necessary.
   * @param graph The graph the exited node belongs to.
   * @param node The exited node.
   */
  public CNodeExitState(
      final CStateFactory<NodeType, EdgeType> factory,
      final AbstractZyGraph<NodeType, EdgeType> graph,
      final Node node) {
    m_factory = factory;
    m_graph = graph;
    m_node = node;
  }

  /**
   * Returns the graph the exited node belongs to.
   *
   * @return The graph the exited node belongs to.
   */
  public AbstractZyGraph<NodeType, EdgeType> getGraph() {
    return m_graph;
  }

  /**
   * Returns the exited node.
   *
   * @return The exited node.
   */
  public Node getNode() {
    return m_node;
  }

  @Override
  public CStateFactory<NodeType, EdgeType> getStateFactory() {
    return m_factory;
  }

  @Override
  public IMouseStateChange mouseDragged(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return new CStateChange(m_factory.createDefaultState(), true);
  }

  @Override
  public IMouseStateChange mouseMoved(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    final double x = m_graph.getEditMode().translateX(event.getX());
    final double y = m_graph.getEditMode().translateY(event.getY());

    final HitInfo hitInfo = m_graph.getGraph().getHitInfo(x, y);

    if (hitInfo.hasHitNodes()) {
      return CHitNodesTransformer.enterNode(m_factory, event, hitInfo);
    } else {
      return CHitNodesTransformer.exitNode(m_factory, event, hitInfo, this);
    }
  }

  @Override
  public IMouseStateChange mousePressed(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return CMousePressedHandler.handleMousePressed(m_factory, this, graph, event);
  }

  @Override
  public IMouseStateChange mouseReleased(
      final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return new CStateChange(m_factory.createDefaultState(), true);
  }
}
