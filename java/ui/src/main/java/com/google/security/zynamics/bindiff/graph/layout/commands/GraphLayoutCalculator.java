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

package com.google.security.zynamics.bindiff.graph.layout.commands;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.exceptions.GraphLayoutException;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.SuperGraph;
import com.google.security.zynamics.bindiff.graph.edges.SingleDiffEdge;
import com.google.security.zynamics.bindiff.graph.edges.SuperDiffEdge;
import com.google.security.zynamics.bindiff.graph.helpers.GraphViewFitter;
import com.google.security.zynamics.bindiff.graph.layout.LayoutCreator;
import com.google.security.zynamics.bindiff.graph.layout.util.CustomizedPCListOptimizer;
import com.google.security.zynamics.bindiff.graph.layout.util.PortConstraints;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperDiffNode;
import com.google.security.zynamics.bindiff.graph.settings.GraphLayoutSettings;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.zylib.types.common.ICancelableCommand;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.helpers.ProximityHelper;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.layouters.ZyGraphLayouter;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.layout.CanonicMultiStageLayouter;
import y.layout.DefaultGraphLayout;
import y.layout.EdgeLayout;
import y.layout.GraphLayout;
import y.layout.LayoutTool;
import y.layout.NodeLayout;
import y.layout.circular.CircularLayouter;
import y.layout.hierarchic.HierarchicGroupLayouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.orthogonal.OrthogonalLayouter;
import y.view.Graph2D;

public class GraphLayoutCalculator implements ICancelableCommand {
  private final BinDiffGraph<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>>
      referenceGraph;

  private GraphLayout primaryLayout = new DefaultGraphLayout();
  private GraphLayout secondaryLayout = new DefaultGraphLayout();

  private GraphLayout combinedLayout = null;

  private InternalLayoutThread superLayoutThread = null;
  private InternalLayoutThread combinedLayoutThread = null;

  private CountDownLatch doneLatch;

  private volatile boolean canceled;

  public GraphLayoutCalculator(
      final BinDiffGraph<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>> referenceGraph) {
    checkNotNull(referenceGraph);
    checkArgument(
        !(referenceGraph instanceof SuperGraph), "Reference graph cannot be a super graph");

    this.referenceGraph = referenceGraph;
  }

  private static void addSuperProxiEdgesToMaps(
      final SuperGraph superDiffGraph,
      final Map<Edge, Edge> superToPriEdgeMap,
      final Map<Edge, Edge> superToSecEdgeMap) {
    final Graph2D superYGraph = superDiffGraph.getGraph();

    for (final Edge superYEdge : superYGraph.getEdgeArray()) {
      if (!ProximityHelper.isProximityEdge(superDiffGraph.getGraph(), superYEdge)) {
        continue;
      }

      final Node superYSource = superYEdge.source();
      final Node superYTarget = superYEdge.target();

      SuperDiffNode superDiffNode = superDiffGraph.getNode(superYSource);

      final boolean sourceIsProxi = superDiffNode == null;

      if (sourceIsProxi) {
        superDiffNode = superDiffGraph.getNode(superYEdge.target());
      }

      if (!ProximityHelper.isProximityNode(
          superDiffGraph.getGraph(), sourceIsProxi ? superYSource : superYTarget)) {
        continue;
      }

      final SingleDiffNode priDiffNode = superDiffNode.getPrimaryDiffNode();
      if (priDiffNode != null) {
        final Node priYNode = priDiffNode.getNode();

        for (final EdgeCursor ec = sourceIsProxi ? priYNode.inEdges() : priYNode.outEdges();
            ec.ok();
            ec.next()) {
          if (ProximityHelper.isProximityEdge(
              superDiffGraph.getPrimaryGraph().getGraph(), ec.edge())) {
            superToPriEdgeMap.put(superYEdge, ec.edge());
            break;
          }
        }
      }

      final SingleDiffNode secDiffNode = superDiffNode.getSecondaryDiffNode();
      if (secDiffNode != null) {
        final Node secYNode = secDiffNode.getNode();

        for (final EdgeCursor ec = sourceIsProxi ? secYNode.inEdges() : secYNode.outEdges();
            ec.ok();
            ec.next()) {
          if (ProximityHelper.isProximityEdge(
              superDiffGraph.getSecondaryGraph().getGraph(), ec.edge())) {
            superToSecEdgeMap.put(superYEdge, ec.edge());
            break;
          }
        }
      }
    }
  }

