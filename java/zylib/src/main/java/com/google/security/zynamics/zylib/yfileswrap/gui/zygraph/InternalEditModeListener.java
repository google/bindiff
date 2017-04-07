// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph;

import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyProximityNode;

import y.view.EdgeLabel;

import java.awt.event.MouseEvent;


/**
 * Listens on the edit mode and passes click events forward to the attached listeners.
 */
public class InternalEditModeListener<NodeType, EdgeType> implements
    IZyEditModeListener<NodeType, EdgeType> {
  private final ListenerProvider<IZyGraphListener<NodeType, EdgeType>> m_graphListeners;

  public InternalEditModeListener(
      final ListenerProvider<IZyGraphListener<NodeType, EdgeType>> graphListeners) {
    m_graphListeners = graphListeners;
  }

  @Override
  public void edgeClicked(final EdgeType edge, final MouseEvent event, final double x,
      final double y) {
    for (final IZyGraphListener<NodeType, EdgeType> listener : m_graphListeners) {
      try {
        listener.edgeClicked(edge, event, x, y);
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  @Override
  public void edgeLabelEntered(final EdgeLabel label, final MouseEvent event) {
    for (final IZyGraphListener<NodeType, EdgeType> listener : m_graphListeners) {
      try {
        listener.edgeLabelEntered(label, event);
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  @Override
  public void edgeLabelLeft(final EdgeLabel label) {
    for (final IZyGraphListener<NodeType, EdgeType> listener : m_graphListeners) {
      try {
        listener.edgeLabelExited(label);
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  @Override
  public void nodeClicked(final NodeType node, final MouseEvent event, final double x,
      final double y) {
    for (final IZyGraphListener<NodeType, EdgeType> listener : m_graphListeners) {
      try {
        listener.nodeClicked(node, event, x, y);
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  @Override
  public void nodeEntered(final NodeType node, final MouseEvent event) {
    for (final IZyGraphListener<NodeType, EdgeType> listener : m_graphListeners) {
      try {
        listener.nodeEntered(node, event);
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  @Override
  public void nodeHovered(final NodeType node, final double x, final double y) {
    for (final IZyGraphListener<NodeType, EdgeType> listener : m_graphListeners) {
      try {
        listener.nodeHovered(node, x, y);
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  @Override
  public void nodeLeft(final NodeType node) {
    for (final IZyGraphListener<NodeType, EdgeType> listener : m_graphListeners) {
      try {
        listener.nodeLeft(node);
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  @Override
  public void proximityBrowserNodeClicked(final ZyProximityNode<?> proximityNode,
      final MouseEvent e, final double x, final double y) {
    for (final IZyGraphListener<NodeType, EdgeType> listener : m_graphListeners) {
      try {
        listener.proximityBrowserNodeClicked(proximityNode, e, x, y);
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }
}
