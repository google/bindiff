package com.google.security.zynamics.bindiff.graph.listeners;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.GraphsContainer;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.log.Logger;

public class GraphViewsListenerManager {
  private final GraphsContainer graphs;

  private final SingleViewCanvasListener primaryViewCanvasListener;
  private final SingleViewCanvasListener secondaryViewCanvasListener;

  private final CombinedGraphMouseListener combinedGraphMouseListener;
  private final SingleGraphMouseListener primaryGraphMouseListener;
  private final SingleGraphMouseListener secondaryGraphMouseListener;

  private final SingleViewFocusListener primaryViewFocusListener;
  private final SingleViewFocusListener secondaryViewFocusListener;

  public GraphViewsListenerManager(
      final GraphsContainer graphs, final ViewTabPanelFunctions controller) {
    Preconditions.checkNotNull(controller);
    this.graphs = Preconditions.checkNotNull(graphs);

    primaryViewCanvasListener = new SingleViewCanvasListener(controller, graphs.getPrimaryGraph());
    secondaryViewCanvasListener =
        new SingleViewCanvasListener(controller, graphs.getSecondaryGraph());

    combinedGraphMouseListener =
        new CombinedGraphMouseListener(controller, graphs.getCombinedGraph());
    primaryGraphMouseListener = new SingleGraphMouseListener(controller, graphs.getPrimaryGraph());
    secondaryGraphMouseListener =
        new SingleGraphMouseListener(controller, graphs.getSecondaryGraph());

    primaryViewFocusListener = new SingleViewFocusListener(controller, graphs.getPrimaryGraph());
    secondaryViewFocusListener =
        new SingleViewFocusListener(controller, graphs.getSecondaryGraph());
  }

  public void addGraphMouseListener(final BinDiffGraph<?, ?> graph) {
    if (graph == graphs.getPrimaryGraph()) {
      primaryGraphMouseListener.addListener();
    } else if (graph == graphs.getSecondaryGraph()) {
      secondaryGraphMouseListener.addListener();
    } else if (graph == graphs.getCombinedGraph()) {
      combinedGraphMouseListener.addListener();
    } else {
      Logger.logWarning("Unknown graph! Add graph mouse listener was ignored.");
    }
  }

  public void addViewCanvasListener(final BinDiffGraph<?, ?> graph) {
    if (graph == graphs.getPrimaryGraph()) {
      primaryViewCanvasListener.addListener();
    } else if (graph == graphs.getSecondaryGraph()) {
      secondaryViewCanvasListener.addListener();
    } else {
      Logger.logWarning("Unknown graph! Add view canvas listener was ignored.");
    }
  }

  public void addViewFocusListener(final BinDiffGraph<?, ?> graph) {
    if (graph == graphs.getPrimaryGraph()) {
      primaryViewFocusListener.addListener();
    } else if (graph == graphs.getSecondaryGraph()) {
      secondaryViewFocusListener.addListener();
    } else {
      Logger.logWarning("Unknown graph! Add graph view focus listener was ignored.");
    }
  }

  public void dispose() {
    removeViewCanvasListener(graphs.getPrimaryGraph());
    removeViewCanvasListener(graphs.getSecondaryGraph());

    removeViewFocusListener(graphs.getPrimaryGraph());
    removeViewFocusListener(graphs.getSecondaryGraph());

    removeGraphMouseListener(graphs.getPrimaryGraph());
    removeGraphMouseListener(graphs.getSecondaryGraph());
    removeGraphMouseListener(graphs.getCombinedGraph());
  }

  public void removeGraphMouseListener(final BinDiffGraph<?, ?> graph) {
    try {
      if (graph == graphs.getPrimaryGraph()) {
        primaryGraphMouseListener.removeListener();
      } else if (graph == graphs.getSecondaryGraph()) {
        secondaryGraphMouseListener.removeListener();
      } else if (graph == graphs.getCombinedGraph()) {
        combinedGraphMouseListener.removeListener();
      } else {
        Logger.logWarning("Unknown graph! Remove graph mouse listener was ignored.");
      }
    } catch (final IllegalStateException e) {
      Logger.logWarning("Listener was not listening.");
    }
  }

  public void removeViewCanvasListener(final BinDiffGraph<?, ?> graph) {
    try {
      if (graph == graphs.getPrimaryGraph()) {
        primaryViewCanvasListener.removeListener();
      } else if (graph == graphs.getSecondaryGraph()) {
        secondaryViewCanvasListener.removeListener();
      } else {
        Logger.logWarning("Unknown graph! Remove view canvas listener was ignored.");
      }
    } catch (final IllegalStateException e) {
      Logger.logWarning("Listener was not listening.");
    }
  }

  public void removeViewFocusListener(final BinDiffGraph<?, ?> graph) {
    try {
      if (graph == graphs.getPrimaryGraph()) {
        primaryViewFocusListener.addListener();
      } else if (graph == graphs.getSecondaryGraph()) {
        secondaryViewFocusListener.addListener();
      } else {
        Logger.logWarning("Unknown graph! Add graph view focus listener was ignored.");
      }
    } catch (final IllegalStateException e) {
      Logger.logWarning("Listener was not listening.");
    }
  }

  public void suppressUpdating(final boolean suppress) {
    primaryViewCanvasListener.suppressUpdateGraph(suppress);
    secondaryViewCanvasListener.suppressUpdateGraph(suppress);
  }
}
