package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.treenodes;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.popupmenu.NodePopupMenu;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.UnmatchedFunctionsTreeNodeContextPanel;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.matches.MatchData;
import com.google.security.zynamics.bindiff.utils.ImageUtils;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;

public final class UnmatchedFunctionViewsNode extends AbstractTreeNode {
  private static final ImageIcon PRIMARY_ICON =
      ImageUtils.getImageIcon("data/treeicons/primary-unmatched-functions.png");
  private static final ImageIcon SECONDRAY_ICON =
      ImageUtils.getImageIcon("data/treeicons/secondary-unmatched-functions.png");

  private final NodePopupMenu popupMenu;

  private final ESide side;

  private final UnmatchedFunctionsTreeNodeContextPanel component;

  public UnmatchedFunctionViewsNode(
      final WorkspaceTabPanelFunctions controller, final Diff diff, final ESide side) {
    super(controller, diff);

    this.side = side;
    popupMenu = new NodePopupMenu(controller);
    component = new UnmatchedFunctionsTreeNodeContextPanel(diff, getController(), side);
  }

  @Override
  protected void createChildren() {
    // no children
  }

  @Override
  protected void delete() {
    popupMenu.dispose();
  }

  @Override
  public void doubleClicked() {}

  @Override
  public Component getComponent() {
    return component;
  }

  @Override
  public Icon getIcon() {
    return side == ESide.PRIMARY ? PRIMARY_ICON : SECONDRAY_ICON;
  }

  @Override
  public JPopupMenu getPopupMenu() {
    return popupMenu;
  }

  @Override
  public String toString() {
    final MatchData matches = getDiff().getMatches();

    final int unmatched = matches.getSizeOfUnmatchedFunctions(side);
    final int functions = matches.getSizeOfFunctions(side);

    if (side == ESide.PRIMARY) {
      return String.format("Primary Unmatched Functions (%d/%d)", unmatched, functions);
    }

    return String.format("Secondary Unmatched Functions (%d/%d)", unmatched, functions);
  }
}
