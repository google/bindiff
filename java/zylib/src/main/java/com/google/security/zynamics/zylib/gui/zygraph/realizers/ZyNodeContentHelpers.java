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

package com.google.security.zynamics.zylib.gui.zygraph.realizers;

import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent.ObjectWrapper;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;
import java.awt.geom.Rectangle2D;

public class ZyNodeContentHelpers {

  public static Object getObject(final ZyGraphNode<?> node, final double x, final double y) {
    final ObjectWrapper wrapper = getObjectWrapper(node, x, y);
    return wrapper == null ? null : wrapper.getObject();
  }

  public static ZyLineContent.ObjectWrapper getObjectWrapper(
      final ZyGraphNode<?> node, final double x, final double y) {
    final IZyNodeRealizer realizer = node.getRealizer();
    final ZyLabelContent content = realizer.getNodeContent();

    final Rectangle2D bounds = content.getBounds();

    final double xScale = realizer.getWidth() / bounds.getWidth();

    final double yPos = y - node.getY();

    final int row = node.positionToRow(yPos);

    if (row == -1) {
      return null;
    }

    final ZyLineContent lineContent = content.getLineContent(row);

    final double position =
        (((x - node.getX()) / xScale) - content.getPaddingLeft()) / lineContent.getCharWidth();
    return lineContent.getObjectWrapper((int) position);
  }
}
