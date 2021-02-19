// Copyright 2011-2021 Google LLC
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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.actions;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.IZyEditModeListener;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CEdgeHighlighter;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CEdgeLabelExitState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import y.view.EdgeRealizer;

import java.awt.event.MouseEvent;

public class CDefaultEdgeLabelExitAction<NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    implements IStateAction<CEdgeLabelExitState<NodeType, EdgeType>> {
  /**
   * Removes highlighting from the edges attached to a edge label.
   * 
   * @param edge the EdgeRealizer of the edge
   */
  protected void unhighlightEdges(final EdgeRealizer edge) {
    CEdgeHighlighter.highlightEdge(edge, false);
  }

  @Override
  public void execute(final CEdgeLabelExitState<NodeType, EdgeType> state, final MouseEvent event) {
    unhighlightEdges(state.getLabel().getOwner());

    if (state.getLabel() != null) {
      unhighlightEdges(state.getLabel().getOwner());

      for (final IZyEditModeListener<NodeType, EdgeType> listener : state.getStateFactory()
          .getListeners()) {
        try {
          listener.edgeLabelLeft(state.getLabel());
        } catch (final Exception exception) {
          // TODO: (timkornau): implement logging.
        }
      }
    }

    state.getGraph().updateViews();

  }

}
