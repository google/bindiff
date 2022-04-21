// Copyright 2011-2022 Google LLC
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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges;

import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.zygraph.edges.CBend;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;
import com.google.security.zynamics.zylib.gui.zygraph.edges.IViewEdge;
import com.google.security.zynamics.zylib.gui.zygraph.edges.IViewEdgeListener;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;

import y.base.Edge;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ZyInfoEdge extends ZyGraphEdge<ZyGraphNode<?>, ZyInfoEdge, IViewEdge<?>> {
  public ZyInfoEdge(final ZyGraphNode<?> source, final ZyGraphNode<?> target, final Edge edge,
      final ZyEdgeRealizer<ZyInfoEdge> r) {
    super(source, target, edge, r, new CInfoEdge());
  }

  private static class CInfoEdge implements IViewEdge<Object> {
    private final List<CBend> m_bends = new ArrayList<CBend>();

    private final ListenerProvider<IViewEdgeListener> m_listeners =
        new ListenerProvider<IViewEdgeListener>();

    @Override
    public void addBend(final double x, final double y) {
      final CBend path = new CBend(x, y);

      // if (m_paths.contains(path))
      // {
      // return;
      // }

      m_bends.add(path);

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
      m_bends.clear();

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
      return m_bends.size();
    }

    @Override
    public List<CBend> getBends() {
      return new ArrayList<CBend>(m_bends);
    }

    @Override
    public Color getColor() {
      return Color.BLACK;
    }

    @Override
    public int getId() {
      return 0;
    }

    @Override
    public Object getSource() {
      return null;
    }

    @Override
    public Object getTarget() {
      return null;
    }

    @Override
    public EdgeType getType() {
      return EdgeType.TEXTNODE_EDGE;
    }

    @Override
    public double getX1() {
      return 0;
    }

    @Override
    public double getX2() {
      return 0;
    }

    @Override
    public double getY1() {
      return 0;
    }

    @Override
    public double getY2() {
      return 0;
    }

    @Override
    public void insertBend(final int index, final double x, final double y) {
      final CBend path = new CBend(x, y);

      m_bends.add(index, path);

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
      return false;
    }

    @Override
    public boolean isVisible() {
      return true;
    }

    @Override
    public void removeBend(final int index) {
      m_bends.remove(index);
    }

    @Override
    public void removeListener(final IViewEdgeListener listener) {
      m_listeners.removeListener(listener);
    }

    @Override
    public void setColor(final Color color) {
      // Ignore this
    }

    @Override
    public void setEdgeType(final EdgeType type) {
      // Ignore this
    }

    @Override
    public void setId(final int id) {
    }

    @Override
    public void setSelected(final boolean selected) {
      // Ignore this
    }

    @Override
    public void setVisible(final boolean visible) {
      // Ignore this
    }

    @Override
    public void setX1(final double x1) {
      // Ignore this
    }

    @Override
    public void setX2(final double x2) {
      // Ignore this
    }

    @Override
    public void setY1(final double y1) {
      // Ignore this
    }

    @Override
    public void setY2(final double y2) {
      // Ignore this
    }
  }
}
