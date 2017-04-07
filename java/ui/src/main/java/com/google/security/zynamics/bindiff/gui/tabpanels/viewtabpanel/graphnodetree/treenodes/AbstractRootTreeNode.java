package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes;

import com.google.common.base.Preconditions;
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

    this.controller = Preconditions.checkNotNull(controller);
    this.tree = Preconditions.checkNotNull(tree);
    this.view = Preconditions.checkNotNull(view);
    this.searcher = Preconditions.checkNotNull(searcher);
    this.filter = Preconditions.checkNotNull(filter);
    this.sorter = Preconditions.checkNotNull(sorter);
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
