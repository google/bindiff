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

import com.google.security.zynamics.zylib.gui.zygraph.editmode.CStateChange;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseStateChange;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.CStateFactory;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import y.base.Node;

import java.awt.event.MouseEvent;

public class CNodePressedLeftState<NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    implements IMouseState {
  private final CStateFactory<NodeType, EdgeType> m_factory;

  private final AbstractZyGraph<NodeType, EdgeType> m_graph;

  private final MouseEvent m_event;

  private final Node m_node;

  public CNodePressedLeftState(final CStateFactory<NodeType, EdgeType> factory,
      final AbstractZyGraph<NodeType, EdgeType> graph, final Node node, final MouseEvent event) {
    m_factory = factory;
    m_graph = graph;
    m_node = node;
    m_event = event;
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

    if (m_graph.getNode(m_node) == null) {
      // Trying to drag a proximity node => Block this because proximity nodes can not be moved

      return new CStateChange(m_factory.createNodeExitState(m_node, event), true);
    } else {
      return new CStateChange(m_factory.createNodeDraggedLeftState(m_node, event, distX, distY),
          false);
    }
  }

  @Override
  public IMouseStateChange mouseMoved(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return new CStateChange(this, true);
  }

  @Override
  public IMouseStateChange mousePressed(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return new CStateChange(this, true);
  }

  @Override
  public IMouseStateChange mouseReleased(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    // A left-click is complete.

    return new CStateChange(m_factory.createNodeClickedLeftState(m_node, event), false);
  }
}
