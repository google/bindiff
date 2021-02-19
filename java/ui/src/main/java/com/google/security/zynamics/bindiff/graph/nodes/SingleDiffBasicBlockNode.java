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

package com.google.security.zynamics.bindiff.graph.nodes;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.resources.Colors;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.CStyleRunData;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;
import java.awt.Color;
import java.util.List;
import y.base.Node;

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

          if (currentColor != null && !Colors.SEARCH_HIGHLIGHT_COLOR.equals(currentColor)) {
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
