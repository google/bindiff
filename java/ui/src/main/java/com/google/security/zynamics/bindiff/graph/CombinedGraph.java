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

package com.google.security.zynamics.bindiff.graph;

import com.google.security.zynamics.bindiff.enums.EGraphType;
import com.google.security.zynamics.bindiff.exceptions.GraphLayoutException;
import com.google.security.zynamics.bindiff.graph.edges.CombinedDiffEdge;
import com.google.security.zynamics.bindiff.graph.edges.SuperDiffEdge;
import com.google.security.zynamics.bindiff.graph.edges.SuperViewEdge;
import com.google.security.zynamics.bindiff.graph.editmode.CombinedGraphEditMode;
import com.google.security.zynamics.bindiff.graph.layout.LayoutCommandHelper;
import com.google.security.zynamics.bindiff.graph.layout.commands.ProximityBrowserUnhideNode;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperViewNode;
import com.google.security.zynamics.bindiff.graph.realizers.CodeNodeRealizerUpdater;
import com.google.security.zynamics.bindiff.graph.realizers.CombinedEdgeRealizer;
import com.google.security.zynamics.bindiff.graph.realizers.CombinedNodeRealizer;
import com.google.security.zynamics.bindiff.graph.searchers.CombinedGraphSearcher;
import com.google.security.zynamics.bindiff.graph.searchers.GraphSearcher;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.selectionhistory.SelectionHistory;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedJump;
import com.google.security.zynamics.zylib.gui.zygraph.edges.ZyEdgeData;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.ZyNodeData;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.ZyGraph2DView;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.ZyEditMode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import y.base.Edge;
import y.base.Node;

public final class CombinedGraph extends BinDiffGraph<CombinedDiffNode, CombinedDiffEdge> {
  private SelectionHistory selectionHistory;
  private GraphSearcher graphSearcher;

  public CombinedGraph(
      final ZyGraph2DView view,
      final LinkedHashMap<Node, CombinedDiffNode> nodeMap,
      final LinkedHashMap<Edge, CombinedDiffEdge> edgeMap,
      final GraphSettings settings,
      final EGraphType viewGraph) {
    super(view, nodeMap, edgeMap, settings, viewGraph);

    selectionHistory = new SelectionHistory(this, GraphSettings.MAX_SELECTION_UNDO_CACHE);
    graphSearcher = new CombinedGraphSearcher();
  }

