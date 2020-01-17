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

package com.google.security.zynamics.bindiff.graph.helpers;

import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.edges.SingleViewEdge;
import com.google.security.zynamics.bindiff.graph.edges.SuperViewEdge;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperDiffNode;
import com.google.security.zynamics.bindiff.graph.synchronizer.GraphViewCanvasSynchronizer;
import com.google.security.zynamics.zylib.gui.zygraph.edges.CBend;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;

import y.base.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GraphElementMover {
  private static List<SuperDiffNode> getAffectedSuperDiffNodes(
      final AbstractZyGraph<?, ?> graph, final SingleDiffNode thisDiffNode) {
    final Set<SingleDiffNode> singleDiffNodes = new HashSet<>();

    if (thisDiffNode.isSelected()) {
      for (final Object selectedNode : graph.getSelectedNodes()) {
        singleDiffNodes.add((SingleDiffNode) selectedNode);
      }

      final BinDiffGraph<?, ?> otherGraph =
          thisDiffNode.getSide() == ESide.PRIMARY
              ? ((BinDiffGraph<?, ?>) graph).getSecondaryGraph()
              : ((BinDiffGraph<?, ?>) graph).getPrimaryGraph();

      for (final Object selectedNode : otherGraph.getSelectedNodes()) {
        if (((SingleDiffNode) selectedNode).getCombinedRawNode().getMatchState()
            != EMatchState.MATCHED) {
          singleDiffNodes.add((SingleDiffNode) selectedNode);
        }
      }
    } else {
      singleDiffNodes.add(thisDiffNode);
    }

    final List<SuperDiffNode> affectedSuperNodes = new ArrayList<>();

    for (final SingleDiffNode selectedDiffNode : singleDiffNodes) {
      final SuperDiffNode selectedSuperNode = selectedDiffNode.getSuperDiffNode();
      affectedSuperNodes.add(selectedSuperNode);
    }

    return affectedSuperNodes;
  }

  private static void moveBends(
      final List<SuperDiffNode> superNodes,
      final ESide eventSourceSide,
      final double distX,
      final double distY) {
    final Set<CBend> bendsToMove = new HashSet<>();

    for (final SuperDiffNode superNode : superNodes) {
      for (final SuperViewEdge<?> superEdge : superNode.getRawNode().getOutgoingEdges()) {
        bendsToMove.addAll(superEdge.getBends());
      }

      for (final SuperViewEdge<?> superEdge : superNode.getRawNode().getIncomingEdges()) {
        bendsToMove.addAll(superEdge.getBends());
      }
    }

    for (final SuperDiffNode superNode : superNodes) {
      SingleDiffNode singleDiffNode = superNode.getPrimaryDiffNode();

      if (eventSourceSide == ESide.PRIMARY) {
        singleDiffNode = superNode.getSecondaryDiffNode();
      }

      if (singleDiffNode != null) {
        for (final SingleViewEdge<?> singleEdge : singleDiffNode.getRawNode().getOutgoingEdges()) {
          bendsToMove.addAll(singleEdge.getBends());
        }
        for (final SingleViewEdge<?> singleEdge : singleDiffNode.getRawNode().getIncomingEdges()) {
          bendsToMove.addAll(singleEdge.getBends());
        }
      }
    }

    for (final CBend bend : bendsToMove) {
      bend.setX(bend.getX() + distX);
      bend.setY(bend.getY() + distY);
    }
  }

  public static void moveNodes(
      final AbstractZyGraph<?, ?> graph,
      final Node draggedNode,
      final double distX,
      final double distY) {
    final Object node = graph.getNode(draggedNode);

    if (node instanceof SingleDiffNode && ((BinDiffGraph<?, ?>) graph).getSettings().isSync()) {
      final SingleDiffNode diffNode = (SingleDiffNode) node;

      final List<SuperDiffNode> affectedSuperNodes = getAffectedSuperDiffNodes(graph, diffNode);

      for (final SuperDiffNode superNode : affectedSuperNodes) {
        // FIXME: ZyLib: Do not forget to move the nodes to front.
        // See zylib, does not work properly (only works if a single node is dragged)

        final IZyNodeRealizer superRealizer = superNode.getRealizer();

        superRealizer.setX(superRealizer.getX() + distX);
        superRealizer.setY(superRealizer.getY() + distY);

        GraphViewCanvasSynchronizer.adoptSuperWorldRect(
            ((BinDiffGraph<?, ?>) graph).getSuperGraph());

        if (diffNode.getSide() != ESide.PRIMARY) {
          final SingleDiffNode primaryDiffNode = superNode.getPrimaryDiffNode();
          if (primaryDiffNode != null) {
            primaryDiffNode.getRealizer().setX(superNode.getX());
            primaryDiffNode.getRealizer().setY(superNode.getY());
          }
        } else if (diffNode.getSide() != ESide.SECONDARY) {
          final SingleDiffNode secondaryDiffNode = superNode.getSecondaryDiffNode();
          if (secondaryDiffNode != null) {
            secondaryDiffNode.getRealizer().setX(superNode.getX());
            secondaryDiffNode.getRealizer().setY(superNode.getY());
          }
        }
      }

      moveBends(affectedSuperNodes, diffNode.getSide(), distX, distY);
    }

    ((BinDiffGraph<?, ?>) graph).getPrimaryGraph().getGraph().updateViews();
    ((BinDiffGraph<?, ?>) graph).getSecondaryGraph().getGraph().updateViews();
  }
}
