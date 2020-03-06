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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.single.callgraph;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.filter.GraphNodeMultiFilter;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.AbstractGraphNodeTree;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.searcher.TreeNodeSearcher;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter.TreeNodeMultiSorter;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.AbstractRootTreeNode;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.userview.ViewData;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JPopupMenu;

public class SingleCallGraphRootTreeNode extends AbstractRootTreeNode {
  private SingleGraph graph;

  public SingleCallGraphRootTreeNode(final ViewTabPanelFunctions controller,
      final AbstractGraphNodeTree tree,
      final Diff diff,
      final ViewData view,
      final TreeNodeSearcher searcher,
      final GraphNodeMultiFilter filter,
      final TreeNodeMultiSorter sorter) {
    super(controller, tree, diff, view, searcher, filter, sorter);

    graph = (SingleGraph) tree.getGraph();

    createChildren();
  }

  @Override
  protected void delete() {
    super.delete();

    graph = null;
  }

  @Override
  public void createChildren() {
    add(new SingleCallGraphBaseTreeNode(this));
  }

  @Override
  public SingleGraph getGraph() {
    return graph;
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  public JPopupMenu getPopupMenu() {
    return null;
  }

  @Override
  public SingleCallGraphRootTreeNode getRootNode() {
    return this;
  }

  @Override
  public ESide getSide() {
    return graph.getSide();
  }

  @Override
  public String getTooltipText() {
    return null;
  }

  @Override
  public void handleMouseEvent(final MouseEvent event) {
    // do nothing
  }

  @Override
  public boolean isSelected() {
    return false;
  }

  @Override
  public boolean isVisible() {
    return true;
  }

  @Override
  public String toString() {
    return "";
  }
}
