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

package com.google.security.zynamics.bindiff.graph.layout.commands;

import com.google.security.zynamics.bindiff.enums.EDiffViewMode;
import com.google.security.zynamics.bindiff.exceptions.GraphLayoutException;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.layout.LayoutCommandHelper;
import com.google.security.zynamics.bindiff.graph.listeners.GraphsIntermediateListeners;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperDiffNode;
import com.google.security.zynamics.bindiff.graph.settings.GraphProximityBrowsingSettings;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import java.util.HashSet;
import java.util.Set;

public class GraphReactivateViewSynchronization {
  private static void synchronizeNodeSelection(final BinDiffGraph<ZyGraphNode<?>, ?> graph) {
    final Set<SuperDiffNode> finalSuperNodesToSelect = new HashSet<>();
    final Set<CombinedDiffNode> finalCombinedNodesToSelect = new HashSet<>();
    final Set<SingleDiffNode> finalPrimaryNodesToSelect = new HashSet<>();
    final Set<SingleDiffNode> finalSecondaryNodesToSelect = new HashSet<>();

    final GraphSettings settings = graph.getSettings();
    for (final CombinedDiffNode node : graph.getCombinedGraph().getNodes()) {
      final SingleDiffNode priDiffNode = node.getPrimaryDiffNode();
      final SingleDiffNode secDiffNode = node.getSecondaryDiffNode();
      final boolean isPriSelected = priDiffNode != null && priDiffNode.isSelected();
      final boolean isSecSelected = secDiffNode != null && secDiffNode.isSelected();

      boolean isSelected =
          settings.getDiffViewMode() == EDiffViewMode.NORMAL_VIEW
              && (isPriSelected || isSecSelected);
      isSelected =
          isSelected
              || settings.getDiffViewMode() == EDiffViewMode.COMBINED_VIEW && node.isSelected();

      if (isSelected) {
        if (priDiffNode != null) {
          finalPrimaryNodesToSelect.add(priDiffNode);
        }
        if (secDiffNode != null) {
          finalSecondaryNodesToSelect.add(secDiffNode);
        }

        finalSuperNodesToSelect.add(node.getSuperDiffNode());
        finalCombinedNodesToSelect.add(node);
      }
    }

    final boolean oldProximityBrowsingFrozen =
        settings.getProximitySettings().getProximityBrowsingFrozen();
    try {
      settings.getProximitySettings().setProximityBrowsingFrozen(true);

      final Set<CombinedDiffNode> finalCombinedNodesToUnselect = new HashSet<>();
      finalCombinedNodesToUnselect.addAll(graph.getCombinedGraph().getNodes());
      finalCombinedNodesToUnselect.removeAll(finalCombinedNodesToSelect);
      graph
          .getCombinedGraph()
          .selectNodes(finalCombinedNodesToSelect, finalCombinedNodesToUnselect);

      final Set<SuperDiffNode> finalSuperNodesToUnselect = new HashSet<>();
      finalSuperNodesToUnselect.addAll(graph.getSuperGraph().getNodes());
      finalSuperNodesToUnselect.removeAll(finalSuperNodesToSelect);
      graph.getSuperGraph().selectNodes(finalSuperNodesToSelect, finalSuperNodesToUnselect);

      final Set<SingleDiffNode> finalPrimaryNodesToUnselect = new HashSet<>();
      finalPrimaryNodesToUnselect.addAll(graph.getPrimaryGraph().getNodes());
      finalPrimaryNodesToUnselect.removeAll(finalPrimaryNodesToSelect);
      graph.getPrimaryGraph().selectNodes(finalPrimaryNodesToSelect, finalPrimaryNodesToUnselect);

      final Set<SingleDiffNode> finalSecondaryNodesToUnselect = new HashSet<>();
      finalSecondaryNodesToUnselect.addAll(graph.getSecondaryGraph().getNodes());
      finalSecondaryNodesToUnselect.removeAll(finalSecondaryNodesToSelect);
      graph
          .getSecondaryGraph()
          .selectNodes(finalSecondaryNodesToSelect, finalSecondaryNodesToUnselect);
    } finally {
      settings.getProximitySettings().setProximityBrowsingFrozen(oldProximityBrowsingFrozen);
    }
  }

