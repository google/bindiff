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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers;

import com.google.security.zynamics.zylib.gui.zygraph.CDefaultLabelEventHandler;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyRegenerateableNodeRealizer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;

import y.base.Node;

import java.awt.event.MouseEvent;

public final class CEditNodeHelper {
  private CEditNodeHelper() {
  }

  public static void removeCaret(final AbstractZyGraph<?, ?> graph) {
    final CDefaultLabelEventHandler labelEventHandler = graph.getEditMode().getLabelEventHandler();

    if (labelEventHandler.isActive()) {
      labelEventHandler.deactivateLabelContent();
    }
  }

  public static void select(final AbstractZyGraph<?, ?> graph, final Node node,
      final MouseEvent event) {
    final double mouseX = graph.getEditMode().translateX(event.getX());
    final double mouseY = graph.getEditMode().translateY(event.getY());

    final IZyNodeRealizer realizer = (IZyNodeRealizer) graph.getGraph().getRealizer(node);
    final ZyLabelContent labelContent = realizer.getNodeContent();

    final CDefaultLabelEventHandler labelEventHandler = graph.getEditMode().getLabelEventHandler();

    if (labelContent.isSelectable()) {
      final double zoom = graph.getView().getZoom();

      final double nodeX = realizer.getRealizer().getX();
      final double nodeY = realizer.getRealizer().getY();

      labelEventHandler.handleMouseDraggedEvent(nodeX, nodeY, mouseX, mouseY, zoom);
    }
  }

  public static void setCaretEnd(final AbstractZyGraph<?, ?> graph, final Node node,
      final MouseEvent event) {
    final double mouseX = graph.getEditMode().translateX(event.getX());
    final double mouseY = graph.getEditMode().translateY(event.getY());

    final IZyNodeRealizer realizer = (IZyNodeRealizer) graph.getGraph().getRealizer(node);
    final ZyLabelContent labelContent = realizer.getNodeContent();

    final CDefaultLabelEventHandler labelEventHandler = graph.getEditMode().getLabelEventHandler();

    if (labelContent.isSelectable()) {
      final double zoom = graph.getView().getZoom();

      final double nodeX = realizer.getRealizer().getX();
      final double nodeY = realizer.getRealizer().getY();

      labelEventHandler.handleMouseReleasedEvent(nodeX, nodeY, mouseX, mouseY, zoom,
          event.getClickCount());
    }
  }

  public static void setCaretStart(final AbstractZyGraph<?, ?> graph, final Node node,
      final MouseEvent event) {
    final double mouseX = graph.getEditMode().translateX(event.getX());
    final double mouseY = graph.getEditMode().translateY(event.getY());

    final IZyNodeRealizer realizer = (IZyNodeRealizer) graph.getGraph().getRealizer(node);
    final ZyLabelContent labelContent = realizer.getNodeContent();

    final CDefaultLabelEventHandler labelEventHandler = graph.getEditMode().getLabelEventHandler();

    graph.getEditMode().getLabelEventHandler()
        .activateLabelContent(labelContent, new ZyRegenerateableNodeRealizer(realizer));

    if (labelContent.isSelectable()) {
      final double zoom = graph.getView().getZoom();

      final double nodeX = realizer.getRealizer().getX();
      final double nodeY = realizer.getRealizer().getY();

      labelEventHandler.handleMousePressedEvent(nodeX, nodeY, mouseX, mouseY, zoom);
    }
  }
}
