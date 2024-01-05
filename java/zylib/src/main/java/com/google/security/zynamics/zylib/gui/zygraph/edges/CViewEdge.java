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

package com.google.security.zynamics.zylib.gui.zygraph.edges;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class CViewEdge<NodeType> implements IViewEdge<NodeType> {
  private final NodeType m_sourceNode;
  private final NodeType m_targetNode;
  private EdgeType m_type;
  private double m_x1;
  private double m_y1;
  private double m_x2;
  private double m_y2;
  private Color m_color;
  private boolean m_visible;
  private boolean m_selected;
  private int m_id;

  private final List<CBend> m_paths;

  protected final ListenerProvider<IViewEdgeListener> m_listeners =
      new ListenerProvider<IViewEdgeListener>();

  public CViewEdge(
      final int id,
      final NodeType sourceNode,
      final NodeType targetNode,
      final EdgeType type,
      final double x1,
      final double y1,
      final double x2,
      final double y2,
      final Color color,
      final boolean selected,
      final boolean visible,
      final List<CBend> edgePaths) {
    m_sourceNode =
        Preconditions.checkNotNull(sourceNode, "Error: Source node argument can't be null");
    m_targetNode =
        Preconditions.checkNotNull(targetNode, "Error: Target node argument can't be null");

    m_id = id;
    m_type = type;
    m_x1 = x1;
    m_y1 = y1;
    m_x2 = x2;
    m_y2 = y2;
    m_color = color;
    m_visible = visible;
    m_selected = selected;
    m_paths = edgePaths;
  }

  @Override
  public void addBend(final double x, final double y) {
    final CBend path = new CBend(x, y);

    // if (m_paths.contains(path))
    // {
    // return;
    // }

    m_paths.add(path);

    for (final IViewEdgeListener listener : m_listeners) {
      try {
        listener.addedBend(this, path);
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  @Override
  public void addListener(final IViewEdgeListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public void clearBends() {
    m_paths.clear();

    for (final IViewEdgeListener listener : m_listeners) {
      try {
        listener.clearedBends(this);
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  @Override
  public int getBendCount() {
    return m_paths.size();
  }

  @Override
  public List<CBend> getBends() {
    return new ArrayList<CBend>(m_paths);
  }

  @Override
  public Color getColor() {
    return m_color;
  }

  @Override
  public int getId() {
    return m_id;
  }

  @Override
  public NodeType getSource() {
    return m_sourceNode;
  }

  @Override
  public NodeType getTarget() {
    return m_targetNode;
  }

  @Override
  public EdgeType getType() {
    return m_type;
  }

  @Override
  public double getX1() {
    return m_x1;
  }

  @Override
  public double getX2() {
    return m_x2;
  }

  @Override
  public double getY1() {
    return m_y1;
  }

  @Override
  public double getY2() {
    return m_y2;
  }

  @Override
  public void insertBend(final int index, final double x, final double y) {
    final CBend path = new CBend(x, y);

    // if (m_paths.contains(path))
    // {
    // return;
    // }

    m_paths.add(index, path);

    for (final IViewEdgeListener listener : m_listeners) {
      try {
        listener.insertedBend(this, index, path);
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
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
    final List<IViewEdgeListener> listeners = new ArrayList<IViewEdgeListener>();
    for (final IViewEdgeListener listener : m_listeners) {
      listeners.add(listener);
    }
    for (final IViewEdgeListener listener : listeners) {
      removeListener(listener);
    }
  }

  @Override
  public void removeBend(final int index) {
    final CBend path = m_paths.get(index);

    m_paths.remove(index);

    for (final IViewEdgeListener listener : m_listeners) {
      listener.removedBend(this, index, path);
    }
  }

  @Override
  public void removeListener(final IViewEdgeListener listener) {
    m_listeners.removeListener(listener);
  }

  @Override
  public void setColor(final Color color) {
    Preconditions.checkNotNull(color, "Error: Color argument can not be null");

    if (m_color.equals(color)) {
      return;
    }

    m_color = color;

    for (final IViewEdgeListener listener : m_listeners) {
      listener.changedColor(this, color);
    }
  }

  @Override
  public void setEdgeType(final EdgeType type) {
    Preconditions.checkNotNull(type, "Error: Type argument can't be null");

    if (type == m_type) {
      return;
    }

    m_type = type;

    for (final IViewEdgeListener listener : m_listeners) {
      listener.changedType(this, type);
    }
  }

  @Override
  public void setId(final int id) {
    m_id = id;
  }

  @Override
  public void setSelected(final boolean selected) {
    if (m_selected == selected) {
      return;
    }

    m_selected = selected;

    for (final IViewEdgeListener listener : m_listeners) {
      listener.changedSelection(this, selected);
    }
  }

  @Override
  public void setVisible(final boolean visible) {
    if (m_visible == visible) {
      return;
    }

    m_visible = visible;

    for (final IViewEdgeListener listener : m_listeners) {
      listener.changedVisibility(this, m_visible);
    }
  }

  @Override
  public void setX1(final double x1) {
    if (Double.compare(m_x1, x1) == 0) {
      return;
    }

    m_x1 = x1;

    for (final IViewEdgeListener listener : m_listeners) {
      listener.changedSourceX(this, x1);
    }
  }

  @Override
  public void setX2(final double x2) {
    if (Double.compare(m_x2, x2) == 0) {
      return;
    }

    m_x2 = x2;

    for (final IViewEdgeListener listener : m_listeners) {
      listener.changedTargetX(this, x2);
    }
  }

  @Override
  public void setY1(final double y1) {
    if (Double.compare(m_y1, y1) == 0) {
      return;
    }

    m_y1 = y1;

    for (final IViewEdgeListener listener : m_listeners) {
      listener.changedSourceY(this, y1);
    }
  }

  @Override
  public void setY2(final double y2) {
    if (Double.compare(m_y2, y2) == 0) {
      return;
    }

    m_y2 = y2;

    for (final IViewEdgeListener listener : m_listeners) {
      listener.changedTargetY(this, y2);
    }
  }
}
