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
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.transformations.CHitBendsTransformer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.transformations.CHitEdgeLabelsTransformer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.transformations.CHitEdgesTransformer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.transformations.CHitNodesTransformer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import y.view.EdgeLabel;
import y.view.HitInfo;

import java.awt.event.MouseEvent;

public class CEdgeLabelHoverState implements IMouseState {
  /**
   * Used to create the next state when a state change is necessary.
   */
  private final CStateFactory<?, ?> m_factory;

  /**
   * The graph for which the mouse state is tracked.
   */
  private final AbstractZyGraph<?, ?> m_graph;

  /**
   * The node the mouse cursor is hovering over.
   */
  private final EdgeLabel m_label;

  /**
   * Creates a new state object.
   * 
   * @param factory Used to create the next state when a state change is necessary.
   * @param graph The graph for which the mouse state is tracked.
   */
  public CEdgeLabelHoverState(final CStateFactory<?, ?> factory, final AbstractZyGraph<?, ?> graph,
      final EdgeLabel label) {
    m_graph = Preconditions.checkNotNull(graph, "Error: graph argument can not be null");
    m_factory = Preconditions.checkNotNull(factory, "Error: factory argument can not be null");
    m_label = Preconditions.checkNotNull(label, "Error: label argument can not be null");
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
    final double x = m_graph.getEditMode().translateX(event.getX());
    final double y = m_graph.getEditMode().translateY(event.getY());

    final HitInfo hitInfo = m_graph.getGraph().getHitInfo(x, y);

    if (hitInfo.hasHitNodes()) {
      m_factory.createEdgeLabelExitState(m_label, event);

      return CHitNodesTransformer.enterNode(m_factory, event, hitInfo);
    } else if (hitInfo.hasHitNodeLabels()) {
      throw new IllegalStateException();
    } else if (hitInfo.hasHitEdges()) {
      m_factory.createEdgeLabelExitState(m_label, event);

      return CHitEdgesTransformer.enterEdge(m_factory, event, hitInfo);
    } else if (hitInfo.hasHitEdgeLabels()) {
      return CHitEdgeLabelsTransformer.changeEdgeLabel(m_factory, event, hitInfo, m_label);
    } else if (hitInfo.hasHitBends()) {
      m_factory.createEdgeLabelExitState(m_label, event);

      return CHitBendsTransformer.enterBend(m_factory, event, hitInfo);
    } else if (hitInfo.hasHitPorts()) {
      return new CStateChange(this, true);
    } else {
      // We are in the node hover state but now there are no more hit nodes.
      // That means we just left the node and we have to undo all GUI changes
      // we made when entering the node.

      m_factory.createEdgeLabelExitState(m_label, event);

      return new CStateChange(m_factory.createDefaultState(), true);
    }
  }

  @Override
  public IMouseStateChange mousePressed(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    final double x = m_graph.getEditMode().translateX(event.getX());
    final double y = m_graph.getEditMode().translateY(event.getY());

    final HitInfo hitInfo = m_graph.getGraph().getHitInfo(x, y);

    if (hitInfo.hasHitNodes()) {
      throw new IllegalStateException();
    } else if (hitInfo.hasHitNodeLabels()) {
      throw new IllegalStateException();
    } else if (hitInfo.hasHitEdges()) {
      throw new IllegalStateException();
    } else if (hitInfo.hasHitEdgeLabels()) {
      final EdgeLabel label = hitInfo.getHitEdgeLabel();

      if (label == m_label) {
        // if (SwingUtilities.isLeftMouseButton(event))
        // {
        // return new CStateChange(m_factory.createEdgeLabelPressedLeftState(label, event), true);
        // }
        // else
        // {
        // return new CStateChange(m_factory.createEdgeLabelPressedRightState(label, event), true);
        // }
        return new CStateChange(this, true);
      } else {
        throw new IllegalStateException();
      }
    } else if (hitInfo.hasHitBends()) {
      throw new IllegalStateException();
    } else if (hitInfo.hasHitPorts()) {
      return new CStateChange(this, true);
    } else {
      m_factory.createEdgeLabelExitState(m_label, event);

      return new CStateChange(this, true);
    }
  }

  @Override
  public IMouseStateChange mouseReleased(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return new CStateChange(this, true);
  }
}
