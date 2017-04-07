package com.google.security.zynamics.bindiff.graph.listeners;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.EGraph;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.eventhandlers.GraphLayoutEventHandler;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.zygraph.IZyGraphSelectionListener;
import com.google.security.zynamics.zylib.gui.zygraph.IZyGraphVisibilityListener;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

public class GraphsIntermediateListeners {
  private final BinDiffGraph<ZyGraphNode<? extends IViewNode<?>>, ?> graph;

  private final ListenerProvider<IZyGraphSelectionListener> intermediateSelectionListener =
      new ListenerProvider<>();
  private final ListenerProvider<IZyGraphVisibilityListener> intermediateVisibilityListener =
      new ListenerProvider<>();

  private final InternalGraphSelectionListener selectionListener;
  private final InternalGraphVisibilityListener visibilityListener;

  private InternalGraphSelectionListener[] savedSelectionListeners =
      new InternalGraphSelectionListener[4];
  private InternalGraphVisibilityListener[] savedVisibilityListeners =
      new InternalGraphVisibilityListener[4];

  public GraphsIntermediateListeners(
      final BinDiffGraph<ZyGraphNode<? extends IViewNode<?>>, ?> graph) {
    Preconditions.checkNotNull(graph);

    this.graph = graph;

    selectionListener = new InternalGraphSelectionListener(graph);
    visibilityListener = new InternalGraphVisibilityListener(graph);

    addListeners();
  }

  public static void notifyIntermediateSelectionListeners(final BinDiffGraph<?, ?> graph) {
    if (graph.getSettings().isSync()) {
      graph.getPrimaryGraph().getIntermediateListeners().notifyIntermediateSelectionListener();
      graph.getSecondaryGraph().getIntermediateListeners().notifyIntermediateSelectionListener();
      graph.getCombinedGraph().getIntermediateListeners().notifyIntermediateSelectionListener();
      graph.getSuperGraph().getIntermediateListeners().notifyIntermediateSelectionListener();
    } else {
      graph.getIntermediateListeners().notifyIntermediateSelectionListener();
    }
  }

  public static void notifyIntermediateVisibilityListeners(final BinDiffGraph<?, ?> graph) {
    if (graph.getSettings().isSync()) {
      graph.getPrimaryGraph().getIntermediateListeners().notifyIntermediateVisibilityListener();
      graph.getSecondaryGraph().getIntermediateListeners().notifyIntermediateVisibilityListener();
      graph.getCombinedGraph().getIntermediateListeners().notifyIntermediateVisibilityListener();
      graph.getSuperGraph().getIntermediateListeners().notifyIntermediateVisibilityListener();
    } else {
      graph.getIntermediateListeners().notifyIntermediateSelectionListener();
    }
  }

  private void addListeners() {
    graph.addListener(selectionListener);
    graph.addListener(visibilityListener);
  }

  private GraphsIntermediateListeners getGraphIntermediateListener(final EGraph type) {
    Preconditions.checkNotNull(graph);
    switch (type) {
      case PRIMARY_GRAPH:
        Preconditions.checkNotNull(graph.getPrimaryGraph());
        return graph.getPrimaryGraph().getIntermediateListeners();
      case SECONDARY_GRAPH:
        Preconditions.checkNotNull(graph.getSecondaryGraph());
        return graph.getSecondaryGraph().getIntermediateListeners();
      case COMBINED_GRAPH:
        Preconditions.checkNotNull(graph.getCombinedGraph());
        return graph.getCombinedGraph().getIntermediateListeners();
      case SUPER_GRAPH:
        Preconditions.checkNotNull(graph.getSuperGraph());
        return graph.getSuperGraph().getIntermediateListeners();
      default:
        throw new IllegalStateException();
    }
  }

  private InternalGraphSelectionListener getSelectionListener() {
    return selectionListener;
  }

  private InternalGraphVisibilityListener getVisibilityListener() {
    return visibilityListener;
  }

  private void notifyIntermediateSelectionListener() {
    for (final IZyGraphSelectionListener listener : intermediateSelectionListener) {
      listener.selectionChanged();
    }
  }

