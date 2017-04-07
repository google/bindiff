package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.treenodes;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.popupmenu.NodePopupMenu;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.MatchedFunctionsTreeNodeContextPanel;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.utils.ImageUtils;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;

public final class MatchedFunctionViewsNode extends AbstractTreeNode {
  private static final ImageIcon ICON =
      ImageUtils.getImageIcon("data/treeicons/matched-functions.png");

  private NodePopupMenu popupMenu;

  private final MatchedFunctionsTreeNodeContextPanel component;

  public MatchedFunctionViewsNode(final WorkspaceTabPanelFunctions controller, final Diff diff) {
    super(controller, diff);

    popupMenu = new NodePopupMenu(controller);

    component = new MatchedFunctionsTreeNodeContextPanel(controller, diff);
  }

  @Override
  protected void createChildren() {
    // no children
  }

  @Override
  protected void delete() {
    component.dispose();
    popupMenu.dispose();
    popupMenu = null;
  }

  @Override
  public void doubleClicked() {}

  @Override
  public Component getComponent() {
    return component;
  }

  @Override
  public Icon getIcon() {
    return ICON;
  }

  @Override
  public JPopupMenu getPopupMenu() {
    return popupMenu;
  }

  @Override
  public String toString() {
    return String.format(
        "Matched Functions (%d)", getDiff().getMatches().getSizeOfMatchedFunctions());
  }
}
