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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.CStateChange;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseStateChange;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.CStateFactory;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import y.base.Edge;
import y.base.Node;
import y.view.Bend;
import y.view.HitInfo;

public class CMousePressedHandler {
  public static IMouseStateChange handleMousePressed(
      final CStateFactory<?, ?> factory,
      final IMouseState defaultState,
      final AbstractZyGraph<?, ?> graph,
      final MouseEvent event) {
    final double x = graph.getEditMode().translateX(event.getX());
    final double y = graph.getEditMode().translateY(event.getY());

    final HitInfo hitInfo = graph.getGraph().getHitInfo(x, y);

    if (hitInfo.hasHitNodes()) {
      final Node n = hitInfo.getHitNode();

      if (SwingUtilities.isLeftMouseButton(event) && !event.isAltDown()) {
        return new CStateChange(factory.createNodePressedLeftState(n, event), true);
      } else if (SwingUtilities.isRightMouseButton(event)) {
        return new CStateChange(factory.createNodePressedRightState(n, event), true);
      } else if (SwingUtilities.isMiddleMouseButton(event)
          || (event.isAltDown() && SwingUtilities.isLeftMouseButton(event))) {
        return new CStateChange(factory.createNodePressedMiddleState(n, event), false);
      } else {
        // A button was pressed that does not have any special functionality.

        return new CStateChange(defaultState, true);
      }
    } else if (hitInfo.hasHitNodeLabels()) {
      throw new IllegalStateException("yFiles Labels are not in use...");
    } else if (hitInfo.hasHitEdges()) {
      final Edge edge = hitInfo.getHitEdge();

      if (SwingUtilities.isLeftMouseButton(event)) {
        return new CStateChange(factory.createEdgePressedLeftState(edge, event), true);
      } else if (SwingUtilities.isRightMouseButton(event)) {
        return new CStateChange(factory.createEdgePressedRightState(edge, event), true);
      } else {
        return new CStateChange(defaultState, true);
      }
    } else if (hitInfo.hasHitEdgeLabels()) {
      // final EdgeLabel label = hitInfo.getHitEdgeLabel();
      //
      // if (SwingUtilities.isLeftMouseButton(event))
      // {
      // return new CStateChange(factory.createEdgeLabelPressedLeftState(label, event), true);
      // }
      // else if (SwingUtilities.isRightMouseButton(event))
      // {
      // return new CStateChange(factory.createEdgeLabelPressedRightState(label, event), true);
      // }
      // else
      // {
      return new CStateChange(defaultState, true);
      // }
    } else if (hitInfo.hasHitBends()) {
      final Bend bend = hitInfo.getHitBend();

      if (SwingUtilities.isLeftMouseButton(event)) {
        return new CStateChange(factory.createBendPressedLeftState(bend, event), true);
      } else {
        return new CStateChange(defaultState, true);
      }
    } else if (hitInfo.hasHitPorts()) {
      return new CStateChange(factory.createDefaultState(), true);
    } else {
      if (SwingUtilities.isLeftMouseButton(event)) {
        return new CStateChange(factory.createBackgroundPressedLeftState(event), true);
      } else if (SwingUtilities.isRightMouseButton(event)) {
        return new CStateChange(factory.createBackgroundPressedRightState(event), true);
      }

      return new CStateChange(factory.createDefaultState(), true);
    }
  }
}
