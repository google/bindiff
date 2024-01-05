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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.transformations;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.CStateChange;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseStateChange;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.CStateFactory;
import java.awt.event.MouseEvent;
import y.base.Edge;
import y.view.HitInfo;

/** Utility class to encapsulate state changes for edges */
public class CHitEdgesTransformer {
  /**
   * Changes the state of the edge depending on the current hitInfo.
   *
   * @param m_factory The state factory for all states.
   * @param event The mouse event that caused the state change.
   * @param hitInfo The information about what was hit.
   * @param oldEdge The edge which we come from.
   * @return The state object that describes the mouse state.
   */
  public static IMouseStateChange changeEdge(
      final CStateFactory<?, ?> m_factory,
      final MouseEvent event,
      final HitInfo hitInfo,
      final Edge oldEdge) {
    final Edge edge = hitInfo.getHitEdge();

    if (edge == oldEdge) {
      return new CStateChange(m_factory.createEdgeHoverState(edge, event), true);
    } else {
      m_factory.createEdgeExitState(oldEdge, event);

      return new CStateChange(m_factory.createEdgeEnterState(edge, event), true);
    }
  }

  /**
   * Changes the state to edge enter state.
   *
   * @param m_factory The state factory for all states.
   * @param event The mouse event that caused the state change.
   * @param hitInfo The information about what was hit.
   * @return The state object that describes the mouse state.
   */
  public static IMouseStateChange enterEdge(
      final CStateFactory<?, ?> m_factory, final MouseEvent event, final HitInfo hitInfo) {
    final Edge e = hitInfo.getHitEdge();

    return new CStateChange(m_factory.createEdgeEnterState(e, event), true);
  }

  /**
   * Changes the state depending on the hit information from an edge state to a non edge state.
   *
   * @param m_factory The state factory for all states.
   * @param event The mouse event that caused the state change.
   * @param hitInfo The information about what was hit.
   * @param state The state which we come from.
   * @return The state object that describes the mouse state.
   */
  public static IMouseStateChange exitEdge(
      final CStateFactory<?, ?> m_factory,
      final MouseEvent event,
      final HitInfo hitInfo,
      final IMouseState state) {
    if (hitInfo.hasHitNodes()) {
      return CHitNodesTransformer.enterNode(m_factory, event, hitInfo);
    } else if (hitInfo.hasHitNodeLabels()) {
      throw new IllegalStateException();
    } else if (hitInfo.hasHitEdgeLabels()) {
      return CHitEdgeLabelsTransformer.enterEdgeLabel(m_factory, event, hitInfo);
    } else if (hitInfo.hasHitBends()) {
      return CHitBendsTransformer.enterBend(m_factory, event, hitInfo);
    } else if (hitInfo.hasHitPorts()) {
      return new CStateChange(state, true);
    } else {
      return new CStateChange(m_factory.createDefaultState(), true);
    }
  }
}
