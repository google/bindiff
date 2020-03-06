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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class PrintViewAction extends AbstractAction {
  private final ViewTabPanelFunctions controller;

  private final BinDiffGraph<?, ?> graph;

  public PrintViewAction(final ViewTabPanelFunctions controller, final BinDiffGraph<?, ?> graph) {
    this.controller = checkNotNull(controller);
    this.graph = checkNotNull(graph);
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    controller.printView(graph);
  }
}
