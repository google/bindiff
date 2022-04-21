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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.subpanels;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.EDiffViewMode;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.searchers.GraphSearcherFunctions;
import com.google.security.zynamics.bindiff.graph.settings.GraphLayoutSettings;
import com.google.security.zynamics.bindiff.graph.settings.GraphProximityBrowsingSettings;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettingsChangedListenerAdapter;
import com.google.security.zynamics.bindiff.gui.components.graphsearchfield.GraphSearchField;
import com.google.security.zynamics.bindiff.gui.dialogs.GraphSearchOptionsDialog;
import com.google.security.zynamics.bindiff.gui.dialogs.searchresultsdialog.SearchResultsDialog;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.CircularGraphLayoutAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.FitGraphContentAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.HierarchicalGraphLayoutAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.InverseSelectionAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.OrthogonalGraphLayoutAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.ResetDefaultViewLayoutAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.SelectAncestorsAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.SelectByCriteriaAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.SelectSuccessorsAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.ShowSearchResultsDialogAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.SwitchToCombinedViewModeAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.SwitchToNormalViewModeAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.ToggleAutomaticLayoutAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.ToggleGraphSynchronizationAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.ToggleProximityBrowsingAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.ToggleProximityFreezeModeAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.ZoomToSelectedAction;
import com.google.security.zynamics.bindiff.utils.ResourceUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

public class ViewToolbarPanel extends JPanel {
  private static final String ICONPATH = "data/toolbaricons/";

  private static final Icon DEFAULT_WINDOW_LAYOUT_UP =
      ResourceUtils.getImageIcon(ICONPATH + "default_window_layout_up.png");
  private static final Icon DEFAULT_WINDOW_LAYOUT_HOVER =
      ResourceUtils.getImageIcon(ICONPATH + "default_window_layout_hover.png");
  private static final Icon DEFAULT_WINDOW_LAYOUT_DOWN =
      ResourceUtils.getImageIcon(ICONPATH + "default_window_layout_down.png");

  // Group B
  private static final Icon VIEW_MODE_NORMAL_UP =
      ResourceUtils.getImageIcon(ICONPATH + "view_mode_normal_up.png");
  private static final Icon VIEW_MODE_NORMAL_HOVER =
      ResourceUtils.getImageIcon(ICONPATH + "view_mode_normal_hover.png");
  private static final Icon VIEW_MODE_NORMAL_DOWN =
      ResourceUtils.getImageIcon(ICONPATH + "view_mode_normal_down.png");

  private static final Icon VIEW_MODE_COMBINED_UP =
      ResourceUtils.getImageIcon(ICONPATH + "view_mode_combined_up.png");
  private static final Icon VIEW_MODE_COMBINED_HOVER =
      ResourceUtils.getImageIcon(ICONPATH + "view_mode_combined_hover.png");
  private static final Icon VIEW_MODE_COMBINED_DOWN =
      ResourceUtils.getImageIcon(ICONPATH + "view_mode_combined_down.png");

  // Group C
  private static final Icon HIERARCHIC_LAYOUT_UP =
      ResourceUtils.getImageIcon(ICONPATH + "hierarchic_layout_up.png");
  private static final Icon HIERARCHIC_LAYOUT_HOVER =
      ResourceUtils.getImageIcon(ICONPATH + "hierarchic_layout_hover.png");
  private static final Icon HIERARCHIC_LAYOUT_DOWN =
      ResourceUtils.getImageIcon(ICONPATH + "hierarchic_layout_down.png");

  private static final Icon ORTHOGONAL_LAYOUT_UP =
      ResourceUtils.getImageIcon(ICONPATH + "orthogonal_layout_up.png");
  private static final Icon ORTHOGONAL_LAYOUT_HOVER =
      ResourceUtils.getImageIcon(ICONPATH + "orthogonal_layout_hover.png");
  private static final Icon ORTHOGONAL_LAYOUT_DOWN =
      ResourceUtils.getImageIcon(ICONPATH + "orthogonal_layout_down.png");

