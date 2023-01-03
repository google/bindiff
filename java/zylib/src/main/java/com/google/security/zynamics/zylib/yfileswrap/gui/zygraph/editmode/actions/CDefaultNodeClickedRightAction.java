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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.actions;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.helpers.CMouseCursorHelper;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.IZyEditModeListener;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CEditNodeHelper;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodeClickedRightState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.helpers.ProximityHelper;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyProximityNode;
import java.awt.event.MouseEvent;
import y.base.Node;

public class CDefaultNodeClickedRightAction<
        NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    implements IStateAction<CNodeClickedRightState<NodeType, EdgeType>> {
  @Override
  public void execute(
      final CNodeClickedRightState<NodeType, EdgeType> state, final MouseEvent event) {
    CMouseCursorHelper.setDefaultCursor(state.getGraph());

    final AbstractZyGraph<NodeType, EdgeType> graph = state.getGraph();

    final Node yNode = state.getNode();

    final NodeType node = graph.getNode(yNode);

    final double x = graph.getEditMode().translateX(event.getX());
    final double y = graph.getEditMode().translateY(event.getY());

    if (graph.getEditMode().getLabelEventHandler().isActive()
        && graph.getEditMode().getLabelEventHandler().hasEmptySelection()) {
      CEditNodeHelper.removeCaret(graph);
    }

    if (node == null) {
      // Proximity node was clicked

      final ZyProximityNode<?> proximityNode =
          ProximityHelper.getProximityNode(graph.getGraph(), yNode);

      for (final IZyEditModeListener<NodeType, EdgeType> listener :
          state.getStateFactory().getListeners()) {
        try {
          listener.proximityBrowserNodeClicked(proximityNode, event, x, y);
        } catch (final Exception exception) {
          // TODO: (timkornau): implement logging here.
        }
      }
    } else {
      for (final IZyEditModeListener<NodeType, EdgeType> listener :
          state.getStateFactory().getListeners()) {
        try {
          listener.nodeClicked(node, event, x, y);
        } catch (final Exception exception) {
          // TODO: (timkornau): implement logging here.
        }
      }
    }
  }
}
