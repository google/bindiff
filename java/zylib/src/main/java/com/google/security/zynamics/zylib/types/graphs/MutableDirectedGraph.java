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

package com.google.security.zynamics.zylib.types.graphs;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Simple mutable directed graph class that contains nodes and directed edges.
 *
 * @param <NodeType> Type of the nodes in the graph.
 * @param <EdgeType> Type of the edges in the graph.
 */
public class MutableDirectedGraph<NodeType, EdgeType extends IGraphEdge<NodeType>>
    implements IDirectedGraph<NodeType, EdgeType>, Iterable<NodeType> {

  /** Nodes of the graph. */
  private final List<NodeType> m_nodes;

  /** Edges of the graph. */
  private final List<EdgeType> m_edges;

  /** Map that keeps track what edges belong to a node. */
  private final Map<NodeType, List<EdgeType>> m_nodeToEdges =
      new HashMap<NodeType, List<EdgeType>>();

  /**
   * Creates a new mutable directed graph.
   *
   * @param nodes The nodes of the graph.
   * @param edges The edges of the graph.
   */
  public MutableDirectedGraph(final List<NodeType> nodes, final List<EdgeType> edges) {
    m_nodes = Preconditions.checkNotNull(nodes, "Nodes argument can not be null");
    m_edges = Preconditions.checkNotNull(edges, "Edges argument can not be null");

    for (final NodeType node : nodes) {
      Preconditions.checkNotNull(node, "Node list contains null-nodes");
      m_nodeToEdges.put(node, new ArrayList<EdgeType>());
    }

    for (final EdgeType edge : edges) {
      updateNodeToEdgeMapping(edge);
    }
  }

  private void updateNodeToEdgeMapping(final EdgeType edge) {
    Preconditions.checkNotNull(edge, "Error: edge argument can not be null");

    if (m_nodeToEdges.get(edge.getSource()) != null) {
      m_nodeToEdges.get(edge.getSource()).add(edge);
    } else {
      throw new IllegalStateException(
          "Error: The given edge has a source node which is not known to the graph.");
    }
    if (m_nodeToEdges.get(edge.getTarget()) != null) {
      m_nodeToEdges.get(edge.getTarget()).add(edge);
    } else {
      throw new IllegalStateException(
          "Error: the given edge has a target node which is not known to the graph.");
    }
  }

  /**
   * Adds an edge to the graph.
   *
   * @param edge The edge to add.
   */
  public void addEdge(final EdgeType edge) {
    Preconditions.checkNotNull(edge, "Edge argument can not be null");

    m_edges.add(edge);
    updateNodeToEdgeMapping(edge);
  }

  /**
   * Adds a node to the graph.
   *
   * @param node The node to add to the graph.
   */
  public void addNode(final NodeType node) {
    Preconditions.checkNotNull(node, "Node argument can not be null");

    m_nodes.add(node);
    m_nodeToEdges.put(node, new ArrayList<EdgeType>());
  }

  @Override
  public int edgeCount() {
    return m_edges.size();
  }

  /**
   * Returns an unmodifiable list of edges in the graph.
   *
   * @return Unmodifiable collection of edges.
   */
  @Override
  public List<EdgeType> getEdges() {
    return Collections.unmodifiableList(m_edges);
  }

  /**
   * Returns an unmodifiable list of nodes in the graph.
   *
   * @return Unmodifiable collection of nodes.
   */
  @Override
  public List<NodeType> getNodes() {
    return Collections.unmodifiableList(m_nodes);
  }

  @Override
  public Iterator<NodeType> iterator() {
    return m_nodes.iterator();
  }

  @Override
  public int nodeCount() {
    return m_nodes.size();
  }

  /**
   * Removes an edge from the graph.
   *
   * @param edge The edge to remove.
   */
  public void removeEdge(final EdgeType edge) {
    Preconditions.checkArgument(m_edges.remove(edge), "Error: Edge was not part of the graph");
    m_nodeToEdges.get(edge.getSource()).remove(edge);
    m_nodeToEdges.get(edge.getTarget()).remove(edge);
  }

  /**
   * Removes a node from the graph.
   *
   * @param node The node to remove.
   */
  public void removeNode(final NodeType node) {
    Preconditions.checkArgument(
        m_nodes.remove(node), "Error: Node '%s' was not part of the graph", node);
    m_edges.removeAll(m_nodeToEdges.get(node));
    m_nodeToEdges.remove(node);
  }
}
