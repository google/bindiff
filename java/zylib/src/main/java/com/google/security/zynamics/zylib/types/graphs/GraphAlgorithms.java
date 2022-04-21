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

package com.google.security.zynamics.zylib.types.graphs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeFilter;
import com.google.security.zynamics.zylib.types.graphs.algorithms.LengauerTarjan;
import com.google.security.zynamics.zylib.types.graphs.algorithms.MalformedGraphException;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;
import com.google.security.zynamics.zylib.types.trees.TreeAlgorithms;

/**
 * Provides a number of generic algorithms for working with graphs.
 */
public class GraphAlgorithms {
  private GraphAlgorithms() {
    // You are not supposed to instantiate this class.
  }

  /**
   * Helper function that supports the public getPredecessors function.
   * 
   * @param <NodeType> The type of the nodes in the graph.
   * 
   * @param node The node which is the starting point for finding the predecessors.
   * @param predecessors List of predecessors of the start node.
   * @param visited List of nodes in the graph that were already visited.
   */
  private static <NodeType extends IGraphNode<NodeType>> void getPredecessors(
      final IGraphNode<NodeType> node, final Set<NodeType> predecessors, final Set<NodeType> visited) {
    for (final NodeType parent : node.getParents()) {
      // Make sure that each node is only visited once.
      if (visited.contains(parent)) {
        continue;
      }

      visited.add(parent);

      predecessors.add(parent);

      // Recursively find the predecessors of all parent nodes.
      getPredecessors(parent, predecessors, visited);
    }
  }

  private static <NodeType extends IGraphNode<NodeType>> void getPredecessorsInternal(
      final NodeType node, final int depth, final List<NodeType> nodes, final Set<NodeType> visited) {
    if (depth <= 0) {
      return;
    }

    for (final NodeType parent : node.getParents()) {
      if (visited.contains(parent)) {
        continue;
      }

      visited.add(parent);

      nodes.add(parent);

      getPredecessorsInternal(parent, depth - 1, nodes, visited);
    }
  }

  /**
   * Helper function that supports the public getSuccessors function.
   * 
   * @param <NodeType> The type of the nodes in the graph.
   * 
   * @param node The node which is the starting point for finding the successors.
   * @param successors List of successors of the start node.
   * @param visited List of nodes in the graph that were already visited.
   */
  private static <NodeType extends IGraphNode<NodeType>> void getSuccessors(
      final IGraphNode<NodeType> node, final Set<NodeType> successors, final Set<NodeType> visited) {
    for (final NodeType child : node.getChildren()) {
      // Make sure that each node is only visited once.
      if (visited.contains(child)) {
        continue;
      }

      visited.add(child);

      successors.add(child);

      // Recursively find the successors of all child nodes.
      getSuccessors(child, successors, visited);
    }
  }

  private static <NodeType extends IGraphNode<NodeType>> void getSuccessorsInternal(
      final NodeType node, final int depth, final List<NodeType> nodes,
      final HashSet<NodeType> visited) {
    if (depth <= 0) {
      return;
    }

    for (final NodeType child : node.getChildren()) {
      if (visited.contains(child)) {
        continue;
      }

      visited.add(child);

      nodes.add(child);

      getSuccessorsInternal(child, depth - 1, nodes, visited);
    }
  }

  /**
   * Finds all children of a given node that pass the check by a given node filter.
   * 
   * @param <NodeType> The type of the nodes in the graph.
   * 
   * @param node The parent node of all the child nodes.
   * @param filter The filter that provides the node check.
   * 
   * @return All child nodes of the parent node that passed the filter check.
   */
  public static <NodeType extends IGraphNode<NodeType>> Collection<NodeType> collectChildren(
      final NodeType node, final INodeFilter<NodeType> filter) {
    Preconditions.checkNotNull(node, "Error: Node argument can't be null");

    return collectNodes(node.getChildren(), filter);
  }

