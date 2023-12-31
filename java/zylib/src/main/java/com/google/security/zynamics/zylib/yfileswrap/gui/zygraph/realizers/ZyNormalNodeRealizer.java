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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import y.view.LineType;
import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;

/**
 * Realizer class for default rectangular text nodes.
 *
 * @param <NodeType>
 */
public class ZyNormalNodeRealizer<NodeType extends ZyGraphNode<?>>
    extends ZyNodeRealizer<NodeType> {

  /** Content that is displayed in the realizer. */
  private final ZyLabelContent m_content;

  /**
   * Creates a new node realizer.
   *
   * @param content Content of the realizer.
   */
  public ZyNormalNodeRealizer(final ZyLabelContent content) {
    Preconditions.checkNotNull(content, "Error: Node content can't be null.");

    m_content = content;

    setShapeType(ShapeNodeRealizer.ROUND_RECT);
    setLineType(LineType.LINE_2);

    final Rectangle2D bounds = getNodeContent().getBounds();
    setSize(bounds.getWidth(), bounds.getHeight());
  }

  @Override
  public ZyLabelContent getNodeContent() {
    return m_content;
  }

  @Override
  public NodeRealizer getRealizer() {
    return this;
  }

  @Override
  public void paintHotSpots(final Graphics2D g) {
    return;
  }

  @Override
  public void paintNode(final Graphics2D gfx) {
    super.paintNode(gfx);

    final Rectangle2D contentBounds = getNodeContent().getBounds();
    final double xratio = getWidth() / contentBounds.getWidth();
    final double yratio = getHeight() / contentBounds.getHeight();

    gfx.scale(xratio, yratio);
    getNodeContent().draw(gfx, (getX() * 1) / xratio, (getY() * 1) / yratio);
    gfx.scale(1 / xratio, 1 / yratio);
  }

  @Override
  public String toString() {
    return m_content.toString();
  }
}