  private static void addSuperProxiNodesToMaps(
      final SuperGraph superDiffGraph,
      final Map<Node, Node> superToPriNodeMap,
      final Map<Node, Node> superToSecNodeMap) {
    final Graph2D superYGraph = superDiffGraph.getGraph();

    for (final Node superYNode : superYGraph.getNodeArray()) {
      if (superDiffGraph.getNode(superYNode) == null && superYNode.degree() == 1) {
        final Node superYNeighbor = superYNode.neighbors().node();
        final SuperDiffNode superNeighbor = superDiffGraph.getNode(superYNeighbor);
        final SingleDiffNode priDiffNeighbor = superNeighbor.getPrimaryDiffNode();
        final SingleDiffNode secDiffNeighbor = superNeighbor.getSecondaryDiffNode();

        if (superYNode.outDegree() == 1) {
          if (priDiffNeighbor != null) {
            final Node priYNeighbor = priDiffNeighbor.getNode();

            for (final EdgeCursor ec = priYNeighbor.inEdges(); ec.ok(); ec.next()) {
              if (ProximityHelper.isProximityEdge(
                  superDiffGraph.getPrimaryGraph().getGraph(), ec.edge())) {
                final Node priYProxi = ec.edge().source();
                superToPriNodeMap.put(superYNode, priYProxi);

                break;
              }
            }
          }

          if (secDiffNeighbor != null) {
            final Node secYNeighbor = secDiffNeighbor.getNode();

            for (final EdgeCursor ec = secYNeighbor.inEdges(); ec.ok(); ec.next()) {
              if (ProximityHelper.isProximityEdge(
                  superDiffGraph.getSecondaryGraph().getGraph(), ec.edge())) {
                final Node secYProxi = ec.edge().source();
                superToSecNodeMap.put(superYNode, secYProxi);

                break;
              }
            }
          }
        } else if (superYNode.inDegree() == 1) {
          if (priDiffNeighbor != null) {
            final Node priYNeighbor = priDiffNeighbor.getNode();

            for (final EdgeCursor ec = priYNeighbor.outEdges(); ec.ok(); ec.next()) {
              if (ProximityHelper.isProximityEdge(
                  superDiffGraph.getPrimaryGraph().getGraph(), ec.edge())) {
                final Node priYProxi = ec.edge().target();
                superToPriNodeMap.put(superYNode, priYProxi);

                break;
              }
            }
          }

          if (secDiffNeighbor != null) {
            final Node secYNeighbor = secDiffNeighbor.getNode();

            for (final EdgeCursor ec = secYNeighbor.outEdges(); ec.ok(); ec.next()) {
              if (ProximityHelper.isProximityEdge(
                  superDiffGraph.getSecondaryGraph().getGraph(), ec.edge())) {
                final Node secYProxi = ec.edge().target();
                superToSecNodeMap.put(superYNode, secYProxi);

                break;
              }
            }
          }
        }
      }
    }
  }

  private static void createSuperEdgeMaps(
      final SuperGraph superDiffGraph,
      final Map<Edge, Edge> superToPriEdgeMap,
      final Map<Edge, Edge> superToSecEdgeMap) {
    for (final SuperDiffEdge superDiffEdge : superDiffGraph.getEdges()) {
      final SingleDiffEdge primaryDiffEdge = superDiffEdge.getPrimaryDiffEdge();
      final SingleDiffEdge secondaryDiffEdge = superDiffEdge.getSecondaryDiffEdge();

      if (primaryDiffEdge != null && superDiffEdge.isVisible()) {
        final Edge primaryYedge = primaryDiffEdge.getEdge();
        superToPriEdgeMap.put(superDiffEdge.getEdge(), primaryYedge);
      }

      if (secondaryDiffEdge != null && superDiffEdge.isVisible()) {
        final Edge secondaryYedge = secondaryDiffEdge.getEdge();
        superToSecEdgeMap.put(superDiffEdge.getEdge(), secondaryYedge);
      }
    }

    addSuperProxiEdgesToMaps(superDiffGraph, superToPriEdgeMap, superToSecEdgeMap);
  }

  private static void createSuperNodeMaps(
      final SuperGraph superDiffGraph,
      final Map<Node, Node> superToPriNodeMap,
      final Map<Node, Node> superToSecNodeMap) {
    for (final SuperDiffNode superDiffNode : superDiffGraph.getNodes()) {
      final SingleDiffNode primaryDiffNode = superDiffNode.getPrimaryDiffNode();
      final SingleDiffNode secondaryDiffNode = superDiffNode.getSecondaryDiffNode();

      if (primaryDiffNode != null && superDiffNode.isVisible()) {
        final Node primaryYnode = primaryDiffNode.getNode();
        superToPriNodeMap.put(superDiffNode.getNode(), primaryYnode);
      }

      if (secondaryDiffNode != null && superDiffNode.isVisible()) {
        final Node secondaryYnode = secondaryDiffNode.getNode();
        superToSecNodeMap.put(superDiffNode.getNode(), secondaryYnode);
      }
    }

    addSuperProxiNodesToMaps(superDiffGraph, superToPriNodeMap, superToSecNodeMap);
  }

