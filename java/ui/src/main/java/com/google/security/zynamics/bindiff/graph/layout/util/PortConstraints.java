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

package com.google.security.zynamics.bindiff.graph.layout.util;

import com.google.security.zynamics.bindiff.enums.ELayoutOrientation;
import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.edges.CombinedDiffEdge;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.settings.GraphLayoutSettings;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Node;
import y.base.YList;
import y.layout.LayoutGraph;
import y.layout.PortCandidate;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.util.Maps;
import y.view.Graph2D;

import java.util.Collection;

public class PortConstraints {
  // creates the top and bottom left port
  @SuppressWarnings("unchecked")
  private static Collection<PortCandidate> createLeftPorts(final Node n, final LayoutGraph graph) {
    final double halfWidth = graph.getWidth(n) * 0.5;
    final double halfHeight = graph.getHeight(n) * 0.5;
    final YList leftPortCandidates = new YList();
    leftPortCandidates.add(
        PortCandidate.createCandidate(-halfWidth, -halfHeight, PortCandidate.NORTH));
    leftPortCandidates.add(
        PortCandidate.createCandidate(-halfWidth, halfHeight, PortCandidate.SOUTH));

    return leftPortCandidates;
  }

  // creates the top and bottom middle port
  @SuppressWarnings("unchecked")
  private static Collection<PortCandidate> createMiddlePorts(
      final Node n, final LayoutGraph graph) {
    final double halfHeight = graph.getHeight(n) * 0.5;
    final YList middlePortCandidates = new YList();
    middlePortCandidates.add(PortCandidate.createCandidate(0, -halfHeight, PortCandidate.NORTH));
    middlePortCandidates.add(PortCandidate.createCandidate(0, halfHeight, PortCandidate.SOUTH));

    return middlePortCandidates;
  }

  // creates the top and bottom right port
  @SuppressWarnings("unchecked")
  private static Collection<PortCandidate> createRightPorts(final Node n, final LayoutGraph graph) {
    final double halfWidth = graph.getWidth(n) * 0.5;
    final double halfHeight = graph.getHeight(n) * 0.5;
    final YList rightPortCandidates = new YList();
    rightPortCandidates.add(
        PortCandidate.createCandidate(halfWidth, -halfHeight, PortCandidate.NORTH));
    rightPortCandidates.add(
        PortCandidate.createCandidate(halfWidth, halfHeight, PortCandidate.SOUTH));

    return rightPortCandidates;
  }

  // configures port candidates such that each edge is attached to the desired location
  public static void configureConstraints(final CombinedGraph combinedGraph) {
    final GraphLayoutSettings settings = combinedGraph.getSettings().getLayoutSettings();
    if (settings.getCurrentLayouter() instanceof IncrementalHierarchicLayouter
        && settings.getHierarchicOrientation() == ELayoutOrientation.HORIZONTAL) {
      final Graph2D graph = combinedGraph.getGraph();

      final EdgeMap edge2SPC = Maps.createHashedEdgeMap();
      graph.addDataProvider(PortCandidate.SOURCE_PCLIST_DPKEY, edge2SPC);
      final EdgeMap edge2TPC = Maps.createHashedEdgeMap();
      graph.addDataProvider(PortCandidate.TARGET_PCLIST_DPKEY, edge2TPC);

      // for each edge we chose the ports of the desired location
      for (final EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
        final Edge e = ec.edge();

        final CombinedDiffNode sourceCombinedNode = combinedGraph.getNode(e.source());
        final CombinedDiffNode targetCombinedNode = combinedGraph.getNode(e.target());

        // is proximity node
        if (sourceCombinedNode == null || targetCombinedNode == null) {
          edge2SPC.set(e, createMiddlePorts(e.source(), graph));
          edge2TPC.set(e, createMiddlePorts(e.target(), graph));

          continue;
        }

        final CombinedDiffEdge combinedEdge = combinedGraph.getEdge(e);
        final EMatchState edgeMatchState = combinedEdge.getRawEdge().getMatchState();

        if (sourceCombinedNode.getPrimaryDiffNode() == null
            || sourceCombinedNode.getSecondaryDiffNode() == null) {
          // unmatched source
          edge2SPC.set(e, createMiddlePorts(e.source(), graph));
        } else {
          // is matched source
          if (edgeMatchState == EMatchState.MATCHED) {
            edge2SPC.set(e, createMiddlePorts(e.source(), graph));
          } else if (edgeMatchState == EMatchState.PRIMARY_UNMATCHED) {
            edge2SPC.set(e, createLeftPorts(e.source(), graph));
          } else {
            edge2SPC.set(e, createRightPorts(e.source(), graph));
          }
        }

        if (targetCombinedNode.getPrimaryDiffNode() == null
            || targetCombinedNode.getSecondaryDiffNode() == null) {
          // primary unmatched target
          edge2TPC.set(e, createMiddlePorts(e.target(), graph));
        } else {
          // is matched target
          if (edgeMatchState == EMatchState.MATCHED) {
            edge2TPC.set(e, createMiddlePorts(e.target(), graph));
          } else if (edgeMatchState == EMatchState.PRIMARY_UNMATCHED) {
            edge2TPC.set(e, createLeftPorts(e.target(), graph));
          } else {
            edge2TPC.set(e, createRightPorts(e.target(), graph));
          }
        }
      }
    }
  }
}