  /**
   * Filters all nodes from a list of nodes that pass a filter check.
   * 
   * @param <NodeType> The type of nodes in the list.
   * 
   * @param nodes The unfiltered list of nodes.
   * @param filter The filter that provides the node check.
   * 
   * @return All nodes that pass the filter check.
   */
  public static <NodeType> Collection<NodeType> collectNodes(
      final Collection<? extends NodeType> nodes, final INodeFilter<NodeType> filter) {
    Preconditions.checkNotNull(nodes, "Error: Nodes argument can't be null");
    Preconditions.checkNotNull(filter, "Error: Filter argument can't be null");

    final Collection<NodeType> filteredNodes = new ArrayList<NodeType>();

    for (final NodeType child : nodes) {
      // Don't bother to re-select the nodes that are already selected
      if (filter.qualifies(child)) {
        filteredNodes.add(child);
      }
    }

    return filteredNodes;
  }

  /**
   * Finds all parents of a given node that pass the check by a given node filter.
   * 
   * @param <NodeType> The type of the nodes in the graph.
   * 
   * @param node The child node of all the parent nodes.
   * @param filter The filter that provides the node check.
   * 
   * @return All parent nodes of the child node that passed the filter check.
   */
  public static <NodeType extends IGraphNode<NodeType>> Collection<NodeType> collectParents(
      final NodeType node, final INodeFilter<NodeType> filter) {
    Preconditions.checkNotNull(node, "Error: Node argument can't be null");

    return collectNodes(node.getParents(), filter);
  }

  /**
   * Calculates the back edges of the current graph.
   * 
   * @param graph The input graph.
   * @param <NodeType> rootNode The root node of the graph.
   * 
   * @return A HashMap which contains the relation of nodes and their respective back edges.
   * 
   * @throws MalformedGraphException Thrown if the graph has more then one entry node.
   */
  public static <NodeType extends IGraphNode<NodeType>> HashMap<NodeType, ArrayList<NodeType>> getBackEdges(
      final IDirectedGraph<NodeType, ?> graph, final NodeType rootNode)
      throws MalformedGraphException {
    Preconditions.checkNotNull(graph, "Error: Graph argument can not be null");
    Preconditions.checkNotNull(rootNode, "Error: Root Node argument can not be null");

    final HashMap<NodeType, ArrayList<NodeType>> nodeToBackedges =
        new HashMap<NodeType, ArrayList<NodeType>>();
    final Pair<com.google.security.zynamics.zylib.types.trees.Tree<NodeType>, HashMap<NodeType, ITreeNode<NodeType>>> dominatorPair =
        LengauerTarjan.calculate(graph, rootNode);
    final HashMap<NodeType, ITreeNode<NodeType>> dominatorTreeMapping = dominatorPair.second();
    final HashMap<ITreeNode<NodeType>, Set<ITreeNode<NodeType>>> treeNodeDominateRelation =
        TreeAlgorithms.getDominateRelation(dominatorPair.first().getRootNode());

    for (final NodeType t : graph.getNodes()) {
      final ArrayList<NodeType> currentNodesBackedges = new ArrayList<NodeType>();

      final Set<ITreeNode<NodeType>> currentTreeNodeDominateRelation =
          treeNodeDominateRelation.get(dominatorTreeMapping.get(t));

      if (currentTreeNodeDominateRelation != null) {
        for (final NodeType graphNode : t.getChildren()) {
          if (currentTreeNodeDominateRelation.contains(dominatorTreeMapping.get(graphNode))) {
            currentNodesBackedges.add(graphNode);
          }
        }
      }

      nodeToBackedges.put(t, currentNodesBackedges);
    }

    return nodeToBackedges;
  }

  /**
   * Calculates the loops contained in a graph.
   * 
   * @param graph the input graph
   * 
   * @return A List of Sets where each set contains the nodes of one loop.
   * 
   * @throws MalformedGraphException Thrown if the graph has more than one entry node.
   */
  public static <T extends IGraphNode<T>> ArrayList<Set<T>> getGraphLoops(
      final IDirectedGraph<T, ?> graph) throws MalformedGraphException {
    T rootNode = null;
    final ArrayList<Set<T>> resultList = new ArrayList<Set<T>>();

    for (final T currentNode : graph.getNodes()) {
      if (currentNode.getParents().size() == 0) {
        rootNode = currentNode;
        break;
      }
    }

    if (rootNode == null) {
      return null;
    }

    final HashMap<T, ArrayList<T>> nodeToBackEdges = getBackEdges(graph, rootNode);

    for (final T graphNode : graph.getNodes()) {
      final ArrayList<T> nodesBackEdges = nodeToBackEdges.get(graphNode);
      for (final T backEdgeNode : nodesBackEdges) {
        resultList.add(getLoopNodes(graphNode, backEdgeNode));
      }
    }
    return resultList;
  }

