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

package com.google.security.zynamics.bindiff.graph.layout.commands;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.exceptions.GraphLayoutException;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.layout.LayoutCommandHelper;
import com.google.security.zynamics.bindiff.graph.settings.GraphLayoutSettings;
import com.google.security.zynamics.zylib.types.common.ICommand;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

public class GraphLayoutInitializer implements ICommand {
  private final BinDiffGraph<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>> graph;

  public GraphLayoutInitializer(
      final BinDiffGraph<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>> graph) {
    checkNotNull(graph);

    this.graph = graph;
  }

  public static void executeStatic(
      final BinDiffGraph<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>> graph)
      throws GraphLayoutException {
    final GraphLayoutSettings settings = graph.getSettings().getLayoutSettings();

    final boolean animated = settings.getAnimateLayout();
    settings.setAnimateLayout(false);
    try {
      ProximityBrowserInitializer.executeStatic(graph);
      if (LayoutCommandHelper.isAutoLayout(graph)) {
        GraphLayoutUpdater.executeStatic(graph, false);
      }
    } finally {
      settings.setAnimateLayout(animated);
    }
  }

  @Override
  public void execute() throws GraphLayoutException {
    executeStatic(graph);
  }
}
