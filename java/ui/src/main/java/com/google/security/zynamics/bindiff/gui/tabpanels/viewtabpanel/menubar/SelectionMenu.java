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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.ColorInvisibleNodeAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.ColorSelectedNodesAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.ColorUnselectedNodesAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.DeselectLeafsAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.DeselectPeripheryAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.DeselectRootsAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.InverseSelectionAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.RedoLastSelectionAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.ResetDefaultNodeColorsAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.SelectAncestorsAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.SelectByCriteriaAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.SelectChildrenAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.SelectNeighboursAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.SelectParentsAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.SelectSuccessorsAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.UndoLastSelectionAction;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

public class SelectionMenu extends JMenu {

  public SelectionMenu(final ViewTabPanelFunctions controller) {
    super("Selection");
    setMnemonic('S');

    final int CTRL_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    checkNotNull(controller);

    final JMenuItem undoLastSelection =
        GuiUtils.buildMenuItem(
            "Undo Selection",
            'U',
            KeyEvent.VK_Z,
            CTRL_MASK,
            new UndoLastSelectionAction(controller));

    final JMenuItem redoLastSelection =
        GuiUtils.buildMenuItem(
            "Redo Selection",
            'R',
            KeyEvent.VK_Y,
            CTRL_MASK,
            new RedoLastSelectionAction(controller));

    final JMenuItem selectAncestors =
        GuiUtils.buildMenuItem("Select Ancestors", 'A', new SelectAncestorsAction(controller));
    final JMenuItem selectSuccessors =
        GuiUtils.buildMenuItem("Select Successors", 'S', new SelectSuccessorsAction(controller));
    final JMenuItem inverseSelection =
        GuiUtils.buildMenuItem(
            "Invert Selection",
            'I',
            KeyEvent.VK_I,
            CTRL_MASK | InputEvent.SHIFT_DOWN_MASK,
            new InverseSelectionAction(controller));

    final JMenuItem selectNeighbours =
        GuiUtils.buildMenuItem("Select Neighbors", 'N', new SelectNeighboursAction(controller));
    final JMenuItem deselectPeriphery =
        GuiUtils.buildMenuItem("Deselect Periphery", 'D', new DeselectPeripheryAction(controller));
    final JMenuItem selectParents =
        GuiUtils.buildMenuItem("Select Parents", 'P', new SelectParentsAction(controller));
    final JMenuItem selectChildren =
        GuiUtils.buildMenuItem("Select Children", 'C', new SelectChildrenAction(controller));
    final JMenuItem deselectRoots =
        GuiUtils.buildMenuItem("Deselect Roots", 'R', new DeselectRootsAction(controller));

    final JMenuItem deselectLeafs =
        GuiUtils.buildMenuItem("Deselect Leafs", 'L', new DeselectLeafsAction(controller));

    final JMenuItem selectByCriteria =
        GuiUtils.buildMenuItem(
            "Select by Criteria...",
            'I',
            KeyEvent.VK_F3,
            0,
            new SelectByCriteriaAction(controller));

    final JMenuItem colorSelectedNodes =
        GuiUtils.buildMenuItem(
            "Color selected Nodes", 'S', new ColorSelectedNodesAction(controller));
    final JMenuItem colorUnselectedNodes =
        GuiUtils.buildMenuItem(
            "Color unselected Nodes", 'U', new ColorUnselectedNodesAction(controller));
    final JMenuItem colorInvisibleNodes =
        GuiUtils.buildMenuItem(
            "Color invisible Nodes", 'N', new ColorInvisibleNodeAction(controller));
    final JMenuItem resetDefaultNodeColors =
        GuiUtils.buildMenuItem(
            "Reset default Node Colors", 'R', new ResetDefaultNodeColorsAction(controller));

    add(undoLastSelection);
    add(redoLastSelection);
    add(new JSeparator());
    add(selectAncestors);
    add(selectSuccessors);
    add(inverseSelection);
    add(new JSeparator());
    add(selectNeighbours);
    add(deselectPeriphery);
    add(new JSeparator());
    add(selectParents);
    add(selectChildren);
    add(deselectRoots);
    add(deselectLeafs);
    add(new JSeparator());
    add(selectByCriteria);
    add(new JSeparator());
    add(colorSelectedNodes);
    add(colorUnselectedNodes);
    add(colorInvisibleNodes);
    add(resetDefaultNodeColors);
  }
}
