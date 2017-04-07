// Copyright 2011 Google Inc. All Rights Reserved.

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

  public static ZyLineContent.ObjectWrapper getObjectWrapper(final ZyGraphNode<?> node,
      final double x, final double y) {
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