  private static final Icon CIRCULAR_LAYOUT_UP =
      ResourceUtils.getImageIcon(ICONPATH + "circular_layout_up.png");
  private static final Icon CIRCULAR_LAYOUT_HOVER =
      ResourceUtils.getImageIcon(ICONPATH + "circular_layout_hover.png");
  private static final Icon CIRCULAR_LAYOUT_DOWN =
      ResourceUtils.getImageIcon(ICONPATH + "circular_layout_down.png");

  // Group D;
  private static final Icon AUTOLAYOUT_ON_UP =
      ResourceUtils.getImageIcon(ICONPATH + "autolayout_on_up.png");
  private static final Icon AUTOLAYOUT_ON_HOVER =
      ResourceUtils.getImageIcon(ICONPATH + "autolayout_on_hover.png");
  private static final Icon AUTOLAYOUT_ON_DOWN =
      ResourceUtils.getImageIcon(ICONPATH + "autolayout_on_down.png");
  private static final Icon AUTOLAYOUT_OFF_UP =
      ResourceUtils.getImageIcon(ICONPATH + "autolayout_off_up.png");
  private static final Icon AUTOLAYOUT_OFF_HOVER =
      ResourceUtils.getImageIcon(ICONPATH + "autolayout_off_hover.png");
  private static final Icon AUTOLAYOUT_OFF_DOWN =
      ResourceUtils.getImageIcon(ICONPATH + "autolayout_off_down.png");

  private static final Icon PROXIMITY_BROWSING_ON_UP =
      ResourceUtils.getImageIcon(ICONPATH + "proximity_browsing_on_up.png");
  private static final Icon PROXIMITY_BROWSING_ON_HOVER =
      ResourceUtils.getImageIcon(ICONPATH + "proximity_browsing_on_hover.png");
  private static final Icon PROXIMITY_BROWSING_ON_DOWN =
      ResourceUtils.getImageIcon(ICONPATH + "proximity_browsing_on_down.png");
  private static final Icon PROXIMITY_BROWSING_OFF_UP =
      ResourceUtils.getImageIcon(ICONPATH + "proximity_browsing_off_up.png");
  private static final Icon PROXIMITY_BROWSING_OFF_HOVER =
      ResourceUtils.getImageIcon(ICONPATH + "proximity_browsing_off_hover.png");
  private static final Icon PROXIMITY_BROWSING_OFF_DOWN =
      ResourceUtils.getImageIcon(ICONPATH + "proximity_browsing_off_down.png");

  private static final Icon PROXIMITY_FREEZE_ON_UP =
      ResourceUtils.getImageIcon(ICONPATH + "proximity_browsing_freeze_on_up.png");
  private static final Icon PROXIMITY_FREEZE_ON_HOVER =
      ResourceUtils.getImageIcon(ICONPATH + "proximity_browsing_freeze_on_hover.png");
  private static final Icon PROXIMITY_FREEZE_ON_DOWN =
      ResourceUtils.getImageIcon(ICONPATH + "proximity_browsing_freeze_on_down.png");
  private static final Icon PROXIMITY_FREEZE_OFF_UP =
      ResourceUtils.getImageIcon(ICONPATH + "proximity_browsing_freeze_off_up.png");
  private static final Icon PROXIMITY_FREEZE_OFF_HOVER =
      ResourceUtils.getImageIcon(ICONPATH + "proximity_browsing_freeze_off_hover.png");
  private static final Icon PROXIMITY_FREEZE_OFF_DOWN =
      ResourceUtils.getImageIcon(ICONPATH + "proximity_browsing_freeze_off_down.png");

  private static final Icon PROXIMITY_FREEZE_ON_UP_GRAY =
      ResourceUtils.getImageIcon(ICONPATH + "proximity_browsing_freeze_on_up_gray.png");
  private static final Icon PROXIMITY_FREEZE_ON_HOVER_GRAY =
      ResourceUtils.getImageIcon(ICONPATH + "proximity_browsing_freeze_on_hover_gray.png");
  private static final Icon PROXIMITY_FREEZE_ON_DOWN_GRAY =
      ResourceUtils.getImageIcon(ICONPATH + "proximity_browsing_freeze_on_down_gray.png");
  private static final Icon PROXIMITY_FREEZE_OFF_UP_GRAY =
      ResourceUtils.getImageIcon(ICONPATH + "proximity_browsing_freeze_off_up_gray.png");
  private static final Icon PROXIMITY_FREEZE_OFF_HOVER_GRAY =
      ResourceUtils.getImageIcon(ICONPATH + "proximity_browsing_freeze_off_hover_gray.png");
  private static final Icon PROXIMITY_FREEZE_OFF_DOWN_GRAY =
      ResourceUtils.getImageIcon(ICONPATH + "proximity_browsing_freeze_off_down_gray.png");

