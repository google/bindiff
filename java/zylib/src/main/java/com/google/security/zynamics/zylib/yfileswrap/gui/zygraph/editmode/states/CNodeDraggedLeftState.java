// Copyright 2011-2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.CStateChange;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseStateChange;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.CStateFactory;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.transformations.CHitNodesTransformer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import y.base.Node;
import y.view.HitInfo;

import java.awt.event.MouseEvent;

public class CNodeDraggedLeftState<NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    implements IMouseState {
  private final CStateFactory<NodeType, EdgeType> m_factory;

  private final AbstractZyGraph<NodeType, EdgeType> m_graph;

  private final MouseEvent m_event;

  private final Node m_node;

  private final double m_distX;

  private final double m_distY;

  public CNodeDraggedLeftState(final CStateFactory<NodeType, EdgeType> factory,
      final AbstractZyGraph<NodeType, EdgeType> graph, final Node node, final MouseEvent event) {
    this(factory, graph, node, event, 0, 0);
  }

  public CNodeDraggedLeftState(final CStateFactory<NodeType, EdgeType> factory,
      final AbstractZyGraph<NodeType, EdgeType> graph, final Node node, final MouseEvent event,
      final double distX, final double distY) {
    m_factory = Preconditions.checkNotNull(factory, "Error: factory argument can not be null");
    m_graph = Preconditions.checkNotNull(graph, "Error: graph argument can not be null");
    m_node = Preconditions.checkNotNull(node, "Error: node argument can not be null");
    m_event = Preconditions.checkNotNull(event, "Error: event argument can not be null");

    m_distX = distX;
    m_distY = distY;
  }

  public double getDistanceX() {
    return m_distX;
  }

  public double getDistanceY() {
    return m_distY;
  }

  public AbstractZyGraph<NodeType, EdgeType> getGraph() {
    return m_graph;
  }

  public Node getNode() {
    return m_node;
  }

  @Override
  public CStateFactory<NodeType, EdgeType> getStateFactory() {
    return m_factory;
  }

  @Override
  public IMouseStateChange mouseDragged(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    final double distX =
        m_graph.getEditMode().translateX(event.getX())
            - m_graph.getEditMode().translateX(m_event.getX());
    final double distY =
        m_graph.getEditMode().translateY(event.getY())
            - m_graph.getEditMode().translateY(m_event.getY());

    return new CStateChange(m_factory.createNodeDraggedLeftState(m_node, event, distX, distY),
        false);
  }

  @Override
  public IMouseStateChange mouseMoved(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    final double distX =
        m_graph.getEditMode().translateX(event.getX())
            - m_graph.getEditMode().translateX(m_event.getX());
    final double distY =
        m_graph.getEditMode().translateY(event.getY())
            - m_graph.getEditMode().translateY(m_event.getY());

    return new CStateChange(m_factory.createNodeDraggedLeftState(m_node, event, distX, distY),
        false);
  }

  @Override
  public IMouseStateChange mousePressed(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    final double distX =
        m_graph.getEditMode().translateX(event.getX())
            - m_graph.getEditMode().translateX(m_event.getX());
    final double distY =
        m_graph.getEditMode().translateY(event.getY())
            - m_graph.getEditMode().translateY(m_event.getY());

    return new CStateChange(m_factory.createNodeDraggedLeftState(m_node, event, distX, distY),
        false);
  }

  @Override
  public IMouseStateChange mouseReleased(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    final double x = m_graph.getEditMode().translateX(event.getX());
    final double y = m_graph.getEditMode().translateY(event.getY());

    final HitInfo hitInfo = m_graph.getGraph().getHitInfo(x, y);

    if (hitInfo.hasHitNodes()) {
      return CHitNodesTransformer.changeNode(m_factory, event, hitInfo, m_node);
    } else {
      m_factory.createNodeExitState(m_node, event);

      return CHitNodesTransformer.exitNode(m_factory, event, hitInfo, this);
    }
  }
}
