// Copyright 2011-2023 Google LLC
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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers;

import y.base.Node;
import y.view.Graph2D;
import y.view.LineType;
import y.view.NodeRealizer;

/** Helper class for highlighting the border of nodes. */
public final class CNodeHighlighter {
  /**
   * Highlights the border of a node.
   *
   * @param node The node to highlight.
   * @param state True, to highlight the node. False, to unhighlight it.
   */
  public static void highlightNode(final Node node, final boolean state) {
    final NodeRealizer r = ((Graph2D) node.getGraph()).getRealizer(node);

    if (r == null) {
      return;
    }

    if (state) {
      if (r.getLineType() == LineType.LINE_2) {
        r.setLineType(LineType.LINE_5);
      }
    } else {
      if (r.getLineType() == LineType.LINE_5) {
        r.setLineType(LineType.LINE_2);
      }
    }
  }
}