  private void notifyIntermediateVisibilityListener() {
    for (final IZyGraphVisibilityListener listener : intermediateVisibilityListener) {
      listener.visibilityChanged();
    }
  }

  public void addIntermediateListener(final IZyGraphSelectionListener listener) {
    intermediateSelectionListener.addListener(listener);
  }

  public void addIntermediateListener(final IZyGraphVisibilityListener listener) {
    intermediateVisibilityListener.addListener(listener);
  }

  public void blockZyLibSelectionListeners() {
    savedSelectionListeners[0] =
        getGraphIntermediateListener(EGraph.PRIMARY_GRAPH).getSelectionListener();
    savedSelectionListeners[1] =
        getGraphIntermediateListener(EGraph.SECONDARY_GRAPH).getSelectionListener();
    savedSelectionListeners[2] =
        getGraphIntermediateListener(EGraph.SUPER_GRAPH).getSelectionListener();
    savedSelectionListeners[3] =
        getGraphIntermediateListener(EGraph.COMBINED_GRAPH).getSelectionListener();

    for (final InternalGraphSelectionListener listener : savedSelectionListeners) {
      if (listener == null) {
        continue;
      }
      listener.getGraph().removeListener(listener);
    }
  }

  public void blockZyLibVisibilityListeners() {
    savedVisibilityListeners[0] =
        getGraphIntermediateListener(EGraph.PRIMARY_GRAPH).getVisibilityListener();
    savedVisibilityListeners[1] =
        getGraphIntermediateListener(EGraph.SECONDARY_GRAPH).getVisibilityListener();
    savedVisibilityListeners[2] =
        getGraphIntermediateListener(EGraph.SUPER_GRAPH).getVisibilityListener();
    savedVisibilityListeners[3] =
        getGraphIntermediateListener(EGraph.COMBINED_GRAPH).getVisibilityListener();

    for (final InternalGraphVisibilityListener listener : savedVisibilityListeners) {
      if (listener == null) {
        continue;
      }
      listener.getGraph().removeListener(listener);
    }
  }

  public void dispose() {
    graph.removeListener(selectionListener);
    graph.removeListener(visibilityListener);

    savedSelectionListeners = null;
    savedVisibilityListeners = null;
  }

  public void freeZyLibSelectionListeners() {
    for (final InternalGraphSelectionListener listener : savedSelectionListeners) {
      if (listener == null) {
        continue;
      }
      listener.getGraph().addListener(listener);
    }
  }

  public void freeZyLibVisibilityListeners() {
    for (final InternalGraphVisibilityListener listener : savedVisibilityListeners) {
      if (listener == null) {
        continue;
      }
      listener.getGraph().addListener(listener);
    }
  }

  public void removeIntermediateListener(final IZyGraphSelectionListener listener) {
    intermediateSelectionListener.removeListener(listener);
  }

  public void removeIntermediateListener(final IZyGraphVisibilityListener listener) {
    intermediateVisibilityListener.removeListener(listener);
  }

  private static class InternalGraphSelectionListener implements IZyGraphSelectionListener {
    private final BinDiffGraph<ZyGraphNode<? extends IViewNode<?>>, ?> graph;

    private InternalGraphSelectionListener(
        final BinDiffGraph<ZyGraphNode<? extends IViewNode<?>>, ?> graph) {
      this.graph = graph;
    }

    public BinDiffGraph<ZyGraphNode<? extends IViewNode<?>>, ?> getGraph() {
      return graph;
    }

    @Override
    public void selectionChanged() {
      GraphLayoutEventHandler.handleSelectionChangedEvent(graph, true);
    }
  }

  // TODO(cblichmann): This class seems to be unused
  private static class InternalGraphVisibilityListener implements IZyGraphVisibilityListener {
    private final BinDiffGraph<ZyGraphNode<? extends IViewNode<?>>, ?> graph;

    private InternalGraphVisibilityListener(
        final BinDiffGraph<ZyGraphNode<? extends IViewNode<?>>, ?> graph) {
      this.graph = graph;
    }

    public BinDiffGraph<ZyGraphNode<? extends IViewNode<?>>, ?> getGraph() {
      return graph;
    }

    @Override
    public void nodeDeleted() {}

    @Override
    public void visibilityChanged() {}
  }
}
