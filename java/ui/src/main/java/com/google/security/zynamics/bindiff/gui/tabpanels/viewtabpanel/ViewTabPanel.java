// Copyright 2011-2024 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel;

import static com.google.common.base.Preconditions.checkNotNull;

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
import com.google.security.zynamics.bindiff.utils.ResourceUtils;
import java.awt.BorderLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuBar;
import y.view.Graph2DView;

/** Tab sheet containing the graph views for flow and call graphs. */
public class ViewTabPanel extends TabPanel {
  private static final ImageIcon CALLGRAPHS_ICON =
      ResourceUtils.getImageIcon("data/tabicons/callgraphs-tab.png");

  private static final ImageIcon FLOWGRAPHS_IDENTICAL_MATCHED_ICON =
      ResourceUtils.getImageIcon("data/tabicons/flowgraphs-identical-matched-tab.png");
  private static final ImageIcon FLOWGRAPHS_INSTRUCTION_CHANGED_ONLY_ICON =
      ResourceUtils.getImageIcon("data/tabicons/flowgraphs-changed-instructions-only-tab.png");
  private static final ImageIcon FLOWGRAPHS_STRUTURAL_CHANGED_ICON =
      ResourceUtils.getImageIcon("data/tabicons/flowgraphs_structural-changed-tab.png");

  private static final ImageIcon FLOWGRAPH_PRIMARY_UNMATCHED_ICON =
      ResourceUtils.getImageIcon("data/tabicons/flowgraph-primary-unmatched-tab.png");
  private static final ImageIcon FLOWGRAPH_SECONDARY_UNMATCHED_ICON =
      ResourceUtils.getImageIcon("data/tabicons/flowgraph-secondary-unmatched-tab.png");

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

    checkNotNull(window);
    this.tabPanelManager = checkNotNull(tabPanelManager);
    checkNotNull(workspace);
    checkNotNull(diff);
    checkNotNull(view);

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
    if (viewData.isCallGraphView()) {
      return CALLGRAPHS_ICON;
    }
    if (viewData.isFlowGraphView()) {
      final FlowGraphViewData flowGraphView = (FlowGraphViewData) viewData;
      if (flowGraphView.isMatched()) {
        if (flowGraphView.isMatchedIdentical()) {
          return FLOWGRAPHS_IDENTICAL_MATCHED_ICON;
        }
        if (flowGraphView.isChangedOnlyInstructions()) {
          return FLOWGRAPHS_INSTRUCTION_CHANGED_ONLY_ICON;
        }
        if (flowGraphView.isChangedStructural()) {
          return FLOWGRAPHS_STRUTURAL_CHANGED_ICON;
        }
      } else {
        if (flowGraphView.getRawGraph(ESide.PRIMARY) == null) {
          return FLOWGRAPH_SECONDARY_UNMATCHED_ICON;
        }
        if (flowGraphView.getRawGraph(ESide.SECONDARY) == null) {
          return FLOWGRAPH_PRIMARY_UNMATCHED_ICON;
        }
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
