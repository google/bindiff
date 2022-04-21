// Copyright 2011-2022 Google LLC
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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.security.zynamics.bindiff.enums.EGraphType;
import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.graph.edges.SingleDiffEdge;
import com.google.security.zynamics.bindiff.graph.edges.SuperDiffEdge;
import com.google.security.zynamics.bindiff.graph.edges.SuperViewEdge;
import com.google.security.zynamics.bindiff.graph.editmode.EditMode;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperViewNode;
import com.google.security.zynamics.bindiff.graph.searchers.GraphSearcher;
import com.google.security.zynamics.bindiff.graph.searchers.SuperGraphSearcher;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.zylib.gui.zygraph.edges.ZyEdgeData;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.ZyNodeData;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.ZyGraph2DView;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.ZyEditMode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyNormalNodeRealizer;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import y.base.Edge;
import y.base.Node;
import y.view.NodeRealizer;

public class SuperGraph extends BinDiffGraph<SuperDiffNode, SuperDiffEdge> {
  private static final int NODELABEL_PADDING = 10;

  private GraphSearcher graphSearcher = new SuperGraphSearcher();

  public SuperGraph(
      final ZyGraph2DView view,
      final LinkedHashMap<Node, SuperDiffNode> nodeMap,
      final LinkedHashMap<Edge, SuperDiffEdge> edgeMap,
      final SingleGraph primaryGraph,
      final SingleGraph secondaryGraph,
      final GraphSettings settings,
      final EGraphType graphType) {
    super(view, nodeMap, edgeMap, settings, graphType);
    checkArgument(
        primaryGraph != null || secondaryGraph != null,
        "Primary graph and secondary graph cannot both be null");
  }

  public static SuperDiffEdge buildDiffEdge(
      final SuperGraph diffGraph,
      final SuperViewEdge<? extends SuperViewNode> rawSuperJump,
      final SingleDiffEdge primaryDiffEdge,
      final SingleDiffEdge secondaryDiffEdge) {
    Edge ySuperEdge;

    final ZyLabelContent superEdgeContent = new ZyLabelContent(null);
    final ZyEdgeRealizer<SuperDiffEdge> superEdgeRealizer =
        new ZyEdgeRealizer<>(superEdgeContent, null);

    SuperDiffNode srcSuperDiffNode = null;
    SuperDiffNode tarSuperDiffNode = null;

    if (primaryDiffEdge != null) {
      srcSuperDiffNode = primaryDiffEdge.getSource().getSuperDiffNode();
      tarSuperDiffNode = primaryDiffEdge.getTarget().getSuperDiffNode();
    } else if (secondaryDiffEdge != null) {
      srcSuperDiffNode = secondaryDiffEdge.getSource().getSuperDiffNode();
      tarSuperDiffNode = secondaryDiffEdge.getTarget().getSuperDiffNode();
    }

    final Node srcSuperYNode = srcSuperDiffNode.getNode();
    final Node tarSuperYNode = tarSuperDiffNode.getNode();

    final boolean srcVisible = srcSuperDiffNode.getRawNode().isVisible();
    final boolean tarVisible = tarSuperDiffNode.getRawNode().isVisible();
    srcSuperDiffNode.getRawNode().setVisible(true);
    tarSuperDiffNode.getRawNode().setVisible(true);

    ySuperEdge = diffGraph.getGraph().createEdge(srcSuperYNode, tarSuperYNode);
    final SuperDiffEdge superDiffEdge =
        new SuperDiffEdge(
            srcSuperDiffNode,
            tarSuperDiffNode,
            ySuperEdge,
            superEdgeRealizer,
            rawSuperJump,
            primaryDiffEdge,
            secondaryDiffEdge);
    SuperDiffNode.link(srcSuperDiffNode, tarSuperDiffNode);
    superEdgeRealizer.setUserData(new ZyEdgeData<>(superDiffEdge));

    srcSuperDiffNode.getRawNode().setVisible(srcVisible);
    tarSuperDiffNode.getRawNode().setVisible(tarVisible);

    diffGraph.addEdgeToMappings(superDiffEdge);

    return superDiffEdge;
  }

  public static SuperDiffNode buildDiffNode(
      final SuperGraph superGraph,
      final SingleDiffNode primaryDiffNode,
      final SingleDiffNode secondaryDiffNode,
      final SuperViewNode superBasicBlock) {
    final ZyLabelContent superNodeContent = new ZyLabelContent(null);
    final ZyNormalNodeRealizer<SuperDiffNode> superNodeRealizer =
        new ZyNormalNodeRealizer<>(superNodeContent);

    final Node ySuperNode = superGraph.getGraph().createNode();

    final SuperDiffNode superDiffNode =
        new SuperDiffNode(
            ySuperNode, superNodeRealizer, superBasicBlock, primaryDiffNode, secondaryDiffNode);
    superNodeRealizer.setUserData(new ZyNodeData<>(superDiffNode));

    return superDiffNode;
  }

  @Override
  protected ZyEditMode<SuperDiffNode, SuperDiffEdge> createEditMode() {
    return new EditMode<>(this);
  }

