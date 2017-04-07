// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph;

import com.google.common.base.Preconditions;

import y.base.Node;
import y.view.Graph2D;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ZyGraphVerifier {
  /**
   * Verifies the map that maps between ynode objects and node objects. If the map has an incorrect
   * format, the function throws an {@link IllegalArgumentException}.
   * 
   * @param graph
   */
  public static <NodeType> void verifyMap(final Graph2D graph, final HashMap<Node, NodeType> nodeMap) {
    // Let's verify the node map.
    //
    // - The number of mappings must equal or less than the number of graphs in the node (each node
    // must be mapped, but some nodes can be hidden in folder nodes)
    // - No element of the key set/value set of the mapping must appear more than once
    // - No key or value of the mapping must be null

    Preconditions.checkArgument(graph.nodeCount() <= nodeMap.size(),
        "Error: Invalid node map (Graph contains " + graph.nodeCount()
            + " nodes while nodeMap contains " + nodeMap.size() + " nodes");

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
