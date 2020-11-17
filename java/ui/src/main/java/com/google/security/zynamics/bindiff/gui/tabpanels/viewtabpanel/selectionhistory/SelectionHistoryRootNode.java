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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.selectionhistory;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.utils.ResourceUtils;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.JTree;

public class SelectionHistoryRootNode extends AbstractSelectionHistoryTreeNode {
  private static final Icon ICON_ROOT = ResourceUtils.getImageIcon("data/selectionicons/root.png");

  private final ViewTabPanelFunctions controller;
  private JTree tree;
  private final BinDiffGraph<?, ?> graph;

  public SelectionHistoryRootNode(
      final ViewTabPanelFunctions controller, final BinDiffGraph<?, ?> graph, final String name) {
    super(name);

    this.controller = checkNotNull(controller);
    this.graph = checkNotNull(graph);
  }

  @Override
  public ViewTabPanelFunctions getController() {
    return controller;
  }

  public BinDiffGraph<?, ?> getGraph() {
    return graph;
  }

  @Override
  public Icon getIcon() {
    return ICON_ROOT;
  }

  @Override
  public JPopupMenu getPopupMenu() {
    return null;
  }

  @Override
  public JTree getTree() {
    return tree;
  }

  public void setTree(final JTree tree) {
    this.tree = tree;
  }
}
