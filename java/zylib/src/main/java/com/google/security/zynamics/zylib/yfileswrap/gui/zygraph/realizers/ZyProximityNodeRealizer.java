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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IRealizerUpdater;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyNodeRealizerListener;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyProximityNode;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import y.view.LineType;
import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;

/** Realizer class for circular proximity browsing nodes. */
public class ZyProximityNodeRealizer<RawNodeType extends IViewNode<?>>
    extends ZyNodeRealizer<ZyProximityNode<RawNodeType>> {
  private static Color m_backGroundColor = new Color(250, 250, 255);

  /** Content that is displayed in the realizer. */
  private final ZyLabelContent m_content;

  /**
   * Creates a new node realizer.
   *
   * @param content Content of the realizer.
   */
  public ZyProximityNodeRealizer(final ZyLabelContent content) {
    Preconditions.checkNotNull(content, "Error: Node content can't be null.");

    m_content = content;

    setShapeType(ShapeNodeRealizer.ELLIPSE);

    setLineType(LineType.LINE_2);

    setFillColor(m_backGroundColor);
    setSloppySelectionColor(m_backGroundColor);

    final Rectangle2D bounds = getNodeContent().getBounds();

    final double diameter = Math.max(bounds.getWidth(), bounds.getHeight());

    setSize(diameter, diameter);
  }

  @Override
  protected void paintFilledShape(final Graphics2D gfx) {
    gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.66f));
    super.paintFilledShape(gfx);
    gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.f));
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
  public void paintSloppy(final Graphics2D gfx) {
    super.paintFilledShape(gfx);
    super.paintShapeBorder(gfx);
  }

  @Override
  public void addListener(final IZyNodeRealizerListener<? extends ZyGraphNode<?>> listener) {
    // Do nothing
  }

  @Override
  public void removeListener(final IZyNodeRealizerListener<? extends ZyGraphNode<?>> listener) {
    // Do nothing
  }

  @Override
  public void setSelected(final boolean value) {
    return;
  }

  @Override
  public void setUpdater(final IRealizerUpdater<? extends ZyGraphNode<?>> updater) {
    // Do nothing
  }
}
