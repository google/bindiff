package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.single.flowgraph;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
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
import javax.swing.JPopupMenu;

public class SingleFlowGraphRootTreeNode extends AbstractFlowGraphRootTreeNode {
  public SingleFlowGraphRootTreeNode(final ViewTabPanelFunctions controller,
      final AbstractGraphNodeTree tree,
      final Diff diff,
      final ViewData view,
      final TreeNodeSearcher searcher,
      final GraphNodeMultiFilter filter,
      final TreeNodeMultiSorter sorter) {
    super(controller, tree, diff, view, searcher, filter, sorter);

    createChildren();
  }

  @Override
  public void createChildren() {
    if (getView().getRawGraph(getSide()) != null) {
      add(new SingleFlowGraphBaseTreeNode(this));
    }
  }

  @Override
  public SingleGraph getGraph() {
    return (SingleGraph) getTree().getGraph();
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
  public SingleFlowGraphRootTreeNode getRootNode() {
    return this;
  }

  @Override
  public ESide getSide() {
    return getGraph().getSide();
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