  public static CombinedDiffEdge buildDiffEdge(
      final CombinedGraph diffGraph,
      final SuperViewEdge<? extends SuperViewNode> rawSuperJump,
      final SuperDiffEdge superDiffEdge)
      throws GraphLayoutException {
    Edge yCombinedEdge;

    @SuppressWarnings("unchecked")
    final RawCombinedJump<RawCombinedBasicBlock> rawCombinedJump =
        (RawCombinedJump<RawCombinedBasicBlock>) rawSuperJump.getCombinedEdge();

    final ZyLabelContent combinedEdgeContent = new ZyLabelContent(null);
    final CombinedEdgeRealizer combinedEdgeRealizer =
        new CombinedEdgeRealizer(combinedEdgeContent, null, diffGraph.getSettings());

    final CombinedDiffNode srcCombinedDiffNode = diffGraph.getNode(rawCombinedJump.getSource());
    final CombinedDiffNode tarCombinedDiffNode = diffGraph.getNode(rawCombinedJump.getTarget());

    final Node srcCombinedYNode = srcCombinedDiffNode.getNode();
    final Node tarCombinedYNode = tarCombinedDiffNode.getNode();

    final boolean srcVisible = srcCombinedDiffNode.getRawNode().isVisible();
    final boolean tarVisible = tarCombinedDiffNode.getRawNode().isVisible();
    srcCombinedDiffNode.getRawNode().setVisible(true);
    tarCombinedDiffNode.getRawNode().setVisible(true);

    yCombinedEdge = diffGraph.getGraph().createEdge(srcCombinedYNode, tarCombinedYNode);

    final CombinedDiffEdge combinedDiffEdge =
        new CombinedDiffEdge(
            srcCombinedDiffNode,
            tarCombinedDiffNode,
            yCombinedEdge,
            combinedEdgeRealizer,
            rawCombinedJump,
            superDiffEdge);
    CombinedDiffNode.link(srcCombinedDiffNode, tarCombinedDiffNode);
    combinedEdgeRealizer.setUserData(new ZyEdgeData<>(combinedDiffEdge));

    srcCombinedDiffNode.getRawNode().setVisible(srcVisible);
    tarCombinedDiffNode.getRawNode().setVisible(tarVisible);

    diffGraph.addEdgeToMappings(combinedDiffEdge);

    // TODO: Keep proximity nodes instead of resolving them here.
    if (!srcVisible || !tarVisible) {
      @SuppressWarnings("unchecked")
      final BinDiffGraph<ZyGraphNode<?>, ?> castedGraph =
          (BinDiffGraph<ZyGraphNode<?>, ?>) (BinDiffGraph<?, ?>) diffGraph;

      final boolean autoLayout = LayoutCommandHelper.deactivateAutoLayout(castedGraph);
      try {
        ProximityBrowserUnhideNode.executeStatic(
            castedGraph, srcVisible ? srcCombinedDiffNode : tarCombinedDiffNode);
      } finally {
        LayoutCommandHelper.activateAutoLayout(castedGraph, autoLayout);
      }
    }

    return combinedDiffEdge;
  }

  public static CombinedDiffNode buildDiffNode(
      final CombinedGraph combinedGraph,
      final SingleDiffNode primaryDiffNode,
      final SingleDiffNode secondaryDiffNode,
      final SuperDiffNode superDiffNode,
      final RawCombinedBasicBlock combinedBasicblock) {
    ZyLabelContent primaryNodeContent=getZyLabelContent(primaryDiffNode);

    ZyLabelContent secondaryNodeContent=getZyLabelContent(secondaryDiffNode);

    final CombinedNodeRealizer combinedNodeRealizer =
        new CombinedNodeRealizer(primaryNodeContent, secondaryNodeContent);

    final CodeNodeRealizerUpdater updater = new CodeNodeRealizerUpdater();
    combinedNodeRealizer.setUpdater(updater);
    updater.setRealizer(combinedNodeRealizer);

    final Node yCombinedNode = combinedGraph.getGraph().createNode();
    final CombinedDiffNode combinedDiffNode =
        new CombinedDiffNode(
            yCombinedNode, combinedNodeRealizer, combinedBasicblock, superDiffNode);
    combinedNodeRealizer.setUserData(new ZyNodeData<>(combinedDiffNode));

    return combinedDiffNode;
  }

  private static ZyLabelContent getZyLabelContent(SingleDiffNode primaryDiffNode) {
    ZyLabelContent primaryNodeContent=null;
    if (primaryDiffNode != null) {
      primaryNodeContent=primaryDiffNode.getRealizer().getNodeContent();
    }
    return primaryNodeContent;
  }

  @Override
  protected ZyEditMode<CombinedDiffNode, CombinedDiffEdge> createEditMode() {
    return new CombinedGraphEditMode(this);
  }

  @Override
  public void dispose() {
    selectionHistory.dispose();

    super.dispose();

    graphSearcher.clearResults();

    selectionHistory = null;
    graphSearcher = null;
  }

  @Override
  public GraphSearcher getGraphSearcher() {
    return graphSearcher;
  }

  @Override
  public Set<CombinedDiffNode> getSelectedNodes() {
    return new HashSet<>(CollectionHelpers.filter(getNodes(), item -> item.isSelected()));
  }

  public SelectionHistory getSelectionHistory() {
    return selectionHistory;
  }
}
