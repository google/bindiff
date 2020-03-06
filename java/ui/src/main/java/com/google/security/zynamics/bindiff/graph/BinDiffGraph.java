// Copyright 2011-2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.bindiff.graph;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.enums.EGraphType;
import com.google.security.zynamics.bindiff.exceptions.GraphLayoutException;
import com.google.security.zynamics.bindiff.graph.listeners.GraphsIntermediateListeners;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperDiffNode;
import com.google.security.zynamics.bindiff.graph.searchers.GraphSearcher;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.CViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.ZyGraph2DView;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.ZyGraphMappings;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.functions.LayoutFunctions;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.helpers.ProximityHelper;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.Window;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import y.algo.AlgorithmAbortedException;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.layout.BufferedLayouter;
import y.layout.CanonicMultiStageLayouter;
import y.layout.GraphLayout;
import y.layout.LabelLayoutTranslator;

public abstract class BinDiffGraph<
        NodeType extends ZyGraphNode<? extends IViewNode<?>>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    extends AbstractZyGraph<NodeType, EdgeType> {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private GraphsIntermediateListeners intermediateListeners;

  private final ProximityBrowser<NodeType, EdgeType> proximityBrowser;

  private GraphSettings settings;

  private final EGraphType viewType;

  private GraphsContainer graphs;

  @SuppressWarnings("unchecked")
  protected BinDiffGraph(
      final ZyGraph2DView view,
      final LinkedHashMap<Node, NodeType> nodeMap,
      final LinkedHashMap<Edge, EdgeType> edgeMap,
      final GraphSettings settings,
      final EGraphType graphType) {
    super(view, nodeMap, edgeMap, settings);

    // Must be set first
    this.settings = checkNotNull(settings);
    this.viewType = checkNotNull(graphType);

    this.intermediateListeners =
        new GraphsIntermediateListeners(
            (BinDiffGraph<ZyGraphNode<? extends IViewNode<?>>, ?>) this);
    this.proximityBrowser = new ProximityBrowser<>(this);

    setProximityBrowser(this.proximityBrowser);
  }

  public static Window getParentWindow(final BinDiffGraph<?, ?> graph) {
    return SwingUtilities.getWindowAncestor(graph.getView());
  }

  public void addEdgeToMappings(final EdgeType diffEdge) {
    final Edge edge = diffEdge.getEdge();

    final ZyGraphMappings<NodeType, EdgeType> mappings = getMappings();
    mappings.addEdge(edge, diffEdge);
  }

  public void addNodeToMappings(final NodeType diffNode) {
    final Node node = diffNode.getNode();

    final ZyGraphMappings<NodeType, EdgeType> mappings = getMappings();
    mappings.addNode(node, diffNode);
  }

  public GraphLayout calculateLayout() throws GraphLayoutException {
    // Note: Not thread-safe, in the sense of simultaneous multiple layout calculations.
    GraphLayout graphLayout = null;

    final CanonicMultiStageLayouter layouter =
        getSettings().getLayoutSettings().getCurrentLayouter();

    try {
      layouter.setLabelLayouter(new LabelLayoutTranslator());
      layouter.setLabelLayouterEnabled(true);

      graphLayout = new BufferedLayouter(layouter).calcLayout(getGraph());

      LayoutFunctions.recalculatePorts(layouter, getGraph());
    } catch (final AlgorithmAbortedException e) {
      // Do nothing, user has canceled layout thread
      logger.at(Level.SEVERE).withCause(e).log(e.getMessage());
    } catch (final Exception e) {
      throw new GraphLayoutException(e, "Could not calculate graph layout.");
    }

    return graphLayout;
  }

  public GraphLayout calculateLayout(final CanonicMultiStageLayouter layouter)
      throws GraphLayoutException {
    // Note: This function is thread-save as long as each calling thread uses it's own layouter
    // instance.

    GraphLayout graphLayout = null;

    try {
      layouter.setLabelLayouter(new LabelLayoutTranslator());
      layouter.setLabelLayouterEnabled(true);

      graphLayout = new BufferedLayouter(layouter).calcLayout(getGraph());

      LayoutFunctions.recalculatePorts(layouter, getGraph());
    } catch (final AlgorithmAbortedException e) {
      // Do nothing, user has canceled layout thread
      logger.at(Level.SEVERE).withCause(e).log(e.getMessage());
    } catch (final Exception e) {
      throw new GraphLayoutException(e, "Could not calculate graph layout.");
    }

    return graphLayout;
  }

  // Be careful: Just use this function from static CMatchRemover functions
  public void deleteEdge(EdgeType diffEdge) {
    if (diffEdge == null) {
      return;
    }

    @SuppressWarnings("unchecked")
    final NodeType sourceNode = (NodeType) diffEdge.getSource();
    @SuppressWarnings("unchecked")
    final NodeType targetNode = (NodeType) diffEdge.getTarget();

    Node yProxyNode = null;
    if (ProximityHelper.isProximityNode(getGraph(), sourceNode.getNode())) {
      yProxyNode = sourceNode.getNode();
    } else if (ProximityHelper.isProximityNode(getGraph(), targetNode.getNode())) {
      yProxyNode = targetNode.getNode();
    }

    if (yProxyNode != null) {
      getProximityBrowser().deleteProximityBrowsingNode(yProxyNode);
    } else {
      diffEdge.dispose();

      getMappings().removeEdge(diffEdge);
      getGraph().removeEdge(diffEdge.getEdge());

      if (sourceNode instanceof SingleDiffNode && targetNode instanceof SingleDiffNode) {
        SingleDiffNode.unlink((SingleDiffNode) sourceNode, (SingleDiffNode) targetNode);
      } else if (sourceNode instanceof CombinedDiffNode && targetNode instanceof CombinedDiffNode) {
        CombinedDiffNode.unlink((CombinedDiffNode) sourceNode, (CombinedDiffNode) targetNode);
      } else if (sourceNode instanceof SuperDiffNode && targetNode instanceof SuperDiffNode) {
        SuperDiffNode.unlink((SuperDiffNode) sourceNode, (SuperDiffNode) targetNode);
      }
    }

    diffEdge = null;
  }

  // Be careful: Just use this function from static MatchRemover functions
  public void deleteNode(NodeType diffNode) {
    if (diffNode == null) {
      return;
    }

    Node node = diffNode.getNode();
    CViewNode<?> viewNode = (CViewNode<?>) diffNode.getRawNode();

    viewNode.removeAllListeners();

    final ZyGraphMappings<NodeType, EdgeType> mappings = getMappings();

    for (final EdgeCursor ec = node.edges(); ec.ok(); ec.next()) {
      final EdgeType diffEdge = mappings.getEdge(ec.edge());
      deleteEdge(diffEdge);
    }

    getMappings().removeNode(diffNode);
    getGraph().removeNode(node);

    node = null;
    viewNode = null;
    diffNode = null;
  }

  @Override
  public void dispose() {
    super.dispose();

    intermediateListeners.dispose();

    settings = null;
    intermediateListeners = null;
    graphs = null;
  }

  @Override
  public void doLayout() {
    // doLayout() is not handled here. Look at GraphLayoutHandler!
  }

  public CombinedGraph getCombinedGraph() {
    return graphs.getCombinedGraph();
  }

  @Override
  public Collection<EdgeType> getEdges() {
    return super.getEdges();
  }

  public GraphsContainer getGraphs() {
    return graphs;
  }

  public abstract GraphSearcher getGraphSearcher();

  public EGraphType getGraphType() {
    return viewType;
  }

  public GraphsIntermediateListeners getIntermediateListeners() {
    return intermediateListeners;
  }

  @Override
  public Collection<NodeType> getNodes() {
    return super.getNodes();
  }

  public SingleGraph getPrimaryGraph() {
    return graphs.getPrimaryGraph();
  }

  @Override
  public ProximityBrowser<NodeType, EdgeType> getProximityBrowser() {
    return proximityBrowser;
  }

  public SingleGraph getSecondaryGraph() {
    return graphs.getSecondaryGraph();
  }

  @Override
  public abstract Set<NodeType> getSelectedNodes();

  @Override
  public GraphSettings getSettings() {
    return settings;
  }

  public SuperGraph getSuperGraph() {
    return graphs.getSuperGraph();
  }

  public boolean isCombinedGraph() {
    return this == getCombinedGraph();
  }

  public boolean isPrimaryGraph() {
    return this == getPrimaryGraph();
  }

  public boolean isSecondaryGraph() {
    return this == getSuperGraph();
  }

  public boolean isSingleGraph() {
    return isPrimaryGraph() || isSecondaryGraph();
  }

  public boolean isSuperGraph() {
    return this == getSuperGraph();
  }

  public void setGraphs(final GraphsContainer graphs) {
    this.graphs = checkNotNull(graphs);
  }
}
