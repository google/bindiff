package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.selectionhistory;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.utils.ImageUtils;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.JTree;

public class SelectionHistoryRootNode extends AbstractSelectionHistoryTreeNode {
  private static final Icon ICON_ROOT = ImageUtils.getImageIcon("data/selectionicons/root.png");

  private final ViewTabPanelFunctions controller;
  private JTree tree;
  private final BinDiffGraph<?, ?> graph;

  public SelectionHistoryRootNode(
      final ViewTabPanelFunctions controller, final BinDiffGraph<?, ?> graph, final String name) {
    super(name);

    this.controller = Preconditions.checkNotNull(controller);
    this.graph = Preconditions.checkNotNull(graph);
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
