// Copyright 2011-2022 Google LLC
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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.CStateChange;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseStateChange;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.CStateFactory;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.transformations.CHitEdgesTransformer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import y.base.Edge;
import y.view.HitInfo;

import java.awt.event.MouseEvent;

public class CEdgeClickedRightState<NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    implements IMouseState {
  private final CStateFactory<NodeType, EdgeType> m_factory;

  private final AbstractZyGraph<NodeType, EdgeType> m_graph;

  private final Edge m_edge;

  public CEdgeClickedRightState(final CStateFactory<NodeType, EdgeType> factory,
      final AbstractZyGraph<NodeType, EdgeType> graph, final Edge edge) {
    m_factory = Preconditions.checkNotNull(factory, "Error: factory argument can not be null");
    m_graph = Preconditions.checkNotNull(graph, "Error: graph argument can not be null");
    m_edge = Preconditions.checkNotNull(edge, "Error: edge argument can not be null");
  }

  public ZyGraphEdge<?, ?, ?> getEdge() {
    return m_graph.getEdge(m_edge);
  }

  public AbstractZyGraph<NodeType, EdgeType> getGraph() {
    return m_graph;
  }

  @Override
  public CStateFactory<NodeType, EdgeType> getStateFactory() {
    return m_factory;
  }

  @Override
  public IMouseStateChange mouseDragged(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return new CStateChange(this, true);
  }

  @Override
  public IMouseStateChange mouseMoved(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    final double x = graph.getEditMode().translateX(event.getX());
    final double y = graph.getEditMode().translateY(event.getY());

    final HitInfo hitInfo = graph.getGraph().getHitInfo(x, y);

    if (hitInfo.hasHitEdges()) {
      return CHitEdgesTransformer.changeEdge(m_factory, event, hitInfo, m_edge);
    } else {
      m_factory.createEdgeExitState(m_edge, event);

      return CHitEdgesTransformer.exitEdge(m_factory, event, hitInfo, this);
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
