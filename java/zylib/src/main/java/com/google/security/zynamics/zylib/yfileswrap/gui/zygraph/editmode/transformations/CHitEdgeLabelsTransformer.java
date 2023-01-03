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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.transformations;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.CStateChange;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseStateChange;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.CStateFactory;
import java.awt.event.MouseEvent;
import y.view.EdgeLabel;
import y.view.HitInfo;

/** Utility class to encapsulate state changes for edge labels. */
public class CHitEdgeLabelsTransformer {
  /**
   * Changes the state of the edge label depending on the current hitInfo.
   *
   * @param m_factory The state factory for all states.
   * @param event The mouse event that caused the state change.
   * @param hitInfo The information about what was hit.
   * @param oldLabel The edge label which we come from.
   * @return The state object that describes the mouse state.
   */
  public static CStateChange changeEdgeLabel(
      final CStateFactory<?, ?> m_factory,
      final MouseEvent event,
      final HitInfo hitInfo,
      final EdgeLabel oldLabel) {
    final EdgeLabel label = hitInfo.getHitEdgeLabel();

    if (label == oldLabel) {
      return new CStateChange(m_factory.createEdgeLabelHoverState(label, event), true);
    } else {
      m_factory.createEdgeLabelExitState(oldLabel, event);

      return new CStateChange(m_factory.createEdgeLabelEnterState(label, event), true);
    }
  }

  /**
   * Changes the state to edge label enter state.
   *
   * @param m_factory The state factory for all states.
   * @param event The mouse event that caused the state change.
   * @param hitInfo The information about what was hit.
   * @return The state object that describes the mouse state.
   */
  public static IMouseStateChange enterEdgeLabel(
      final CStateFactory<?, ?> m_factory, final MouseEvent event, final HitInfo hitInfo) {
    final EdgeLabel l = hitInfo.getHitEdgeLabel();

    return new CStateChange(m_factory.createEdgeLabelEnterState(l, event), true);
  }
}
