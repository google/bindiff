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

package com.google.security.zynamics.bindiff.graph.listeners;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SingleViewFocusListener extends MouseAdapter {
  private final SingleGraph graph;

  private final ViewTabPanelFunctions controller;

  protected SingleViewFocusListener(
      final ViewTabPanelFunctions controller, final SingleGraph graph) {
    this.controller = Preconditions.checkNotNull(controller);
    this.graph = Preconditions.checkNotNull(graph);

    addListener();
  }

  public void addListener() {
    graph.getView().getCanvasComponent().addMouseListener(this);
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
    controller.setViewFocus(graph.getSide());
  }

  public void removeListener() {
    graph.getView().getCanvasComponent().removeMouseListener(this);
  }
}
