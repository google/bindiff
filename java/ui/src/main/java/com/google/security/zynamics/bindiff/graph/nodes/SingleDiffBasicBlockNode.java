package com.google.security.zynamics.bindiff.graph.nodes;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.resources.Colors;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.CStyleRunData;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;

import y.base.Node;

import java.awt.Color;
import java.util.List;

public class SingleDiffBasicBlockNode extends SingleDiffNode {
  public SingleDiffBasicBlockNode(
      final Node node,
      final IZyNodeRealizer realizer,
      final SingleViewNode rawNode,
      final ESide side) {
    super(node, realizer, rawNode, side);
  }

  @Override
  protected void onSelectionChanged() {
    Color newColor = getSide() == ESide.PRIMARY ? Colors.PRIMARY_BASE : Colors.SECONDARY_BASE;

    if (isSelected()) {
      newColor = newColor.darker();
    }

    for (final ZyLineContent line : getRealizer().getNodeContent().getContent()) {
      final int lineLength = line.getText().length();
      if (lineLength > 0) {
        final List<CStyleRunData> styleRun = line.getBackgroundStyleRunData(0, lineLength - 1);
        for (final CStyleRunData data : styleRun) {
          final Color currentColor = data.getColor();

          if (currentColor != Colors.SEARCH_HIGHLIGHT_COLOR && currentColor != null) {
            if (lineLength == data.getEnd()) {
              line.setBackgroundColor(data.getStart(), data.getLength(), newColor);
            } else {
              line.setBackgroundColor(data.getStart(), data.getLength() - 1, newColor);
            }
          }
        }
      }
    }
  }
}
