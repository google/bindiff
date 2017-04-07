package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.filter.GraphNodeMultiFilter;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.renderers.CombinedTreeNodeRenderer;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.searcher.TreeNodeSearcher;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter.TreeNodeMultiSorter;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.combined.callgraph.CombinedCallGraphRootTreeNode;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.userview.ViewData;
import javax.swing.border.EmptyBorder;

public class CombinedCallGraphTree extends AbstractGraphNodeTree {
  private CombinedGraph combinedGraph;

  public CombinedCallGraphTree(
      final ViewTabPanelFunctions controller,
      final Diff diff,
      final ViewData view,
      final CombinedGraph combinedGraph,
      final TreeNodeSearcher searcher,
      final GraphNodeMultiFilter filter,
      final TreeNodeMultiSorter sorter) {
    super();

    Preconditions.checkNotNull(controller);
    Preconditions.checkNotNull(diff);
    Preconditions.checkNotNull(view);
    Preconditions.checkNotNull(combinedGraph);
    Preconditions.checkNotNull(searcher);
    Preconditions.checkNotNull(filter);
    Preconditions.checkNotNull(sorter);

    this.combinedGraph = combinedGraph;

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
    final CombinedCallGraphRootTreeNode rootNode =
        new CombinedCallGraphRootTreeNode(controller, this, diff, view, searcher, filter, sorter);

    setRootVisible(false);

    getModel().setRoot(rootNode);

    setCellRenderer(new CombinedTreeNodeRenderer());
  }

  @Override
  public void dispose() {
    super.dispose();

    combinedGraph = null;
  }

  @Override
  public CombinedGraph getGraph() {
    return combinedGraph;
  }
}
