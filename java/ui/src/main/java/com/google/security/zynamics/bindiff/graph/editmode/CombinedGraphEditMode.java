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

package com.google.security.zynamics.bindiff.graph.editmode;

import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.edges.CombinedDiffEdge;
import com.google.security.zynamics.bindiff.graph.editmode.helpers.TooltipGenerationHelper;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.resources.Fonts;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateActionFactory;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.HtmlGenerator;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.ZyGraph2DView;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CTooltipUpdater;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyProximityNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;
import y.base.Edge;
import y.base.Node;

/** EditMode for combined graphs that handles combined node/edge tooltip generation. */
public final class CombinedGraphEditMode extends EditMode<CombinedDiffNode, CombinedDiffEdge> {
  public CombinedGraphEditMode(final BinDiffGraph<CombinedDiffNode, CombinedDiffEdge> graph) {
    super(graph);
  }

  @Override
  protected IStateActionFactory<CombinedDiffNode, CombinedDiffEdge> createStateActionFactory() {
    return new CombinedGraphActionFactory<CombinedDiffNode, CombinedDiffEdge>();
  }

  @Override
  protected String getEdgeTip(final Edge edge) {
    final CombinedGraph combinedGraph = (CombinedGraph) getGraph();

    if (CTooltipUpdater.isProximityNode(combinedGraph, edge.source())) {
      return getNodeTip(edge.source());
    }

    if (CTooltipUpdater.isProximityNode(combinedGraph, edge.target())) {
      return getNodeTip(edge.target());
    }

    final CombinedDiffEdge diffEdge = combinedGraph.getEdge(edge);
    final CombinedDiffNode source = diffEdge.getSource();
    final CombinedDiffNode target = diffEdge.getTarget();

    return TooltipGenerationHelper.generateCombinedEdgeTooltips(
        Fonts.NORMAL_FONT.getName(), source, target);
  }

  @Override
  protected String getNodeTip(final Node node) {
    final CombinedGraph combinedGraph = (CombinedGraph) getGraph();
    final ZyGraph2DView view = (ZyGraph2DView) combinedGraph.getView();

    if (CTooltipUpdater.isProximityNode(combinedGraph, node)) {
      final IZyNodeRealizer realizer = (IZyNodeRealizer) combinedGraph.getGraph().getRealizer(node);
      return TooltipGenerationHelper.generateProximityNodeTooltip(
          Fonts.NORMAL_FONT.getName(), (ZyProximityNode<?>) realizer.getUserData().getNode());
    }

    if (view.isNodeSloppyPaintMode()) {
      final SingleDiffNode priDiffNode = combinedGraph.getNode(node).getPrimaryDiffNode();
      final SingleDiffNode secDiffNode = combinedGraph.getNode(node).getSecondaryDiffNode();

      if (secDiffNode == null) {
        final ZyLabelContent nodeContent = priDiffNode.getRealizer().getNodeContent();
        return HtmlGenerator.getHtml(nodeContent, Fonts.NORMAL_FONT.getName(), false, true);
      }
      if (priDiffNode == null) {
        final ZyLabelContent nodeContent = secDiffNode.getRealizer().getNodeContent();
        return HtmlGenerator.getHtml(nodeContent, Fonts.NORMAL_FONT.getName(), false, true);
      }
      return TooltipGenerationHelper.generateCombinedNodeTooltip(
          Fonts.NORMAL_FONT.getName(), priDiffNode, secDiffNode, 0, 0);
    }
    return null;
  }
}
