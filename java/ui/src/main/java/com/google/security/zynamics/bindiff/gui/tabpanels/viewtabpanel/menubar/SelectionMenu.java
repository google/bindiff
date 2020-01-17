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
  private JMenuItem undoLastSelection;
  private JMenuItem redoLastSelection;

  private JMenuItem selectAncestors;
  private JMenuItem selectSuccessors;
  private JMenuItem inverseSelection;

  private JMenuItem selectParents;
  private JMenuItem selectChildren;
  private JMenuItem selectNeighbours;
  private JMenuItem deselectRoots;
  private JMenuItem deselectLeafs;
  private JMenuItem deselectPeriphery;

  private JMenuItem selectByCriteria;

  private JMenuItem colorSelectedNodes;
  private JMenuItem colorUnselectedNodes;
  private JMenuItem colorInvisibleNodes;
  private JMenuItem resetDefaultNodeColors;

  public SelectionMenu(final ViewTabPanelFunctions controller) {
    super("Selection");
    setMnemonic('S');

    final int CTRL_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    Preconditions.checkNotNull(controller);

    undoLastSelection =
        GuiUtils.buildMenuItem(
            "Undo Selection",
            'U',
            KeyEvent.VK_Z,
            CTRL_MASK,
            new UndoLastSelectionAction(controller));

    redoLastSelection =
        GuiUtils.buildMenuItem(
            "Redo Selection",
            'R',
            KeyEvent.VK_Y,
            CTRL_MASK,
            new RedoLastSelectionAction(controller));

    selectAncestors =
        GuiUtils.buildMenuItem("Select Ancestors", 'A', new SelectAncestorsAction(controller));
    selectSuccessors =
        GuiUtils.buildMenuItem("Select Successors", 'S', new SelectSuccessorsAction(controller));
    inverseSelection =
        GuiUtils.buildMenuItem(
            "Invert Selection",
            'I',
            KeyEvent.VK_I,
            CTRL_MASK | InputEvent.SHIFT_DOWN_MASK,
            new InverseSelectionAction(controller));

    selectNeighbours =
        GuiUtils.buildMenuItem("Select Neighbors", 'N', new SelectNeighboursAction(controller));
    deselectPeriphery =
        GuiUtils.buildMenuItem("Deselect Periphery", 'D', new DeselectPeripheryAction(controller));
    selectParents =
        GuiUtils.buildMenuItem("Select Parents", 'P', new SelectParentsAction(controller));
    selectChildren =
        GuiUtils.buildMenuItem("Select Children", 'C', new SelectChildrenAction(controller));
    deselectRoots =
        GuiUtils.buildMenuItem("Deselect Roots", 'R', new DeselectRootsAction(controller));

    deselectLeafs =
        GuiUtils.buildMenuItem("Deselect Leafs", 'L', new DeselectLeafsAction(controller));

    selectByCriteria =
        GuiUtils.buildMenuItem(
            "Select by Criteria...",
            'I',
            KeyEvent.VK_F3,
            0,
            new SelectByCriteriaAction(controller));

    colorSelectedNodes =
        GuiUtils.buildMenuItem(
            "Color selected Nodes", 'S', new ColorSelectedNodesAction(controller));
    colorUnselectedNodes =
        GuiUtils.buildMenuItem(
            "Color unselected Nodes", 'U', new ColorUnselectedNodesAction(controller));
    colorInvisibleNodes =
        GuiUtils.buildMenuItem(
            "Color invisible Nodes", 'N', new ColorInvisibleNodeAction(controller));
    resetDefaultNodeColors =
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

  public void dispose() {
    undoLastSelection = null;
    redoLastSelection = null;
    selectAncestors = null;
    selectSuccessors = null;
    inverseSelection = null;
    selectParents = null;
    selectChildren = null;
    selectNeighbours = null;
    deselectRoots = null;
    deselectLeafs = null;
    deselectPeriphery = null;
    selectByCriteria = null;
    colorSelectedNodes = null;
    colorUnselectedNodes = null;
    colorInvisibleNodes = null;
    resetDefaultNodeColors = null;
  }
}
