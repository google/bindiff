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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;

import y.base.Edge;
import y.base.Node;
import y.view.Graph2D;

import java.util.Collection;
import java.util.Map;

public class ZyGraphMappings<NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>> {
  /**
   * This map provides a mapping between the yedge objects and the edge objects of the graph.
   */
  private final Map<Edge, EdgeType> m_edgeMap;
  private final Map<Object, EdgeType> m_rawEdgeMap;

  /**
   * This map provides a mapping between the ynode objects and the node objects of the graph.
   * (Proximity info nodes are not included);
   */
  private final Map<Node, NodeType> m_nodeMap;
  private final Map<Object, NodeType> m_rawNodeMap;

  private final Graph2D m_graph;

  public ZyGraphMappings(final Graph2D graph, final Map<Node, NodeType> nodeMap,
      final Map<Edge, EdgeType> edgeMap) {
    m_graph = graph;

    // Create a clone so that we have our own map that's not exposed to the outside.
    m_nodeMap = Maps.newHashMap(nodeMap);
    m_rawNodeMap = Maps.newHashMap();

    // Create a clone so that we have our own map that's not exposed to the outside.
    m_edgeMap = Maps.newHashMap(edgeMap);
    m_rawEdgeMap = Maps.newHashMap();

    for (final EdgeType e : edgeMap.values()) {
      m_rawEdgeMap.put(e.getRawEdge(), e);
    }

    for (final NodeType n : nodeMap.values()) {
      m_rawNodeMap.put(n.getRawNode(), n);
    }
  }

  public void addEdge(final Edge n, final EdgeType n2) {
    m_edgeMap.put(n, n2);
    m_rawEdgeMap.put(n2.getRawEdge(), n2);
  }

  public void addNode(final Node n, final NodeType n2) {
    m_nodeMap.put(n, n2);
    m_rawNodeMap.put(n2.getRawNode(), n2);
  }

  public EdgeType getEdge(final Edge edge) {
    if (edge.getGraph() != m_graph) {
      return null;
    }

    @SuppressWarnings("unchecked")
    final EdgeType result =
        (EdgeType) ((ZyEdgeRealizer<?>) m_graph.getRealizer(edge)).getUserData().getEdge();
    return result;
  }

  public EdgeType getEdge(final Object object) {
    return m_rawEdgeMap.get(object);
  }

  public Collection<EdgeType> getEdges() {
    return m_edgeMap.values();
  }

  public NodeType getNode(final Node node) {
    Preconditions.checkNotNull(node, "Node argument cannot be null");
    return m_nodeMap.get(node);
  }

  public NodeType getNode(final Object sourceNode) {
    return m_rawNodeMap.get(sourceNode);
  }

  public Collection<NodeType> getNodes() {
    return m_nodeMap.values();
  }

  public Edge getYEdge(final Object rawEdge) {
    // TODO(cblichmann): Check interaction of m_edgeMap with
    // HierarchyManager from yFiles
    return m_rawEdgeMap.get(rawEdge).getEdge();
  }

  public Node getYNode(final Object rawNode) {
    if (m_rawNodeMap.containsKey(rawNode)) {
      return m_rawNodeMap.get(rawNode).getNode();
    } else {
      return null;
    }
  }

  public void removeEdge(final EdgeType edge) {
    m_edgeMap.remove(edge.getEdge());
    m_rawEdgeMap.remove(edge.getRawEdge());
  }

  public void removeNode(final NodeType node) {
    m_nodeMap.remove(node.getNode());
    m_rawNodeMap.remove(node.getRawNode());
  }

  public void setEdge(final Object newEdge, final EdgeType oldEdge) {
    m_rawEdgeMap.put(newEdge, oldEdge);
  }

  public void setNode(final Object newNode, final NodeType oldNode) {
    m_rawNodeMap.put(newNode, oldNode);
  }
}
