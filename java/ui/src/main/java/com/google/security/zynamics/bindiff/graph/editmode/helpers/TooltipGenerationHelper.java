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

package com.google.security.zynamics.bindiff.graph.editmode.helpers;

import com.google.common.base.Strings;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCombinedFunction;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedBasicBlock;
import com.google.security.zynamics.bindiff.resources.Fonts;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.HtmlGenerator;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;
import com.google.security.zynamics.zylib.types.graphs.IGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyProximityNode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TooltipGenerationHelper {
  private static final int NODE_TOOLTIP_MAX_LINES = 25;

  private static final String MISSING_STRING = "missing";

  private static String createTwoSidedContent(
      final String fontname, final String leftContent, final String rightContent) {
    final StringBuilder html =
        new StringBuilder(
            String.format("<html><font face=\"%s\" size=\"3\" color=\"000000\">", fontname));

    html.append("<table cellspacing=\"5\" border=\"0\" cellpadding=\"0\">");
    html.append("<tr valign=\"middle\" align=\"left\">");

    html.append("<td>");
    html.append(leftContent);
    html.append("</td>");

    html.append("<td width=\"1\" bgcolor=\"#000000\"><br></td>");

    html.append("<td>");
    html.append(rightContent);
    html.append("</td>");

    html.append("</tr>");
    html.append("</table>");

    html.append("</font></html>");

    return html.toString();
  }

  private static String getHtml(
      final ZyLabelContent content, final String fontname, final int maxLineLength) {
    final StringBuilder html =
        new StringBuilder(
            String.format("<html><font face=\"%s\" size=\"3\" color=\"000000\">", fontname));

    final int tooltipMaxLines = Math.min(content.getLineCount(), NODE_TOOLTIP_MAX_LINES);

    for (int i = 0; i < tooltipMaxLines; ++i) {
      String text = content.getLineContent(i).getText();
      if (i == 0 && text.length() < maxLineLength) {
        for (int j = text.length(); j < maxLineLength; ++j) {
          text += " ";
        }
      }

      html.append(HtmlGenerator.escapeHtml(text));
      html.append("<br>");
    }

    if (content.getLineCount() > NODE_TOOLTIP_MAX_LINES) {
      html.append("...");
      html.append("<br>");
    }

    html.append("</font></html>");

    return html.toString();
  }

  private static int getMaxLineCharacterCount(final SingleDiffNode diffNode) {
    if (diffNode == null) {
      return MISSING_STRING.length();
    }

    int counter = 0;
    int max = 0;
    for (final ZyLineContent line : diffNode.getRealizer().getNodeContent()) {
      max = Math.max(max, line.getText().length());
      if (++counter > NODE_TOOLTIP_MAX_LINES) {
        return max;
      }
    }

    return max;
  }

  public static String generateCombinedEdgeTooltips(
      final String fontname, final CombinedDiffNode source, final CombinedDiffNode target) {
    final StringBuilder html = new StringBuilder();

    final SingleDiffNode priSource = source.getPrimaryDiffNode();
    final SingleDiffNode secSource = source.getSecondaryDiffNode();

    final SingleDiffNode priTarget = target.getPrimaryDiffNode();
    final SingleDiffNode secTarget = target.getSecondaryDiffNode();

    int maxLeftLineLength = getMaxLineCharacterCount(priSource);
    maxLeftLineLength = Math.max(maxLeftLineLength, getMaxLineCharacterCount(priTarget));

    int maxRightLineLength = getMaxLineCharacterCount(secSource);
    maxRightLineLength = Math.max(maxRightLineLength, getMaxLineCharacterCount(secTarget));

    String priNodesHtml =
        generateCombinedNodeTooltip(
            fontname, priSource, secSource, maxLeftLineLength, maxRightLineLength);
    priNodesHtml = priNodesHtml.replace("</html>", "");

    String secNodesHtml =
        generateCombinedNodeTooltip(
            fontname, priTarget, secTarget, maxLeftLineLength, maxRightLineLength);
    secNodesHtml = secNodesHtml.replace("<html>", "");

    html.append(priNodesHtml);
    html.append("<hr></hr>");
    html.append(secNodesHtml);

    return html.toString();
  }

  public static String generateCombinedNodeTooltip(
      final String fontname,
      final SingleDiffNode priDiffNode,
      final SingleDiffNode secDiffNode,
      final int maxLeftLineLength,
      final int maxRightLineLength) {
    String priCellContent = Strings.padEnd(MISSING_STRING, maxLeftLineLength + 1, ' ');
    String secCellContent = Strings.padEnd(MISSING_STRING, maxRightLineLength + 1, ' ');

    if (priDiffNode != null) {
      final ZyLabelContent priNodeContent = priDiffNode.getRealizer().getNodeContent();
      priCellContent = getHtml(priNodeContent, Fonts.NORMAL_FONT.getName(), maxLeftLineLength);
      priCellContent = priCellContent.replace("<html>", "");
      priCellContent = priCellContent.replace("</html>", "");
    } else {
      final List<String> missingText = new ArrayList<>();
      missingText.add(priCellContent);
      priCellContent = HtmlGenerator.getHtml(missingText, Fonts.NORMAL_FONT.getName(), false);
    }

    if (secDiffNode != null) {
      final ZyLabelContent secNodeContent = secDiffNode.getRealizer().getNodeContent();
      secCellContent = getHtml(secNodeContent, Fonts.NORMAL_FONT.getName(), maxRightLineLength);
      secCellContent = secCellContent.replace("<html>", "");
      secCellContent = secCellContent.replace("</html>", "");
    } else {
      final List<String> missingText = new ArrayList<>();
      missingText.add(secCellContent);
      secCellContent = HtmlGenerator.getHtml(missingText, Fonts.NORMAL_FONT.getName(), false);
    }

    return createTwoSidedContent(fontname, priCellContent, secCellContent);
  }

  public static <NodeType extends ZyGraphNode<?>> String generateProximityNodeTooltip(
      final ZyProximityNode<?> node) {
    final Set<String> strings = new LinkedHashSet<>();

    final List<? extends Object> nodes =
        node.isIncoming()
            ? ((IGraphNode<?>) node.getRawNode().getAttachedNode()).getChildren()
            : ((IGraphNode<?>) node.getRawNode().getAttachedNode()).getParents();

    boolean cutoff = false;
    int counter = 0;

    for (final Object child : nodes) {
      final IViewNode<?> childNode = (IViewNode<?>) child;

      if (childNode.isVisible()) {
        continue;
      }

      if (cutoff) {
        counter = 0;
        break;
      }

      if (child instanceof RawFunction) {
        final RawFunction function = (RawFunction) child;

        strings.add(
            String.format("%s  %s", function.getAddress().toHexString(), function.getName()));
      } else if (child instanceof RawBasicBlock) {
        final RawBasicBlock basicblock = (RawBasicBlock) child;

        strings.add(basicblock.getAddress().toHexString());
      }

      ++counter;

      if (counter == NODE_TOOLTIP_MAX_LINES) {
        cutoff = true;
      }
    }

    if (cutoff && counter == 0) {
      strings.add("...");
    }

    return HtmlGenerator.getHtml(strings, GuiHelper.getMonospacedFont().getFontName(), false);
  }

  public static <NodeType extends ZyGraphNode<?>> String generateProximityNodeTooltip(
      final String fontname, final ZyProximityNode<?> node) {
    final List<? extends Object> nodes =
        node.isIncoming()
            ? ((IGraphNode<?>) node.getRawNode().getAttachedNode()).getChildren()
            : ((IGraphNode<?>) node.getRawNode().getAttachedNode()).getParents();

    boolean cutoff = false;
    int counter = 0;

    final StringBuffer priCellContent = new StringBuffer();
    final StringBuffer secCellContent = new StringBuffer();

    for (final Object child : nodes) {
      final IViewNode<?> childNode = (IViewNode<?>) child;

      if (childNode.isVisible()) {
        continue;
      }

      if (cutoff) {
        counter = 0;
        break;
      }

      if (child instanceof RawCombinedFunction) {
        final RawCombinedFunction function = (RawCombinedFunction) child;

        final RawFunction priFunction = function.getRawNode(ESide.PRIMARY);
        final String priLine =
            priFunction == null
                ? MISSING_STRING
                : String.format(
                    "%s  %s",
                    priFunction.getAddress().toHexString(),
                    HtmlGenerator.escapeHtml(priFunction.getName()));
        priCellContent.append(priLine);

        final RawFunction secFunction = function.getRawNode(ESide.SECONDARY);
        final String secLine =
            secFunction == null
                ? MISSING_STRING
                : String.format(
                    "%s  %s",
                    secFunction.getAddress().toHexString(),
                    HtmlGenerator.escapeHtml(secFunction.getName()));
        secCellContent.append(secLine);
      } else if (child instanceof RawCombinedBasicBlock) {
        final RawCombinedBasicBlock basicblock = (RawCombinedBasicBlock) child;

        final RawBasicBlock priBasicblock = basicblock.getRawNode(ESide.PRIMARY);
        priCellContent.append(
            priBasicblock == null ? MISSING_STRING : priBasicblock.getAddress().toHexString());
        final RawBasicBlock secBasicblock = basicblock.getRawNode(ESide.SECONDARY);
        secCellContent.append(
            secBasicblock == null ? MISSING_STRING : secBasicblock.getAddress().toHexString());
      } else {
        continue;
      }

      priCellContent.append("<br>");
      secCellContent.append("<br>");

      ++counter;

      if (counter == NODE_TOOLTIP_MAX_LINES) {
        cutoff = true;
      }
    }

    if (cutoff && counter == 0) {
      priCellContent.append("...");
      secCellContent.append("...");
    }

    return createTwoSidedContent(fontname, priCellContent.toString(), secCellContent.toString());
  }
}