  private void adoptSuperGraphLayout(final GraphLayout superLayout) {
    final SuperGraph superDiffGraph = referenceGraph.getSuperGraph();

    final Map<Node, Node> superToPriNodeMap = new HashMap<>();
    final Map<Node, Node> superToSecNodeMap = new HashMap<>();

    createSuperNodeMaps(superDiffGraph, superToPriNodeMap, superToSecNodeMap);

    for (final Node superYNode : referenceGraph.getSuperGraph().getGraph().getNodeArray()) {
      final NodeLayout refNodeLayout = superLayout.getNodeLayout(superYNode);

      final Node priYNode = superToPriNodeMap.get(superYNode);
      final Node secYNode = superToSecNodeMap.get(superYNode);

      if (priYNode != null) {
        ((DefaultGraphLayout) primaryLayout).setNodeLayout(priYNode, refNodeLayout);
      }

      if (secYNode != null) {
        ((DefaultGraphLayout) secondaryLayout).setNodeLayout(secYNode, refNodeLayout);
      }
    }

    superToPriNodeMap.clear();
    superToSecNodeMap.clear();

    final Map<Edge, Edge> superToPriEdgeMap = new HashMap<>();
    final Map<Edge, Edge> superToSecEdgeMap = new HashMap<>();

    createSuperEdgeMaps(superDiffGraph, superToPriEdgeMap, superToSecEdgeMap);

    for (final Edge superYEdge : superDiffGraph.getGraph().getEdgeArray()) {
      final EdgeLayout refEdgeLayout = superLayout.getEdgeLayout(superYEdge);

      final Edge priYEdge = superToPriEdgeMap.get(superYEdge);
      final Edge secYEdge = superToSecEdgeMap.get(superYEdge);

      if (priYEdge != null) {
        ((DefaultGraphLayout) primaryLayout).setEdgeLayout(priYEdge, refEdgeLayout);
      }

      if (secYEdge != null) {
        ((DefaultGraphLayout) secondaryLayout).setEdgeLayout(secYEdge, refEdgeLayout);
      }
    }
  }

  @SuppressWarnings("deprecation") // yFiles support 20101028: "The only way to stop the y.jar
  // internal layout threads!
  private void cancelLayoutCalculation() {
    if (superLayoutThread != null && superLayoutThread.isAlive()) {
      threadStop(superLayoutThread);
      superLayoutThread = null;

      doneLatch.countDown();
    }

    if (combinedLayoutThread != null && combinedLayoutThread.isAlive()) {

      threadStop(combinedLayoutThread);
      combinedLayoutThread = null;

      doneLatch.countDown();
    }

    setCanceled();
  }

