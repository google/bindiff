// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.types.graphs;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Simple directed graph class that contains nodes and directed edges.
 * 
 * @param <NodeType> Type of the nodes in the graph.
 * @param <EdgeType> Type of the edges in the graph.
 */
public class DirectedGraph<NodeType extends IGraphNode<NodeType>, EdgeType extends IGraphEdge<NodeType>>
    implements Iterable<NodeType>, IDirectedGraph<NodeType, EdgeType> {
  /**
   * Nodes of the graph.
   */
  private final List<NodeType> m_nodes;

  /**
   * Edges of the graph.
   */
  private final List<EdgeType> m_edges;

  /**
   * Map of nodes to incoming edges.
   */
  private final SetMultimap<NodeType, EdgeType> m_incomingEdges = HashMultimap.create();

  /**
   * Map of nodes to outgoing edges.
   */
  private final SetMultimap<NodeType, EdgeType> m_outgoingEdges = HashMultimap.create();

  /**
   * Creates a new directed edge object.
   * 
   * @param nodes Nodes of the graph.
   * @param edges Edges of the graph.
   */
  public DirectedGraph(final List<NodeType> nodes, final List<EdgeType> edges) {
    Preconditions.checkNotNull(nodes, "Error: Nodes argument can not be null");
    Preconditions.checkNotNull(edges, "Error: Edges argument can not be null");

    for (final NodeType node : nodes) {
      Preconditions.checkNotNull(node, "Error: Node list contains null-nodes");
    }

    for (final EdgeType edge : edges) {
      m_outgoingEdges.put(edge.getSource(), edge);
      m_incomingEdges.put(edge.getTarget(), edge);
    }

    m_nodes = nodes;
    m_edges = edges;
  }

  @Override
  public int edgeCount() {
    return m_edges.size();
  }

  @Override
  public List<EdgeType> getEdges() {
    return Collections.unmodifiableList(m_edges);
  }

  public Set<EdgeType> getIncomingEdges(final NodeType node) {
    return m_incomingEdges.get(node);
  }

  @Override
  public List<NodeType> getNodes() {
    return Collections.unmodifiableList(m_nodes);
  }

  public Set<EdgeType> getOutgoingEdges(final NodeType node) {
    return m_outgoingEdges.get(node);
  }

  @Override
  public Iterator<NodeType> iterator() {
    return m_nodes.iterator();
  }

  @Override
  public int nodeCount() {
    return m_nodes.size();
  }
}
