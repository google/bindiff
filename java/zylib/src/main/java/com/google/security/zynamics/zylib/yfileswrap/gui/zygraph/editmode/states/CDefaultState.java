// Copyright 2011-2023 Google LLC
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
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.transformations.CHitBendsTransformer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.transformations.CHitEdgeLabelsTransformer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.transformations.CHitEdgesTransformer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.transformations.CHitNodesTransformer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.event.MouseEvent;
import y.view.HitInfo;

/**
 * This class describes the mouse state where the cursor is not hovering over any special object and
 * no button is clicked.
 */
public final class CDefaultState implements IMouseState {
  /** Factory class for creating state changes. */
  private final CStateFactory<?, ?> m_factory;

  /**
   * Creates a new default state object.
   *
   * @param factory Factory class for creating state changes.
   */
  public CDefaultState(final CStateFactory<?, ?> factory) {
    m_factory = factory;
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
      return CHitNodesTransformer.enterNode(m_factory, event, hitInfo);
    } else if (hitInfo.hasHitEdges()) {
      return CHitEdgesTransformer.enterEdge(m_factory, event, hitInfo);
    } else if (hitInfo.hasHitEdgeLabels()) {
      return CHitEdgeLabelsTransformer.enterEdgeLabel(m_factory, event, hitInfo);
    } else if (hitInfo.hasHitBends()) {
      return CHitBendsTransformer.enterBend(m_factory, event, hitInfo);
    } else if (hitInfo.hasHitPorts()) {
      return new CStateChange(this, true);
    } else {
      // Nothing was hit at all. The current state remains unchanged.

      return new CStateChange(this, true);
    }
  }

  @Override
  public IMouseStateChange mousePressed(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return CMousePressedHandler.handleMousePressed(m_factory, this, graph, event);
  }

  @Override
  public IMouseStateChange mouseReleased(
      final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return new CStateChange(this, true);
  }
}
