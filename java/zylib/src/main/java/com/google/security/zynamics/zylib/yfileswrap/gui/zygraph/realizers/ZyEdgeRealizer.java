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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.zygraph.edges.ZyEdgeData;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IEdgeRealizerUpdater;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyEdgeRealizer;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyEdgeRealizerListener;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;

import y.base.Graph;
import y.geom.YPoint;
import y.view.Arrow;
import y.view.Bend;
import y.view.LineType;
import y.view.PolyLineEdgeRealizer;

import java.awt.Color;
import java.awt.Graphics2D;

public class ZyEdgeRealizer<EdgeType> extends PolyLineEdgeRealizer implements IZyEdgeRealizer {
  private ZyEdgeData<EdgeType> m_edgeData;

  private IEdgeRealizerUpdater<EdgeType> m_updater;

  private boolean m_drawSloppyEdges = true;

  private boolean m_drawBends = false;

  private boolean m_isHighlighted = false;

  private boolean m_isSloppyPainting = false;

  private final ListenerProvider<IZyEdgeRealizerListener<EdgeType>> m_listeners =
      new ListenerProvider<IZyEdgeRealizerListener<EdgeType>>();

  private ZyLabelContent m_content;


  public ZyEdgeRealizer(final ZyLabelContent content, final IEdgeRealizerUpdater<EdgeType> updater) {
    Preconditions.checkNotNull(content, "Internal Error: Content cannot be null");

    m_content = content;

    setArrow(Arrow.STANDARD);
    setSmoothedBends(true);

    if (content.getLineCount() > 0) {
      addLabel(new ZyEdgeLabel(content));
    }

    setUpdater(updater);
  }

  private void notifyLocationChanged() {
    for (final IZyEdgeRealizerListener<EdgeType> listener : m_listeners) {
      try {
        listener.changedLocation(this);
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  @Override
  protected void paintHighlightedBends(final Graphics2D gfx) {
    if (m_drawBends && !m_isSloppyPainting) {
      super.paintHighlightedBends(gfx);
    }
  }

  public void addListener(final IZyEdgeRealizerListener<EdgeType> listener) {
    m_listeners.addListener(listener);
  }

  public void addSilent(final double x, final double y) {
    super.appendBend(x, y);
  }

  @Override
  public Bend appendBend(final double x, final double y) {
    final Bend bend = super.appendBend(x, y);
    bend.setSelected(isSelected());

    return bend;
  }

  @Override
  public void bendChanged(final Bend bend, final double x, final double y) {
    final int index = bendPos(bend);

    super.bendChanged(bend, x, y);

    for (final IZyEdgeRealizerListener<EdgeType> listener : m_listeners) {
      try {
        listener.bendChanged(index, bend.getX(), bend.getY());
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  @Override
  public void clearBends() {
    super.clearBends();

    for (final IZyEdgeRealizerListener<EdgeType> listener : m_listeners) {
      try {
        listener.clearedBends();
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  public ZyLabelContent getEdgeLabelContent() {
    return m_content;
  }

  public ZyEdgeData<EdgeType> getUserData() {
    return m_edgeData;
  }

  @Override
  public void paint(final Graphics2D gfx) {
    m_content.setSloppy(false);
    m_isSloppyPainting = false;
    super.paint(gfx);
  }

  @Override
  public void paintBends(final Graphics2D gfx) {
    return;
  }

  @Override
  public void paintPorts(final Graphics2D gfx) {
    return;
  }

  @Override
  public void paintSloppy(final Graphics2D gfx) {
    // A sloppy edge draws twice as fast as a non-sloppy edge
    m_isSloppyPainting = true;

    m_content.setSloppy(true);

    if (!m_drawSloppyEdges) {
      return;
    }

    if (isSelected() || m_isHighlighted) {
      final LineType originalLineType = getLineType();
      final Color originalSelectionColor = getSelectionColor();

      setLineType(LineType.LINE_5);
      setSelectionColor(getLineColor());

      super.paint(gfx);

      setLineType(originalLineType);
      setSelectionColor(originalSelectionColor);
    } else {
      super.paintSloppy(gfx);
    }
  }

  /**
   * Regenerates the content of the realizer.
   */
  @Override
  public void regenerate() {
    m_content = m_updater.generateContent(this);

    removeLabel(getLabel());

    if (m_content.getLineCount() > 0) {
      addLabel(new ZyEdgeLabel(m_content));
    }

    for (final IZyEdgeRealizerListener<EdgeType> listener : m_listeners) {
      try {
        listener.regenerated(this);
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }

    getLabel().repaint();
    repaint();
  }

  @Override
  public void reInsertBend(final Bend bend, final Bend refBend, final int dir) {

    final int index = ((bendPos(refBend) + dir) == Graph.BEFORE) || (refBend == null) ? 0 : 1;

    super.reInsertBend(bend, refBend, dir);

    for (final IZyEdgeRealizerListener<EdgeType> listener : m_listeners) {
      try {
        listener.insertedBend(index, bend.getX(), bend.getY());
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  @Override
  public Bend removeBend(final Bend bend) {
    final int index = bendPos(bend);

    final Bend rbend = super.removeBend(bend);

    for (final IZyEdgeRealizerListener<EdgeType> listener : m_listeners) {
      try {
        listener.removedBend(this, index);
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }

    return rbend;
  }

  public void removeListener(final IZyEdgeRealizerListener<EdgeType> listener) {
    m_listeners.removeListener(listener);
  }

  public void setDrawBends(final boolean draw) {
    m_drawBends = draw;
  }

  public void setDrawSloppyEdges(final boolean draw) {
    m_drawSloppyEdges = draw;
  }

  @Override
  public void setLineType(final LineType lineType) {
    m_isHighlighted =
        (lineType == LineType.LINE_5) || (lineType == LineType.DASHED_5)
            || (lineType == LineType.DOTTED_5) || (lineType == LineType.DASHED_DOTTED_5);

    super.setLineType(lineType);
  }

  @Override
  public void setSelected(final boolean selected) {
    super.setSelected(selected);
    for (int i = 0; i < bendCount(); ++i) {
      getBend(i).setSelected(selected);
    }
  }

  @Override
  public void setSourcePoint(final YPoint point) {
    super.setSourcePoint(point);

    notifyLocationChanged();
  }

  @Override
  public void setTargetPoint(final YPoint point) {
    super.setTargetPoint(point);

    notifyLocationChanged();
  }

  public void setUpdater(final IEdgeRealizerUpdater<EdgeType> updater) {
    m_updater = updater;

    if (updater != null) {
      updater.setRealizer(this);
    }
  }

  public void setUserData(final ZyEdgeData<EdgeType> data) {
    m_edgeData = data;
  }

  @Override
  public void setVisible(final boolean visible) {
    super.setVisible(visible);

    for (final IZyEdgeRealizerListener<EdgeType> listener : m_listeners) {
      try {
        listener.changedVisibility(this);
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }
}
