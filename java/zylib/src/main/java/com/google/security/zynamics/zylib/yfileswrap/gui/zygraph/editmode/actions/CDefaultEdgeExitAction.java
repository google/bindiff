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
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CEdgeHighlighter;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CEdgeExitState;
import java.awt.event.MouseEvent;
import y.base.Edge;
import y.view.Graph2D;

public class CDefaultEdgeExitAction implements IStateAction<CEdgeExitState> {
  protected void clearTooltip(final AbstractZyGraph<?, ?> graph) {
    graph.getView().setToolTipText(null);
  }

  protected void unhighlightEdge(final Edge edge) {
    CEdgeHighlighter.highlightEdge(((Graph2D) edge.getGraph()).getRealizer(edge), false);
  }

  @Override
  public void execute(final CEdgeExitState state, final MouseEvent event) {
    clearTooltip(state.getGraph());

    // This check is necessary because it is possible for edges to be deleted
    // while the mouse is hovering over them. In that case unhighlighting
    // a deleted edge causes problems.
    if (state.getEdge().getGraph() != null) {
      unhighlightEdge(state.getEdge());
    }

    state.getGraph().updateViews();
  }
}
