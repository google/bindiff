// Copyright 2011-2020 Google LLC
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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.combined.flowgraph;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.filter.GraphNodeMultiFilter;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.AbstractGraphNodeTree;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.searcher.TreeNodeSearcher;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter.TreeNodeMultiSorter;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.AbstractFlowGraphRootTreeNode;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.userview.ViewData;


import java.awt.event.MouseEvent;

import javax.swing.Icon;

public class CombinedFlowGraphRootTreeNode extends AbstractFlowGraphRootTreeNode {
  private final CombinedGraph combinedGraph;

  public CombinedFlowGraphRootTreeNode(final ViewTabPanelFunctions controller,
      final AbstractGraphNodeTree tree,
      final Diff diff,
      final ViewData view,
      final TreeNodeSearcher searcher,
      final GraphNodeMultiFilter filter,
      final TreeNodeMultiSorter sorter) {
    super(controller, tree, diff, view, searcher, filter, sorter);

    combinedGraph = (CombinedGraph) tree.getGraph();

    createChildren();
  }

  @Override
  public void createChildren() {
    add(new CombinedFlowGraphBaseTreeNode(this));
  }

  @Override
  public CombinedGraph getGraph() {
    return combinedGraph;
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  public CombinedFlowGraphRootTreeNode getRootNode() {
    return this;
  }

  @Override
  public ESide getSide() {
    return null;
  }

  @Override
  public void handleMouseEvent(final MouseEvent event) {
    // Do nothing
  }

  @Override
  public String toString() {
    return "Root Node";
  }
}
