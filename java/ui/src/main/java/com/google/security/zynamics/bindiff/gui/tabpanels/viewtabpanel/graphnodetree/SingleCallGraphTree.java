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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.filter.GraphNodeMultiFilter;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.renderers.SingleTreeNodeRenderer;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.searcher.TreeNodeSearcher;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter.TreeNodeMultiSorter;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.single.callgraph.SingleCallGraphRootTreeNode;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.userview.ViewData;
import javax.swing.border.EmptyBorder;

public class SingleCallGraphTree extends AbstractGraphNodeTree {
  private SingleGraph singleGraph;

  public SingleCallGraphTree(
      final ViewTabPanelFunctions controller,
      final Diff diff,
      final ViewData view,
      final SingleGraph singleGraph,
      final TreeNodeSearcher searcher,
      final GraphNodeMultiFilter filter,
      final TreeNodeMultiSorter sorter) {
    super();

    checkNotNull(controller);
    checkNotNull(diff);
    checkNotNull(view);
    this.singleGraph = checkNotNull(singleGraph);
    checkNotNull(searcher);
    checkNotNull(filter);
    checkNotNull(sorter);

    createTree(controller, diff, view, searcher, filter, sorter);

    setBorder(new EmptyBorder(1, 1, 1, 1));

    addListeners();

    expandRow(0);
  }

  private void createTree(
      final ViewTabPanelFunctions controller,
      final Diff diff,
      final ViewData view,
      final TreeNodeSearcher searcher,
      final GraphNodeMultiFilter filter,
      final TreeNodeMultiSorter sorter) {
    final SingleCallGraphRootTreeNode rootNode =
        new SingleCallGraphRootTreeNode(controller, this, diff, view, searcher, filter, sorter);

    setRootVisible(false);

    getModel().setRoot(rootNode);

    setCellRenderer(new SingleTreeNodeRenderer());
  }

  @Override
  public void dispose() {
    super.dispose();

    singleGraph = null;
  }

  @Override
  public SingleGraph getGraph() {
    return singleGraph;
  }
}
