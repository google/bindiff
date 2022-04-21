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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.helpers;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.HtmlGenerator;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.ICodeNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IFunctionNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;
import com.google.security.zynamics.zylib.types.graphs.IGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyProximityNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeLabel;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyProximityNodeRealizer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import y.base.Edge;
import y.base.Node;

/**
 * This class is used to generate the tooltips of nodes and edges of a graph.
 */
public final class TooltipGenerator {
  private static <NodeType extends ZyGraphNode<?>> String generateProximityNodeRealizer(
      final AbstractZyGraph<NodeType, ?> graph, final ZyProximityNode<?> node) {
    final Set<String> strings = new LinkedHashSet<String>();

    final List<? extends Object> nodes =
        node.isIncoming() ? ((IGraphNode<?>) node.getRawNode().getAttachedNode()).getChildren()
            : ((IGraphNode<?>) node.getRawNode().getAttachedNode()).getParents();

    boolean cutoff = false;

    int counter = 0;

    final int invisibleNodesCount =
        CollectionHelpers.countIf(nodes, new ICollectionFilter<Object>() {
          @Override
          public boolean qualifies(final Object item) {
            return !((IViewNode<?>) item).isVisible();
          }
        });

    for (final Object child : nodes) {
      final IViewNode<?> childNode = (IViewNode<?>) child;

      if (childNode.isVisible()) {
        continue;
      }

      final IZyNodeRealizer realizer =
          (IZyNodeRealizer) graph.getGraph().getRealizer(graph.getYNode(child));

      final ZyLabelContent content = realizer.getNodeContent();

      if (child instanceof IFunctionNode<?, ?>) {
        final IFunctionNode<?, ?> fnode = (IFunctionNode<?, ?>) child;

        strings.add("F: " + fnode.getFunction().getName());
      } else if (child instanceof ICodeNode<?, ?, ?>) {
        final ICodeNode<?, ?, ?> cnode = (ICodeNode<?, ?, ?>) child;

        strings.add("B: " + cnode.getAddress().toHexString());
      } else {
        if (content.getLineCount() > 0) {
          strings.add(content.getLineContent(0).getText());
        }
      }

      ++counter;

      if (strings.size() == 25) {
        cutoff = counter != invisibleNodesCount;
        break;
      }
    }

    if (cutoff) {
      strings.add("...");
    }

    return HtmlGenerator.getHtml(strings, GuiHelper.getMonospacedFont().getFontName(), false);
  }

  /**
   * Determines whether the first line of a tooltip should be displayed in bold-face or not.
   * 
   * @param <NodeType> Type of the node.
   * 
   * @param node The node the generated tooltip belongs to.
   * 
   * @return True, if the first line should be printed in bold-face. False, otherwise.
   */
  private static <NodeType extends ZyGraphNode<?>> boolean requiresBoldFirstLine(final NodeType node) {
    return (node != null)
        && ((node.getRawNode() instanceof ICodeNode) || (node.getRawNode() instanceof IFunctionNode));
  }

  /**
   * Creates the tooltip for an edge in the graph.
   * 
   * @param <NodeType> Type of the nodes in the graph.
   * 
   * @param graph The graph the edge belongs to.
   * @param edge The edge the tooltip is created for.
   * 
   * @return The generated tooltip text for the edge.
   */
  public static <NodeType extends ZyGraphNode<?>> String createTooltip(
      final AbstractZyGraph<NodeType, ?> graph, final Edge edge) {
    Preconditions.checkNotNull(graph, "Error: Graph argument can not be null");
    Preconditions.checkNotNull(edge, "Error: Edge argument can not be null");

    // The generated tooltip has the following form:
    //
    // 1. Content of the source node
    // 2. Content of the edge label
    // 3. Content of the target node

    String text = createTooltip(graph, edge.source()).replace("</html>", "");

    final ZyEdgeRealizer<?> realizer = (ZyEdgeRealizer<?>) graph.getGraph().getRealizer(edge);

    if (realizer.labelCount() > 0) {
      final ZyEdgeLabel edgeLabel = (ZyEdgeLabel) realizer.getLabel();
      final ZyLabelContent content = edgeLabel.getLabelContent();

      text +=
          "<hr>"
              + HtmlGenerator.getHtml(content, GuiHelper.getMonospacedFont().getFontName(), false)
                  .replace("<html>", "")
                  .replace("</html>", "");
    }

    text += "<hr>" + createTooltip(graph, edge.target()).replace("<html>", "");

    return text;
  }

  /**
   * Creates the tooltip for a node in a graph.
   *
   * @param <NodeType> Type of the nodes in the graph.
   * @param graph The graph the node belongs to.
   * @param node The node the tooltip is created for.
   * @return The created tooltip text.
   */
  public static <NodeType extends ZyGraphNode<?>> String createTooltip(
      final AbstractZyGraph<NodeType, ?> graph, final Node node) {
    Preconditions.checkNotNull(graph, "Error: Graph argument can not be null");
    Preconditions.checkNotNull(node, "Error: Node argument can not be null");

    final IZyNodeRealizer realizer = (IZyNodeRealizer) graph.getGraph().getRealizer(node);

    if (realizer instanceof ZyProximityNodeRealizer<?>) {
      return generateProximityNodeRealizer(
          graph, (ZyProximityNode<?>) realizer.getUserData().getNode());
    }
    final ZyLabelContent content = realizer.getNodeContent();
    final boolean boldFirstLine = requiresBoldFirstLine(graph.getNode(node));

    return HtmlGenerator.getHtml(
        content, GuiHelper.getMonospacedFont().getFontName(), boldFirstLine);
  }
}
