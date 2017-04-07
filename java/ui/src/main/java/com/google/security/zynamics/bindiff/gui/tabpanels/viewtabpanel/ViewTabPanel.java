package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.EGraph;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.GraphsContainer;
import com.google.security.zynamics.bindiff.graph.backgroundrendering.BackgroundRendererManager;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanelManager;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.menubar.ViewMenuBar;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.subpanels.GraphPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.subpanels.ViewToolbarPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.viewpanel.CNormalViewPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.viewpanel.CombinedViewPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.viewpanel.TextViewPanel;
import com.google.security.zynamics.bindiff.gui.window.MainWindow;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.userview.FlowGraphViewData;
import com.google.security.zynamics.bindiff.project.userview.ViewData;
import com.google.security.zynamics.bindiff.utils.ImageUtils;
import java.awt.BorderLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuBar;
import y.view.Graph2DView;

public class ViewTabPanel extends TabPanel {
  private static final ImageIcon CALLGRAPHS_ICON =
      ImageUtils.getImageIcon("data/tabicons/callgraphs-tab.png");

  private static final ImageIcon FLOWGRAPHS_IDENTICAL_MATCHED_ICON =
      ImageUtils.getImageIcon("data/tabicons/flowgraphs-identical-matched-tab.png");
  private static final ImageIcon FLOWGRAPHS_INSTRUCTION_CHANGED_ONLY_ICON =
      ImageUtils.getImageIcon("data/tabicons/flowgraphs-changed-instructions-only-tab.png");
  private static final ImageIcon FLOWGRAPHS_STRUTURAL_CHANGED_ICON =
      ImageUtils.getImageIcon("data/tabicons/flowgraphs_structural-changed-tab.png");

  private static final ImageIcon FLOWGRAPH_PRIMARY_UNMATCHED_ICON =
      ImageUtils.getImageIcon("data/tabicons/flowgraph-primary-unmatched-tab.png");
  private static final ImageIcon FLOWGRAPH_SECONDARY_UNMATCHED_ICON =
      ImageUtils.getImageIcon("data/tabicons/flowgraph-secondary-unmatched-tab.png");

  private final ViewMenuBar viewMenuBar;

  private ViewToolbarPanel toolbar;

  private ViewData viewData;

  private final ViewTabPanelFunctions controller;

  private final BackgroundRendererManager primaryViewBackgroundRendererManager;
  private final BackgroundRendererManager secondaryViewBackgroundRendererManager;
  private final BackgroundRendererManager combinedViewBackgroundRendererManager;

  private final CNormalViewPanel normalViewPanel;
  private final CombinedViewPanel combinedViewPanel;
  private final TextViewPanel textViewPanel;

  private TabPanelManager tabPanelManager;

  public ViewTabPanel(
      final MainWindow window,
      final TabPanelManager tabPanelManager,
      final Workspace workspace,
      final Diff diff,
      final ViewData view) {
    super();

    Preconditions.checkNotNull(window);
    this.tabPanelManager = Preconditions.checkNotNull(tabPanelManager);
    Preconditions.checkNotNull(workspace);
    Preconditions.checkNotNull(diff);
    Preconditions.checkNotNull(view);

    final GraphsContainer graphs = view.getGraphs();
    final GraphSettings settings = graphs.getSettings();

    controller = new ViewTabPanelFunctions(window, workspace, this, view);

    viewMenuBar = new ViewMenuBar(controller);

    toolbar = new ViewToolbarPanel(controller);

    normalViewPanel = new CNormalViewPanel(diff, controller, view);
    combinedViewPanel = new CombinedViewPanel(diff, controller, view);
    textViewPanel = new TextViewPanel();

    viewData = view;

    final Graph2DView primaryGraph2DView = graphs.getPrimaryGraph().getView();
    final Graph2DView secondaryGraph2DView = graphs.getSecondaryGraph().getView();
    final Graph2DView combinedGraph2DView = graphs.getCombinedGraph().getView();

    primaryViewBackgroundRendererManager =
        new BackgroundRendererManager(viewData, primaryGraph2DView, EGraph.PRIMARY_GRAPH, settings);
    secondaryViewBackgroundRendererManager =
        new BackgroundRendererManager(
            viewData, secondaryGraph2DView, EGraph.SECONDARY_GRAPH, settings);
    combinedViewBackgroundRendererManager =
        new BackgroundRendererManager(
            viewData, combinedGraph2DView, EGraph.COMBINED_GRAPH, settings);

    add(toolbar, BorderLayout.NORTH);

    add(controller.getCurrentViewPanel(), BorderLayout.CENTER);

    window.setJMenuBar(viewMenuBar);

    updateUI();
  }

  public void dispose() {
    primaryViewBackgroundRendererManager.removeListener();
    secondaryViewBackgroundRendererManager.removeListener();
    combinedViewBackgroundRendererManager.removeListener();

    viewMenuBar.dispose();
    toolbar.dispose();

    normalViewPanel.dispose();
    combinedViewPanel.dispose();

    tabPanelManager = null;
    toolbar = null;

    viewData = null;
  }

  public GraphPanel getCombinedGraphPanel() {
    return combinedViewPanel.getGraphPanel();
  }

  public CombinedViewPanel getCombinedViewPanel() {
    return combinedViewPanel;
  }

  public ViewTabPanelFunctions getController() {
    return controller;
  }

  public Diff getDiff() {
    return viewData.getGraphs().getDiff();
  }

  @Override
  public Icon getIcon() {
    if (viewData.isCallgraphView()) {
      return CALLGRAPHS_ICON;
    } else if (viewData.isFlowgraphView()) {
      if (((FlowGraphViewData) viewData).isMatched()) {
        if (((FlowGraphViewData) viewData).isMatchedIdentical()) {
          return FLOWGRAPHS_IDENTICAL_MATCHED_ICON;
        } else if (((FlowGraphViewData) viewData).isChangedOnlyInstructions()) {
          return FLOWGRAPHS_INSTRUCTION_CHANGED_ONLY_ICON;
        } else if (((FlowGraphViewData) viewData).isChangedStructural()) {
          return FLOWGRAPHS_STRUTURAL_CHANGED_ICON;
        }
      } else if (((FlowGraphViewData) viewData).getRawGraph(ESide.PRIMARY) == null) {
        return FLOWGRAPH_SECONDARY_UNMATCHED_ICON;
      } else if (((FlowGraphViewData) viewData).getRawGraph(ESide.SECONDARY) == null) {
        return FLOWGRAPH_PRIMARY_UNMATCHED_ICON;
      }
    }

    return null;
  }

  @Override
  public JMenuBar getMenuBar() {
    return viewMenuBar;
  }

  public CNormalViewPanel getNormalViewPanel() {
    return normalViewPanel;
  }

  public GraphPanel getPrimaryGraphPanel() {
    return normalViewPanel.getPrimaryPanel();
  }

  public GraphPanel getSecondaryGraphPanel() {
    return normalViewPanel.getSecondaryPanel();
  }

  public TabPanelManager getTabPanelManager() {
    return tabPanelManager;
  }

  public TextViewPanel getTextViewPanel() {
    return textViewPanel;
  }

  @Override
  public String getTitle() {
    return viewData.getViewName();
  }

  public ViewToolbarPanel getToolbar() {
    return toolbar;
  }

  public ViewData getView() {
    return viewData;
  }
}
