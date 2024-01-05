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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.subpanels;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.ZyGraphLayeredRenderer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.ZyOverview;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import y.view.Graph2DView;

public class GraphOverviewPanel extends JPanel {
  private static final float DEFAULT_FOG_COLOR = .90f;

  private static final int DEFAULT_SIZE = 200;

  public GraphOverviewPanel(final Graph2DView view) {
    super(new BorderLayout());

    checkNotNull(view);

    setBorder(new TitledBorder(""));

    final JPanel borderPanel = new JPanel(new BorderLayout());
    borderPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

    final ZyOverview overview = new ZyOverview(view);
    overview.setGraph2DRenderer(new ZyGraphLayeredRenderer<>(overview));

    overview.setDrawingMode(Graph2DView.NORMAL_MODE);

    // animates the scrolling
    overview.putClientProperty("Overview.AnimateScrollTo", Boolean.TRUE);

    // allows zooming from within the overview
    overview.putClientProperty("Overview.AllowZooming", Boolean.TRUE);

    // provides functionality for navigation via keyboard (zoom in (+), zoom out (-), navigation
    // with arrow keys)
    overview.putClientProperty("Overview.AllowKeyboardNavigation", Boolean.TRUE);

    // determines how to differ between the part of the graph that can currently be seen, and the
    // rest
    overview.putClientProperty("Overview.Inverse", Boolean.TRUE);

    // determines the border color of the border between seen and unseen regions of the graph
    overview.putClientProperty("Overview.BorderColor", Color.BLACK);

    // determines the degree of blurriness
    overview.putClientProperty("Overview.funkyTheta", Double.valueOf(0.92));

    // determines the color of the part of the graph that can currently not be seen
    overview.putClientProperty(
        "Overview.FogColor",
        new Color(DEFAULT_FOG_COLOR, DEFAULT_FOG_COLOR, DEFAULT_FOG_COLOR, 0.30f));

    overview.setAntialiasedPainting(true);
    overview.setDoubleBuffered(true);
    overview.setPreferredSize(new Dimension(DEFAULT_SIZE, DEFAULT_SIZE));
    overview.setMinimumSize(new Dimension(0, 0)); // required to restore collapsed split panes later

    borderPanel.add(overview, BorderLayout.CENTER);

    add(borderPanel, BorderLayout.CENTER);
  }
}
