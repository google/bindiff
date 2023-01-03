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
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.IZyEditModeListener;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CEdgeHighlighter;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CEdgeLabelEnterState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.event.MouseEvent;
import y.view.EdgeLabel;

public class CDefaultEdgeLabelEnterAction<
        NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    implements IStateAction<CEdgeLabelEnterState<NodeType, EdgeType>> {
  /** Highlights the edges that are attached to the label that is entered. */
  protected void highlightEdge(final EdgeLabel label) {
    CEdgeHighlighter.highlightEdge(label.getOwner(), true);
  }

  @Override
  public void execute(
      final CEdgeLabelEnterState<NodeType, EdgeType> state, final MouseEvent event) {
    highlightEdge(state.getLabel());

    final AbstractZyGraph<NodeType, EdgeType> graph = state.getGraph();

    final EdgeLabel label = state.getLabel();

    for (final IZyEditModeListener<NodeType, EdgeType> listener :
        state.getStateFactory().getListeners()) {
      try {
        listener.edgeLabelEntered(label, event);
      } catch (final Exception exception) {
        // TODO: (timkornau): implement logging here.
      }
    }

    graph.updateViews();
  }
}
