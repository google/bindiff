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

package com.google.security.zynamics.bindiff.gui.dialogs.graphnodetreeoptionsdialog.tabpanels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class SearchingTabPanel extends JPanel {
  private final JCheckBox regExCheckBox;
  private final JCheckBox caseSensitiveCheckBox;
  private final JCheckBox primarySideCheckBox;
  private final JCheckBox secondarySideCheckBox;

  private final JCheckBox useTempResultsCheckBox;
  private final JCheckBox hightlightGraphNodesCheckBox;

  private boolean initialRegEx;

  private boolean initialCaseSensitive;

  private boolean initialPrimarySide;

  private boolean initialSecondarySide;

  private boolean initialHighlightGraphNode;

  private boolean initialUseTempResults;

  public SearchingTabPanel(final boolean isCombinedView) {
    super(new BorderLayout());

    regExCheckBox = new JCheckBox("Regular Expression");
    caseSensitiveCheckBox = new JCheckBox("Case Sensitive");
    primarySideCheckBox = new JCheckBox("Primary Side");
    secondarySideCheckBox = new JCheckBox("Secondary Side");

    useTempResultsCheckBox = new JCheckBox("Search in last temporary Results");
    hightlightGraphNodesCheckBox = new JCheckBox("Highlight Graph Nodes");

    add(createPanel(isCombinedView), BorderLayout.CENTER);

    setDefaults();
  }

  private JPanel createPanel(final boolean isCombinedView) {
    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new LineBorder(Color.GRAY));

    final JPanel searchOptionsPanel = new JPanel(new BorderLayout());

    final JPanel gridPanel_1 = new JPanel(new GridLayout(isCombinedView ? 4 : 2, 1));
    gridPanel_1.setBorder(new TitledBorder("Search Options"));
    gridPanel_1.add(regExCheckBox);
    gridPanel_1.add(caseSensitiveCheckBox);
    if (isCombinedView) {
      gridPanel_1.add(primarySideCheckBox);
      gridPanel_1.add(secondarySideCheckBox);
    }

    final JPanel resultOptionsPanel = new JPanel(new BorderLayout());

    final JPanel gridPanel_2 = new JPanel(new GridLayout(2, 1));
    gridPanel_2.setBorder(new TitledBorder("Result options"));
    gridPanel_2.add(useTempResultsCheckBox);
    gridPanel_2.add(hightlightGraphNodesCheckBox);

    searchOptionsPanel.add(gridPanel_1, BorderLayout.NORTH);
    resultOptionsPanel.add(gridPanel_2, BorderLayout.NORTH);

    panel.add(searchOptionsPanel, BorderLayout.NORTH);
    panel.add(resultOptionsPanel, BorderLayout.CENTER);

    return panel;
  }

  public boolean getCaseSensitive() {
    return caseSensitiveCheckBox.isSelected();
  }

  public boolean getHighlightGraphNodes() {
    return hightlightGraphNodesCheckBox.isSelected();
  }

  public boolean getPrimarySide() {
    return primarySideCheckBox.isSelected();
  }

  public boolean getRegEx() {
    return regExCheckBox.isSelected();
  }

  public boolean getSecondarySide() {
    return secondarySideCheckBox.isSelected();
  }

  public boolean getUseTemporaryResult() {
    return useTempResultsCheckBox.isSelected();
  }

  public void restoreInitialSettings() {
    regExCheckBox.setSelected(initialRegEx);
    caseSensitiveCheckBox.setSelected(initialCaseSensitive);
    primarySideCheckBox.setSelected(initialPrimarySide);
    secondarySideCheckBox.setSelected(initialSecondarySide);

    hightlightGraphNodesCheckBox.setSelected(initialHighlightGraphNode);
    useTempResultsCheckBox.setSelected(initialUseTempResults);
  }

  public void setDefaults() {
    regExCheckBox.setSelected(false);
    caseSensitiveCheckBox.setSelected(false);
    primarySideCheckBox.setSelected(true);
    secondarySideCheckBox.setSelected(true);

    hightlightGraphNodesCheckBox.setSelected(true);
    useTempResultsCheckBox.setSelected(false);
  }

  public void storeInitialSettings() {
    initialRegEx = getRegEx();
    initialCaseSensitive = getCaseSensitive();
    initialPrimarySide = getPrimarySide();
    initialSecondarySide = getSecondarySide();

    initialHighlightGraphNode = getHighlightGraphNodes();
    initialUseTempResults = getUseTemporaryResult();
  }
}
