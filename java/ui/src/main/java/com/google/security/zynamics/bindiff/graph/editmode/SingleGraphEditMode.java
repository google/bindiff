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

package com.google.security.zynamics.bindiff.graph.editmode;

import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.edges.SingleDiffEdge;
import com.google.security.zynamics.bindiff.graph.editmode.helpers.TooltipGenerationHelper;
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

/** EditMode that handles node/edge tooltip generation for single graphs (primary/secondary). */
public class SingleGraphEditMode extends EditMode<SingleDiffNode, SingleDiffEdge> {
  public SingleGraphEditMode(final BinDiffGraph<SingleDiffNode, SingleDiffEdge> graph) {
    super(graph);
  }

  @Override
  protected IStateActionFactory<SingleDiffNode, SingleDiffEdge> createStateActionFactory() {
    return new SingleGraphActionFactory<SingleDiffNode, SingleDiffEdge>();
  }

  @Override
  protected String getEdgeTip(final Edge edge) {
    final SingleGraph singleGraph = (SingleGraph) getGraph();

    if (CTooltipUpdater.isProximityNode(singleGraph, edge.source())) {
      return getNodeTip(edge.source());
    }

    if (CTooltipUpdater.isProximityNode(singleGraph, edge.target())) {
      return getNodeTip(edge.target());
    }

    final SingleDiffEdge diffEdge = singleGraph.getEdge(edge);
    final SingleDiffNode source = diffEdge.getSource();
    final SingleDiffNode target = diffEdge.getTarget();
    final ZyLabelContent sourceContent = source.getRealizer().getNodeContent();
    final ZyLabelContent targetContent = target.getRealizer().getNodeContent();

    return HtmlGenerator.getHtml(
        sourceContent, targetContent, Fonts.NORMAL_FONT.getName(), false, true);
  }

  @Override
  protected String getNodeTip(final Node node) {
    final SingleGraph singleGraph = (SingleGraph) getGraph();
    final ZyGraph2DView view = (ZyGraph2DView) singleGraph.getView();

    if (CTooltipUpdater.isProximityNode(singleGraph, node)) {
      final IZyNodeRealizer realizer = (IZyNodeRealizer) singleGraph.getGraph().getRealizer(node);
      return TooltipGenerationHelper.generateProximityNodeTooltip(
          (ZyProximityNode<?>) realizer.getUserData().getNode());
    }

    if (view.isNodeSloppyPaintMode()) {
      final SingleDiffNode diffNode = singleGraph.getNode(node);
      final ZyLabelContent nodeContent = diffNode.getRealizer().getNodeContent();

      return HtmlGenerator.getHtml(nodeContent, Fonts.NORMAL_FONT.getName(), false, true);
    }

    return null;
  }
}
