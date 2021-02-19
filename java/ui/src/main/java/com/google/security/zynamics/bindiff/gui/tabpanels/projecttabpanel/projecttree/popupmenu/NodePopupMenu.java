// Copyright 2011-2021 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.popupmenu;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.CloseDiffAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.CloseViewsAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.DeleteDiffAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.LoadDiffAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.WorkspaceTreeListener;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.treenodes.AbstractTreeNode;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.diff.DiffListener;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

public class NodePopupMenu extends JPopupMenu {
  private final WorkspaceTabPanelFunctions controller;

  private final JMenuItem openDiff;
  private final JMenuItem closeDiff;
  private final JMenuItem deleteDiff;
  // private final JMenuItem report;
  private final JMenuItem closeViews;

  private final InternalWorkspaceTreeListener workspaceTreeListener =
      new InternalWorkspaceTreeListener();

  private final InternalDiffListener diffListener = new InternalDiffListener();

  private Diff lastSelectedDiff = null;

  public NodePopupMenu(final WorkspaceTabPanelFunctions controller) {
    this.controller = checkNotNull(controller);

    openDiff = GuiUtils.buildMenuItem("Open Diff", new LoadDiffAction(controller));
    closeDiff = GuiUtils.buildMenuItem("Close Diff", new CloseDiffAction(controller));
    deleteDiff = GuiUtils.buildMenuItem("Delete Diff", new DeleteDiffAction(controller));
    closeViews = GuiUtils.buildMenuItem("Close Views", new CloseViewsAction(controller));

    add(openDiff);
    add(closeDiff);
    add(new JSeparator());
    add(deleteDiff);
    add(new JSeparator());
    add(closeViews);

    openDiff.setEnabled(true);
    closeDiff.setEnabled(false);
    closeViews.setEnabled(false);

    this.controller.getWorkspaceTree().addListener(workspaceTreeListener);
  }

  private void registerCurrentDiffListener(final Diff diff) {
    unregisterCurrentDiffListener();

    lastSelectedDiff = diff;
    lastSelectedDiff.addListener(diffListener);
  }

  private void unregisterCurrentDiffListener() {
    if (lastSelectedDiff != null) {
      lastSelectedDiff.removeListener(diffListener);
      lastSelectedDiff = null;
    }
  }

  private void updateMenu(final Diff diff) {
    openDiff.setEnabled(!diff.isLoaded());
    closeDiff.setEnabled(diff.isLoaded());
    deleteDiff.setEnabled(true);

    final Set<Diff> diffSet = new HashSet<>();
    diffSet.add(diff);
    closeViews.setEnabled(controller.getOpenViews(diffSet).size() > 0);
  }

  public void dispose() {
    unregisterCurrentDiffListener();
    controller.getWorkspaceTree().removeListener(workspaceTreeListener);
  }

  private class InternalDiffListener implements DiffListener {
    @Override
    public void closedView(final Diff diff) {
      updateMenu(diff);
    }

    @Override
    public void loadedDiff(final Diff diff) {
      updateMenu(diff);

      if (diff.getMatches() == null) {
        openDiff.setEnabled(false);
      }
    }

    @Override
    public void loadedView(final Diff diff) {
      updateMenu(diff);
    }

    @Override
    public void removedDiff(final Diff diff) {
      updateMenu(diff);

      unregisterCurrentDiffListener();

      controller.getWorkspaceTree().removeListener(workspaceTreeListener);
    }

    @Override
    public void unloadedDiff(final Diff diff) {
      updateMenu(diff);
    }
  }

  private class InternalWorkspaceTreeListener implements WorkspaceTreeListener {
    @Override
    public void changedSelection(final AbstractTreeNode node) {
      final Diff diff = node.getDiff();
      if (diff == null) { // root node selected
        openDiff.setEnabled(false);
        closeDiff.setEnabled(false);
        deleteDiff.setEnabled(false);
        closeViews.setEnabled(false);

        unregisterCurrentDiffListener();
      } else {
        updateMenu(diff);

        registerCurrentDiffListener(diff);
      }
    }
  }
}
