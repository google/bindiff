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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.viewpanel;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.EGraph;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.subpanels.CombinedGraphPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.subpanels.GraphPanel;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.userview.ViewData;
import java.awt.BorderLayout;
import javax.swing.JPanel;

public class CombinedViewPanel extends JPanel {
  private final ViewTabPanelFunctions controller;

  private final GraphPanel combinedGraphPanel;

  public CombinedViewPanel(
      final Diff diff, final ViewTabPanelFunctions controller, final ViewData view) {
    super(new BorderLayout());
    checkNotNull(diff);
    checkNotNull(controller);
    checkNotNull(view);

    this.controller = controller;

    combinedGraphPanel = new CombinedGraphPanel(controller, diff, view, EGraph.COMBINED_GRAPH);

    add(combinedGraphPanel, BorderLayout.CENTER);
  }

  public void dispose() {
    combinedGraphPanel.dispose();
  }

  public ViewTabPanelFunctions getController() {
    return controller;
  }

  public GraphPanel getGraphPanel() {
    return combinedGraphPanel;
  }

  public void resetDefaultPerspective() {
    combinedGraphPanel.resetDefaultPerspective();
  }

  public void toggleGraphsPerspective() {
    combinedGraphPanel.toggleGraphsPerspective();
  }
}
