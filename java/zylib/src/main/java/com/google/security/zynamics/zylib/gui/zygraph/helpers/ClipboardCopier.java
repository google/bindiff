// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.helpers;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.general.ClipboardHelpers;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;

/**
 * Provides methods that can be used to copy parts of a graph to the clip board.
 */
public final class ClipboardCopier {
  /**
   * Copies the text content of a node to the clip board.
   * 
   * @param node The node to copy to the clip board.
   */
  public static void copyToClipboard(final ZyGraphNode<?> node) {
    Preconditions.checkNotNull(node, "Error: Node argument can not be null");
    final IZyNodeRealizer realizer = node.getRealizer();
    final ZyLabelContent content = realizer.getNodeContent();

    if (content.isSelectable()) {
      final ZyLabelContent zyContent = content;

      final StringBuilder textBuilder = new StringBuilder();

      for (final ZyLineContent zyLineContent : zyContent) {
        textBuilder.append(zyLineContent.getText());
        textBuilder.append("\n"); //$NON-NLS-1$
      }

      ClipboardHelpers.copyToClipboard(textBuilder.toString());
    }
  }

  /**
   * Copies the text of a line of a node to the clip board.
   * 
   * @param node The node that contains the line.
   * @param line Index of the line to copy to the clip board.
   */
  public static void copyToClipboard(final ZyGraphNode<?> node, final int line) {
    Preconditions.checkNotNull(node, "Error: Node argument can not be null");
    final IZyNodeRealizer realizer = node.getRealizer();
    final ZyLabelContent content = realizer.getNodeContent();
    Preconditions.checkArgument((line >= 0) && (line < content.getLineCount()),
        "Error: Line argument is out of bounds");

    if (content.isSelectable()) {
      final ZyLabelContent zyContent = content;

      ClipboardHelpers.copyToClipboard(zyContent.getLineContent(line).getText());
    }
  }
}
