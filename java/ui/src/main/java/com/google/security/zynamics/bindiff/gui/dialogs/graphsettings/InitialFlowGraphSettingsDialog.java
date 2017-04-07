package com.google.security.zynamics.bindiff.gui.dialogs.graphsettings;

import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.config.GraphViewSettingsConfigItem;
import com.google.security.zynamics.bindiff.enums.ECircularLayoutStyle;
import com.google.security.zynamics.bindiff.enums.EGraphLayout;
import com.google.security.zynamics.bindiff.enums.ELayoutOrientation;
import com.google.security.zynamics.bindiff.enums.EMouseAction;
import com.google.security.zynamics.bindiff.enums.EOrthogonalLayoutStyle;
import com.google.security.zynamics.bindiff.gui.dialogs.BaseDialog;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels.CircularLayoutPanel;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels.ControlsPanel;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels.EdgesPanel;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels.HierarchicalLayoutPanel;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels.LayoutingPanel;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels.MiscPanel;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels.OrthogonalLayoutPanel;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels.ProximityBrowsingPanel;
import com.google.security.zynamics.bindiff.log.Logger;
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

public class InitialFlowGraphSettingsDialog extends BaseDialog {
  private static final int DIALOG_WIDTH = 630;
  private static final int DIALOG_HEIGHT = 282;

  private final LayoutingPanel layoutingPanel =
      new LayoutingPanel("Layouting", ESettingsDialogType.INITIAL_FLOWGRAPH_SETTINGS);
  private final ProximityBrowsingPanel proximityBrowsingPanel =
      new ProximityBrowsingPanel(
          "Proximity Browsing", ESettingsDialogType.INITIAL_FLOWGRAPH_SETTINGS);
  private final EdgesPanel edgesPanel =
      new EdgesPanel("Edges", ESettingsDialogType.INITIAL_FLOWGRAPH_SETTINGS);
  private final HierarchicalLayoutPanel hierarchicalLayoutPanel =
      new HierarchicalLayoutPanel(
          "Hierarchical Layout", ESettingsDialogType.INITIAL_FLOWGRAPH_SETTINGS);
  private final OrthogonalLayoutPanel orthogonalLayoutPanel =
      new OrthogonalLayoutPanel(
          "Orthogonal Layout", ESettingsDialogType.INITIAL_FLOWGRAPH_SETTINGS);
  private final CircularLayoutPanel circularLayoutPanel =
      new CircularLayoutPanel("Circular Layout", ESettingsDialogType.INITIAL_FLOWGRAPH_SETTINGS);
  private final ControlsPanel controlsPanel =
      new ControlsPanel("Controls", ESettingsDialogType.INITIAL_FLOWGRAPH_SETTINGS);
  private final MiscPanel miscPanel =
      new MiscPanel("Miscellaneous", ESettingsDialogType.INITIAL_FLOWGRAPH_SETTINGS);

  private final CPanelTwoButtons buttons =
      new CPanelTwoButtons(new InternalButtonListener(), "Ok", "Cancel");

  public InitialFlowGraphSettingsDialog(final Window parent) {
    super(parent, "Initial Flow Graph Settings");

    init();

    pack();

    setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
    setMinimumSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));

    GuiHelper.centerChildToParent(parent, this, true);
  }

  private void save() throws IOException {
    final BinDiffConfig config = BinDiffConfig.getInstance();

    final GraphViewSettingsConfigItem settings = config.getInitialFlowgraphSettings();

    settings.setDefaultGraphLayout(EGraphLayout.getOrdinal(layoutingPanel.getDefaultLayout()));
    settings.setAutoLayouting(layoutingPanel.getAutoLayouting());

    settings.setProximityBrowsing(proximityBrowsingPanel.getProximityBrowsing());
    settings.setAutoProximityBrowsingActivationThreshold(
        proximityBrowsingPanel.getAutoProximityBrowsingActivationThreshold());
    settings.setProximityBrowsingChildDepth(
        proximityBrowsingPanel.getProximityBrowsingChildDepth());
    settings.setProximityBrowsingParentDepth(
        proximityBrowsingPanel.getProximityBrowsingParentDepth());

    settings.setDrawBends(edgesPanel.getDrawBends());

    settings.setHierarchicalOrthogonalEdgeRouting(
        hierarchicalLayoutPanel.getOrthogonalEdgeRouting());
    settings.setHierarchicalOrientation(
        ELayoutOrientation.getOrdinal(hierarchicalLayoutPanel.getLayoutOrientation()));
    settings.setHierarchicalMinimumLayerDistance(hierarchicalLayoutPanel.getMinimumLayerDistance());
    settings.setHierarchicalMinimumNodeDistance(hierarchicalLayoutPanel.getMinimumNodeDistance());

    settings.setOrthogonalLayoutStyle(
        EOrthogonalLayoutStyle.getOrdinal(orthogonalLayoutPanel.getOrthogonalLayoutStyle()));
    settings.setOrthogonalOrientation(
        ELayoutOrientation.getOrdinal(orthogonalLayoutPanel.getOrthogonalOrientation()));
    settings.setOrthogonalMinimumNodeDistance(orthogonalLayoutPanel.getMinimumNodeDistance());

    settings.setCircularLayoutStyle(
        ECircularLayoutStyle.getOrdinal(circularLayoutPanel.getCircularLayoutStyle()));
    settings.setCircularMinimumNodeDistance(circularLayoutPanel.getMinimumNodeDistance());

    settings.setShowScrollbars(controlsPanel.getShowScrollbars());
    settings.setMouseWheelAction(EMouseAction.getOrdinal(controlsPanel.getMouseWheelBehaviour()));
    settings.setZoomSensitivity(controlsPanel.getZoomSensitivity());
    settings.setScrollSensitivity(controlsPanel.getScrollSensitivity());

    settings.setViewSynchronization(miscPanel.getViewSynchronization());
    settings.setGradientBackground(miscPanel.getGradientBackground());
    settings.setLayoutAnimation(miscPanel.getLayoutAnimation());
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
          CMessageBox.showError(
              InitialFlowGraphSettingsDialog.this, "Couldn't save inital flow graph settings.");
          Logger.logException(e, "Couldn't save inital flow graph settings.");
        }
      }

      dispose();
    }
  }
}
