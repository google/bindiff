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

import com.google.security.zynamics.bindiff.enums.EDiffViewMode;
import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.GraphsContainer;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.ZoomInAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.ZoomOutAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.AddNodeMatchAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.CircularGraphLayoutAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.DeleteNodeMatchAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.FitGraphContentAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.HierarchicalGraphLayoutAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.OrthogonalGraphLayoutAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.ShowGraphSettingsDialogAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.ZoomToSelectedAction;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import com.google.security.zynamics.zylib.gui.zygraph.IZyGraphSelectionListener;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

public class GraphsMenu extends JMenu {
  private final ViewTabPanelFunctions controller;

  final JMenuItem addMatch;
  final JMenuItem deleteMatch;

  private final InternalGraphSelectionListener listener = new InternalGraphSelectionListener();

  public GraphsMenu(final ViewTabPanelFunctions controller) {
    super("Graphs");

    this.controller = checkNotNull(controller);
    final int CTRL_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    controller
        .getGraphs()
        .getPrimaryGraph()
        .getIntermediateListeners()
        .addIntermediateListener(listener);
    controller
        .getGraphs()
        .getSecondaryGraph()
        .getIntermediateListeners()
        .addIntermediateListener(listener);
    controller
        .getGraphs()
        .getCombinedGraph()
        .getIntermediateListeners()
        .addIntermediateListener(listener);

    setMnemonic('G');

    final JMenuItem graphSettings =
        GuiUtils.buildMenuItem(
            "Graph Settings",
            'G',
            KeyEvent.VK_F2,
            0,
            new ShowGraphSettingsDialogAction(controller));

    final JMenuItem hierarchicalLayout =
        GuiUtils.buildMenuItem(
            "Hierarchical Layout",
            'H',
            KeyEvent.VK_H,
            CTRL_MASK | InputEvent.SHIFT_DOWN_MASK,
            new HierarchicalGraphLayoutAction(controller));
    final JMenuItem orthogonalLayout =
        GuiUtils.buildMenuItem(
            "Orthogonal Layout",
            'L',
            KeyEvent.VK_O,
            CTRL_MASK | InputEvent.SHIFT_DOWN_MASK,
            new OrthogonalGraphLayoutAction(controller));
    final JMenuItem circularLayout =
        GuiUtils.buildMenuItem(
            "Circular Layout",
            'C',
            KeyEvent.VK_C,
            CTRL_MASK | InputEvent.SHIFT_DOWN_MASK,
            new CircularGraphLayoutAction(controller));

    final JMenuItem fitContent =
        GuiUtils.buildMenuItem(
            "Fit Graph",
            'F',
            KeyEvent.VK_M,
            CTRL_MASK | InputEvent.SHIFT_DOWN_MASK,
            new FitGraphContentAction(controller));
    final JMenuItem zoomToSelected =
        GuiUtils.buildMenuItem(
            "Zoom to Selected",
            'Z',
            KeyEvent.VK_S,
            CTRL_MASK | InputEvent.SHIFT_DOWN_MASK,
            new ZoomToSelectedAction(controller));

    final JMenuItem zoomIn =
        GuiUtils.buildMenuItem(
            "Zoom in", 'I', KeyEvent.VK_PLUS, CTRL_MASK, new ZoomInAction(controller));

    final JMenuItem zoomOut =
        GuiUtils.buildMenuItem(
            "Zoom out", 'O', KeyEvent.VK_MINUS, CTRL_MASK, new ZoomOutAction(controller));

    deleteMatch =
        GuiUtils.buildMenuItem("Delete Matches", 'D', new DeleteNodeMatchAction(controller));
    addMatch = GuiUtils.buildMenuItem("Add Match", 'A', new AddNodeMatchAction(controller));

    add(graphSettings);
    add(new JSeparator());
    add(hierarchicalLayout);
    add(orthogonalLayout);
    add(circularLayout);
    add(new JSeparator());
    add(fitContent);
    add(zoomToSelected);
    add(zoomIn);
    add(zoomOut);
    add(new JSeparator());
    add(deleteMatch);
    add(addMatch);

    addMatch.setEnabled(false);
    deleteMatch.setEnabled(false);
  }

  public void dispose() {
    controller
        .getGraphs()
        .getPrimaryGraph()
        .getIntermediateListeners()
        .removeIntermediateListener(listener);
    controller
        .getGraphs()
        .getSecondaryGraph()
        .getIntermediateListeners()
        .removeIntermediateListener(listener);
    controller
        .getGraphs()
        .getCombinedGraph()
        .getIntermediateListeners()
        .removeIntermediateListener(listener);
  }

  private class InternalGraphSelectionListener implements IZyGraphSelectionListener {
    private int countSelectedMatchedNodes(final CombinedGraph graph) {
      int counter = 0;

      for (final CombinedDiffNode selectedNode : graph.getSelectedNodes()) {
        if (selectedNode.getRawNode().getMatchState() == EMatchState.MATCHED) {
          ++counter;
        }
      }

      return counter;
    }

    private int countSelectedNodes(final SingleGraph graph, final boolean matched) {
      int counter = 0;

      for (final SingleDiffNode selectedNode : graph.getSelectedNodes()) {
        if (matched && selectedNode.getRawNode().getMatchState() == EMatchState.MATCHED) {
          ++counter;
        } else if (!matched && selectedNode.getRawNode().getMatchState() != EMatchState.MATCHED) {
          ++counter;
        }
      }

      return counter;
    }

    private int countSelectedUnmatchedNodes(final CombinedGraph graph, final ESide side) {
      int counter = 0;

      for (final CombinedDiffNode selectedNode : graph.getSelectedNodes()) {

        if (side == ESide.PRIMARY
            && selectedNode.getRawNode().getMatchState() == EMatchState.PRIMARY_UNMATCHED) {
          ++counter;
        } else if (side == ESide.SECONDARY
            && selectedNode.getRawNode().getMatchState() != EMatchState.SECONDRAY_UNMATCHED) {
          ++counter;
        }
      }

      return counter;
    }

    @Override
    public void selectionChanged() {
      final GraphsContainer graphs = controller.getGraphs();

      boolean canAdd = false;
      boolean canDelete = false;

      if (graphs.getPrimaryGraph().getNodes().size() != 0
          && graphs.getSecondaryGraph().getNodes().size() != 0) {
        if (controller.getGraphSettings().getDiffViewMode() == EDiffViewMode.NORMAL_VIEW) {
          canAdd =
              countSelectedNodes(graphs.getPrimaryGraph(), false) == 1
                  && countSelectedNodes(graphs.getSecondaryGraph(), false) == 1;
          canDelete =
              countSelectedNodes(graphs.getPrimaryGraph(), true) > 0
                  || countSelectedNodes(graphs.getSecondaryGraph(), true) > 0;
        } else if (controller.getGraphSettings().getDiffViewMode() == EDiffViewMode.COMBINED_VIEW) {
          final CombinedGraph combinedGraph = graphs.getCombinedGraph();

          canAdd =
              countSelectedUnmatchedNodes(combinedGraph, ESide.PRIMARY) == 1
                  && countSelectedUnmatchedNodes(combinedGraph, ESide.SECONDARY) == 1;
          canDelete = countSelectedMatchedNodes(combinedGraph) > 0;
        }
      }

      addMatch.setEnabled(canAdd);
      deleteMatch.setEnabled(canDelete);
    }
  }
}
