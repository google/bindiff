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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import y.base.Node;
import y.view.Graph2D;

public class ZyGraphVerifier {
  /**
   * Verifies the map that maps between ynode objects and node objects. If the map has an incorrect
   * format, the function throws an {@link IllegalArgumentException}.
   *
   * @param graph
   */
  public static <NodeType> void verifyMap(
      final Graph2D graph, final HashMap<Node, NodeType> nodeMap) {
    // Let's verify the node map.
    //
    // - The number of mappings must equal or less than the number of graphs in the node (each node
    // must be mapped, but some nodes can be hidden in folder nodes)
    // - No element of the key set/value set of the mapping must appear more than once
    // - No key or value of the mapping must be null

    Preconditions.checkArgument(
        graph.nodeCount() <= nodeMap.size(),
        "Error: Invalid node map (Graph contains "
            + graph.nodeCount()
            + " nodes while nodeMap contains "
            + nodeMap.size()
            + " nodes");

    final HashSet<Node> visitedNodes = new HashSet<Node>();
    final HashSet<NodeType> visitedNodes2 = new HashSet<NodeType>();

    for (final Map.Entry<Node, NodeType> elem : nodeMap.entrySet()) {
      final Node ynode = elem.getKey();
      final NodeType node = elem.getValue();

      Preconditions.checkArgument((ynode != null) && (node != null), "Error: Invalid node map");

      // We can not check this because of nodes hidden in folder nodes
      // if (!graph.contains(ynode))
      Preconditions.checkArgument(!visitedNodes.contains(ynode), "Error: Invalid node map");
      Preconditions.checkArgument(!visitedNodes2.contains(node), "Error: Invalid node map");

      visitedNodes.add(ynode);
      visitedNodes2.add(node);
    }
  }
}
