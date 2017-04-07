package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.treenodes;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.popupmenu.NodePopupMenu;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.CallGraphsTreeNodeContextPanel;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.matches.MatchData;
import com.google.security.zynamics.bindiff.utils.ImageUtils;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;

public final class CallGraphNode extends AbstractTreeNode {
  private static final ImageIcon ICON = ImageUtils.getImageIcon("data/treeicons/callgraph.png");

  private final NodePopupMenu popupMenu;

  private final CallGraphsTreeNodeContextPanel component;

  public CallGraphNode(final WorkspaceTabPanelFunctions controller, final Diff diff) {
    super(controller, diff);

    popupMenu = new NodePopupMenu(controller);

    component = new CallGraphsTreeNodeContextPanel(getDiff(), getController());
  }

  @Override
  protected void createChildren() {
    // no children
  }

  @Override
  protected void delete() {
    popupMenu.dispose();
    component.dipose();
  }

  @Override
  public void doubleClicked() {
    final WorkspaceTabPanelFunctions controller = getController();
    controller.openCallgraphView(controller.getMainWindow(), getDiff());
  }

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

    final MatchData matches = getDiff().getMatches();

    return String.format(
        "Call Graph (%d/%d)",
        matches.getSizeOfFunctions(ESide.PRIMARY), matches.getSizeOfFunctions(ESide.SECONDARY));
  }
}
