// Copyright 2011-2023 Google LLC
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

import com.google.security.zynamics.bindiff.graph.GraphsContainer;
import com.google.security.zynamics.bindiff.graph.SuperGraph;
import com.google.security.zynamics.bindiff.graph.eventhandlers.GraphLayoutEventHandler;
import com.google.security.zynamics.bindiff.graph.helpers.GraphViewFitter;
import com.google.security.zynamics.bindiff.graph.helpers.GraphZoomer;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.subpanels.GraphPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.viewpanel.CNormalViewPanel;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessHelperThread;
import java.awt.Dimension;
import javax.swing.ScrollPaneConstants;
import y.view.Graph2DView;

public class ViewTabPanelInitializer {
  private static void configureScrollPanes(final GraphsContainer graphs) {
    final boolean scrollbars = graphs.getSettings().getShowScrollbars();

    int horizontalState = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
    int verticalState = ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;

    if (scrollbars) {
      horizontalState = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS;
      verticalState = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
    }

    graphs.getPrimaryGraph().getEditMode().setAdjustScrollBarPolicy(scrollbars);
    graphs.getSecondaryGraph().getEditMode().setAdjustScrollBarPolicy(scrollbars);
    graphs.getCombinedGraph().getEditMode().setAdjustScrollBarPolicy(scrollbars);

    graphs.getPrimaryGraph().getView().setHorizontalScrollBarPolicy(horizontalState);
    graphs.getSecondaryGraph().getView().setHorizontalScrollBarPolicy(horizontalState);
    graphs.getCombinedGraph().getView().setHorizontalScrollBarPolicy(horizontalState);
    graphs.getPrimaryGraph().getView().setVerticalScrollBarPolicy(verticalState);
    graphs.getSecondaryGraph().getView().setVerticalScrollBarPolicy(verticalState);
    graphs.getCombinedGraph().getView().setVerticalScrollBarPolicy(verticalState);
  }

  private static void setDoubleBufferedGraphViews(final GraphsContainer graphs) {
    graphs.getPrimaryGraph().getView().setDoubleBuffered(true);
    graphs.getSecondaryGraph().getView().setDoubleBuffered(true);
    graphs.getCombinedGraph().getView().setDoubleBuffered(true);
  }

  public static void centerCombinedGraph(
      final GraphsContainer graphs, final ViewTabPanel viewPanel) {
    // ensures that the combined graph is positioned in the middle of the combined view
    final Graph2DView combinedYView = graphs.getCombinedGraph().getView();

    final CNormalViewPanel normalViewPanel = viewPanel.getNormalViewPanel();

    final int wNormal = normalViewPanel.getSize().width;
    final int hNormal = normalViewPanel.getSize().height;

    final long w = Math.round(wNormal * (1.0 - GraphPanel.COMBINED_MAIN_DIVIDER_WIDTH));
    final long h = Math.round(hNormal * 1.0);
    combinedYView.setSize((int) w, (int) h);
    combinedYView.setPreferredSize(new Dimension((int) w, (int) h));

    combinedYView.fitWorldRect();
    combinedYView.fitContent();

    combinedYView.setZoom(
        graphs.getCombinedGraph().getView().getZoom() * GraphZoomer.ZOOM_OUT_FACTOR);
  }

  public static void centerSingleGraphs(final SuperGraph superGraph) {
    GraphViewFitter.adoptSuperViewCanvasProperties(superGraph);
    GraphViewFitter.fitSingleViewToSuperViewContent(superGraph);
  }

  public static void initialize(final GraphsContainer graphs, final CEndlessHelperThread thread) {
    thread.setDescription("Configuring view...");
    configureScrollPanes(graphs);
    setDoubleBufferedGraphViews(graphs);

    thread.setDescription("Layouting graphs...");

    GraphLayoutEventHandler.handleInitialLayoutEvent(graphs.getCombinedGraph());
  }
}
