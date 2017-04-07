package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.filter.GraphNodeMultiFilter;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.AbstractGraphNodeTree;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.searcher.TreeNodeSearcher;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter.TreeNodeMultiSorter;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.matches.FunctionMatchData;
import com.google.security.zynamics.bindiff.project.userview.ViewData;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

public abstract class AbstractTreeNode extends DefaultMutableTreeNode {
  private AbstractRootTreeNode rootNode;

  public AbstractTreeNode(final AbstractRootTreeNode rootNode) {
    Preconditions.checkArgument(
        rootNode != null || this instanceof AbstractRootTreeNode,
        "Root node cannot be null for non-root nodes");

    this.rootNode = rootNode;
  }

  protected void delete() {
    for (int i = 0; i < getChildCount(); i++) {
      AbstractTreeNode node = (AbstractTreeNode) getChildAt(i);
      node.delete();
      node = null;
    }
    removeAllChildren();

    rootNode = null;
  }

  protected AbstractRootTreeNode getAbstractRootNode() {
    return rootNode;
  }

  protected Diff getDiff() {
    return rootNode.getDiff();
  }

  protected GraphNodeMultiFilter getFilter() {
    return getRootNode().getFilter();
  }

  protected FunctionMatchData getFunctionMatch() {
    return rootNode.getFunctionMatch();
  }

  protected BinDiffGraph<?, ?> getGraph() {
    return getTree().getGraph();
  }

  protected TreeNodeSearcher getSearcher() {
    return rootNode.getSearcher();
  }

  protected TreeNodeMultiSorter getSorter() {
    return getRootNode().getSorter();
  }

  protected AbstractGraphNodeTree getTree() {
    return rootNode.getTree();
  }

  protected ViewData getView() {
    return rootNode.getView();
  }

  public void createChildren() {
    // Do nothing by default
  }

  public abstract Icon getIcon();

  public JPopupMenu getPopupMenu() {
    return null;
  }

  public AbstractRootTreeNode getRootNode() {
    return getAbstractRootNode();
  }

  public String getTooltipText() {
    // No tool tip text by default
    return null;
  }

  public void handleMouseEvent(final MouseEvent event) {
    if (event.getButton() != MouseEvent.BUTTON3 || !event.isPopupTrigger()) {
      return;
    }
    final JPopupMenu popup = getPopupMenu();
    if (popup != null) {
      popup.show(getTree(), event.getX(), event.getY());
    }
  }

  public boolean isSelected() {
    // By default, tree nodes are not selected
    return false;
  }

  public boolean isVisible() {
    // By default, all tree nodes are visible
    return true;
  }

  @Override
  public abstract String toString();
}