  private static void synchronizeNodeVisibility(final BinDiffGraph<ZyGraphNode<?>, ?> graph) {
    final Set<SuperDiffNode> superNodesToShow = new HashSet<>();
    final Set<SuperDiffNode> superNodesToHide = new HashSet<>();

    if (graph.getSettings().getDiffViewMode() == EDiffViewMode.COMBINED_VIEW) {
      for (final CombinedDiffNode combinedNode : graph.getCombinedGraph().getNodes()) {
        if (combinedNode.isVisible()) {
          superNodesToShow.add(combinedNode.getSuperDiffNode());
        } else {
          superNodesToHide.add(combinedNode.getSuperDiffNode());
        }
      }
    } else if (graph.getSettings().getDiffViewMode() == EDiffViewMode.NORMAL_VIEW) {
      final Set<CombinedDiffNode> combinedNodesToShow = new HashSet<>();
      final Set<CombinedDiffNode> combinedNodesToHide = new HashSet<>();

      for (final SingleDiffNode priNode : graph.getPrimaryGraph().getNodes()) {
        if (priNode.isVisible()) {
          superNodesToShow.add(priNode.getSuperDiffNode());
          combinedNodesToShow.add(priNode.getCombinedDiffNode());
        } else {
          superNodesToHide.add(priNode.getSuperDiffNode());
          combinedNodesToHide.add(priNode.getCombinedDiffNode());
        }
      }
      for (final SingleDiffNode secNode : graph.getSecondaryGraph().getNodes()) {
        if (secNode.isVisible()) {
          superNodesToShow.add(secNode.getSuperDiffNode());
          combinedNodesToShow.add(secNode.getCombinedDiffNode());
          final SingleDiffNode priNode = secNode.getOtherSideDiffNode();
          if (priNode != null && !priNode.isVisible()) {
            superNodesToHide.remove(priNode.getSuperDiffNode());
            combinedNodesToHide.remove(secNode.getCombinedDiffNode());
          }
        } else if (!superNodesToShow.contains(secNode.getSuperDiffNode())) {
          superNodesToHide.add(secNode.getSuperDiffNode());
          combinedNodesToHide.add(secNode.getCombinedDiffNode());
        }
      }

      graph.getCombinedGraph().showNodes(combinedNodesToShow, combinedNodesToHide, false);
    }

    graph.getSuperGraph().showNodes(superNodesToShow, superNodesToHide, false);
  }

  public static void executeStatic(final BinDiffGraph<ZyGraphNode<?>, ?> graph)
      throws GraphLayoutException {
    final GraphProximityBrowsingSettings proxiSettings = graph.getSettings().getProximitySettings();
    if (proxiSettings.getProximityBrowsing()) {
      if (graph.getSettings().getDiffViewMode() != EDiffViewMode.COMBINED_VIEW) {
        ProximityBrowserUpdater.deleteAllProximityNodes(graph.getCombinedGraph());
      }

      synchronizeNodeSelection(graph);
      synchronizeNodeVisibility(graph);

      ProximityBrowserUpdater.adoptSuperGraphVisibility(graph.getSuperGraph());

      ProximityBrowserUpdater.deleteAllProximityNodes(graph.getPrimaryGraph());
      ProximityBrowserUpdater.deleteAllProximityNodes(graph.getSecondaryGraph());

      ProximityBrowserUpdater.createProximityNodesAndEdges(graph.getSuperGraph());
    } else {
      ProximityBrowserDeactivator.executeStatic(graph);
    }

    GraphsIntermediateListeners.notifyIntermediateVisibilityListeners(graph);

    if (LayoutCommandHelper.isAutolayout(graph)) {
      GraphLayoutUpdater.executeStatic(graph, false);
    }
  }
}