  // Group F
  private static final Icon SELECT_ANCESTORS_UP =
      ResourceUtils.getImageIcon(ICONPATH + "select_ancestors_up.png");
  private static final Icon SELECT_ANCESTORS_HOVER =
      ResourceUtils.getImageIcon(ICONPATH + "select_ancestors_hover.png");
  private static final Icon SELECT_ANCESTORS_DOWN =
      ResourceUtils.getImageIcon(ICONPATH + "select_ancestors_down.png");

  private static final Icon SELECT_SUCCESSORS_UP =
      ResourceUtils.getImageIcon(ICONPATH + "select_successors_up.png");
  private static final Icon SELECT_SUCCESSORS_HOVER =
      ResourceUtils.getImageIcon(ICONPATH + "select_successors_hover.png");
  private static final Icon SELECT_SUCCESSORS_DOWN =
      ResourceUtils.getImageIcon(ICONPATH + "select_successors_down.png");

  private static final Icon INVERT_SELECTION_UP =
      ResourceUtils.getImageIcon(ICONPATH + "invert_selection_up.png");
  private static final Icon INVERT_SELECTION_HOVER =
      ResourceUtils.getImageIcon(ICONPATH + "invert_selection_hover.png");
  private static final Icon INVERT_SELECTION_DOWN =
      ResourceUtils.getImageIcon(ICONPATH + "invert_selection_down.png");

  private static final Icon SELECT_BY_CRITERIA_UP =
      ResourceUtils.getImageIcon(ICONPATH + "select_by_criteria_up.png");
  private static final Icon SELECT_BY_CRITERIA_HOVER =
      ResourceUtils.getImageIcon(ICONPATH + "select_by_criteria_hover.png");
  private static final Icon SELECT_BY_CRITERIA_DOWN =
      ResourceUtils.getImageIcon(ICONPATH + "select_by_criteria_down.png");

  // Group G
  private static final Icon FIT_GRAPH_CONTENT_UP =
      ResourceUtils.getImageIcon(ICONPATH + "fit_graph_to_panel_up.png");
  private static final Icon FIT_GRAPH_CONTENT_HOVER =
      ResourceUtils.getImageIcon(ICONPATH + "fit_graph_to_panel_hover.png");
  private static final Icon FIT_GRAPH_CONTENT_DOWN =
      ResourceUtils.getImageIcon(ICONPATH + "fit_graph_to_panel_down.png");

  private static final Icon ZOOM_TO_SELECTED_UP =
      ResourceUtils.getImageIcon(ICONPATH + "zoom_selected_up.png");
  private static final Icon ZOOM_TO_SELECTED_HOVER =
      ResourceUtils.getImageIcon(ICONPATH + "zoom_selected_hover.png");
  private static final Icon ZOOM_TO_SELECTED_DOWN =
      ResourceUtils.getImageIcon(ICONPATH + "zoom_selected_down.png");

  // Sync
  private static final Icon SYNC_GRAPHS_ON_UP =
      ResourceUtils.getImageIcon(ICONPATH + "synchron_graphs_on_up.png");
  private static final Icon SYNC_GRAPHS_ON_HOVER =
      ResourceUtils.getImageIcon(ICONPATH + "synchron_graphs_on_hover.png");
  private static final Icon SYNC_GRAPHS_ON_DOWN =
      ResourceUtils.getImageIcon(ICONPATH + "synchron_graphs_on_down.png");
  private static final Icon SYNC_GRAPHS_OFF_UP =
      ResourceUtils.getImageIcon(ICONPATH + "synchron_graphs_off_up.png");
  private static final Icon SYNC_GRAPHS_OFF_HOVER =
      ResourceUtils.getImageIcon(ICONPATH + "synchron_graphs_off_hover.png");
  private static final Icon SYNC_GRAPHS_OFF_DOWN =
      ResourceUtils.getImageIcon(ICONPATH + "synchron_graphs_off_down.png");

