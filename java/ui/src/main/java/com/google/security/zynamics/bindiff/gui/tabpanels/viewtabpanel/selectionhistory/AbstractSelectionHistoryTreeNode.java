package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.selectionhistory;

import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

public abstract class AbstractSelectionHistoryTreeNode extends DefaultMutableTreeNode {
  public AbstractSelectionHistoryTreeNode(final String name) {
    super(name);
  }

  public ViewTabPanelFunctions getController() {
    return getRootNode().getController();
  }

  public abstract Icon getIcon();

  public abstract JPopupMenu getPopupMenu();

  public SelectionHistoryRootNode getRootNode() {
    return (SelectionHistoryRootNode) getRoot();
  }

  public JTree getTree() {
    return getRootNode().getTree();
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
}
