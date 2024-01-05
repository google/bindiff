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

import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.config.GraphViewSettingsConfigItem;
import com.google.security.zynamics.bindiff.gui.dialogs.BaseDialog;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels.CircularLayoutPanel;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels.ControlsPanel;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels.EdgesPanel;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels.HierarchicalLayoutPanel;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels.LayoutingPanel;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels.MiscPanel;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels.OrthogonalLayoutPanel;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels.ProximityBrowsingPanel;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.CPanelTwoButtons;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

public class InitialCallGraphSettingsDialog extends BaseDialog {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private static final int DIALOG_WIDTH = 680;
  private static final int DIALOG_HEIGHT = 282;

  private final LayoutingPanel layoutingPanel =
      new LayoutingPanel("Layouting", ESettingsDialogType.INITIAL_CALL_GRAPH_SETTING);
  private final ProximityBrowsingPanel proximityBrowsingPanel =
      new ProximityBrowsingPanel(
          "Proximity Browsing", ESettingsDialogType.INITIAL_CALL_GRAPH_SETTING);
  private final EdgesPanel edgesPanel =
      new EdgesPanel("Edges", ESettingsDialogType.INITIAL_CALL_GRAPH_SETTING);
  private final HierarchicalLayoutPanel hierarchicalLayoutPanel =
      new HierarchicalLayoutPanel(
          "Hierarchical Layout", ESettingsDialogType.INITIAL_CALL_GRAPH_SETTING);
  private final OrthogonalLayoutPanel orthogonalLayoutPanel =
      new OrthogonalLayoutPanel(
          "Orthogonal Layout", ESettingsDialogType.INITIAL_CALL_GRAPH_SETTING);
  private final CircularLayoutPanel circularLayoutPanel =
      new CircularLayoutPanel("Circular Layout", ESettingsDialogType.INITIAL_CALL_GRAPH_SETTING);
  private final ControlsPanel controlsPanel =
      new ControlsPanel("Controls", ESettingsDialogType.INITIAL_CALL_GRAPH_SETTING);
  private final MiscPanel miscPanel =
      new MiscPanel("Miscellaneous", ESettingsDialogType.INITIAL_CALL_GRAPH_SETTING);

  private final CPanelTwoButtons buttons =
      new CPanelTwoButtons(new InternalButtonListener(), "Ok", "Cancel");

  public InitialCallGraphSettingsDialog(final Window parent) {
    super(parent, "Initial Call Graph Settings");

    init();
    pack();

    setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
    setMinimumSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));

    GuiHelper.centerChildToParent(parent, this, true);
  }

  private void save() throws IOException {
    final BinDiffConfig config = BinDiffConfig.getInstance();

    final GraphViewSettingsConfigItem settings = config.getInitialCallGraphSettings();

    settings.setDefaultGraphLayout(layoutingPanel.getDefaultLayout());
    settings.setAutoLayouting(layoutingPanel.getAutoLayouting());

    settings.setProximityBrowsing(proximityBrowsingPanel.getProximityBrowsing());
    settings.setAutoProximityBrowsingActivationThreshold(
        proximityBrowsingPanel.getAutoProximityBrowsingActivationThreshold());
    settings.setProximityBrowsingChildDepth(
        proximityBrowsingPanel.getProximityBrowsingChildDepth());
    settings.setProximityBrowsingParentDepth(
        proximityBrowsingPanel.getProximityBrowsingParentDepth());
    settings.setVisibilityWarningThreshold(proximityBrowsingPanel.getVisibilityWarningThreshold());

    settings.setDrawBends(edgesPanel.getDrawBends());

    settings.setHierarchicalOrthogonalEdgeRouting(
        hierarchicalLayoutPanel.getOrthogonalEdgeRouting());
    settings.setHierarchicalOrientation(hierarchicalLayoutPanel.getLayoutOrientation());
    settings.setHierarchicalMinimumLayerDistance(hierarchicalLayoutPanel.getMinimumLayerDistance());
    settings.setHierarchicalMinimumNodeDistance(hierarchicalLayoutPanel.getMinimumNodeDistance());

    settings.setOrthogonalLayoutStyle(orthogonalLayoutPanel.getOrthogonalLayoutStyle());
    settings.setOrthogonalOrientation(orthogonalLayoutPanel.getOrthogonalOrientation());
    settings.setOrthogonalMinimumNodeDistance(orthogonalLayoutPanel.getMinimumNodeDistance());

    settings.setCircularLayoutStyle(circularLayoutPanel.getCircularLayoutStyle());
    settings.setCircularMinimumNodeDistance(circularLayoutPanel.getMinimumNodeDistance());

    settings.setShowScrollbars(controlsPanel.getShowScrollbars());
    settings.setMouseWheelAction(controlsPanel.getMouseWheelBehavior());
    settings.setZoomSensitivity(controlsPanel.getZoomSensitivity());
    settings.setScrollSensitivity(controlsPanel.getScrollSensitivity());

    settings.setViewSynchronization(miscPanel.getViewSynchronization());
    settings.setAnimationSpeed(miscPanel.getAnimationSpeed());

    config.write();
  }

  private void init() {
    final JTabbedPane tabs = new JTabbedPane();

    tabs.addTab("Layouting", layoutingPanel);
    tabs.addTab("Browsing", proximityBrowsingPanel);
    tabs.addTab("Edges", edgesPanel);
    tabs.addTab("Hierarchical", hierarchicalLayoutPanel);
    tabs.addTab("Orthogonal", orthogonalLayoutPanel);
    tabs.addTab("Circular", circularLayoutPanel);
    tabs.addTab("Controls", controlsPanel);
    tabs.addTab("Miscellaneous", miscPanel);

    final JPanel panel = new JPanel(new BorderLayout());

    panel.setBorder(new EmptyBorder(1, 1, 1, 1));

    panel.add(tabs, BorderLayout.CENTER);
    panel.add(buttons, BorderLayout.SOUTH);

    add(panel, BorderLayout.CENTER);
  }

  @Override
  public void setVisible(final boolean visible) {
    layoutingPanel.setCurrentValues();
    proximityBrowsingPanel.setCurrentValues();
    edgesPanel.setCurrentValues();
    hierarchicalLayoutPanel.setCurrentValues();
    orthogonalLayoutPanel.setCurrentValues();
    circularLayoutPanel.setCurrentValues();
    controlsPanel.setCurrentValues();
    miscPanel.setCurrentValues();

    super.setVisible(visible);
  }

  private class InternalButtonListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getActionCommand().equals("Ok")) {
        try {
          save();
        } catch (final IOException e) {
          logger.atSevere().withCause(e).log("Couldn't save initial call graph settings");
          CMessageBox.showError(
              InitialCallGraphSettingsDialog.this, "Couldn't save initial call graph settings.");
        }
      }

      dispose();
    }
  }
}