  // search icons
  private static final Icon ICON_OPTIONS =
      ResourceUtils.getImageIcon("data/buttonicons/options.png");

  private InternalSettingsListener settingsListener = new InternalSettingsListener();

  private InternalSearchButtonsListener buttonListener = new InternalSearchButtonsListener();

  private final GraphSearchField searchField;

  private final GraphSearchOptionsDialog searchOptionsDialog;
  private final SearchResultsDialog searchResultsDialog;

  private ViewTabPanelFunctions controller;

  private GraphSettings settings;

  private final JButton clearSearchResultsButton = new JButton();
  private final JButton searchOptionsButton = new JButton(ICON_OPTIONS);

  private final JButton graphSyncButton = new JButton();

  private final JToolBar groupSave = new JToolBar();
  private final JToolBar groupView = new JToolBar();
  private final JToolBar groupLayout = new JToolBar();
  private final JToolBar groupModes = new JToolBar();
  private final JToolBar groupSelection = new JToolBar();
  private final JToolBar groupFitContent = new JToolBar();

  public ViewToolbarPanel(final ViewTabPanelFunctions controller) {
    super(new BorderLayout());

    this.controller = checkNotNull(controller);

    settings = controller.getGraphSettings();

    searchField = new GraphSearchField(controller, clearSearchResultsButton);
    searchResultsDialog = new SearchResultsDialog(controller.getMainWindow(), searchField);

    searchOptionsDialog =
        new GraphSearchOptionsDialog(
            controller.getMainWindow(), controller.getGraphs().getCombinedGraph());

    clearSearchResultsButton.addActionListener(buttonListener);
    searchOptionsButton.addActionListener(buttonListener);

    settings.addListener(settingsListener);

    groupSave.setFloatable(false);
    groupView.setFloatable(false);
    groupLayout.setFloatable(false);
    groupModes.setFloatable(false);
    groupSelection.setFloatable(false);
    groupFitContent.setFloatable(false);

    groupSave.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 0, 0, Color.WHITE),
            BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY)));
    groupView.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 0, 0, Color.WHITE),
            BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY)));
    groupLayout.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 0, 0, Color.WHITE),
            BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY)));
    groupSelection.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 0, 0, Color.WHITE),
            BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY)));
    groupFitContent.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 0, 0, Color.WHITE),
            BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY)));
    groupModes.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 0, 0, Color.WHITE),
            BorderFactory.createMatteBorder(0, 0, 0, 0, Color.GRAY)));

    setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

    JButton button;

    button = groupSave.add(new ResetDefaultViewLayoutAction(controller));
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setRolloverEnabled(true);
    button.setIcon(DEFAULT_WINDOW_LAYOUT_UP);
    button.setRolloverIcon(DEFAULT_WINDOW_LAYOUT_HOVER);
    button.setPressedIcon(DEFAULT_WINDOW_LAYOUT_DOWN);
    button.setToolTipText("Reset default Window Layout");

    // diff view
    button = groupView.add(new SwitchToNormalViewModeAction(controller));
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setRolloverEnabled(true);
    button.setIcon(
        settings.getDiffViewMode() == EDiffViewMode.NORMAL_VIEW
            ? VIEW_MODE_NORMAL_UP
            : VIEW_MODE_NORMAL_UP);
    button.setRolloverIcon(
        settings.getDiffViewMode() == EDiffViewMode.NORMAL_VIEW
            ? VIEW_MODE_NORMAL_HOVER
            : VIEW_MODE_NORMAL_HOVER);
    button.setPressedIcon(
        settings.getDiffViewMode() == EDiffViewMode.NORMAL_VIEW
            ? VIEW_MODE_NORMAL_DOWN
            : VIEW_MODE_NORMAL_DOWN);
    button.setToolTipText("Switch to Normal View Mode");

    button = groupView.add(new SwitchToCombinedViewModeAction(controller));
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setRolloverEnabled(true);
    button.setIcon(
        settings.getDiffViewMode() == EDiffViewMode.COMBINED_VIEW
            ? VIEW_MODE_COMBINED_UP
            : VIEW_MODE_COMBINED_UP);
    button.setRolloverIcon(
        settings.getDiffViewMode() == EDiffViewMode.COMBINED_VIEW
            ? VIEW_MODE_COMBINED_HOVER
            : VIEW_MODE_COMBINED_HOVER);
    button.setPressedIcon(
        settings.getDiffViewMode() == EDiffViewMode.COMBINED_VIEW
            ? VIEW_MODE_COMBINED_DOWN
            : VIEW_MODE_COMBINED_DOWN);
    button.setToolTipText("Switch to Combined View Mode");

    // layout
    button = groupLayout.add(new HierarchicalGraphLayoutAction(controller));
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setRolloverEnabled(true);
    button.setIcon(HIERARCHIC_LAYOUT_UP);
    button.setRolloverIcon(HIERARCHIC_LAYOUT_HOVER);
    button.setPressedIcon(HIERARCHIC_LAYOUT_DOWN);
    button.setToolTipText("Switch to Hierarchical Layout");

    button = groupLayout.add(new OrthogonalGraphLayoutAction(controller));
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setRolloverEnabled(true);
    button.setIcon(ORTHOGONAL_LAYOUT_UP);
    button.setRolloverIcon(ORTHOGONAL_LAYOUT_HOVER);
    button.setPressedIcon(ORTHOGONAL_LAYOUT_DOWN);
    button.setToolTipText("Switch to Orthogonal Layout");

    button = groupLayout.add(new CircularGraphLayoutAction(controller));
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setRolloverEnabled(true);
    button.setIcon(CIRCULAR_LAYOUT_UP);
    button.setRolloverIcon(CIRCULAR_LAYOUT_HOVER);
    button.setPressedIcon(CIRCULAR_LAYOUT_DOWN);
    button.setToolTipText("Switch to Circlular Layout");

    // modes
    final boolean autoLayout = settings.getLayoutSettings().getAutomaticLayouting();
    button = groupModes.add(new ToggleAutomaticLayoutAction(controller));
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setRolloverEnabled(true);
    button.setIcon(autoLayout ? AUTOLAYOUT_ON_UP : AUTOLAYOUT_OFF_UP);
    button.setRolloverIcon(autoLayout ? AUTOLAYOUT_ON_HOVER : AUTOLAYOUT_OFF_HOVER);
    button.setPressedIcon(autoLayout ? AUTOLAYOUT_ON_DOWN : AUTOLAYOUT_OFF_DOWN);
    button.setToolTipText("Toogle Autolayout");

    final boolean proximityBrowsing = settings.getProximitySettings().getProximityBrowsing();
    button = groupModes.add(new ToggleProximityBrowsingAction(controller));
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setRolloverEnabled(true);
    button.setIcon(proximityBrowsing ? PROXIMITY_BROWSING_ON_UP : PROXIMITY_BROWSING_OFF_UP);
    button.setRolloverIcon(
        proximityBrowsing ? PROXIMITY_BROWSING_ON_HOVER : PROXIMITY_BROWSING_OFF_HOVER);
    button.setPressedIcon(
        proximityBrowsing ? PROXIMITY_BROWSING_ON_DOWN : PROXIMITY_BROWSING_OFF_DOWN);
    button.setToolTipText("Toogle Proximity Browsing");

    final boolean proximityFrozen = settings.getProximitySettings().getProximityBrowsingFrozen();
    button = groupModes.add(new ToggleProximityFreezeModeAction(controller));
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setRolloverEnabled(true);

    if (proximityBrowsing) {
      button.setIcon(proximityFrozen ? PROXIMITY_FREEZE_ON_UP : PROXIMITY_FREEZE_OFF_UP);
      button.setRolloverIcon(
          proximityFrozen ? PROXIMITY_FREEZE_ON_HOVER : PROXIMITY_FREEZE_OFF_HOVER);
      button.setPressedIcon(proximityFrozen ? PROXIMITY_FREEZE_ON_DOWN : PROXIMITY_FREEZE_OFF_DOWN);
    } else {
      button.setIcon(proximityFrozen ? PROXIMITY_FREEZE_ON_UP_GRAY : PROXIMITY_FREEZE_OFF_UP_GRAY);
      button.setRolloverIcon(
          proximityFrozen ? PROXIMITY_FREEZE_ON_HOVER_GRAY : PROXIMITY_FREEZE_OFF_HOVER_GRAY);
      button.setPressedIcon(
          proximityFrozen ? PROXIMITY_FREEZE_ON_DOWN_GRAY : PROXIMITY_FREEZE_OFF_DOWN_GRAY);
    }
    button.setToolTipText("Toogle Proximity Freeze");

    // selection
    button = groupSelection.add(new SelectByCriteriaAction(controller));
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setRolloverEnabled(true);
    button.setIcon(SELECT_BY_CRITERIA_UP);
    button.setRolloverIcon(SELECT_BY_CRITERIA_HOVER);
    button.setPressedIcon(SELECT_BY_CRITERIA_DOWN);
    button.setToolTipText("Select by Criteria");

    button = groupSelection.add(new SelectAncestorsAction(controller));
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setRolloverEnabled(true);
    button.setIcon(SELECT_ANCESTORS_UP);
    button.setRolloverIcon(SELECT_ANCESTORS_HOVER);
    button.setPressedIcon(SELECT_ANCESTORS_DOWN);
    button.setToolTipText("Select Ancestors");

    button = groupSelection.add(new SelectSuccessorsAction(controller));
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setRolloverEnabled(true);
    button.setIcon(SELECT_SUCCESSORS_UP);
    button.setRolloverIcon(SELECT_SUCCESSORS_HOVER);
    button.setPressedIcon(SELECT_SUCCESSORS_DOWN);
    button.setToolTipText("Select Successors");

    if (java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth() > 1024) {
      button = groupSelection.add(new InverseSelectionAction(controller));
      button.setMargin(new Insets(0, 0, 0, 0));
      button.setRolloverEnabled(true);
      button.setIcon(INVERT_SELECTION_UP);
      button.setRolloverIcon(INVERT_SELECTION_HOVER);
      button.setPressedIcon(INVERT_SELECTION_DOWN);
      button.setToolTipText("Invert Selection");
    }

    // fit content
    button = groupFitContent.add(new FitGraphContentAction(controller));
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setRolloverEnabled(true);
    button.setIcon(FIT_GRAPH_CONTENT_UP);
    button.setRolloverIcon(FIT_GRAPH_CONTENT_HOVER);
    button.setPressedIcon(FIT_GRAPH_CONTENT_DOWN);
    button.setToolTipText("Fit Graph Content");

    button = groupFitContent.add(new ZoomToSelectedAction(controller));
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setRolloverEnabled(true);
    button.setIcon(ZOOM_TO_SELECTED_UP);
    button.setRolloverIcon(ZOOM_TO_SELECTED_HOVER);
    button.setPressedIcon(ZOOM_TO_SELECTED_DOWN);
    button.setToolTipText("Zoom to selected Nodes");

    final JPanel p1 = new JPanel(new BorderLayout());
    p1.add(groupSave, BorderLayout.WEST);

    final JPanel p2 = new JPanel(new BorderLayout());
    p2.add(groupView, BorderLayout.WEST);

    final JPanel p3 = new JPanel(new BorderLayout());
    p3.add(groupLayout, BorderLayout.WEST);

    final JPanel p4 = new JPanel(new BorderLayout());
    p4.add(groupFitContent, BorderLayout.WEST);

    final JPanel p5 = new JPanel(new BorderLayout());
    p5.add(groupSelection, BorderLayout.WEST);

    final JPanel p6 = new JPanel(new BorderLayout());
    p6.add(groupModes, BorderLayout.WEST);

    p1.add(p2, BorderLayout.CENTER);
    p2.add(p3, BorderLayout.CENTER);
    p3.add(p4, BorderLayout.CENTER);
    p4.add(p5, BorderLayout.CENTER);
    p5.add(p6, BorderLayout.CENTER);
    p6.add(createRightToolbarPanel());

    add(p1, BorderLayout.CENTER);
  }

  private JPanel createRightToolbarPanel() {
    final JPanel panel = new JPanel(new BorderLayout());

    panel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY));

    panel.add(createSyncPanel(), BorderLayout.WEST);
    panel.add(createSearchPanel(), BorderLayout.CENTER);

    return panel;
  }

  private JPanel createSearchPanel() {

    final JPanel panel = new JPanel(new BorderLayout());
    panel.setPreferredSize(new Dimension(panel.getPreferredSize().width, 25));

    searchField.setPreferredSize(new Dimension(getPreferredSize().width, 20));
    searchField.setBackground(Color.WHITE);

    final JButton bt = new JButton(new ShowSearchResultsDialogAction(controller));
    bt.setText("Results");

    bt.setBackground(Color.WHITE);
    bt.setFocusable(false);

    clearSearchResultsButton.setToolTipText("Clear Search Results");
    clearSearchResultsButton.setFocusable(false);
    clearSearchResultsButton.setBackground(Color.WHITE);
    clearSearchResultsButton.setPreferredSize(
        new Dimension(32, clearSearchResultsButton.getPreferredSize().height));

    searchOptionsButton.setToolTipText("Search Settings");
    searchOptionsButton.setFocusable(false);
    searchOptionsButton.setBackground(Color.WHITE);
    searchOptionsButton.setPreferredSize(
        new Dimension(32, clearSearchResultsButton.getPreferredSize().height));

    final JPanel optionButtonPanel = new JPanel(new BorderLayout());
    optionButtonPanel.setBorder(new EmptyBorder(0, 1, 0, 1));
    optionButtonPanel.add(searchOptionsButton, BorderLayout.CENTER);

    final JPanel iconButtonPanel = new JPanel(new BorderLayout());
    iconButtonPanel.add(clearSearchResultsButton, BorderLayout.CENTER);
    iconButtonPanel.add(optionButtonPanel, BorderLayout.EAST);

    final JPanel buttonPanel = new JPanel(new BorderLayout());
    buttonPanel.setBorder(new EmptyBorder(0, 1, 0, 0));
    buttonPanel.add(bt, BorderLayout.CENTER);
    buttonPanel.add(iconButtonPanel, BorderLayout.WEST);

    final JPanel innerPanel = new JPanel(new BorderLayout());
    innerPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
    innerPanel.add(searchField, BorderLayout.CENTER);
    innerPanel.add(buttonPanel, BorderLayout.EAST);

    panel.add(innerPanel, BorderLayout.CENTER);

    final JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBorder(new EmptyBorder(6, 10, 6, 5));

    mainPanel.add(panel, BorderLayout.CENTER);

    return mainPanel;
  }

  private JPanel createSyncPanel() {
    final JPanel panel = new JPanel(new BorderLayout());

    graphSyncButton.addActionListener(new ToggleGraphSynchronizationAction(controller));

    graphSyncButton.setMargin(new Insets(0, 0, 0, 0));
    graphSyncButton.setBorderPainted(false);
    graphSyncButton.setContentAreaFilled(false);
    graphSyncButton.setFocusable(false);
    graphSyncButton.setRolloverEnabled(true);
    graphSyncButton.setIcon(settings.isSync() ? SYNC_GRAPHS_ON_UP : SYNC_GRAPHS_OFF_UP);
    graphSyncButton.setRolloverIcon(
        settings.isSync() ? SYNC_GRAPHS_ON_HOVER : SYNC_GRAPHS_OFF_HOVER);
    graphSyncButton.setPressedIcon(settings.isSync() ? SYNC_GRAPHS_OFF_DOWN : SYNC_GRAPHS_ON_DOWN);
    graphSyncButton.setToolTipText("Toogle Graph Synchronization");

    final JPanel innerPanel = new JPanel(new BorderLayout());
    innerPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    innerPanel.add(graphSyncButton, BorderLayout.CENTER);

    panel.add(new JLabel("  " + "Sync"), BorderLayout.WEST);
    panel.add(innerPanel, BorderLayout.CENTER);

    return panel;
  }

  public void dispose() {
    clearSearchResultsButton.removeActionListener(buttonListener);
    searchOptionsButton.removeActionListener(buttonListener);
    settings.removeListener(settingsListener);

    searchField.dispose();
    searchResultsDialog.dispose();
    searchOptionsDialog.dispose();

    settingsListener = null;
    buttonListener = null;
    controller = null;
    settings = null;
  }

  public SearchResultsDialog getSearchResultsDialog() {
    return searchResultsDialog;
  }

  public void setCaretIntoJumpToAddressField(final ESide side) {
    searchField.setCaretIntoJumpToAddressField(side);
  }

  public void setCaretIntoSearchField() {
    searchField.setCaretIntoSearchField();
  }

  private class InternalSettingsListener extends GraphSettingsChangedListenerAdapter {
    @Override
    public void autoLayoutChanged(final GraphLayoutSettings settings) {
      final boolean autoLayout = settings.getAutomaticLayouting();

      final JButton button = (JButton) groupModes.getComponentAtIndex(0);
      button.setIcon(autoLayout ? AUTOLAYOUT_ON_UP : AUTOLAYOUT_OFF_UP);
      button.setRolloverIcon(autoLayout ? AUTOLAYOUT_ON_HOVER : AUTOLAYOUT_OFF_HOVER);
      button.setPressedIcon(autoLayout ? AUTOLAYOUT_ON_DOWN : AUTOLAYOUT_OFF_DOWN);

      updateUI();
    }

    @Override
    public void graphSyncChanged(final GraphSettings settings) {
      graphSyncButton.setIcon(settings.isSync() ? SYNC_GRAPHS_ON_UP : SYNC_GRAPHS_OFF_UP);
      graphSyncButton.setRolloverIcon(
          settings.isSync() ? SYNC_GRAPHS_ON_HOVER : SYNC_GRAPHS_OFF_HOVER);
      graphSyncButton.setPressedIcon(
          settings.isSync() ? SYNC_GRAPHS_ON_DOWN : SYNC_GRAPHS_OFF_DOWN);

      updateUI();
    }

    @Override
    public void proximityBrowsingChanged(final GraphProximityBrowsingSettings settings) {
      final boolean proximityBrowsing = settings.getProximityBrowsing();

      proximityBrowsingFrozenChanged(settings);

      final JButton button = (JButton) groupModes.getComponentAtIndex(1);
      button.setIcon(proximityBrowsing ? PROXIMITY_BROWSING_ON_UP : PROXIMITY_BROWSING_OFF_UP);
      button.setRolloverIcon(
          proximityBrowsing ? PROXIMITY_BROWSING_ON_HOVER : PROXIMITY_BROWSING_OFF_HOVER);
      button.setPressedIcon(
          proximityBrowsing ? PROXIMITY_BROWSING_ON_DOWN : PROXIMITY_BROWSING_OFF_DOWN);

      updateUI();
    }

    @Override
    public void proximityBrowsingFrozenChanged(final GraphProximityBrowsingSettings settings) {
      final boolean proximityBrowsing = settings.getProximityBrowsing();
      final boolean proximityFreezed = settings.getProximityBrowsingFrozen();

      if (proximityBrowsing) {
        final JButton button = (JButton) groupModes.getComponentAtIndex(2);
        button.setIcon(proximityFreezed ? PROXIMITY_FREEZE_ON_UP : PROXIMITY_FREEZE_OFF_UP);
        button.setRolloverIcon(
            proximityFreezed ? PROXIMITY_FREEZE_ON_HOVER : PROXIMITY_FREEZE_OFF_HOVER);
        button.setPressedIcon(
            proximityFreezed ? PROXIMITY_FREEZE_ON_DOWN : PROXIMITY_FREEZE_OFF_DOWN);
      } else {
        final JButton button = (JButton) groupModes.getComponentAtIndex(2);
        button.setIcon(
            proximityFreezed ? PROXIMITY_FREEZE_ON_UP_GRAY : PROXIMITY_FREEZE_OFF_UP_GRAY);
        button.setRolloverIcon(
            proximityFreezed ? PROXIMITY_FREEZE_ON_HOVER_GRAY : PROXIMITY_FREEZE_OFF_HOVER_GRAY);
        button.setPressedIcon(
            proximityFreezed ? PROXIMITY_FREEZE_ON_DOWN_GRAY : PROXIMITY_FREEZE_OFF_DOWN_GRAY);
      }

      updateUI();
    }
  }

  public class InternalSearchButtonsListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getSource().equals(clearSearchResultsButton)) {
        GraphSearcherFunctions.clearResults(controller.getGraphs());

        searchField.notifySearchFieldListener();
      } else if (event.getSource().equals(searchOptionsButton)) {
        searchOptionsDialog.setVisible(true);
      }
    }
  }
}
