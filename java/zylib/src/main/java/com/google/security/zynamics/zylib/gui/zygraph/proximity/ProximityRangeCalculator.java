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

package com.google.security.zynamics.zylib.gui.zygraph.proximity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphConverters;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IGroupNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.ITextNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.NodeHelpers;
import com.google.security.zynamics.zylib.gui.zygraph.wrappers.ViewNodeAdapter;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

/**
 * Used to calculate the proximity browsing neighborhood of nodes.
 */
public final class ProximityRangeCalculator {
  private static Set<ViewNodeAdapter> getGroupMembers(final IGroupNode<?, ?> node) {
    final Set<ViewNodeAdapter> groupMembers = new HashSet<ViewNodeAdapter>();

    for (final IViewNode<?> member : node.getElements()) {
      groupMembers.add(new ViewNodeAdapter(member));
    }

    return groupMembers;
  }

  private static <NodeType extends ZyGraphNode<? extends IViewNode<?>>> Set<ViewNodeAdapter> getParentGroups(
      final NodeType node) {
    final Set<ViewNodeAdapter> parentGroups = new HashSet<ViewNodeAdapter>();

    IGroupNode<?, ?> parentGroup = ((IViewNode<?>) node.getRawNode()).getParentGroup();

    while (parentGroup != null) {
      parentGroups.add(new ViewNodeAdapter(parentGroup));

      parentGroup = parentGroup.getParentGroup();
    }

    return parentGroups;
  }

  private static List<ViewNodeAdapter> getPredecessors(
      final Iterable<ViewNodeAdapter> selectedNodes, final int depth) {
    final List<ViewNodeAdapter> nodes = new ArrayList<ViewNodeAdapter>();

    for (final ViewNodeAdapter node : selectedNodes) {
      nodes.addAll(getPredecessors(node, depth));
    }

    return nodes;
  }

  private static Set<ViewNodeAdapter> getPredecessors(final ViewNodeAdapter node, final int depth) {
    final Set<ViewNodeAdapter> nodes = new HashSet<ViewNodeAdapter>();

    getPredecessorsInternal(node, depth, nodes, new HashSet<ViewNodeAdapter>());

    return nodes;
  }

  private static void getPredecessorsInternal(final ViewNodeAdapter node, final int depth,
      final Set<ViewNodeAdapter> nodes, final Set<ViewNodeAdapter> visited) {
    for (final ViewNodeAdapter parent : node.getParents()) {
      if (/* visited.contains(parent) || */(depth <= 0) && !(parent.getNode() instanceof ITextNode)) {
        // NH: see getSuccessorsInternal for comment
        continue;
      } else if (parent.getNode().getParentGroup() != null) {
        IGroupNode<?, ?> previousNode = parent.getNode().getParentGroup();

        while (previousNode != null) {
          // It is not intuitively obvious how to treat group nodes during
          // proximity browsing. On the one hand we could treat the
          // whole group as one node, on the other hand we could treat
          // the content of the group individually.

          if (!visited(visited, previousNode)) {
            final ViewNodeAdapter groupNodeAdapter = new ViewNodeAdapter(previousNode);

            visited.add(groupNodeAdapter);

            nodes.add(groupNodeAdapter);

            previousNode = previousNode.getParentGroup();
          } else {
            previousNode = null;
          }
        }
      }

      visited.add(parent);

      nodes.add(parent);

      // Silly trick to make the attached text nodes visible
      getSuccessorsInternal(parent, -1, nodes, new HashSet<ViewNodeAdapter>());

      getPredecessorsInternal(parent, depth - 1, nodes, visited);
    }
  }

  private static int getRealDepth(final int depth) {
    return depth == -1 ? Integer.MAX_VALUE : depth;
  }

  private static List<ViewNodeAdapter> getSuccessors(final Iterable<ViewNodeAdapter> selectedNodes,
      final int depth) {
    final List<ViewNodeAdapter> nodes = new ArrayList<ViewNodeAdapter>();

    for (final ViewNodeAdapter node : selectedNodes) {
      nodes.addAll(getSuccessors(node, depth));
    }

    return nodes;
  }

  private static Set<ViewNodeAdapter> getSuccessors(final ViewNodeAdapter node, final int depth) {
    final Set<ViewNodeAdapter> nodes = new HashSet<ViewNodeAdapter>();

    getSuccessorsInternal(node, depth, nodes, new HashSet<ViewNodeAdapter>());

    return nodes;
  }

