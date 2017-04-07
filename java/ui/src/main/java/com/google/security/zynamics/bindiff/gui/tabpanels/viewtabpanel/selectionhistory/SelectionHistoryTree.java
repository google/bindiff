package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.selectionhistory;

import com.google.security.zynamics.zylib.gui.jtree.TreeHelpers;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTree;

public class SelectionHistoryTree extends JTree {
  private final InternalMouseListener mouseListener = new InternalMouseListener();

  public SelectionHistoryTree(final SelectionHistoryRootNode rootNode) {
    super(rootNode);

    addMouseListener(mouseListener);
    setCellRenderer(new SelectionHistoryTreeCellRenderer(rootNode.getGraph()));
  }

  private void handleMouseEvent(final MouseEvent event) {
    final AbstractSelectionHistoryTreeNode selectedNode =
        (AbstractSelectionHistoryTreeNode) TreeHelpers.getNodeAt(this, event.getX(), event.getY());

    if (selectedNode == null) {
      return;
    }

    selectedNode.handleMouseEvent(event);
  }

  public void dispose() {
    removeMouseListener(mouseListener);
  }

  private class InternalMouseListener extends MouseAdapter {
    @Override
    public void mousePressed(final MouseEvent event) {
      handleMouseEvent(event);
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
      handleMouseEvent(event);
    }
  }
}
