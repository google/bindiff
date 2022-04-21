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

package com.google.security.zynamics.bindiff.gui.dialogs.graphnodetreeoptionsdialog;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.ESortByCriterion;
import com.google.security.zynamics.bindiff.enums.ESortOrder;
import com.google.security.zynamics.bindiff.graph.filter.enums.EMatchStateFilter;
import com.google.security.zynamics.bindiff.graph.filter.enums.ESelectionFilter;
import com.google.security.zynamics.bindiff.graph.filter.enums.ESideFilter;
import com.google.security.zynamics.bindiff.graph.filter.enums.EVisibilityFilter;
import com.google.security.zynamics.bindiff.gui.dialogs.BaseDialog;
import com.google.security.zynamics.bindiff.gui.dialogs.graphnodetreeoptionsdialog.tabpanels.FilteringTabPanel;
import com.google.security.zynamics.bindiff.gui.dialogs.graphnodetreeoptionsdialog.tabpanels.SearchingTabPanel;
import com.google.security.zynamics.bindiff.gui.dialogs.graphnodetreeoptionsdialog.tabpanels.SortingTabPanel;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.bindiff.utils.ResourceUtils;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

public class GraphNodeTreeOptionsDialog extends BaseDialog {
  private final InternalButtonListener buttonListener = new InternalButtonListener();

  final JTabbedPane tab = new JTabbedPane();

  private final SearchingTabPanel searchingPanel;
  private final FilteringTabPanel filteringPanel;
  private final SortingTabPanel sortingPanel;

  private final JButton okButton = new JButton("Ok");
  private final JButton cancelButton = new JButton("Cancel");

  private final JButton setDefaultsButton = new JButton("Set Defaults");

  private boolean ok = false;

  public GraphNodeTreeOptionsDialog(
      final Window parent,
      final String title,
      final boolean isCallgraphView,
      final boolean isCombinedView) {
    super(parent, title);

    checkNotNull(parent);
    checkNotNull(title);

    setTitle(title);
    setModal(true);
    setLayout(new BorderLayout());

    final List<Image> imageList = new ArrayList<>();
    imageList.add(ResourceUtils.getImageIcon(Constants.APP_ICON_PATH_16X16).getImage());
    imageList.add(ResourceUtils.getImageIcon(Constants.APP_ICON_PATH_32X32).getImage());
    imageList.add(ResourceUtils.getImageIcon(Constants.APP_ICON_PATH_48X48).getImage());

    setIconImages(imageList);

    searchingPanel = new SearchingTabPanel(isCombinedView);
    filteringPanel = new FilteringTabPanel(isCombinedView, isCallgraphView);
    sortingPanel = new SortingTabPanel(isCombinedView, isCallgraphView);

    init();

    okButton.addActionListener(buttonListener);
    cancelButton.addActionListener(buttonListener);
    setDefaultsButton.addActionListener(buttonListener);
  }

  private JPanel createButtonPanel() {
    final JPanel panel = new JPanel(new GridLayout(1, 2));
    panel.setBorder(new EmptyBorder(10, 5, 5, 5));

    final JPanel leftPanel = new JPanel(new BorderLayout());
    leftPanel.add(setDefaultsButton, BorderLayout.WEST);

    final JPanel rightPanel = new JPanel(new BorderLayout());
    final JPanel okCancelPanel = new JPanel(new GridLayout(1, 2, 5, 5));
    okCancelPanel.add(okButton);
    okCancelPanel.add(cancelButton);
    rightPanel.add(okCancelPanel, BorderLayout.EAST);

    panel.add(leftPanel);
    panel.add(rightPanel);

    return panel;
  }

  @Override
  public void dispose() {
    okButton.removeActionListener(buttonListener);
    cancelButton.removeActionListener(buttonListener);
    setDefaultsButton.removeActionListener(buttonListener);

    super.dispose();
  }

  public boolean getCaseSensitive() {
    return searchingPanel.getCaseSensitive();
  }

  public IAddress getEndAddress() {
    return filteringPanel.getEndAddress();
  }

  public boolean getHighlightGraphNodes() {
    return searchingPanel.getHighlightGraphNodes();
  }

  public EMatchStateFilter getMatchStateFilter() {
    return filteringPanel.getMatchStateFilter();
  }

  public boolean getOkPressed() {
    return ok;
  }

  public boolean getPrimarySide() {
    return searchingPanel.getPrimarySide();
  }

  public boolean getRegEx() {
    return searchingPanel.getRegEx();
  }

  public boolean getSecondarySide() {
    return searchingPanel.getSecondarySide();
  }

  public ESelectionFilter getSelectionFilter() {
    return filteringPanel.getSelectionFilter();
  }

  public ESideFilter getSideFilter() {
    return filteringPanel.getSideFilter();
  }

  public ESortByCriterion getSortByCriterion(final int depth) {
    return sortingPanel.getSortByCriterion(depth);
  }

  public ESortOrder getSortOrder(final int depth) {
    return sortingPanel.getSortOrder(depth);
  }

  public IAddress getStartAddress() {
    return filteringPanel.getStartAddress();
  }

  public boolean getUseTemporaryResult() {
    return searchingPanel.getUseTemporaryResult();
  }

  public EVisibilityFilter getVisibilityFilter() {
    return filteringPanel.getVisibilityFilter();
  }

  private void init() {
    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(1, 1, 1, 1));

    tab.add(searchingPanel);
    tab.add(filteringPanel);
    tab.add(sortingPanel);

    tab.setTitleAt(0, "Search");
    tab.setTitleAt(1, "Filter");
    tab.setTitleAt(2, "Sort");

    final JPanel buttonPanel = createButtonPanel();

    panel.add(tab, BorderLayout.CENTER);
    panel.add(buttonPanel, BorderLayout.SOUTH);

    add(panel, BorderLayout.CENTER);

    pack();

    setPreferredSize(new Dimension(getPreferredSize().width, getPreferredSize().height + 8));
    setMinimumSize(getPreferredSize());
  }

  public void setDefaults(final int tabIndex) {
    switch (tabIndex) {
      case 0:
        searchingPanel.setDefaults();
        break;
      case 1:
        filteringPanel.setDefaults();
        break;
      case 2:
        sortingPanel.setDefaults();
        break;
      default:
        throw new RuntimeException("Invalid tab index");
    }
  }

  @Override
  public void setVisible(final boolean visible) {
    if (visible) {
      searchingPanel.storeInitialSettings();
      filteringPanel.storeInitialSettings();
      sortingPanel.storeInitialSettings();
    }

    super.setVisible(visible);
  }

  private class InternalButtonListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getSource().equals(okButton)) {
        ok = true;

        setVisible(false);
      } else if (event.getSource().equals(cancelButton)) {
        searchingPanel.restoreInitialSettings();
        filteringPanel.restoreInitialSettings();
        sortingPanel.restoreInitialSettings();

        setVisible(false);
      } else if (event.getSource().equals(setDefaultsButton)) {
        setDefaults(tab.getSelectedIndex());
      }
    }
  }
}
