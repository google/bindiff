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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph;

import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyProximityNode;
import java.awt.event.MouseEvent;
import y.view.EdgeLabel;

/** Listens on the edit mode and passes click events forward to the attached listeners. */
public class InternalEditModeListener<NodeType, EdgeType>
    implements IZyEditModeListener<NodeType, EdgeType> {
  private final ListenerProvider<IZyGraphListener<NodeType, EdgeType>> m_graphListeners;

  public InternalEditModeListener(
      final ListenerProvider<IZyGraphListener<NodeType, EdgeType>> graphListeners) {
    m_graphListeners = graphListeners;
  }

  @Override
  public void edgeClicked(
      final EdgeType edge, final MouseEvent event, final double x, final double y) {
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
  public void nodeClicked(
      final NodeType node, final MouseEvent event, final double x, final double y) {
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
  public void proximityBrowserNodeClicked(
      final ZyProximityNode<?> proximityNode, final MouseEvent e, final double x, final double y) {
    for (final IZyGraphListener<NodeType, EdgeType> listener : m_graphListeners) {
      try {
        listener.proximityBrowserNodeClicked(proximityNode, e, x, y);
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }
}
