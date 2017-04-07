package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.menubar;

import com.google.common.base.Preconditions;
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
  private JMenuItem graphSettings;

  private JMenuItem HierarchicalLayout;
  private JMenuItem orthogonalLayout;
  private JMenuItem circularLayout;

  private JMenuItem fitContent;
  private JMenuItem zoomToSelected;
  private final JMenuItem zoomIn;
  private final JMenuItem zoomOut;

  private JMenuItem deleteMatch;
  private JMenuItem addMatch;

  private ViewTabPanelFunctions controller;

  private InternalGraphSelectionListener listener = new InternalGraphSelectionListener();

  public GraphsMenu(final ViewTabPanelFunctions controller) {
    super("Graphs");

    this.controller = Preconditions.checkNotNull(controller);
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

    graphSettings =
        GuiUtils.buildMenuItem(
            "Graph Settings",
            'G',
            KeyEvent.VK_F2,
            0,
            new ShowGraphSettingsDialogAction(controller));

    HierarchicalLayout =
        GuiUtils.buildMenuItem(
            "Hierarchical Layout",
            'H',
            KeyEvent.VK_H,
            CTRL_MASK | InputEvent.SHIFT_DOWN_MASK,
            new HierarchicalGraphLayoutAction(controller));
    orthogonalLayout =
        GuiUtils.buildMenuItem(
            "Orthogonal Layout",
            'L',
            KeyEvent.VK_O,
            CTRL_MASK | InputEvent.SHIFT_DOWN_MASK,
            new OrthogonalGraphLayoutAction(controller));
    circularLayout =
        GuiUtils.buildMenuItem(
            "Circular Layout",
            'C',
            KeyEvent.VK_C,
            CTRL_MASK | InputEvent.SHIFT_DOWN_MASK,
            new CircularGraphLayoutAction(controller));

    fitContent =
        GuiUtils.buildMenuItem(
            "Fit Graph",
            'F',
            KeyEvent.VK_M,
            CTRL_MASK | InputEvent.SHIFT_DOWN_MASK,
            new FitGraphContentAction(controller));
    zoomToSelected =
        GuiUtils.buildMenuItem(
            "Zoom to Selected",
            'Z',
            KeyEvent.VK_S,
            CTRL_MASK | InputEvent.SHIFT_DOWN_MASK,
            new ZoomToSelectedAction(controller));

    zoomIn =
        GuiUtils.buildMenuItem(
            "Zoom in", 'I', KeyEvent.VK_PLUS, CTRL_MASK, new ZoomInAction(controller));

    zoomOut =
        GuiUtils.buildMenuItem(
            "Zoom out", 'O', KeyEvent.VK_MINUS, CTRL_MASK, new ZoomOutAction(controller));

    deleteMatch =
        GuiUtils.buildMenuItem("Delete Matches", 'D', new DeleteNodeMatchAction(controller));
    addMatch = GuiUtils.buildMenuItem("Add Match", 'A', new AddNodeMatchAction(controller));

    add(graphSettings);

    add(new JSeparator());

    add(HierarchicalLayout);
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

    controller = null;
    listener = null;

    graphSettings = null;
    HierarchicalLayout = null;
    orthogonalLayout = null;
    circularLayout = null;
    fitContent = null;
    zoomToSelected = null;
    deleteMatch = null;
    addMatch = null;
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

      boolean addAble = false;
      boolean deleteAble = false;

      if (graphs.getPrimaryGraph().getNodes().size() != 0
          && graphs.getSecondaryGraph().getNodes().size() != 0) {
        if (controller.getGraphSettings().getDiffViewMode() == EDiffViewMode.NORMAL_VIEW) {
          addAble =
              countSelectedNodes(graphs.getPrimaryGraph(), false) == 1
                  && countSelectedNodes(graphs.getSecondaryGraph(), false) == 1;
          deleteAble =
              countSelectedNodes(graphs.getPrimaryGraph(), true) > 0
                  || countSelectedNodes(graphs.getSecondaryGraph(), true) > 0;
        } else if (controller.getGraphSettings().getDiffViewMode() == EDiffViewMode.COMBINED_VIEW) {
          final CombinedGraph combinedGraph = graphs.getCombinedGraph();

          addAble =
              countSelectedUnmatchedNodes(combinedGraph, ESide.PRIMARY) == 1
                  && countSelectedUnmatchedNodes(combinedGraph, ESide.SECONDARY) == 1;
          deleteAble = countSelectedMatchedNodes(combinedGraph) > 0;
        }
      }

      addMatch.setEnabled(addAble);
      deleteMatch.setEnabled(deleteAble);
    }
  }
}
