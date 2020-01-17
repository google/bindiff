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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import y.algo.Bfs;
import y.base.Node;
import y.base.NodeList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CGraphSelector {
  public static <NodeType extends ZyGraphNode<?>> void selectNode(
      final AbstractZyGraph<NodeType, ?> graph, final NodeType node, final boolean addToSelection) {
    final Collection<NodeType> toSelect = new ArrayList<NodeType>();
    final Collection<NodeType> toUnselect = new ArrayList<NodeType>();

    for (final NodeType n : graph.getNodes()) {
      if (n == node) {
        if (node.isSelected()) {
          toUnselect.add(n);
        } else {
          toSelect.add(n);
        }
      } else if (n.isSelected() && addToSelection) {
        toSelect.add(n);
      } else {
        toUnselect.add(n);
      }
    }

    graph.selectNodes(toSelect, toUnselect);
  }

  /**
   * Function which handles the selection of nodes in a path finding scenario. The function performs
   * four BFS runs: BFS 1 searches for all successors of the start nodes. BFS 2 searches for all
   * predecessors of the end nodes. BFS 2 searches for all predecessors of the start nodes. BFS 4
   * searches for all successors of the end nodes.
   * 
   * These four BFS runs are used in two sets: Set 1 is intersect of nodes reached through (BFS 1,
   * BFS 2). Set 2 is intersect of nodes reached through (BFS 3, BFS 4).
   * 
   * Therefore Set 1 represents all nodes on paths if the set of start nodes contains parents of the
   * newly selected node and Set 2 represents all nodes on paths if the set of start nodes contains
   * child nodes of the newly selected node.
   * 
   * 
   * @param graph The graph in which the selection takes place.
   * @param alreadySelectedNodes The List of nodes already selected.
   * @param newlySelectedNode The node which is newly selected.
   */
  @SuppressWarnings("unchecked")
  public static <NodeType extends ZyGraphNode<?>> void selectPath(
      final AbstractZyGraph<NodeType, ?> graph, final ArrayList<NodeType> alreadySelectedNodes,
      final NodeType newlySelectedNode) {
    final Function<NodeType, Node> function = new Function<NodeType, Node>() {
      @Override
      public Node apply(final NodeType input) {
        return input.getNode();
      }
    };

    final Collection<Node> foo = Collections2.transform(alreadySelectedNodes, function);

    final NodeList startNodes = new NodeList(foo.iterator());
    final NodeList endNodes = new NodeList(newlySelectedNode.getNode());

    final Set<Node> startSuccSet = new HashSet<Node>();
    final NodeList[] nodeListsStartSucc =
        Bfs.getLayers(graph.getGraph(), startNodes, Bfs.DIRECTION_SUCCESSOR, graph.getGraph()
            .createNodeMap(), 0);
    for (final NodeList nodeList : nodeListsStartSucc) {
      startSuccSet.addAll(nodeList);
    }
    final Set<Node> endPredSet = new HashSet<Node>();
    final NodeList[] nodeListsEndPred =
        Bfs.getLayers(graph.getGraph(), endNodes, Bfs.DIRECTION_PREDECESSOR, graph.getGraph()
            .createNodeMap(), 0);
    for (final NodeList nodeList : nodeListsEndPred) {
      endPredSet.addAll(nodeList);
    }

    final SetView<Node> startBeforeEndSetView = Sets.intersection(startSuccSet, endPredSet);
    if (!startBeforeEndSetView.isEmpty()) {
      for (final Node node : startBeforeEndSetView) {
        graph.getGraph().setSelected(node, true);
      }
    }

    final Set<Node> startPredSet = new HashSet<Node>();
    final NodeList[] nodeListsStartPred =
        Bfs.getLayers(graph.getGraph(), startNodes, Bfs.DIRECTION_PREDECESSOR, graph.getGraph()
            .createNodeMap(), 0);
    for (final NodeList nodeList : nodeListsStartPred) {
      startPredSet.addAll(nodeList);
    }

    final Set<Node> endSuccSet = new HashSet<Node>();
    final NodeList[] nodeListsEndSucc =
        Bfs.getLayers(graph.getGraph(), endNodes, Bfs.DIRECTION_SUCCESSOR, graph.getGraph()
            .createNodeMap(), 0);
    for (final NodeList nodeList : nodeListsEndSucc) {
      endSuccSet.addAll(nodeList);
    }

    final SetView<Node> endBeforeStartSetView = Sets.intersection(startPredSet, endSuccSet);
    if (!endBeforeStartSetView.isEmpty()) {
      for (final Node node : endBeforeStartSetView) {
        graph.getGraph().setSelected(node, true);
      }
    }
  }
}
