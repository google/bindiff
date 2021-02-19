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

package com.google.security.zynamics.bindiff.graph.layout.commands;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.exceptions.GraphLayoutException;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.layout.LayoutCommandHelper;
import com.google.security.zynamics.zylib.types.common.ICommand;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

public class ProximityBrowserActivator implements ICommand {
  private final BinDiffGraph<ZyGraphNode<?>, ?> graph;

  public ProximityBrowserActivator(final BinDiffGraph<ZyGraphNode<?>, ?> graph) {
    this.graph = checkNotNull(graph);
  }

  public static void executeStatic(final BinDiffGraph<ZyGraphNode<?>, ?> graph)
      throws GraphLayoutException {
    graph.getCombinedGraph().getProximityBrowser().addSettingsListener();
    graph.getSuperGraph().getProximityBrowser().addSettingsListener();

    try {
      graph.getSettings().getProximitySettings().setProximityBrowsing(true);
    } finally {
      graph.getSuperGraph().getProximityBrowser().removeSettingsListener();
      graph.getCombinedGraph().getProximityBrowser().removeSettingsListener();
    }

    if (!LayoutCommandHelper.isProximityBrowsingFrozen(graph)) {
      if (LayoutCommandHelper.hasSelectedNodes(graph)) {
        ProximityBrowserUpdater.executeStatic(graph);

        if (graph.getSettings().getLayoutSettings().getAutomaticLayouting()
            && !graph.getSettings().getProximitySettings().getProximityBrowsingFrozen()) {
          GraphLayoutUpdater.executeStatic(graph, true);
        }
      }

      GraphViewUpdater.updateViews(graph);
    }
  }

  @Override
  public void execute() throws GraphLayoutException {
    executeStatic(graph);
  }
}
