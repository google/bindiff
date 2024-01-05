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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.subpanels;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.EGraph;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettingsChangedListenerAdapter;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.AbstractGraphNodeTree;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.selectionhistory.SelectionHistory;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.selectionhistory.SelectionHistoryTreePanel;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.userview.ViewData;
import com.google.security.zynamics.bindiff.resources.Colors;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public abstract class GraphPanel extends JPanel {
  public static double COMBINED_MAIN_DIVIDER_WIDTH = 0.15;
  public static double PRIMARY_MAIN_DIVIDER_WIDTH = 0.30;
  public static double SECONDRAY_MAIN_DIVIDER_WIDTH = 0.70;

  public static double OVERVIEW_DIVIDER_HEIGHT = 0.25;
  public static double NODETREE_DIVIDER_WIDTH = 0.75;

  private final EGraph graphType;

  private final JSplitPane mainSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
  private final JSplitPane overviewSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
  private final JSplitPane treeSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

  private final JPanel graphPanel = new JPanel(new BorderLayout());
  private final JPanel overviewPanel = new JPanel(new BorderLayout());
  private final JPanel treePanel = new JPanel(new BorderLayout());

  private final GraphNodeTreePanel graphTreePanel;
  private final SelectionHistoryTreePanel selectionHistoryPanel;

  private final BinDiffGraph<?, ?> graph;

  private final ViewData view;

  private final InternalSettingsChangedListener settingsListener =
      new InternalSettingsChangedListener();

  public GraphPanel(
      final ViewTabPanelFunctions controller,
      final Diff diff,
      final ViewData view,
      final BinDiffGraph<?, ?> graph,
      final EGraph graphType) {
    super(new BorderLayout());

    checkNotNull(controller);
    checkNotNull(diff);
    this.view = checkNotNull(view);
    this.graph = checkNotNull(graph);

    this.graphType = graphType;

    graphPanel.add(graph.getView(), BorderLayout.CENTER);

    overviewPanel.add(new GraphOverviewPanel(graph.getView()));

    if (graphType == EGraph.PRIMARY_GRAPH) {
      graphTreePanel = new GraphNodeTreePanel(controller, diff, view, (SingleGraph) graph);
    } else if (graphType == EGraph.SECONDARY_GRAPH) {
      graphTreePanel = new GraphNodeTreePanel(controller, diff, view, (SingleGraph) graph);
    } else if (graphType == EGraph.COMBINED_GRAPH) {
      graphTreePanel = new GraphNodeTreePanel(controller, diff, view, (CombinedGraph) graph);
    } else {
      graphTreePanel = null;
    }

    treePanel.setBorder(new TitledBorder(""));
    treePanel.add(graphTreePanel == null ? new JPanel() : graphTreePanel, BorderLayout.CENTER);

    if (graph instanceof SingleGraph) {
      final SelectionHistory selectionHistory = ((SingleGraph) graph).getSelectionHistory();
      selectionHistoryPanel = new SelectionHistoryTreePanel(controller, graph, selectionHistory);
      selectionHistoryPanel.setBorder(new TitledBorder(""));
    } else if (graph instanceof CombinedGraph) {
      final SelectionHistory selectionHistory = ((CombinedGraph) graph).getSelectionHistory();
      selectionHistoryPanel = new SelectionHistoryTreePanel(controller, graph, selectionHistory);
      selectionHistoryPanel.setBorder(new TitledBorder(""));
    } else {
      selectionHistoryPanel = null;
    }

    graph.getSettings().addListener(settingsListener);

    createPanel();
  }

  private void createPanel() {
    if (EGraph.PRIMARY_GRAPH == graphType || EGraph.COMBINED_GRAPH == graphType) {
      mainSplitter.setLeftComponent(overviewSplitter);
      mainSplitter.setRightComponent(graphPanel);
    } else {
      mainSplitter.setLeftComponent(graphPanel);
      mainSplitter.setRightComponent(overviewSplitter);
    }

    mainSplitter.setBorder(new EmptyBorder(0, 0, 0, 0));
    mainSplitter.setContinuousLayout(true);

    setFocusBorder(graph.getSettings());

    overviewSplitter.setBorder(new EmptyBorder(0, 0, 0, 0));
    treeSplitter.setBorder(new EmptyBorder(0, 0, 0, 0));

    overviewSplitter.setTopComponent(overviewPanel);
    overviewSplitter.setBottomComponent(treeSplitter);

    treeSplitter.setTopComponent(treePanel);
    treeSplitter.setBottomComponent(selectionHistoryPanel);

    overviewSplitter.setMinimumSize(new Dimension(0, 0));
    overviewSplitter.setOneTouchExpandable(true);
    overviewSplitter.setContinuousLayout(true);
    overviewSplitter.setDoubleBuffered(true);

    treeSplitter.setMinimumSize(new Dimension(0, 0));
    treeSplitter.setOneTouchExpandable(true);
    treeSplitter.setContinuousLayout(true);
    treeSplitter.setDoubleBuffered(true);

    mainSplitter.setMinimumSize(new Dimension(0, 0));
    mainSplitter.setOneTouchExpandable(true);
    mainSplitter.setDoubleBuffered(true);

    resetDefaultPerspective();

    add(mainSplitter, BorderLayout.CENTER);
  }

  private void setFocusBorder(final GraphSettings settings) {
    if (settings.isAsync()) {
      if (settings.getFocus() == ESide.PRIMARY && graphType == EGraph.PRIMARY_GRAPH
          || settings.getFocus() == ESide.SECONDARY && graphType == EGraph.SECONDARY_GRAPH) {
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        mainSplitter.setBorder(BorderFactory.createLineBorder(Colors.GRAY64, 3));
      } else {
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        final LineBorder innerBorder = new LineBorder(Colors.GRAY64, 1);
        final LineBorder outerBorder = new LineBorder(Colors.GRAY192, 2);
        mainSplitter.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
      }
    } else {
      setBorder(new EmptyBorder(0, 0, 0, 0));
      mainSplitter.setBorder(new EmptyBorder(0, 0, 0, 0));
    }
  }

  protected ViewData getView() {
    return view;
  }

  public void dispose() {
    graph.getSettings().removeListener(settingsListener);

    graphTreePanel.dispose();
    selectionHistoryPanel.dispose();
  }

  public BinDiffGraph<?, ?> getGraph() {
    return graph;
  }

  public Dimension getGraphComponentSize() {
    if (graphType == EGraph.COMBINED_GRAPH || graphType == EGraph.PRIMARY_GRAPH) {
      return mainSplitter.getRightComponent().getSize();
    }

    return mainSplitter.getLeftComponent().getSize();
  }

  public int getMainDividerLoction() {
    return mainSplitter.getDividerLocation();
  }

  public JSplitPane getMainSplitter() {
    return mainSplitter;
  }

  public AbstractGraphNodeTree getTree() {
    return graphTreePanel.getTree();
  }

  public void resetDefaultPerspective() {
    if (graphType == EGraph.COMBINED_GRAPH) {
      mainSplitter.setResizeWeight(COMBINED_MAIN_DIVIDER_WIDTH);
      mainSplitter.setDividerLocation(COMBINED_MAIN_DIVIDER_WIDTH);
      mainSplitter.setLastDividerLocation(mainSplitter.getDividerLocation());
    } else if (graphType == EGraph.PRIMARY_GRAPH) {
      mainSplitter.setResizeWeight(PRIMARY_MAIN_DIVIDER_WIDTH);
      mainSplitter.setDividerLocation(PRIMARY_MAIN_DIVIDER_WIDTH);
      mainSplitter.setLastDividerLocation(mainSplitter.getDividerLocation());
    } else {
      mainSplitter.setResizeWeight(SECONDRAY_MAIN_DIVIDER_WIDTH);
      mainSplitter.setDividerLocation(SECONDRAY_MAIN_DIVIDER_WIDTH);
      mainSplitter.setLastDividerLocation(mainSplitter.getDividerLocation());
    }

    treeSplitter.setResizeWeight(NODETREE_DIVIDER_WIDTH);
    treeSplitter.setDividerLocation(NODETREE_DIVIDER_WIDTH);
    treeSplitter.setLastDividerLocation(treeSplitter.getDividerLocation());

    overviewSplitter.setResizeWeight(OVERVIEW_DIVIDER_HEIGHT);
    overviewSplitter.setDividerLocation(OVERVIEW_DIVIDER_HEIGHT);
    overviewSplitter.setLastDividerLocation(overviewSplitter.getDividerLocation());
  }

  public void setLastMainDividerPositionAndDefaulResizeWeights(final int lastPosition) {
    mainSplitter.setDividerLocation(lastPosition);

    if (graphType == EGraph.COMBINED_GRAPH) {
      mainSplitter.setResizeWeight(COMBINED_MAIN_DIVIDER_WIDTH);
    } else if (graphType == EGraph.PRIMARY_GRAPH) {
      mainSplitter.setResizeWeight(PRIMARY_MAIN_DIVIDER_WIDTH);
    } else {
      mainSplitter.setResizeWeight(SECONDRAY_MAIN_DIVIDER_WIDTH);
    }
  }

  public void setMainDividerLocationAndResizeWeight(final double position) {
    mainSplitter.setResizeWeight(position);
    mainSplitter.setDividerLocation(position);
  }

  public void toggleGraphsPerspective() {
    if (graphType == EGraph.PRIMARY_GRAPH || graphType == EGraph.COMBINED_GRAPH) {
      if (mainSplitter.getDividerLocation() == 0.) {
        mainSplitter.setDividerLocation(mainSplitter.getLastDividerLocation());
      } else {
        mainSplitter.setDividerLocation(0.);
      }
    } else if (graphType == EGraph.SECONDARY_GRAPH) {
      if (mainSplitter.getRightComponent().getSize().width == 0.) {
        mainSplitter.setDividerLocation(mainSplitter.getLastDividerLocation());
      } else {
        mainSplitter.setDividerLocation(1.);
      }
    }
  }

  private class InternalSettingsChangedListener extends GraphSettingsChangedListenerAdapter {
    @Override
    public void focusSideChanged(final GraphSettings settings) {
      setFocusBorder(settings);
      updateUI();
    }

    @Override
    public void graphSyncChanged(final GraphSettings settings) {
      setFocusBorder(settings);
      updateUI();
    }

    @Override
    public void showScrollbarsChanged(final GraphSettings settings) {
      if (settings.getShowScrollbars()) {
        graph.getPrimaryGraph().getEditMode().setAdjustScrollBarPolicy(true);

        graph
            .getView()
            .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        graph.getView().setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

      } else {
        graph.getEditMode().setAdjustScrollBarPolicy(false);

        graph
            .getView()
            .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        graph.getView().setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
      }

      graph.getView().updateView();
    }
  }
}
