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

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.SuperGraph;
import com.google.security.zynamics.bindiff.graph.builders.ViewCallGraphBuilder;
import com.google.security.zynamics.bindiff.graph.edges.CombinedDiffEdge;
import com.google.security.zynamics.bindiff.graph.edges.SingleDiffEdge;
import com.google.security.zynamics.bindiff.graph.edges.SuperDiffEdge;
import com.google.security.zynamics.bindiff.graph.edges.SuperViewEdge;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperViewNode;
import com.google.security.zynamics.bindiff.graph.realizers.CombinedEdgeRealizer;
import com.google.security.zynamics.bindiff.graph.realizers.SingleEdgeRealizer;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCall;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCombinedCall;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.zylib.gui.zygraph.edges.ZyEdgeData;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;
import y.base.Edge;
import y.base.Node;

public class MatchedFunctionRemover {
  // TODO: Reuse/reimplement this after release 4.0.0
  private static CombinedDiffEdge buildDiffEdge(
      final CombinedGraph diffGraph,
      final RawCombinedCall rawCombinedCall,
      final SuperDiffEdge superDiffEdge) {
    Edge yCombinedEdge;

    final ZyLabelContent combinedEdgeContent = new ZyLabelContent(null);
    final CombinedEdgeRealizer combinedEdgeRealizer =
        new CombinedEdgeRealizer(combinedEdgeContent, null, diffGraph.getSettings());

    final CombinedDiffNode srcCombinedDiffNode = diffGraph.getNode(rawCombinedCall.getSource());
    final CombinedDiffNode tarCombinedDiffNode = diffGraph.getNode(rawCombinedCall.getTarget());

    final Node srcCombinedYNode = srcCombinedDiffNode.getNode();
    final Node tarCombinedYNode = tarCombinedDiffNode.getNode();

    yCombinedEdge = diffGraph.getGraph().createEdge(srcCombinedYNode, tarCombinedYNode);

    final CombinedDiffEdge combinedDiffEdge =
        new CombinedDiffEdge(
            srcCombinedDiffNode,
            tarCombinedDiffNode,
            yCombinedEdge,
            combinedEdgeRealizer,
            rawCombinedCall,
            superDiffEdge);
    CombinedDiffNode.link(srcCombinedDiffNode, tarCombinedDiffNode);
    combinedEdgeRealizer.setUserData(new ZyEdgeData<>(combinedDiffEdge));

    diffGraph.addEdgeToMappings(combinedDiffEdge);

    return combinedDiffEdge;
  }

  private static SingleDiffEdge buildDiffEdge(
      final SingleGraph diffGraph, final RawCombinedCall rawCombinedCall) {
    SingleDiffEdge diffEdge = null;
    Edge yEdge;

    final RawFunction rawSourceNode = rawCombinedCall.getSource().getRawNode(diffGraph.getSide());
    final RawFunction rawTargetNode = rawCombinedCall.getTarget().getRawNode(diffGraph.getSide());

    final RawCall rawCall =
        diffGraph.getSide() == ESide.PRIMARY
            ? rawCombinedCall.getPrimaryEdge()
            : rawCombinedCall.getSecondaryEdge();

    SingleEdgeRealizer edgeRealizer;

    if (rawSourceNode != null && rawTargetNode != null && rawCall != null) {
      final ZyLabelContent edgeContent = new ZyLabelContent(null);
      edgeRealizer = new SingleEdgeRealizer(edgeContent, null, diffGraph.getSettings());

      final SingleDiffNode sourceNode = diffGraph.getNode(rawSourceNode);
      final SingleDiffNode targetNode = diffGraph.getNode(rawTargetNode);

      yEdge = diffGraph.getGraph().createEdge(sourceNode.getNode(), targetNode.getNode());

      diffEdge =
          new SingleDiffEdge(
              sourceNode, targetNode, yEdge, edgeRealizer, rawCall, diffGraph.getSide());
      SingleDiffNode.link(sourceNode, targetNode);

      edgeRealizer.setUserData(new ZyEdgeData<>(diffEdge));

      diffGraph.addEdgeToMappings(diffEdge);
    }

    return diffEdge;
  }

  private static SuperDiffEdge buildDiffEdge(
      final SuperGraph diffGraph,
      final RawCombinedCall rawCombinedCall,
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

    ySuperEdge = diffGraph.getGraph().createEdge(srcSuperYNode, tarSuperYNode);

    final SuperViewNode superRawSource = srcSuperDiffNode.getRawNode();
    final SuperViewNode superRawTarget = tarSuperDiffNode.getRawNode();

    final SuperViewEdge<SuperViewNode> superEdge =
        new SuperViewEdge<>(rawCombinedCall, superRawSource, superRawTarget);

    final SuperDiffEdge superDiffEdge =
        new SuperDiffEdge(
            srcSuperDiffNode,
            tarSuperDiffNode,
            ySuperEdge,
            superEdgeRealizer,
            superEdge,
            primaryDiffEdge,
            secondaryDiffEdge);
    SuperDiffNode.link(srcSuperDiffNode, tarSuperDiffNode);
    superEdgeRealizer.setUserData(new ZyEdgeData<>(superDiffEdge));

    diffGraph.addEdgeToMappings(superDiffEdge);

    return superDiffEdge;
  }

  public static void buildCalls(
      final BinDiffGraph<?, ?> diffGraph, final RawCombinedCall rawCombinedCall) {
    final SingleGraph primaryDiffGraph = diffGraph.getPrimaryGraph();
    final SingleGraph secondaryDiffGraph = diffGraph.getSecondaryGraph();
    final SuperGraph superDiffGraph = diffGraph.getSuperGraph();
    final CombinedGraph combinedDiffGraph = diffGraph.getCombinedGraph();

    // build jumps
    final SingleDiffEdge primaryDiffEdge = buildDiffEdge(primaryDiffGraph, rawCombinedCall);
    final SingleDiffEdge secondaryDiffEdge = buildDiffEdge(secondaryDiffGraph, rawCombinedCall);
    final SuperDiffEdge superDiffEdge =
        buildDiffEdge(superDiffGraph, rawCombinedCall, primaryDiffEdge, secondaryDiffEdge);
    final CombinedDiffEdge combinedDiffEdge =
        buildDiffEdge(combinedDiffGraph, rawCombinedCall, superDiffEdge);

    // each edge gets its representatives from the parallel views
    superDiffEdge.setCombinedDiffEdge(combinedDiffEdge);
    if (primaryDiffEdge != null) {
      primaryDiffEdge.setCombinedDiffEdge(combinedDiffEdge);
    }
    if (secondaryDiffEdge != null) {
      secondaryDiffEdge.setCombinedDiffEdge(combinedDiffEdge);
    }

    // colorize calls
    ViewCallGraphBuilder.colorizeCalls(rawCombinedCall);
  }
}