  /**
   * Gets the nodes of a loop
   * 
   * @param sourceNode The source node of the loop where the back edge originates from.
   * @param destinationNode The destination node of the loop where the back edge points to.
   * 
   * @return The Set of nodes which belong to the loop.
   */
  public static <NodeType extends IGraphNode<NodeType>> Set<NodeType> getLoopNodes(
      final NodeType sourceNode, final NodeType destinationNode) {
    if (sourceNode == destinationNode) {
      final ArrayList<NodeType> nodeList = new ArrayList<NodeType>();
      nodeList.add(sourceNode);
      return new HashSet<NodeType>(nodeList);
    }

    final ArrayList<NodeType> upwardsNodes = new ArrayList<NodeType>();
    upwardsNodes.add(destinationNode);

    final Set<NodeType> resolveUpwards = new HashSet<NodeType>(upwardsNodes);
    final Stack<NodeType> upwardsWorkingList = new Stack<NodeType>();
    upwardsWorkingList.push(sourceNode);

    while (!upwardsWorkingList.empty()) {
      final NodeType currentNode = upwardsWorkingList.pop();
      resolveUpwards.add(currentNode);
      for (final NodeType currentParentNode : currentNode.getParents()) {
        if (!resolveUpwards.contains(currentParentNode)) {
          resolveUpwards.add(currentParentNode);
          upwardsWorkingList.push(currentParentNode);
        }
      }
    }

    final ArrayList<NodeType> downwardsNodes = new ArrayList<NodeType>();

    final Set<NodeType> resolveDownwards = new HashSet<NodeType>(downwardsNodes);
    final Stack<NodeType> downwardsWorkingList = new Stack<NodeType>();
    downwardsWorkingList.push(destinationNode);

    while (!downwardsWorkingList.empty()) {
      final NodeType currentNode = downwardsWorkingList.pop();
      resolveDownwards.add(currentNode);
      for (final NodeType currentChildNode : currentNode.getChildren()) {
        if (!resolveDownwards.contains(currentChildNode)) {
          resolveDownwards.add(currentChildNode);
          downwardsWorkingList.push(currentChildNode);
        }
      }
    }

    resolveUpwards.retainAll(resolveDownwards);

    return resolveUpwards;
  }

  /**
   * Finds all predecessors of a collection of nodes. Those are all the nodes that have a direct or
   * indirect path to the node.
   * 
   * @param <NodeType> The node type of all nodes in the collection.
   * 
   * @param nodes The collection of input nodes.
   * 
   * @return All predecessors of the input nodes.
   */
  public static <NodeType extends IGraphNode<NodeType>> Collection<NodeType> getPredecessors(
      final Collection<NodeType> nodes) {
    Preconditions.checkNotNull(nodes, "Error: Nodes argument can't be null");

    final HashSet<NodeType> predecessors = new HashSet<NodeType>();

    for (final NodeType zyGraphNode : nodes) {
      predecessors.addAll(getPredecessors(zyGraphNode));
    }

    return predecessors;
  }

  /**
   * Finds all predecessors of a node. Those are all the nodes that have a direct or indirect path
   * to the node.
   * 
   * @param <NodeType> The type parameter that specifies the type of the nodes in the graph.
   * 
   * @param node The start node.
   * 
   * @return A list containing all predecessor nodes of the node.
   */
  public static <NodeType extends IGraphNode<NodeType>> Set<NodeType> getPredecessors(
      final IGraphNode<NodeType> node) {
    Preconditions.checkNotNull(node, "Error: Start node can't be null");

    final HashSet<NodeType> predecessors = new HashSet<NodeType>();
    final HashSet<NodeType> visited = new HashSet<NodeType>();

    getPredecessors(node, predecessors, visited);

    return predecessors;
  }

