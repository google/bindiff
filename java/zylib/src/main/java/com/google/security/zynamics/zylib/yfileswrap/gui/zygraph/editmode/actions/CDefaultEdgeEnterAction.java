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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.actions;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CEdgeHighlighter;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CTooltipUpdater;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CEdgeEnterState;
import java.awt.event.MouseEvent;
import y.base.Edge;
import y.view.Graph2D;

public class CDefaultEdgeEnterAction implements IStateAction<CEdgeEnterState> {
  protected void highlightEdge(final Edge edge) {
    CEdgeHighlighter.highlightEdge(((Graph2D) edge.getGraph()).getRealizer(edge), true);
  }

  protected void updateTooltip(final AbstractZyGraph<?, ?> graph, final Edge edge) {
    CTooltipUpdater.updateEdgeTooltip(graph, edge);
  }

  @Override
  public void execute(final CEdgeEnterState state, final MouseEvent event) {
    highlightEdge(state.getEdge());
    updateTooltip(state.getGraph(), state.getEdge());

    state.getGraph().updateViews();
  }
}
