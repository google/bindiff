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

package com.google.security.zynamics.zylib.gui.zygraph.proximity;

import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphConverters;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphHelpers;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.ITextNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.NodeHelpers;
import com.google.security.zynamics.zylib.gui.zygraph.wrappers.ViewableGraph;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;
import com.google.security.zynamics.zylib.types.graphs.GraphAlgorithms;
import com.google.security.zynamics.zylib.types.graphs.IGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyProximityNode;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class ProximityNodeFunctions<
    NodeType extends IViewNode<?> & IGraphNode<NodeType>,
    U extends ZyGraphNode<NodeType>,
    V extends AbstractZyGraph<U, ?>> {
  private Collection<U> filterVisibleNodes(final Collection<U> visibleNodes) {
    return CollectionHelpers.filter(
        visibleNodes,
        new ICollectionFilter<U>() {
          @Override
          public boolean qualifies(final U node) {
            return NodeHelpers.getVisibleNode((IViewNode<?>) node.getRawNode())
                == node.getRawNode();
          }
        });
  }

  @SuppressWarnings("unchecked")
  private Collection<U> getNeighborhood(final V graph, final ZyProximityNode<NodeType> zyNode) {
    final List<? extends NodeType> candidates =
        zyNode.isIncoming()
            ? zyNode.getRawNode().getAttachedNode().getChildren()
            : zyNode.getRawNode().getAttachedNode().getParents();

    final Set<NodeType> nodes = new LinkedHashSet<NodeType>();

    for (final NodeType candidate : candidates) {
      if (candidate.isVisible()) {
        // Candidate nodes that were already visible were obviously not hidden
        // by a proximity node and can therefore be disregarded for the selection
        // neighborhood.

        continue;
      }

      if (candidate.getParentGroup() != null) {
        nodes.add((NodeType) candidate.getParentGroup());
        nodes.add(candidate);
      } else {
        nodes.add(candidate);
      }
    }

    // When calculating the neighborhood of a node one must especially consider
    // that text nodes attached to nodes need to be treated specially since they
    // should always be visible if the node they are attached to is visible.

    for (final NodeType node : new ArrayList<NodeType>(nodes)) {
      if (zyNode.isIncoming()) {
        for (final NodeType parent : node.getParents()) {
          if (parent instanceof ITextNode) {
            nodes.add(parent);
          }
        }
      } else {
        for (final NodeType child : node.getChildren()) {
          if (child instanceof ITextNode) {
            nodes.add(child);
          }
        }
      }
    }

    return GraphConverters.convert(graph, nodes);
  }

  protected abstract void showNodes(Window parent, V graph, List<U> nodes, boolean visible);

  public void showHiddenNodes(
      final Window parent, final V graph, final ZyProximityNode<NodeType> zyNode) {
    final Set<U> toShow = new LinkedHashSet<U>();

    // Use a temporary variable to work around OpenJDK build problem. Original code is:
    // toShow.addAll(filterVisibleNodes(GraphHelpers.getVisibleNodes(ViewableGraph.wrap(graph))));
    final Set<U> nodes = GraphHelpers.getVisibleNodes(ViewableGraph.wrap(graph));
    toShow.addAll(filterVisibleNodes(nodes));

    toShow.addAll(filterVisibleNodes(getNeighborhood(graph, zyNode)));

    showNodes(parent, graph, new ArrayList<U>(toShow), false);
  }

  public void unhideAndSelect(final V graph, final ZyProximityNode<NodeType> node) {
    graph.selectNodes(getNeighborhood(graph, node), true);

    // ATTENTION: showNodes is not necessary because this is handled by the ProximityUpdater
    // showHiddenNodes(parent, graph, node);
  }

  public void unhideAndSelectOnly(final V graph, final ZyProximityNode<NodeType> node) {
    final Collection<U> neighbors = getNeighborhood(graph, node);
    final List<U> notNeighbors = GraphHelpers.getNodes(graph);
    notNeighbors.removeAll(neighbors);

    graph.selectNodes(neighbors, notNeighbors);

    // ATTENTION: showNodes is not necessary because this is handled by the ProximityUpdater
    // showHiddenNodes(parent, graph, node);
  }

  public void unhideChildren(
      final Window parent, final V graph, final ZyProximityNode<NodeType> node) {
    final List<U> toShow = new ArrayList<U>();

    // Use a temporary variable to work around OpenJDK build problem. Original code is:
    // toShow.addAll(GraphHelpers.getVisibleNodes(ViewableGraph.wrap(graph)));
    final Set<U> nodes = GraphHelpers.getVisibleNodes(ViewableGraph.wrap(graph));
    toShow.addAll(nodes);

    toShow.addAll(
        GraphConverters.convert(
            graph, GraphAlgorithms.getSuccessors(node.getRawNode().getAttachedNode())));

    showNodes(parent, graph, toShow, true);
  }

  public void unhideParents(
      final Window parent, final V graph, final ZyProximityNode<NodeType> proximityNode) {
    final List<U> toShow = new ArrayList<U>();

    // Use a temporary variable to work around OpenJDK build problem. Original code is:
    // toShow.addAll(GraphHelpers.getVisibleNodes(ViewableGraph.wrap(graph)));
    final Set<U> nodes = GraphHelpers.getVisibleNodes(ViewableGraph.wrap(graph));
    toShow.addAll(nodes);

    toShow.addAll(
        GraphConverters.convert(
            graph, GraphAlgorithms.getPredecessors(proximityNode.getRawNode().getAttachedNode())));

    showNodes(parent, graph, toShow, true);
  }
}
