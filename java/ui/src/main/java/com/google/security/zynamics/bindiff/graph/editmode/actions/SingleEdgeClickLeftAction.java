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

package com.google.security.zynamics.bindiff.graph.editmode.actions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.helpers.GraphMover;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CEdgeClickedLeftState;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;

public class SingleEdgeClickLeftAction implements IStateAction<CEdgeClickedLeftState> {
  @Override
  public void execute(final CEdgeClickedLeftState state, final MouseEvent event) {
    checkNotNull(event);
    final AbstractZyGraph<?, ?> graph = checkNotNull(state.getGraph());
    final ZyGraphEdge<?, ?, ?> edge = checkNotNull(state.getEdge());

    if (SwingUtilities.isLeftMouseButton(event)) {
      if (event.isShiftDown()) {
        // Shift-Click => Select edge
        graph.getGraph().setSelected(edge.getEdge(), !edge.isSelected());
      } else if (edge.getSource() != edge.getTarget()) {
        // When the user clicks on an edge, the graph scrolls to either
        // the source node or the target node of the edge, depending on
        // their distance from the visible part of the graph.

        final double x = graph.getView().toWorldCoordX(event.getX());
        final double y = graph.getView().toWorldCoordY(event.getY());

        GraphMover.moveToEdgeNode((BinDiffGraph<?, ?>) graph, edge.getEdge(), x, y);
      }
    }
  }
}