  private static void getSuccessorsInternal(final ViewNodeAdapter node, final int depth,
      final Set<ViewNodeAdapter> nodes, final HashSet<ViewNodeAdapter> visited) {
    for (final ViewNodeAdapter child : node.getChildren()) {
      if (/* visited.contains(child) || */(depth <= 0) && !(child.getNode() instanceof ITextNode)) {
        // NH: "visited.contains(child) ||" in the if clause can lead to an incorrect Proximity
        // Browsing state, which does not really harm BinNavi
        // but do lead to exceptions in BinDiff because of asynchronous node visibilities in the
        // primary and secondary graphs.
        // Example:
        // Given:
        // - A graph with the vertices A, B, C, D and E.
        // - A->B, A->D, B->C, B->D, D->E
        // - A is selected
        // - Proximity Browsing depth is 2
        //
        // Whether vertex E is visible is depending on the order of the node list returned by
        // node.getChildren().
        // If A.getChildren() returns list(B, D) E is not visible in the graph.
        // If A.getChildren() returns list(D, B) E is visible in the graph

        continue;
      } else if (child.getNode().getParentGroup() != null) {
        IGroupNode<?, ?> previousNode = child.getNode().getParentGroup();

        // It is not intuitively obvious how to treat group nodes during
        // proximity browsing. On the one hand we could treat the
        // whole group as one node, on the other hand we could treat
        // the content of the group individually.

        while (previousNode != null) {
          if (!visited(visited, previousNode)) {
            final ViewNodeAdapter groupNodeAdapter = new ViewNodeAdapter(previousNode);

            visited.add(groupNodeAdapter);

            nodes.add(groupNodeAdapter);

            previousNode = previousNode.getParentGroup();
          } else {
            previousNode = null;
          }
        }
      }

      if (NodeHelpers.getVisibleNode(child.getNode()) != child.getNode()) {
        continue;
      }

      visited.add(child);

      nodes.add(child);

      // Silly trick to make attached text nodes visible.
      getPredecessorsInternal(child, -1, nodes, new HashSet<ViewNodeAdapter>());

      getSuccessorsInternal(child, depth - 1, nodes, visited);
    }
  }

  private static boolean visited(final Set<ViewNodeAdapter> visited,
      final IGroupNode<?, ?> parentGroup) {
    return CollectionHelpers.any(visited, new ICollectionFilter<ViewNodeAdapter>() {
      @Override
      public boolean qualifies(final ViewNodeAdapter adapter) {
        return adapter.getNode() == parentGroup;
      }
    });
  }

  /**
   * Calculates the proximity browsing neighborhood of a given list of nodes.
   * 
   * @param <NodeType> Node type of the nodes in the graph.
   * 
   * @param graph The graph where the node is located.
   * @param nodes Nodes for which the neighborhood is calculated.
   * @param childDepth Depth of children for each input node.
   * @param parentDepth Depth of parents of each input node.
   * 
   * @return The calculated proximity browsing neighborhood for the given nodes.
   */
  public static <NodeType extends ZyGraphNode<? extends IViewNode<?>>> Set<NodeType> getNeighbors(
      final AbstractZyGraph<NodeType, ?> graph, final Collection<NodeType> nodes,
      final int childDepth, final int parentDepth) {
    Preconditions.checkNotNull(graph, "Graph argument can not be null");
    Preconditions.checkNotNull(nodes, "Nodes argument can not be null");

    final Set<NodeType> all = new LinkedHashSet<NodeType>(nodes);

    for (final NodeType node : nodes) {
      // Note that it is necessary to calculate the proximity neighborhood on the
      // raw nodes of the graph because the visible nodes have parents and children
      // hidden. For this reason we convert the input nodes to raw nodes first.

      final List<NodeType> nodeList = new ArrayList<NodeType>();
      nodeList.add(node);

      final List<IViewNode<?>> converted = GraphConverters.convert(nodeList);
      final List<ViewNodeAdapter> wrapped = ViewNodeAdapter.wrap(Lists.newArrayList(converted));

      final List<ViewNodeAdapter> preds = getPredecessors(wrapped, getRealDepth(parentDepth));
      final List<ViewNodeAdapter> succs = getSuccessors(wrapped, getRealDepth(childDepth));

      // Handling for group nodes
      all.addAll(ViewNodeAdapter.unwrap(graph, getParentGroups(node)));

      if ((node.getRawNode() instanceof IGroupNode<?, ?>) && node.isSelected()) {
        all.addAll(ViewNodeAdapter.unwrap(graph,
            getGroupMembers((IGroupNode<?, ?>) node.getRawNode())));
      }
      // End of handling for group nodes

      all.addAll(ViewNodeAdapter.unwrap(graph, preds));
      all.addAll(ViewNodeAdapter.unwrap(graph, succs));
    }

    return all;
  }
}
