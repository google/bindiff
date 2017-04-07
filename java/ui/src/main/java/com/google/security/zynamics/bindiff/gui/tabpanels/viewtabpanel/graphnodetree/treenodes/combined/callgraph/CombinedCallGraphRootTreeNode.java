package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.combined.callgraph;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
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

public class CombinedCallGraphRootTreeNode extends AbstractRootTreeNode {
  private CombinedGraph graph;

  public CombinedCallGraphRootTreeNode(final ViewTabPanelFunctions controller,
      final AbstractGraphNodeTree tree,
      final Diff diff,
      final ViewData view,
      final TreeNodeSearcher searcher,
      final GraphNodeMultiFilter filter,
      final TreeNodeMultiSorter sorter) {
    super(controller, tree, diff, view, searcher, filter, sorter);

    graph = (CombinedGraph) tree.getGraph();

    createChildren();
  }

  @Override
  protected void delete() {
    super.delete();

    graph = null;
  }

  @Override
  public void createChildren() {
    add(new CombinedCallGraphBaseTreeNode(this));
  }

  @Override
  public CombinedGraph getGraph() {
    return graph;
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  public CombinedCallGraphRootTreeNode getRootNode() {
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