  @SuppressWarnings("unchecked")
  public static <NodeType extends IGraphNode<NodeType>> Set<NodeType> getPredecessorsUpToNode(
      final IGraphNode<NodeType> childNode, final IGraphNode<NodeType> maximumParentNode) {

    Preconditions.checkNotNull(childNode, "Error: endNode argument can not be null");
    Preconditions.checkNotNull(maximumParentNode, "Error: startNode argument can not be null");

    final HashSet<NodeType> predecessors = Sets.newHashSet();
    final HashSet<NodeType> visited = Sets.newHashSet();
    visited.add((NodeType) maximumParentNode);

    getPredecessors(childNode, predecessors, visited);

    return predecessors;
  }

  public static <NodeType extends IGraphNode<NodeType>> List<NodeType> getPredecessors(
      final Iterable<NodeType> selectedNodes, final int depth) {
    final List<NodeType> nodes = new ArrayList<NodeType>();

    for (final NodeType node : selectedNodes) {
      nodes.addAll(getPredecessors(node, depth));
    }

    return nodes;
  }

  public static <NodeType extends IGraphNode<NodeType>> List<NodeType> getPredecessors(
      final NodeType node, final int depth) {
    final List<NodeType> nodes = new ArrayList<NodeType>();

    getPredecessorsInternal(node, depth, nodes, new HashSet<NodeType>());

    return nodes;
  }

  /**
   * Finds all successors of a collection of nodes. Those are all the nodes that have a direct or
   * indirect path from the node.
   * 
   * @param <NodeType> The node type of all nodes in the collection.
   * 
   * @param nodes The collection of input nodes.
   * 
   * @return All successors of the input nodes.
   */
  public static <NodeType extends IGraphNode<NodeType>> Collection<NodeType> getSuccessors(
      final Collection<NodeType> nodes) {
    Preconditions.checkNotNull(nodes, "Error: Nodes argument can't be null");

    final HashSet<NodeType> successors = new HashSet<NodeType>();

    for (final NodeType zyGraphNode : nodes) {
      successors.addAll(getSuccessors(zyGraphNode));
    }

    return successors;
  }

  /**
   * Finds all successors of a node. Those are all the nodes that have a direct or indirect path
   * from the node.
   * 
   * @param <NodeType> The type parameter that specifies the type of the nodes in the graph.
   * @param node The start node.
   * 
   * @return A list containing all successor nodes of the node.
   */
  public static <NodeType extends IGraphNode<NodeType>> Set<NodeType> getSuccessors(
      final IGraphNode<NodeType> node) {
    Preconditions.checkNotNull(node, "Error: Start node can't be null");

    final Set<NodeType> successors = new HashSet<NodeType>();
    final Set<NodeType> visited = new HashSet<NodeType>();

    getSuccessors(node, successors, visited);

    return successors;
  }

  public static <NodeType extends IGraphNode<NodeType>> Set<NodeType> getSuccessorsDownToNode(
      final IGraphNode<NodeType> parentNode, final IGraphNode<NodeType> maximumChildNode) {
    Preconditions.checkNotNull(parentNode, "Error: parent node can't be null");
    Preconditions
        .checkNotNull(maximumChildNode, "Error: maximumChildNode argument can not be null");

    final Set<NodeType> successors = new HashSet<NodeType>();
    final Set<NodeType> visited = new HashSet<NodeType>();

    getSuccessors(parentNode, successors, visited);

    return successors;
  }

  public static <NodeType extends IGraphNode<NodeType>> List<NodeType> getSuccessors(
      final Iterable<NodeType> selectedNodes, final int depth) {
    final List<NodeType> nodes = new ArrayList<NodeType>();

    for (final NodeType node : selectedNodes) {
      nodes.addAll(getSuccessors(node, depth));
    }

    return nodes;
  }

  public static <NodeType extends IGraphNode<NodeType>> List<NodeType> getSuccessors(
      final NodeType node, final int depth) {
    final List<NodeType> nodes = new ArrayList<NodeType>();

    getSuccessorsInternal(node, depth, nodes, new HashSet<NodeType>());

    return nodes;
  }

  /**
   * Determines whether a given node is a root node.
   * 
   * @param <NodeType> Type of the node.
   * 
   * @param node The node in question.
   * 
   * @return True, if the node is a root node. False, otherwise.
   */
  public static <NodeType extends IGraphNode<NodeType>> boolean isRootNode(final NodeType node) {
    return node.getParents().size() == 0;
  }
}
