// Copyright 2011-2020 Google LLC
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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.ZyNodeData;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IRealizerUpdater;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyNodeRealizerListener;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import y.base.Edge;
import y.base.Node;
import y.geom.YPoint;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.LineType;
import y.view.Port;
import y.view.ShapeNodeRealizer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * Realizer that is used to display graph nodes.
 */
public abstract class ZyNodeRealizer<NodeType extends ZyGraphNode<?>> extends ShapeNodeRealizer
implements IZyNodeRealizer {
  /**
   * User-specific data that is associated with the realizer.
   */
  private ZyNodeData<?> m_userData;

  /**
   * Node updater class that is used to update the content of the realizer.
   */
  private IRealizerUpdater<?> m_updater;

  /**
   * Listeners that are notified about changes in the node realizer.
   */
  private final ListenerProvider<IZyNodeRealizerListener<?>> m_listeners =
      new ListenerProvider<IZyNodeRealizerListener<?>>();

  protected boolean m_isHighLighted = false;

  private void notifyLocationChanged(final double x, final double y) {
    for (final IZyNodeRealizerListener<?> listener : m_listeners) {
      try {
        listener.changedLocation(this, x, y);
      } catch (final Exception exception) {
        // TODO: Log this
      }
    }
  }

  protected void notifyHasRegenerated() {
    for (final IZyNodeRealizerListener<?> listener : m_listeners) {
      try {
        listener.regenerated(this);
      } catch (final Exception exception) {
        // TODO: Log this
      }
    }
  }

  protected void scalePortCoordinates(
      final Node node, final double wOld, final double wNew, final double hOld, final double hNew) {
    final Graph2D graph = (Graph2D) node.getGraph();
    final double wScaling = wOld > 0 ? wNew / wOld : 1.0d;
    final double hScaling = hOld > 0 ? hNew / hOld : 1.0d;

    for (Edge e = node.firstOutEdge(); e != null; e = e.nextOutEdge()) {
      final EdgeRealizer er = graph.getRealizer(e);
      final Port port = er.getSourcePort();
      final double px = port.getOffsetX() * wScaling;
      final double py = port.getOffsetY() * hScaling;
      port.setOffsets(px, py);
      graph.setSourcePointRel(e, new YPoint(px, py));
    }

    for (Edge e = node.firstInEdge(); e != null; e = e.nextInEdge()) {
      final EdgeRealizer er = graph.getRealizer(e);
      final Port port = er.getTargetPort();
      final double px = port.getOffsetX() * wScaling;
      final double py = port.getOffsetY() * hScaling;
      port.setOffsets(px, py);
      graph.setTargetPointRel(e, new YPoint(px, py));
    }
  }

  /**
   * Adds a listener that is notified about changes in the node realizer.
   *
   * @param listener The listener object to add.
   */
  @Override
  public void addListener(final IZyNodeRealizerListener<?> listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Returns the node content that is displayed by the realizer.
   *
   * @return The node content that is displayed by the realizer.
   */
  @Override
  public abstract ZyLabelContent getNodeContent();

  @Override
  public IRealizerUpdater<?> getUpdater() {
    return m_updater;
  }

  /**
   * Returns the user data associated with the realizer.
   *
   * @return The user data associated with the realizer.
   */
  @Override
  public ZyNodeData<?> getUserData() {
    return m_userData;
  }

  @Override
  public void moveBy(final double x, final double y) {
    super.moveBy(x, y);

    notifyLocationChanged(getX(), getY());
  }

  @Override
  public void paintSloppy(final Graphics2D gfx) {
    if (isSelected() || m_isHighLighted) {
      final LineType old = getLineType();
      setLineType(LineType.LINE_5);
      paintFilledShape(gfx);
      paintShapeBorder(gfx);
      setLineType(old);
    } else {
      paintFilledShape(gfx);
      paintShapeBorder(gfx);
    }
  }

  /**
   * Converts a y coordinate into a line number of the node content.
   *
   * @param y The y coordinate.
   * @return The number of the line shown at the y address or -1 if there is no line at the given
   *         coordinates.
   */
  @Override
  public int positionToRow(final double y) {
    // TODO: This does not really work because line heights are not constant.

    final ZyLabelContent content = getNodeContent();

    final Rectangle2D contentBounds = getNodeContent().getBounds();
    final double yratio = getHeight() / contentBounds.getHeight();

    final int row = (int) ((y / yratio - content.getPaddingTop()) / content.getLineHeight());

    return row >= content.getLineCount() ? -1 : row;
  }

  /**
   * Regenerates the content of the realizer.
   */
  @Override
  public void regenerate() {
    final ZyLabelContent content = getNodeContent();

    final double widthOld = content.getBounds().getWidth();
    final double heightOld = content.getBounds().getHeight();

    if (m_updater != null) {
      m_updater.generateContent(this, content);
    }

    final Rectangle2D bounds = content.getBounds();

    setSize(bounds.getWidth(), bounds.getHeight());

    scalePortCoordinates(getNode(), widthOld, bounds.getWidth(), heightOld, bounds.getHeight());

    notifyHasRegenerated();

    // TODO(jannewger): check if this method is redundant - it seems that client code often calls
    // regeneerate followe by a redraw operation. That would mean that we needlessly redraw two
    // times.
    repaint();
  }

  /**
   * Removes a listener object from the realizer.
   *
   * @param listener The listener object to be removed.
   */
  @Override
  public void removeListener(final IZyNodeRealizerListener<?> listener) {
    m_listeners.removeListener(listener);
  }

  @Override
  public double rowToPosition(final int row) {
    final ZyLabelContent content = getNodeContent();

    return content.getPaddingTop() + row * content.getLineHeight();
  }

  @Override
  public void setCenter(final double x, final double y) {
    super.setCenter(x, y);

    notifyLocationChanged(getX(), getY());
  }

  @Override
  public void setFillColor(final Color color) {
    if (super.getFillColor() != color) {
      super.setFillColor(color);
      updateContentSelectionColor();
    }
  }

  @Override
  public void setLineType(final LineType linetype) {
    m_isHighLighted = linetype == LineType.LINE_5 || linetype == LineType.DASHED_5
        || linetype == LineType.DOTTED_5 || linetype == LineType.DASHED_DOTTED_5;

    super.setLineType(linetype);
  }

  @Override
  public void setLocation(final double x, final double y) {
    super.setLocation(x, y);
    notifyLocationChanged(x, y);
  }

  @Override
  public void setSelected(final boolean selected) {
    if (super.isSelected() != selected) {
      super.setSelected(selected);

      updateContentSelectionColor();

      for (final IZyNodeRealizerListener<?> listener : m_listeners) {
        try {
          listener.changedSelection(this);
        } catch (final Exception exception) {
          // TODO: Log this
        }
      }
    }
  }

  @Override
  public void setSize(final double x, final double y) {
    if (super.getX() != x || super.getY() != y) {
      super.setSize(x, y);

      for (final IZyNodeRealizerListener<?> listener : m_listeners) {
        try {
          listener.changedSize(this, x, y);
        } catch (final Exception exception) {
          // TODO: Log this
        }
      }
    }
  }

  @Override
  public void setUpdater(final IRealizerUpdater<? extends ZyGraphNode<?>> updater) {
    m_updater = updater;

    if (updater != null) {
      updater.setRealizer(this);
    }
  }

  /**
   * Sets the user data associated with the realizer.
   *
   * @param data The new user data object.
   */
  @Override
  public void setUserData(final ZyNodeData<?> data) {
    Preconditions.checkNotNull(data, "Error: Invalid node data");

    m_userData = data;
  }

  @Override
  public void setVisible(final boolean visible) {
    if (super.isVisible() != visible) {
      super.setVisible(visible);

      updateContentSelectionColor();

      for (final IZyNodeRealizerListener<?> listener : m_listeners) {
        try {
          listener.changedVisibility(this);
        } catch (final Exception exception) {
          // TODO: Log this
        }
      }
    }
  }

  @Override
  public void updateContentSelectionColor() {
    // TODO: This is kind of a hack, should be improved
    final ZyLabelContent content = getNodeContent();

    if (content.isSelectable()) {
      content.updateContentSelectionColor(getFillColor(), isSelected());
    }
  }
}
