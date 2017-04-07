package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree;

import com.google.common.base.Preconditions;
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

    Preconditions.checkNotNull(controller);
    Preconditions.checkNotNull(diff);
    Preconditions.checkNotNull(view);
    this.singleGraph = Preconditions.checkNotNull(singleGraph);
    Preconditions.checkNotNull(searcher);
    Preconditions.checkNotNull(filter);
    Preconditions.checkNotNull(sorter);

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
