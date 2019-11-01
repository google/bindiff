package com.google.security.zynamics.bindiff.graph.eventhandlers;

import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.exceptions.GraphLayoutException;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.layout.commands.GraphLayoutInitializer;
import com.google.security.zynamics.bindiff.graph.layout.commands.GraphLayoutUpdater;
import com.google.security.zynamics.bindiff.graph.layout.commands.GraphReactivateViewSynchronization;
import com.google.security.zynamics.bindiff.graph.layout.commands.GraphSelectionUpdater;
import com.google.security.zynamics.bindiff.graph.layout.commands.ProximityBrowserActivator;
import com.google.security.zynamics.bindiff.graph.layout.commands.ProximityBrowserDeactivator;
import com.google.security.zynamics.bindiff.graph.layout.commands.ProximityBrowserUnhideNode;
import com.google.security.zynamics.bindiff.graph.layout.commands.ProximityNodeClickedUpdater;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyProximityNode;
import java.util.logging.Level;

public class GraphLayoutEventHandler {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private static void blockAllGraphsIntermediateListeners(final BinDiffGraph<?, ?> graph) {
    graph.getIntermediateListeners().blockZyLibSelectionListeners();
    graph.getIntermediateListeners().blockZyLibVisibilityListeners();
  }

  private static void freeAllGraphsIntermediateListeners(final BinDiffGraph<?, ?> graph) {
    graph.getIntermediateListeners().freeZyLibVisibilityListeners();
    graph.getIntermediateListeners().freeZyLibSelectionListeners();
  }

  public static void handleDoLayoutButtonEvent(
      final BinDiffGraph<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>> graph,
      final boolean showProgress) {
    try {
      GraphLayoutUpdater.executeStatic(graph, showProgress);
    } catch (final GraphLayoutException e) {
      logger.at(Level.SEVERE).withCause(e).log(e.getMessage());
      CMessageBox.showError(BinDiffGraph.getParentWindow(graph), e.getMessage());
    }
  }

  public static void handleInitialLayoutEvent(
      final BinDiffGraph<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>> graph) {
    try {
      blockAllGraphsIntermediateListeners(graph);

      GraphLayoutInitializer.executeStatic(graph);
    } catch (final GraphLayoutException e) {
      logger.at(Level.SEVERE).withCause(e).log(e.getMessage());
      CMessageBox.showError(BinDiffGraph.getParentWindow(graph), e.getMessage());
    } finally {
      freeAllGraphsIntermediateListeners(graph);
    }
  }

  public static void handleProximityBrowsingActivatedEvent(
      final BinDiffGraph<ZyGraphNode<?>, ?> graph) {
    try {
      blockAllGraphsIntermediateListeners(graph);

      ProximityBrowserActivator.executeStatic(graph);
    } catch (final GraphLayoutException e) {
      logger.at(Level.SEVERE).withCause(e).log(e.getMessage());
      CMessageBox.showError(BinDiffGraph.getParentWindow(graph), e.getMessage());
    } finally {
      freeAllGraphsIntermediateListeners(graph);
    }
  }

  public static void handleProximityBrowsingDeactivatedEvent(
      final BinDiffGraph<ZyGraphNode<?>, ?> graph) {
    try {
      graph.getSettings().getProximitySettings().setProximityBrowsing(false);

      blockAllGraphsIntermediateListeners(graph);
      ProximityBrowserDeactivator.executeStatic(graph);
    } catch (final GraphLayoutException e) {
      logger.at(Level.SEVERE).withCause(e).log(e.getMessage());
      CMessageBox.showError(BinDiffGraph.getParentWindow(graph), e.getMessage());
    } finally {
      freeAllGraphsIntermediateListeners(graph);
    }
  }

  public static void handleProximityNodeClickedEvent(
      final BinDiffGraph<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>> graph,
      final ZyProximityNode<?> proximityNode) {
    try {
      blockAllGraphsIntermediateListeners(graph);

      ProximityNodeClickedUpdater.executeStatic(graph, proximityNode);
    } catch (final GraphLayoutException e) {
      logger.at(Level.SEVERE).withCause(e).log(e.getMessage());
      CMessageBox.showError(BinDiffGraph.getParentWindow(graph), e.getMessage());
    } finally {
      freeAllGraphsIntermediateListeners(graph);
    }
  }

  public static void handleReactivateViewSynchronization(
      final BinDiffGraph<ZyGraphNode<?>, ?> graph) {
    try {
      blockAllGraphsIntermediateListeners(graph);

      GraphReactivateViewSynchronization.executeStatic(graph);
    } catch (final GraphLayoutException e) {
      logger.at(Level.SEVERE).withCause(e).log(e.getMessage());
      CMessageBox.showError(BinDiffGraph.getParentWindow(graph), e.getMessage());
    } finally {
      freeAllGraphsIntermediateListeners(graph);
    }
  }

  public static void handleSelectionChangedEvent(
      final BinDiffGraph<ZyGraphNode<?>, ?> graph, final boolean showProgress) {
    try {
      blockAllGraphsIntermediateListeners(graph);

      GraphSelectionUpdater.executeStatic(graph);
    } catch (final GraphLayoutException e) {
      logger.at(Level.SEVERE).withCause(e).log(e.getMessage());
      CMessageBox.showError(BinDiffGraph.getParentWindow(graph), e.getMessage());
    } finally {
      freeAllGraphsIntermediateListeners(graph);
    }
  }

  public static void handleUnhideInvisibleNode(
      final BinDiffGraph<ZyGraphNode<?>, ?> graph, final ZyGraphNode<?> node) {
    try {
      blockAllGraphsIntermediateListeners(graph);

      ProximityBrowserUnhideNode.executeStatic(graph, node);
    } catch (final GraphLayoutException e) {
      logger.at(Level.SEVERE).withCause(e).log(e.getMessage());
      CMessageBox.showError(BinDiffGraph.getParentWindow(graph), e.getMessage());
    } finally {
      freeAllGraphsIntermediateListeners(graph);
    }
  }
}
