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

    final long w = Math.round(wNormal * (1 - GraphPanel.COMBINED_MAIN_DIVIDER_WIDTH));
    final long h = Math.round(hNormal);
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
    thread.setDescription("Configure View...");
    configureScrollPanes(graphs);
    setDoubleBufferedGraphViews(graphs);

    thread.setDescription("Layouting graphs...");

    GraphLayoutEventHandler.handleInitialLayoutEvent(graphs.getCombinedGraph());
  }
}