  private void synchronizeSize(
      final SingleGraph primaryGraph,
      final SingleGraph secondaryGraph,
      final SuperDiffNode superDiffNode) {
    if (superDiffNode.getRawNode().getCombinedNode().getMatchState() != EMatchState.MATCHED) {
      return;
    }

    final SingleDiffNode priDiffNode = superDiffNode.getPrimaryDiffNode();
    final ZyLabelContent priLabelContent = priDiffNode.getRealizer().getNodeContent();

    final SingleDiffNode secDiffNode = superDiffNode.getSecondaryDiffNode();
    final ZyLabelContent secLabelContent = secDiffNode.getRealizer().getNodeContent();

    priLabelContent.setRightPadding(NODELABEL_PADDING);
    secLabelContent.setRightPadding(NODELABEL_PADDING);

    final double priWidth = priLabelContent.getBounds().getWidth();
    final double secWidth = secLabelContent.getBounds().getWidth();

    if (priWidth > secWidth) {
      secLabelContent.setRightPadding((int) Math.round(priWidth - secWidth) + NODELABEL_PADDING);
    } else {
      priLabelContent.setRightPadding((int) Math.round(secWidth - priWidth) + NODELABEL_PADDING);
    }

    secDiffNode.getRealizer().regenerate();
    priDiffNode.getRealizer().regenerate();

    primaryGraph.updateViews();
    secondaryGraph.updateViews();
  }

  @Override
  public void dispose() {
    super.dispose();

    graphSearcher.clearResults();
    graphSearcher = null;
  }

  @Override
  public void doLayout() {
    // Do nothing, layout is not done here but return null!
    // This function is called from zylib ZyProximityBrowser
    // each time proximity browsing is activated.
  }

  @Override
  public GraphSearcher getGraphSearcher() {
    return graphSearcher;
  }

  @Override
  public Set<SuperDiffNode> getSelectedNodes() {
    return new HashSet<>(
        CollectionHelpers.filter(super.getMappings().getNodes(), ZyGraphNode::isSelected));
  }

  public void refreshAllSuperNodeSizes(
      final SingleGraph primaryGraph, final SingleGraph secondaryGraph) {
    for (final SuperDiffNode diffNode : getMappings().getNodes()) {
      refreshSuperNodeSize(primaryGraph, secondaryGraph, diffNode);
    }
  }

  public void refreshSuperNodeSize(
      final SingleGraph primaryGraph,
      final SingleGraph secondaryGraph,
      final SuperDiffNode superDiffNode) {
    // syncs primary and secondary node sizes according to the max label size
    synchronizeSize(primaryGraph, secondaryGraph, superDiffNode);

    // set super node width and height
    final SingleDiffNode primaryDiffNode = superDiffNode.getPrimaryDiffNode();
    final SingleDiffNode secondaryDiffNode = superDiffNode.getSecondaryDiffNode();
    final CombinedDiffNode combinedDiffNode = superDiffNode.getCombinedDiffNode();

    final Node superYnode = superDiffNode.getNode();
    Node primaryYnode = null;
    Node secondaryYnode = null;

    final NodeRealizer superNodeRealizer = getGraph().getRealizer(superYnode);
    final NodeRealizer combinedNodeRealizer = combinedDiffNode.getRealizer().getRealizer();

    NodeRealizer primaryNodeRealizer = null;
    NodeRealizer secondaryNodeRealizer = null;

    double priWidth = 0.;
    double priHeight = 0.;

    double secWidth = 0.;
    double secHeight = 0.;

    if (primaryDiffNode != null) {
      primaryYnode = primaryDiffNode.getNode();
      primaryNodeRealizer = primaryGraph.getGraph().getRealizer(primaryYnode);

      priWidth = primaryNodeRealizer.getWidth();
      priHeight = primaryNodeRealizer.getHeight();
    }
    if (secondaryDiffNode != null) {
      secondaryYnode = secondaryDiffNode.getNode();
      secondaryNodeRealizer = secondaryGraph.getGraph().getRealizer(secondaryYnode);

      secWidth = secondaryNodeRealizer.getWidth();
      secHeight = secondaryNodeRealizer.getHeight();
    }

    superNodeRealizer.setWidth(Math.max(priWidth, secWidth));
    superNodeRealizer.setHeight(Math.max(priHeight, secHeight));

    if (primaryNodeRealizer != null && secondaryNodeRealizer != null) {
      combinedNodeRealizer.setWidth(superNodeRealizer.getWidth() * 2 + 20);
      combinedNodeRealizer.setHeight(superNodeRealizer.getHeight());
    }

    if (primaryDiffNode != null) {
      primaryNodeRealizer.setWidth(superNodeRealizer.getWidth());
      primaryNodeRealizer.setHeight(superNodeRealizer.getHeight());
    }

    if (secondaryDiffNode != null) {
      secondaryNodeRealizer.setWidth(superNodeRealizer.getWidth());
      secondaryNodeRealizer.setHeight(superNodeRealizer.getHeight());
    }
  }
}
