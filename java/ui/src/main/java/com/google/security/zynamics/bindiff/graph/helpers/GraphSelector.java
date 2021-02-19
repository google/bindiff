// Copyright 2011-2021 Google LLC
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

import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedViewNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphConverters;
import com.google.security.zynamics.zylib.types.graphs.GraphAlgorithms;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GraphSelector {
  private static <NodeType extends ZyGraphNode<?>> void deselectNodes(
      final BinDiffGraph<NodeType, ?> graph, final Collection<NodeType> nodesToDeselect) {
    final Set<NodeType> toSelect = new HashSet<>();
    final Set<NodeType> toUnselect = new HashSet<>();

    toSelect.addAll(graph.getSelectedNodes());

    final boolean excludeInvisible =
        graph.getSettings().getProximitySettings().getProximityBrowsing()
            && graph.getSettings().getProximitySettings().getProximityBrowsingFrozen();

    for (final NodeType node : nodesToDeselect) {
      // Note: A node may be selected and invisible
      if (node.isVisible() || !excludeInvisible) {
        toSelect.remove(node);
      }
    }

    toUnselect.addAll(graph.getNodes());
    toUnselect.removeAll(toSelect);

    graph.selectNodes(toSelect, toUnselect);
  }

  private static Set<CombinedDiffNode> getSelectedLeafNodes(final CombinedGraph graph) {
    final Set<CombinedDiffNode> nodesToDeselect = new HashSet<>();
    for (final CombinedDiffNode selectedNode : graph.getSelectedNodes()) {
      boolean successorSelection = false;
      for (final CombinedViewNode successor :
          GraphAlgorithms.getSuccessors(selectedNode.getRawNode(), 1)) {
        if (successor.isSelected()) {
          successorSelection = true;
          continue;
        }
      }

      if (!successorSelection) {
        nodesToDeselect.add(selectedNode);
      }
    }

    return nodesToDeselect;
  }

  private static Collection<SingleDiffNode> getSelectedLeafNodes(final SingleGraph graph) {
    final Set<SingleDiffNode> nodesToDeselect = new HashSet<>();
    for (final SingleDiffNode selectedNode : graph.getSelectedNodes()) {
      boolean successorSelection = false;
      for (final SingleViewNode successor :
          GraphAlgorithms.getSuccessors(selectedNode.getRawNode(), 1)) {
        if (successor.isSelected()) {
          successorSelection = true;
          continue;
        }
      }
      if (!successorSelection) {
        nodesToDeselect.add(selectedNode);
      }
    }

    return nodesToDeselect;
  }

  private static Set<CombinedDiffNode> getSelectedRootNodes(final CombinedGraph graph) {
    final Set<CombinedDiffNode> nodesToDeselect = new HashSet<>();
    for (final CombinedDiffNode selectedNode : graph.getSelectedNodes()) {
      boolean predecessorSelection = false;
      for (final CombinedViewNode predecessor :
          GraphAlgorithms.getPredecessors(selectedNode.getRawNode(), 1)) {
        if (predecessor.isSelected()) {
          predecessorSelection = true;
          continue;
        }
      }

      if (!predecessorSelection) {
        nodesToDeselect.add(selectedNode);
      }
    }

    return nodesToDeselect;
  }

  private static Set<SingleDiffNode> getSelectedRootNodes(final SingleGraph graph) {
    final Set<SingleDiffNode> nodesToDeselect = new HashSet<>();
    for (final SingleDiffNode selectedNode : graph.getSelectedNodes()) {
      boolean predecessorSelection = false;
      for (final SingleViewNode predecessor :
          GraphAlgorithms.getPredecessors(selectedNode.getRawNode(), 1)) {
        if (predecessor.isSelected()) {
          predecessorSelection = true;
          continue;
        }
      }

      if (!predecessorSelection) {
        nodesToDeselect.add(selectedNode);
      }
    }

    return nodesToDeselect;
  }

  private static <NodeType extends ZyGraphNode<?>> void selectNodes(
      final BinDiffGraph<NodeType, ?> graph, final Collection<NodeType> nodesToSelect) {
    final Set<NodeType> toSelect = new HashSet<>();
    final Set<NodeType> toUnselect = new HashSet<>();

    toUnselect.addAll(graph.getNodes());
    toSelect.addAll(graph.getSelectedNodes());

    final boolean excludeInvisible =
        graph.getSettings().getProximitySettings().getProximityBrowsing()
            && graph.getSettings().getProximitySettings().getProximityBrowsingFrozen();

    for (final NodeType node : nodesToSelect) {
      // Note: A node may be selected and invisible
      if (node.isVisible() || !excludeInvisible) {
        toSelect.add(node);
      }
    }

    toUnselect.removeAll(toSelect);

    graph.selectNodes(toSelect, toUnselect);
  }

  public static void deselectLeafs(final CombinedGraph graph) {
    deselectNodes(graph, getSelectedLeafNodes(graph));
  }

  public static void deselectLeafs(final SingleGraph graph) {
    deselectNodes(graph, getSelectedLeafNodes(graph));
  }

  public static void deselectPeriphery(final CombinedGraph graph) {
    final Set<CombinedDiffNode> nodesToDeselect = getSelectedRootNodes(graph);
    nodesToDeselect.addAll(getSelectedLeafNodes(graph));

    deselectNodes(graph, nodesToDeselect);
  }

  public static void deselectPeriphery(final SingleGraph graph) {
    final Set<SingleDiffNode> nodesToDeselect = getSelectedRootNodes(graph);
    nodesToDeselect.addAll(getSelectedLeafNodes(graph));

    deselectNodes(graph, nodesToDeselect);
  }

  public static void deselectRoots(final CombinedGraph graph) {
    deselectNodes(graph, getSelectedRootNodes(graph));
  }

  public static void deselectRoots(final SingleGraph graph) {
    deselectNodes(graph, getSelectedRootNodes(graph));
  }

  public static void invertSelection(final CombinedGraph graph) {
    final Collection<CombinedDiffNode> nodesToSelect = new ArrayList<>();
    final Collection<CombinedDiffNode> nodesToUnselect = new ArrayList<>();

    final boolean excludeInvisible =
        graph.getSettings().getProximitySettings().getProximityBrowsing()
            && graph.getSettings().getProximitySettings().getProximityBrowsingFrozen();

    for (final CombinedDiffNode combinedNode : graph.getNodes()) {
      if (excludeInvisible) {
        if (!combinedNode.isVisible()) {
          continue;
        }
      }

      if (combinedNode.isSelected()) {
        nodesToUnselect.add(combinedNode);
      } else {
        nodesToSelect.add(combinedNode);
      }
    }

    graph.selectNodes(nodesToSelect, nodesToUnselect);
  }

  public static void invertSelection(final SingleGraph graph) {
    final Collection<SingleDiffNode> nodesToSelect = new ArrayList<>();
    final Collection<SingleDiffNode> nodesToUnselect = new ArrayList<>();

    final boolean excludeInvisible =
        graph.getSettings().getProximitySettings().getProximityBrowsing()
            && graph.getSettings().getProximitySettings().getProximityBrowsingFrozen();

    for (final SingleDiffNode superNode : graph.getNodes()) {
      if (excludeInvisible) {
        if (!superNode.isVisible()) {
          continue;
        }
      }

      if (superNode.isSelected()) {
        nodesToUnselect.add(superNode);
      } else {
        nodesToSelect.add(superNode);
      }
    }

    graph.selectNodes(nodesToSelect, nodesToUnselect);
  }

  public static void selectAncestorsOfSelection(final CombinedGraph graph) {
    final List<CombinedViewNode> rawNodes = GraphConverters.convert(graph.getSelectedNodes());
    selectNodes(graph, GraphConverters.convert(graph, GraphAlgorithms.getPredecessors(rawNodes)));
  }

  public static void selectAncestorsOfSelection(final SingleGraph graph) {
    final List<SingleViewNode> rawNodes = GraphConverters.convert(graph.getSelectedNodes());
    selectNodes(graph, GraphConverters.convert(graph, GraphAlgorithms.getPredecessors(rawNodes)));
  }

  public static void selectChildrenOfSelection(final CombinedGraph graph) {
    final List<CombinedViewNode> rawNodes = GraphConverters.convert(graph.getSelectedNodes());
    selectNodes(graph, GraphConverters.convert(graph, GraphAlgorithms.getSuccessors(rawNodes, 1)));
  }

  public static void selectChildrenOfSelection(final SingleGraph graph) {
    final List<SingleViewNode> rawNodes = GraphConverters.convert(graph.getSelectedNodes());
    selectNodes(graph, GraphConverters.convert(graph, GraphAlgorithms.getSuccessors(rawNodes, 1)));
  }

  public static void selectNeighboursOfSelection(final CombinedGraph graph) {
    final List<CombinedViewNode> rawNodes = GraphConverters.convert(graph.getSelectedNodes());
    final Set<CombinedDiffNode> nodesToSelect = new HashSet<>();
    nodesToSelect.addAll(
        GraphConverters.convert(graph, GraphAlgorithms.getPredecessors(rawNodes, 1)));
    nodesToSelect.addAll(
        GraphConverters.convert(graph, GraphAlgorithms.getSuccessors(rawNodes, 1)));

    selectNodes(graph, nodesToSelect);
  }

  public static void selectNeighboursOfSelection(final SingleGraph graph) {
    final List<SingleViewNode> rawNodes = GraphConverters.convert(graph.getSelectedNodes());
    final Set<SingleDiffNode> nodesToSelect = new HashSet<>();
    nodesToSelect.addAll(
        GraphConverters.convert(graph, GraphAlgorithms.getPredecessors(rawNodes, 1)));
    nodesToSelect.addAll(
        GraphConverters.convert(graph, GraphAlgorithms.getSuccessors(rawNodes, 1)));

    selectNodes(graph, nodesToSelect);
  }

  public static void selectParentsOfSelection(final CombinedGraph graph) {
    final List<CombinedViewNode> rawNodes = GraphConverters.convert(graph.getSelectedNodes());
    selectNodes(
        graph, GraphConverters.convert(graph, GraphAlgorithms.getPredecessors(rawNodes, 1)));
  }

  public static void selectParentsOfSelection(final SingleGraph graph) {
    final List<SingleViewNode> rawNodes = GraphConverters.convert(graph.getSelectedNodes());
    selectNodes(
        graph, GraphConverters.convert(graph, GraphAlgorithms.getPredecessors(rawNodes, 1)));
  }

  public static void selectSuccessorsOfSelection(final CombinedGraph graph) {
    final List<CombinedViewNode> rawNodes = GraphConverters.convert(graph.getSelectedNodes());
    selectNodes(graph, GraphConverters.convert(graph, GraphAlgorithms.getSuccessors(rawNodes)));
  }

  public static void selectSuccessorsOfSelection(final SingleGraph graph) {
    final List<SingleViewNode> rawNodes = GraphConverters.convert(graph.getSelectedNodes());
    selectNodes(graph, GraphConverters.convert(graph, GraphAlgorithms.getSuccessors(rawNodes)));
  }
}
