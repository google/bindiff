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

package com.google.security.zynamics.bindiff.gui.dialogs.graphsettings;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.graph.settings.GraphLayoutSettings;
import com.google.security.zynamics.bindiff.graph.settings.GraphMouseSettings;
import com.google.security.zynamics.bindiff.graph.settings.GraphProximityBrowsingSettings;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.bindiff.gui.dialogs.BaseDialog;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels.CircularLayoutPanel;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels.ControlsPanel;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels.EdgesPanel;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels.HierarchicalLayoutPanel;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels.MiscPanel;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels.OrthogonalLayoutPanel;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels.ProximityBrowsingPanel;
import com.google.security.zynamics.zylib.gui.CPanelTwoButtons;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

public class GraphSettingsDialog extends BaseDialog implements ActionListener {
  private static final int DIALOG_WIDTH = 630;
  private static final int DIALOG_HEIGHT = 282;

  private final ProximityBrowsingPanel proximityBrowsingPanel;
  private final EdgesPanel edgesPanel;
  private final HierarchicalLayoutPanel hierarchicalLayoutPanel;
  private final OrthogonalLayoutPanel orthogonalLayoutPanel;
  private final CircularLayoutPanel circularLayoutPanel;
  private final ControlsPanel controlsPanel;
  private final MiscPanel miscPanel;

  private final CPanelTwoButtons buttons = new CPanelTwoButtons(this, "Ok", "Cancel");

  private final GraphSettings settings;

  public GraphSettingsDialog(final Window parent, final GraphSettings settings) {
    super(parent, "Graph View Settings");

    this.settings = checkNotNull(settings);

    proximityBrowsingPanel =
        new ProximityBrowsingPanel(
            "Proximity Browsing", ESettingsDialogType.GRAPH_VIEW_SETTINGS, settings);
    edgesPanel = new EdgesPanel("Edges", ESettingsDialogType.GRAPH_VIEW_SETTINGS, settings);
    hierarchicalLayoutPanel =
        new HierarchicalLayoutPanel(
            "Hierarchical Layout", ESettingsDialogType.GRAPH_VIEW_SETTINGS, settings);
    orthogonalLayoutPanel =
        new OrthogonalLayoutPanel(
            "Orthogonal Layout", ESettingsDialogType.GRAPH_VIEW_SETTINGS, settings);
    circularLayoutPanel =
        new CircularLayoutPanel(
            "Circular Layout", ESettingsDialogType.GRAPH_VIEW_SETTINGS, settings);
    controlsPanel =
        new ControlsPanel("Controls", ESettingsDialogType.GRAPH_VIEW_SETTINGS, settings);
    miscPanel = new MiscPanel("Miscellaneous", ESettingsDialogType.GRAPH_VIEW_SETTINGS, settings);

    init();

    pack();

    setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
    setMinimumSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));

    GuiHelper.centerChildToParent(parent, this, true);
  }

  private void applySettings() {
    final GraphLayoutSettings layoutSettings = settings.getLayoutSettings();

    layoutSettings.setVisibilityWarningThreshold(
        proximityBrowsingPanel.getVisibilityWarningThreshold());

    final GraphProximityBrowsingSettings proximitySettings = settings.getProximitySettings();
    proximitySettings.setAutoProximityBrowsingActivationThreshold(
        proximityBrowsingPanel.getAutoProximityBrowsingActivationThreshold());
    proximitySettings.setProximityBrowsingChildren(
        proximityBrowsingPanel.getProximityBrowsingChildDepth());
    proximitySettings.setProximityBrowsingParents(
        proximityBrowsingPanel.getProximityBrowsingParentDepth());

    settings.setDrawBends(edgesPanel.getDrawBends());

    layoutSettings.setHierarchicalOrthogonalEdgeRouting(
        hierarchicalLayoutPanel.getOrthogonalEdgeRouting());
    layoutSettings.setHierarchicOrientation(hierarchicalLayoutPanel.getLayoutOrientation());
    layoutSettings.setMinimumHierarchicLayerDistance(
        hierarchicalLayoutPanel.getMinimumLayerDistance());
    layoutSettings.setMinimumHierarchicNodeDistance(
        hierarchicalLayoutPanel.getMinimumNodeDistance());

    layoutSettings.setOrthogonalLayoutStyle(orthogonalLayoutPanel.getOrthogonalLayoutStyle());
    layoutSettings.setOrthogonalLayoutOrientation(orthogonalLayoutPanel.getOrthogonalOrientation());
    layoutSettings.setMinimumOrthogonalNodeDistance(orthogonalLayoutPanel.getMinimumNodeDistance());

    layoutSettings.setCircularLayoutStyle(circularLayoutPanel.getCircularLayoutStyle());
    layoutSettings.setMinimumCircularNodeDistance(circularLayoutPanel.getMinimumNodeDistance());

    settings.setShowScrollbars(controlsPanel.getShowScrollbars());
    final GraphMouseSettings mouseSettings = settings.getMouseSettings();
    mouseSettings.setMouseWheelAction(controlsPanel.getMouseWheelBehavior());
    mouseSettings.setZoomSensitivity(controlsPanel.getZoomSensitivity());
    mouseSettings.setScrollSensitivity(controlsPanel.getScrollSensitivity());

    settings.getDisplaySettings().setAnimationSpeed(miscPanel.getAnimationSpeed());

    settings.getLayoutSettings().updateLayouter();
  }

  private void init() {
    final JTabbedPane tabs = new JTabbedPane();
    tabs.addTab("Browsing", proximityBrowsingPanel);
    tabs.addTab("Edges", edgesPanel);
    tabs.addTab("Hierarchical", hierarchicalLayoutPanel);
    tabs.addTab("Orthogonal", orthogonalLayoutPanel);
    tabs.addTab("Circular", circularLayoutPanel);
    tabs.addTab("Controls", controlsPanel);
    tabs.addTab("Misc", miscPanel);

    final JPanel panel = new JPanel(new BorderLayout());

    panel.setBorder(new EmptyBorder(1, 1, 1, 1));

    panel.add(tabs, BorderLayout.CENTER);
    panel.add(buttons, BorderLayout.SOUTH);

    add(panel, BorderLayout.CENTER);
  }

  @Override
  public void setVisible(final boolean visible) {
    proximityBrowsingPanel.setCurrentValues();
    edgesPanel.setCurrentValues();
    hierarchicalLayoutPanel.setCurrentValues();
    orthogonalLayoutPanel.setCurrentValues();
    circularLayoutPanel.setCurrentValues();
    controlsPanel.setCurrentValues();
    miscPanel.setCurrentValues();

    super.setVisible(visible);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals("Ok")) {
      applySettings();
    }
    dispose();
  }
}
