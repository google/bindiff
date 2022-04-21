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

package com.google.security.zynamics.bindiff.graph.layout.commands;

import com.google.security.zynamics.bindiff.enums.EGraphType;
import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.exceptions.GraphLayoutException;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.listeners.GraphsIntermediateListeners;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleViewNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperDiffNode;
import com.google.security.zynamics.bindiff.graph.settings.GraphLayoutSettings;
import com.google.security.zynamics.bindiff.graph.settings.GraphProximityBrowsingSettings;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import java.util.ArrayList;
import java.util.Collection;

public class ProximityBrowserInitializer {
  private static final int EDGE_COUNT_THRESHOLD = 3;

  private ProximityBrowserInitializer() {
    // Static methods only
  }

  private static SingleDiffNode getProximityBrowsingStartNode(final SingleGraph graph) {
    SingleDiffNode diffNode = null;
    IAddress minAddr = null;

    if (graph.getGraphType() == EGraphType.CALL_GRAPH) {
      for (final SingleDiffNode node : graph.getNodes()) {
        if (node.getRawNode().getMatchState() == EMatchState.MATCHED) {
          final SingleViewNode rawNode = node.getRawNode();

          if (rawNode.getIncomingEdges().size() + rawNode.getOutgoingEdges().size()
              == EDGE_COUNT_THRESHOLD) {
            final IAddress addr = rawNode.getAddress();

            if (minAddr == null || minAddr.compareTo(addr) > 0) {
              minAddr = addr;
              diffNode = node;
            }
          }
        }
      }
    } else {
      for (final SingleDiffNode node : graph.getNodes()) {
        final RawBasicBlock rawNode = (RawBasicBlock) node.getRawNode();

        if (rawNode.getAddress().equals(rawNode.getFunctionAddr())) {
          diffNode = node;
          break;
        }
      }
    }

    return diffNode;
  }

  private static void getVisibleAndInvisibleNodeSets(
      final BinDiffGraph<?, ?> graph,
      SingleDiffNode primaryStartNode,
      SingleDiffNode secondaryStartNode,
      final Collection<SuperDiffNode> visibleSuperNodes,
      final Collection<SuperDiffNode> invisibleSuperNodes,
      final Collection<CombinedDiffNode> visibleCombinedNodes,
      final Collection<CombinedDiffNode> invisibleCombinedNodes) {
    invisibleCombinedNodes.addAll(graph.getCombinedGraph().getNodes());
    invisibleSuperNodes.addAll(graph.getSuperGraph().getNodes());

    if (primaryStartNode == null && secondaryStartNode == null) {
      return;
    }

    if (primaryStartNode == null) {
      primaryStartNode = secondaryStartNode.getOtherSideDiffNode();
    } else if (secondaryStartNode == null) {
      secondaryStartNode = primaryStartNode.getOtherSideDiffNode();
    } else {
      secondaryStartNode = primaryStartNode.getOtherSideDiffNode();
    }

    final SuperDiffNode superNode = primaryStartNode.getSuperDiffNode();
    invisibleSuperNodes.remove(superNode);
    visibleSuperNodes.add(superNode);

    final CombinedDiffNode combinedNode = primaryStartNode.getCombinedDiffNode();
    invisibleCombinedNodes.remove(combinedNode);
    visibleCombinedNodes.add(combinedNode);
  }

  private static void initialProximityBrowsing(final BinDiffGraph<?, ?> graph)
      throws GraphLayoutException {
    final GraphProximityBrowsingSettings proximitySettings =
        graph.getSettings().getProximitySettings();
    final int proximityActThreshold =
        proximitySettings.getAutoProximityBrowsingActivationThreshold();

    final Collection<CombinedDiffNode> nodes = graph.getCombinedGraph().getNodes();
    final int nodeCount = nodes.size();

    if (nodeCount < proximityActThreshold) {
      return;
    }
    proximitySettings.setProximityBrowsing(true);

    final SingleDiffNode primaryStartNode = getProximityBrowsingStartNode(graph.getPrimaryGraph());
    final SingleDiffNode secondaryStartNode =
        getProximityBrowsingStartNode(graph.getSecondaryGraph());

    final Collection<CombinedDiffNode> visibleCombinedNodes = new ArrayList<>();
    final Collection<CombinedDiffNode> invisibleCombinedNodes = new ArrayList<>();
    final Collection<SuperDiffNode> visibleSuperNodes = new ArrayList<>();
    final Collection<SuperDiffNode> invisibleSuperNodes = new ArrayList<>();

    getVisibleAndInvisibleNodeSets(
        graph,
        primaryStartNode,
        secondaryStartNode,
        visibleSuperNodes,
        invisibleSuperNodes,
        visibleCombinedNodes,
        invisibleCombinedNodes);

    final GraphLayoutSettings layoutSettings = graph.getSettings().getLayoutSettings();
    final boolean autoLayout = layoutSettings.getAutomaticLayouting();
    layoutSettings.setAutomaticLayouting(false);

    // change showNodes(...) call sequence
    graph.getSuperGraph().showNodes(visibleSuperNodes, invisibleSuperNodes);
    graph.getCombinedGraph().showNodes(visibleCombinedNodes, invisibleCombinedNodes);

    ProximityBrowserUpdater.adoptSuperGraphVisibility(graph.getSuperGraph());

    ProximityBrowserUpdater.deleteAllProximityNodes(graph.getPrimaryGraph());
    ProximityBrowserUpdater.deleteAllProximityNodes(graph.getSecondaryGraph());

    ProximityBrowserUpdater.createProximityNodesAndEdges(graph.getSuperGraph());

    layoutSettings.setAutomaticLayouting(autoLayout);
  }

  public static void executeStatic(final BinDiffGraph<?, ?> graph) throws GraphLayoutException {
    initialProximityBrowsing(graph);

    try {
      GraphsIntermediateListeners.notifyIntermediateVisibilityListeners(graph);
    } catch (final Exception e) {
      throw new GraphLayoutException(e, e.getMessage());
    }
  }
}
