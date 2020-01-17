// Copyright 2011-2020 Google LLC
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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.menubar;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ISavableListener;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.CloseViewAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.ExportViewAsImageAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.PrintViewAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.SaveViewAction;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

public class ViewMenu extends JMenu {
  private final InternalViewListener viewListener = new InternalViewListener();

  private ViewTabPanelFunctions viewTabPanelController;

  private JMenuItem saveView;

  private JMenuItem printPrimaryGraph;
  private JMenuItem printSecondaryGraph;
  private JMenuItem printCombinedGraph;
  private JMenuItem exportViewAsPng;
  private JMenuItem closeView;

  public ViewMenu(final ViewTabPanelFunctions controller) {
    super("View");
    setMnemonic('V');

    viewTabPanelController = Preconditions.checkNotNull(controller);

    final Diff diff = controller.getGraphs().getDiff();
    final int CTRL_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    saveView =
        GuiUtils.buildMenuItem(
            "Save View", 'S', KeyEvent.VK_S, CTRL_MASK, new SaveViewAction(controller));

    printPrimaryGraph =
        GuiUtils.buildMenuItem(
            "Print Primary Graph...",
            'P',
            new PrintViewAction(controller, controller.getGraphs().getPrimaryGraph()));

    printSecondaryGraph =
        GuiUtils.buildMenuItem(
            "Print Secondary Graph...",
            'S',
            new PrintViewAction(controller, controller.getGraphs().getSecondaryGraph()));

    printCombinedGraph =
        GuiUtils.buildMenuItem(
            "Print Combined Graph...",
            'C',
            new PrintViewAction(controller, controller.getGraphs().getCombinedGraph()));

    exportViewAsPng =
        GuiUtils.buildMenuItem(
            "Export View as Image...", 'E', new ExportViewAsImageAction(controller));

    closeView = GuiUtils.buildMenuItem("Close View", 'C', new CloseViewAction(controller));

    saveView.setEnabled(diff.isFunctionDiff());

    add(saveView);

    add(new JSeparator());

    add(printPrimaryGraph);
    add(printSecondaryGraph);
    add(printCombinedGraph);

    add(new JSeparator());

    add(exportViewAsPng);

    add(new JSeparator());

    add(closeView);

    viewTabPanelController.addListener(viewListener);
  }

  public void dispose() {
    viewTabPanelController.removeListener(viewListener);
    viewTabPanelController = null;

    saveView = null;
    printPrimaryGraph = null;
    printSecondaryGraph = null;
    printCombinedGraph = null;
    exportViewAsPng = null;
    closeView = null;
  }

  private class InternalViewListener implements ISavableListener {
    @Override
    public void isSavable(final boolean saveable) {
      if (!viewTabPanelController.getGraphs().getDiff().isFunctionDiff()) {
        if (saveView.isEnabled() != saveable) {
          saveView.setEnabled(saveable);
        }
      } else {
        saveView.setEnabled(true);
      }
    }
  }
}
