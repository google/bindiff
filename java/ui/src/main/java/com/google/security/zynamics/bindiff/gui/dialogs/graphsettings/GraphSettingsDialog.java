package com.google.security.zynamics.bindiff.gui.dialogs.graphsettings;

import com.google.common.base.Preconditions;
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

public class GraphSettingsDialog extends BaseDialog {
  private static final int DIALOG_WIDTH = 630;
  private static final int DIALOG_HEIGHT = 282;

  private final ProximityBrowsingPanel proximityBrowsingPanel;
  private final EdgesPanel edgesPanel;
  private final HierarchicalLayoutPanel hierarchicalLayoutPanel;
  private final OrthogonalLayoutPanel orthogonalLayoutPanel;
  private final CircularLayoutPanel circularLayoutPanel;
  private final ControlsPanel controlsPanel;
  private final MiscPanel miscPanel;

  private final CPanelTwoButtons buttons =
      new CPanelTwoButtons(new InternalButtonListener(), "Ok", "Cancel");

  private final GraphSettings settings;

  public GraphSettingsDialog(final Window parent, final GraphSettings settings) {
    super(parent, "Graphview Settings");

    this.settings = Preconditions.checkNotNull(settings);

    proximityBrowsingPanel =
        new ProximityBrowsingPanel("Proximity Browsing", ESettingsDialogType.NON_INITIAL, settings);
    edgesPanel = new EdgesPanel("Edges", ESettingsDialogType.NON_INITIAL, settings);
    hierarchicalLayoutPanel =
        new HierarchicalLayoutPanel(
            "Hierarchical Layout", ESettingsDialogType.NON_INITIAL, settings);
    orthogonalLayoutPanel =
        new OrthogonalLayoutPanel("Orthogonal Layout", ESettingsDialogType.NON_INITIAL, settings);
    circularLayoutPanel =
        new CircularLayoutPanel("Circular Layout", ESettingsDialogType.NON_INITIAL, settings);
    controlsPanel = new ControlsPanel("Controls", ESettingsDialogType.NON_INITIAL, settings);
    miscPanel = new MiscPanel("Miscellaneous", ESettingsDialogType.NON_INITIAL, settings);

    init();

    pack();

    setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
    setMinimumSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));

    GuiHelper.centerChildToParent(parent, this, true);
  }

  private void save() {

    settings
        .getLayoutSettings()
        .setVisibilityWarningThreshold(proximityBrowsingPanel.getVisibilityWarningThreshold());

    settings
        .getProximitySettings()
        .setAutoProximityBrowsingActivationThreshold(
            proximityBrowsingPanel.getAutoProximityBrowsingActivationThreshold());
    settings
        .getProximitySettings()
        .setProximityBrowsingChildren(proximityBrowsingPanel.getProximityBrowsingChildDepth());
    settings
        .getProximitySettings()
        .setProximityBrowsingParents(proximityBrowsingPanel.getProximityBrowsingParentDepth());

    settings.setDrawBends(edgesPanel.getDrawBends());

    settings
        .getLayoutSettings()
        .setHierarchicOrthogonalEdgeRouting(hierarchicalLayoutPanel.getOrthogonalEdgeRouting());
    settings
        .getLayoutSettings()
        .setHierarchicOrientation(hierarchicalLayoutPanel.getLayoutOrientation());
    settings
        .getLayoutSettings()
        .setMinimumHierarchicLayerDistance(hierarchicalLayoutPanel.getMinimumLayerDistance());
    settings
        .getLayoutSettings()
        .setMinimumHierarchicNodeDistance(hierarchicalLayoutPanel.getMinimumNodeDistance());

    settings
        .getLayoutSettings()
        .setOrthogonalLayoutStyle(orthogonalLayoutPanel.getOrthogonalLayoutStyle());
    settings
        .getLayoutSettings()
        .setOrthogonalLayoutOrientation(orthogonalLayoutPanel.getOrthogonalOrientation());
    settings
        .getLayoutSettings()
        .setMinimumOrthogonalNodeDistance(orthogonalLayoutPanel.getMinimumNodeDistance());

    settings
        .getLayoutSettings()
        .setCircularLayoutStyle(circularLayoutPanel.getCircularLayoutStyle());
    settings
        .getLayoutSettings()
        .setMinimumCircularNodeDistance(circularLayoutPanel.getMinimumNodeDistance());

    settings.setShowScrollbars(controlsPanel.getShowScrollbars());
    settings.getMouseSettings().setMousewheelAction(controlsPanel.getMouseWheelBehaviour());
    settings.getMouseSettings().setZoomSensitivity(controlsPanel.getZoomSensitivity());
    settings.getMouseSettings().setScrollSensitivity(controlsPanel.getScrollSensitivity());

    settings.getDisplaySettings().setGradientBackground(miscPanel.getGradientBackground());
    settings.getLayoutSettings().setAnimateLayout(miscPanel.getLayoutAnimation());
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

  private class InternalButtonListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getActionCommand().equals("Ok")) {
        save();
      }

      dispose();
    }
  }
}
