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

package com.google.security.zynamics.zylib.gui.zygraph.nodes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.zygraph.edges.IViewEdge;

/**
 * Abstract base class for all nodes that are part of views. For most of its lifetime, this object
 * is a proxy object that forwards everything to the associated ZyGraphNode.
 */
public abstract class CViewNode<EdgeType extends IViewEdge<? extends IViewNode<?>>> implements
    IViewNode<EdgeType> {
  /**
   * ID of the node.
   */
  private int m_id;

  /**
   * X position of the node in the view.
   */
  private double m_x;

  /**
   * Y position of the node in the view.
   */
  private double m_y;

  /**
   * Background color of the node.
   */
  private Color m_color;

  private Color m_borderColor;

  /**
   * Indicates whether the node is selected or not.
   */
  private boolean m_selected;

  /**
   * Indicates whether the view is visible or not.
   */
  private boolean m_visible;

  /**
   * The incoming edges of the node.
   */
  private final List<EdgeType> m_incomingEdges = new ArrayList<EdgeType>();

  /**
   * The outgoing edges of the node.
   */
  private final List<EdgeType> m_outgoingEdges = new ArrayList<EdgeType>();

  private final ListenerProvider<IViewNodeListener> m_listeners =
      new ListenerProvider<IViewNodeListener>();

  private double m_height;

  private double m_width;

  /**
   * Creates a new view node object.
   * 
   * @param id The ID of the node.
   * @param x The X position of the node in the view.
   * @param y The Y position of the node in the view.
   * @param color The background color of the node.
   * @param selected Selection state of the node.
   * @param visible Visibility state of the node.
   */
  public CViewNode(final int id, final double x, final double y, final double width,
      final double height, final Color color, final Color borderColor, final boolean selected,
      final boolean visible) {
    Preconditions.checkArgument(id >= -1,
        "Error: Node ID must be positive or -1 for unsaved nodes.");

    // X and Y do not need to be bounds-checked.

    Preconditions.checkNotNull(color, "Error: Color argument can't be null");
    Preconditions.checkNotNull(borderColor, "Error: Border color argument can't be null");

    m_id = id;
    m_x = x;
    m_y = y;
    m_height = height;
    m_width = width;
    m_color = color;
    m_borderColor = borderColor;
    m_selected = selected;
    m_visible = visible;
  }

  protected ListenerProvider<IViewNodeListener> getListeners() {
    return m_listeners;
  }

  /**
   * Adds an incoming edge to the node.
   * 
   * @param edge The new incoming edge.
   */
  public void addIncomingEdge(final EdgeType edge) {
    Preconditions.checkNotNull(edge, "Error: Edge argument can't be null");

    m_incomingEdges.add(edge);
  }

  @Override
  public void addListener(final IViewNodeListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Adds an outgoing edge to the node.
   * 
   * @param edge The new outgoing edge.
   */
  public void addOutgoingEdge(final EdgeType edge) {
    Preconditions.checkNotNull(edge, "Error: Edge argument can not be null");
    Preconditions.checkArgument(!m_outgoingEdges.contains(edge),
        "Error: Outgoing edge was added before");
    m_outgoingEdges.add(edge);
  }

  @Override
  public Color getBorderColor() {
    return new Color(m_borderColor.getRGB());
  }

  @Override
  public Color getColor() {
    return new Color(m_color.getRGB());
  }

  @Override
  public double getHeight() {
    return m_height;
  }

  @Override
  public int getId() {
    return m_id;
  }

  @Override
  public List<EdgeType> getIncomingEdges() {
    return new ArrayList<EdgeType>(m_incomingEdges);
  }

  @Override
  public List<EdgeType> getOutgoingEdges() {
    return new ArrayList<EdgeType>(m_outgoingEdges);
  }

  @Override
  public double getWidth() {
    return m_width;
  }

  @Override
  public double getX() {
    return m_x;
  }

  @Override
  public double getY() {
    return m_y;
  }

  @Override
  public boolean isSelected() {
    return m_selected;
  }

  @Override
  public boolean isVisible() {
    return m_visible;
  }

  public void removeAllListeners() {
    final List<IViewNodeListener> listeners = new ArrayList<IViewNodeListener>();

    for (final IViewNodeListener listener : m_listeners) {
      listeners.add(listener);
    }

    for (final IViewNodeListener listener : listeners) {
      m_listeners.removeListener(listener);
    }
  }

  public void removeIncomingEdge(final EdgeType edge) {
    Preconditions.checkNotNull(edge, "Error: Edge argument can not be null");
    m_incomingEdges.remove(edge);
  }

  @Override
  public void removeListener(final IViewNodeListener listener) {
    Preconditions.checkNotNull(listener, "Error: Listener argument can not be null");
    m_listeners.removeListener(listener);
  }

  public void removeOutgoingEdge(final EdgeType edge) {
    Preconditions.checkNotNull(edge, "Error: Edge argument can not be null");
    m_outgoingEdges.remove(edge);
  }

  @Override
  public void setBorderColor(final Color color) {
    Preconditions.checkNotNull(color, "Error: Color argument can not be null");

    if (m_borderColor.equals(color)) {
      return;
    }

    m_borderColor = color;

    for (final IViewNodeListener listener : m_listeners) {
      try {
        listener.changedBorderColor(this, color);
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  @Override
  public void setColor(final Color color) {
    Preconditions.checkNotNull(color, "Error: Color argument can not be null");

    if (m_color.equals(color)) {
      return;
    }

    m_color = color;

    for (final IViewNodeListener listener : m_listeners) {
      try {
        listener.changedColor(this, color);
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  @Override
  public void setHeight(final double height) {
    if (m_height == height) {
      return;
    }

    m_height = height;

    for (final IViewNodeListener listener : m_listeners) {
      try {
        listener.heightChanged(this, height);
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  @Override
  public void setId(final int id) {
    m_id = id;
  }

  @Override
  public void setSelected(final boolean selected) {
    if (selected == m_selected) {
      return;
    }

    m_selected = selected;

    for (final IViewNodeListener listener : m_listeners) {
      // ESCA-JAVA0166: Catch Exception because we are calling a listener function.
      try {
        listener.changedSelection(this, selected);
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  @Override
  public void setVisible(final boolean visible) {
    if (visible == m_visible) {
      return;
    }

    m_visible = visible;

    for (final IViewNodeListener listener : m_listeners) {
      try {
        listener.changedVisibility(this, isVisible());
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  @Override
  public void setWidth(final double width) {
    if (m_width == width) {
      return;
    }

    m_width = width;

    for (final IViewNodeListener listener : m_listeners) {
      try {
        listener.widthChanged(this, width);
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  @Override
  public void setX(final double xpos) {
    if (m_x == xpos) {
      return;
    }

    m_x = xpos;

    for (final IViewNodeListener listener : m_listeners) {
      try {
        listener.xposChanged(this, xpos);
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  @Override
  public void setY(final double ypos) {
    if (m_y == ypos) {
      return;
    }

    m_y = ypos;

    for (final IViewNodeListener listener : m_listeners) {
      try {
        listener.yposChanged(this, ypos);
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }
}