  private static void threadStop(Thread thread) {
    // TODO: b/447223240 - clean up obsolete references to Thread.stop.
    // Thread#stop has been deprecated since JDK 1.2. Starting in JDK 20 it always throws
    // UnsupportedOperationException, and in JDK 26 the method has been removed.
    try {
      Thread.class.getMethod("stop").invoke(thread);
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof UnsupportedOperationException) {
        throw (UnsupportedOperationException) e.getCause();
      }
      throw new UnsupportedOperationException(e);
    } catch (ReflectiveOperationException e) {
      throw new UnsupportedOperationException(e);
    }
  }

  private CanonicMultiStageLayouter createSecondThreadLayouter(
      final CanonicMultiStageLayouter layouter, final GraphLayoutSettings settings) {
    if (layouter instanceof CircularLayouter) {
      return LayoutCreator.getCircularLayout(settings);
    } else if (layouter instanceof IncrementalHierarchicLayouter
        || layouter instanceof HierarchicGroupLayouter) {
      return LayoutCreator.getHierarchicalLayout(settings);
    } else if (layouter instanceof OrthogonalLayouter) {
      return LayoutCreator.getOrthogonalLayout(settings);
    }

    throw new IllegalStateException("Unsupported graph layout style.");
  }

  private synchronized void setCanceled() {
    canceled = true;
  }

  @Override
  public void cancel() throws Exception {
    cancelLayoutCalculation();
  }

  @Override
  public void execute() throws GraphLayoutException {
    try {
      final GraphSettings settings = referenceGraph.getSettings();

      if (settings.isSync()) {
        final SuperGraph superDiffGraph = referenceGraph.getSuperGraph();
        final CombinedGraph combinedDiffGraph = referenceGraph.getCombinedGraph();

        final CanonicMultiStageLayouter supergraphLayouter =
            settings.getLayoutSettings().getCurrentLayouter();
        final CanonicMultiStageLayouter combinedGraphLayouter =
            createSecondThreadLayouter(supergraphLayouter, settings.getLayoutSettings());

        if (combinedGraphLayouter instanceof IncrementalHierarchicLayouter) {
          final IncrementalHierarchicLayouter layouter =
              (IncrementalHierarchicLayouter) combinedGraphLayouter;

          layouter.setBackloopRoutingEnabled(false);
          layouter.setSelfLoopLayouterEnabled(false);

          final CustomizedPCListOptimizer customizedPCListOptimizer =
              new CustomizedPCListOptimizer();
          customizedPCListOptimizer.setBackloopRouting(true);
          layouter.getHierarchicLayouter().setPortConstraintOptimizer(customizedPCListOptimizer);
        }

        ZyGraphLayouter.alignNodesToTopLayer(superDiffGraph.getGraph(), supergraphLayouter);
        ZyGraphLayouter.alignNodesToTopLayer(combinedDiffGraph.getGraph(), combinedGraphLayouter);

        doneLatch = new CountDownLatch(2);

        superLayoutThread = new InternalLayoutThread(superDiffGraph, supergraphLayouter);
        combinedLayoutThread = new InternalLayoutThread(combinedDiffGraph, combinedGraphLayouter);

        superLayoutThread.start();
        combinedLayoutThread.start();

        doneLatch.await();

        if (superLayoutThread != null && combinedLayoutThread != null) {

          if (superLayoutThread.getException() != null) {
            throw superLayoutThread.getException();
          }

          if (combinedLayoutThread.getException() != null) {
            throw combinedLayoutThread.getException();
          }

          combinedLayout = combinedLayoutThread.getGraphLayout();

          final GraphLayout superLayout = superLayoutThread.getGraphLayout();
          LayoutTool.applyGraphLayout(superDiffGraph.getGraph(), superLayout);

          GraphViewFitter.adoptSuperViewCanvasProperties(superDiffGraph);

          adoptSuperGraphLayout(superLayout);
        }
      } else {
        if (referenceGraph instanceof SingleGraph) {
          if (((SingleGraph) referenceGraph).getSide() == ESide.PRIMARY) {

            primaryLayout = referenceGraph.calculateLayout();
          } else {
            secondaryLayout = referenceGraph.calculateLayout();
          }

        } else if (referenceGraph instanceof CombinedGraph) {
          combinedLayout = referenceGraph.calculateLayout();
        }
      }
    } catch (final GraphLayoutException e) {
      throw e;
    } catch (final Exception e) {
      throw new GraphLayoutException(e, "Could not calculate graph layouts.");
    }
  }

  public GraphLayout getCombinedGraphLayout() {
    return combinedLayout;
  }

  public GraphLayout getPrimaryGraphLayout() {
    return primaryLayout;
  }

  public BinDiffGraph<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>>
      getReferenceGraph() {
    return referenceGraph;
  }

  public GraphLayout getSecondaryGraphLayout() {
    return secondaryLayout;
  }

  @Override
  public synchronized boolean wasCanceled() {
    return canceled;
  }

  private class InternalLayoutThread extends Thread {
    private final BinDiffGraph<?, ?> graph;

    private final CanonicMultiStageLayouter layouter;

    private GraphLayout graphLayout = null;

    private GraphLayoutException exception = null;

    protected InternalLayoutThread(
        final BinDiffGraph<?, ?> graph, final CanonicMultiStageLayouter layouter) {
      this.graph = graph;
      this.layouter = layouter;
    }

    protected GraphLayoutException getException() {
      return exception;
    }

    protected GraphLayout getGraphLayout() {
      return graphLayout;
    }

    @Override
    public void run() {
      try {
        if (graph instanceof CombinedGraph) {
          PortConstraints.configureConstraints((CombinedGraph) graph);
        }

        graphLayout = graph.calculateLayout(layouter);
      } catch (final GraphLayoutException e) {
        exception = e;
      } finally {
        doneLatch.countDown();
      }
    }
  }
}
