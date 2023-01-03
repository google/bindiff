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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.filter.GraphNodeMultiFilter;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.AbstractGraphNodeTree;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.searcher.TreeNodeSearcher;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter.TreeNodeMultiSorter;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.userview.ViewData;

public abstract class AbstractRootTreeNode extends AbstractTreeNode {
  private final ViewTabPanelFunctions controller;

  private Diff diff;

  private ViewData view;

  private TreeNodeSearcher searcher;

  private GraphNodeMultiFilter filter;

  private AbstractGraphNodeTree tree;

  private TreeNodeMultiSorter sorter;

  public AbstractRootTreeNode(
      final ViewTabPanelFunctions controller,
      final AbstractGraphNodeTree tree,
      final Diff diff,
      final ViewData view,
      final TreeNodeSearcher searcher,
      final GraphNodeMultiFilter filter,
      final TreeNodeMultiSorter sorter) {
    super(null);

    this.controller = checkNotNull(controller);
    this.tree = checkNotNull(tree);
    this.view = checkNotNull(view);
    this.searcher = checkNotNull(searcher);
    this.filter = checkNotNull(filter);
    this.sorter = checkNotNull(sorter);
    this.diff = diff;
  }

  @Override
  protected Diff getDiff() {
    return diff;
  }

  public void dispose() {
    delete();

    searcher = null;
    filter = null;
    sorter = null;
    view = null;
    tree = null;
    diff = null;
  }

  public ViewTabPanelFunctions getController() {
    return controller;
  }

  @Override
  public GraphNodeMultiFilter getFilter() {
    return filter;
  }

  @Override
  public TreeNodeSearcher getSearcher() {
    return searcher;
  }

  public abstract ESide getSide();

  @Override
  public TreeNodeMultiSorter getSorter() {
    return sorter;
  }

  @Override
  public AbstractGraphNodeTree getTree() {
    return tree;
  }

  @Override
  public ViewData getView() {
    return view;
  }
}
