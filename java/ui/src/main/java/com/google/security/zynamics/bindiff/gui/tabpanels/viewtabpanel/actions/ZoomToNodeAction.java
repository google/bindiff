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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.helpers.GraphZoomer;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCombinedFunction;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedBasicBlock;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class ZoomToNodeAction extends AbstractAction {
  private final BinDiffGraph<?, ?> graph;
  private final ZyGraphNode<?> node;

  public ZoomToNodeAction(final BinDiffGraph<?, ?> graph, final ZyGraphNode<?> node) {
    super(getTitel(node));
    this.graph = checkNotNull(graph);
    this.node = checkNotNull(node);
  }

  private static String getTitel(final ZyGraphNode<?> node) {
    if (node != null) {
      if (node.getRawNode() instanceof RawBasicBlock
          || node.getRawNode() instanceof RawCombinedBasicBlock) {
        return "Zoom to Basicblock";
      }
      if (node.getRawNode() instanceof RawFunction
          || node.getRawNode() instanceof RawCombinedFunction) {
        return "Zoom to Function";
      }
    }

    return "Zoom to Node";
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    GraphZoomer.zoomToNode(graph, node);
  }
}
