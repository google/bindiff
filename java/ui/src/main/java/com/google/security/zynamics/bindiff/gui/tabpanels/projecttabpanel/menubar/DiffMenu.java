package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.menubar;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.AddDiffAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.CloseDiffAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.DeleteDiffAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.DirectoryDiffAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.LoadDiffAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.NewDiffAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.IWorkspaceTreeListener;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.treenodes.AbstractTreeNode;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.project.WorkspaceAdapter;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.diff.DiffListenerAdapter;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

public class DiffMenu extends JMenu {
  private final JMenuItem newDiff;
  private final JMenuItem dirDiff;
  private final JMenuItem addDiff;
  private final JMenuItem openDiff;
  private final JMenuItem closeDiff;
  private final JMenuItem deleteDiff;

  private final InternalWorkspaceTreeListener workspaceTreeListener =
      new InternalWorkspaceTreeListener();

  private final InternalWorkspaceListener workspaceListener = new InternalWorkspaceListener();

  private final InternalDiffListener diffListener = new InternalDiffListener();

  private final WorkspaceTabPanelFunctions controller;

  private Diff lastSelectedDiff = null;

  public DiffMenu(final WorkspaceTabPanelFunctions controller) {
    super("Diffs");
    this.controller = Preconditions.checkNotNull(controller);

    setMnemonic('D');
    setEnabled(false);

    final int CTRL_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    newDiff =
        GuiUtils.buildMenuItem(
            "New Diff...", 'N', KeyEvent.VK_N, CTRL_MASK, new NewDiffAction(controller));

    addDiff =
        GuiUtils.buildMenuItem(
            "Add Existing Diff...", 'A', KeyEvent.VK_A, CTRL_MASK, new AddDiffAction(controller));

    dirDiff =
        GuiUtils.buildMenuItem("New Directory Diff...", 'N', new DirectoryDiffAction(controller));

    openDiff = GuiUtils.buildMenuItem("Open Diff", 'O', new LoadDiffAction(controller));
    closeDiff = GuiUtils.buildMenuItem("Close Diff", 'C', new CloseDiffAction(controller));
    deleteDiff = GuiUtils.buildMenuItem("Delete Diff", 'D', new DeleteDiffAction(controller));

    add(newDiff);
    add(addDiff);
    add(dirDiff);
    add(new JSeparator());
    add(openDiff);
    add(closeDiff);
    add(new JSeparator());
    add(deleteDiff);

    controller.getWorkspace().addListener(workspaceListener);
    controller.getWorkspaceTree().addListener(workspaceTreeListener);
  }

  private void registerCurrentDiffListener(final Diff diff) {
    unregisterCurrentDiffListener();

    lastSelectedDiff = diff;
    lastSelectedDiff.addListener(diffListener);

    unregisterCurrentDiffListener();
  }

  private void unregisterCurrentDiffListener() {
    if (lastSelectedDiff != null) {
      lastSelectedDiff.removeListener(diffListener);
      lastSelectedDiff = null;
    }
  }

  private class InternalDiffListener extends DiffListenerAdapter {
    @Override
    public void loadedDiff(final Diff diff) {
      openDiff.setEnabled(!diff.isLoaded());
      closeDiff.setEnabled(diff.isLoaded());
    }

    @Override
    public void removedDiff(final Diff diff) {
      controller.getWorkspaceTree().removeListener(workspaceTreeListener);
    }

    @Override
    public void unloadedDiff(final Diff diff) {
      openDiff.setEnabled(!diff.isLoaded());
      closeDiff.setEnabled(diff.isLoaded());
    }
  }

  private class InternalWorkspaceListener extends WorkspaceAdapter {
    @Override
    public void closedWorkspace() {
      setEnabled(false);
      unregisterCurrentDiffListener();
    }

    @Override
    public void loadedWorkspace(final Workspace workspace) {
      setEnabled(true);
      newDiff.setEnabled(true);
      addDiff.setEnabled(true);
      openDiff.setEnabled(false);
      closeDiff.setEnabled(false);
      deleteDiff.setEnabled(false);
    }
  }

  private class InternalWorkspaceTreeListener implements IWorkspaceTreeListener {
    @Override
    public void changedSelection(final AbstractTreeNode node) {
      final Diff diff = node.getDiff();
      if (diff == null) { // root node selected
        newDiff.setEnabled(true);
        addDiff.setEnabled(true);
        openDiff.setEnabled(false);
        closeDiff.setEnabled(false);
        deleteDiff.setEnabled(false);
        unregisterCurrentDiffListener();
      } else {
        openDiff.setEnabled(!diff.isLoaded());
        closeDiff.setEnabled(diff.isLoaded());
        deleteDiff.setEnabled(diff.isLoaded());
        registerCurrentDiffListener(diff);
      }
    }
  }
}
