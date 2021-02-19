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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.subpanels;

import com.google.security.zynamics.bindiff.enums.EGraph;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.GraphsContainer;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.userview.ViewData;

public class NormalGraphPanel extends GraphPanel {
  public NormalGraphPanel(
      final ViewTabPanelFunctions controller,
      final Diff diff,
      final ViewData view,
      final ESide side) {
    super(controller, diff, view, getSingleGraph(view.getGraphs(), side), getGraphType(side));
  }

  private static EGraph getGraphType(final ESide side) {
    return side == ESide.PRIMARY ? EGraph.PRIMARY_GRAPH : EGraph.SECONDARY_GRAPH;
  }

  private static BinDiffGraph<?, ?> getSingleGraph(final GraphsContainer graphs, final ESide side) {
    if (side == ESide.PRIMARY) {
      return graphs.getPrimaryGraph();
    } else if (side == ESide.SECONDARY) {
      return graphs.getSecondaryGraph();
    }

    throw new IllegalArgumentException("Illegal graph type.");
  }
}
