package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.treenodes;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.popupmenu.NodePopupMenu;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.DiffTreeNodeContextPanel;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.diff.DiffListenerAdapter;
import com.google.security.zynamics.bindiff.utils.ImageUtils;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;

public final class DiffNode extends AbstractTreeNode {
  private static final ImageIcon ICON_UNLOADED =
      ImageUtils.getImageIcon("data/treeicons/diff-unloaded.png");
  private static final ImageIcon ICON_LOADED = ImageUtils.getImageIcon("data/treeicons/diff.png");
  private static final ImageIcon ICON_MISSING_DIFF_BINARAY =
      ImageUtils.getImageIcon("data/treeicons/missing-diff-db.png");

  private final InternalDiffModelListener diffModelListener = new InternalDiffModelListener();

  private final WorkspaceTabPanelFunctions controller;

  private final NodePopupMenu popupMenu;

  private final DiffTreeNodeContextPanel component;

  public DiffNode(final Diff diff, final WorkspaceTabPanelFunctions controller) {
    super(controller, diff);

    // Cannot be null here, super() checks that
    this.controller = controller;
    popupMenu = new NodePopupMenu(controller);
    component = new DiffTreeNodeContextPanel(diff, controller);

    diff.addListener(diffModelListener);
  }

  @Override
  protected void createChildren() {
    add(new CallGraphNode(getController(), getDiff()));
    add(new MatchedFunctionViewsNode(getController(), getDiff()));
    add(new UnmatchedFunctionViewsNode(getController(), getDiff(), ESide.PRIMARY));
    add(new UnmatchedFunctionViewsNode(getController(), getDiff(), ESide.SECONDARY));
  }

  @Override
  protected void delete() {
    getDiff().removeListener(diffModelListener);
    popupMenu.dispose();

    deleteChildren();
  }

  @Override
  public void doubleClicked() {
    controller.loadDiff(getDiff());
  }

  @Override
  public Component getComponent() {
    return component;
  }

  @Override
  public Icon getIcon() {
    if (!getDiff().getMatchesDatabase().exists()
        || !getDiff().getExportFile(ESide.PRIMARY).exists()
        || !getDiff().getExportFile(ESide.SECONDARY).exists()) {
      return ICON_MISSING_DIFF_BINARAY;
    }

    return getDiff().isLoaded() ? ICON_LOADED : ICON_UNLOADED;
  }

  @Override
  public JPopupMenu getPopupMenu() {
    return popupMenu;
  }

  @Override
  public String toString() {
    return getDiff().getMatchesDatabase().getParentFile().getName();
  }

  private class InternalDiffModelListener extends DiffListenerAdapter {
    @Override
    public void loadedDiff(final Diff diff) {
      if (!diff.isFunctionDiff()) {
        createChildren();
        getTree().expandPath(new TreePath(getPath()));
      }
    }

    @Override
    public void removedDiff(final Diff diff) {
      if (!diff.isFunctionDiff()) {
        removeFromParent();
        delete();
        getTree().setSelectionPath(new TreePath(getRoot().getPath()));
        // TODO(nilsheumer): Why isn't the tree updated automatically when the model has changed?
        getTree().updateTree();
      }
    }

    @Override
    public void unloadedDiff(final Diff diff) {
      if (!diff.isFunctionDiff()) {
        deleteChildren();
        getTree().setSelectionPath(new TreePath(getPath()));
        // TODO(nilsheumer): Why isn't the tree updated automatically when the model has changed.?
        getTree().updateTree();
      }
    }
  }
}
