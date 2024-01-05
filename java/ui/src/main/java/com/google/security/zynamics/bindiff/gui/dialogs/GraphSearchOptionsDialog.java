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

package com.google.security.zynamics.bindiff.gui.dialogs;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.searchers.GraphSearcher;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class GraphSearchOptionsDialog extends BaseDialog {
  private final InternalButtonListener buttonListener = new InternalButtonListener();

  private final JCheckBox regEx = new JCheckBox("Regular Expression");
  private final JCheckBox caseSensitive = new JCheckBox("Case Sensitive");
  private final JCheckBox visibleOnly = new JCheckBox("Visible Only");
  private final JCheckBox selectedOnly = new JCheckBox("Selected Only");

  private final JButton okButton = new JButton("Ok");
  private final JButton cancelButton = new JButton("Cancel");

  private final JButton setDefaultsButton = new JButton("Set Defaults");

  private final GraphSearcher searcher;

  private boolean originalRegEx;
  private boolean originalCaseSensitive;
  private boolean originalVisibleOnly;
  private boolean originalSelectedOnly;

  public GraphSearchOptionsDialog(final Window owner, final BinDiffGraph<?, ?> graph) {
    super(owner, "Search Options");
    checkNotNull(graph);

    setModal(true);

    setLayout(new BorderLayout());

    searcher = graph.getGraphSearcher();

    regEx.setSelected(searcher.isRegEx());
    caseSensitive.setSelected(searcher.isCaseSensitive());
    visibleOnly.setSelected(searcher.isVisibleOnly());
    selectedOnly.setSelected(searcher.isSelectedOnly());

    okButton.addActionListener(buttonListener);
    cancelButton.addActionListener(buttonListener);
    setDefaultsButton.addActionListener(buttonListener);

    init();

    pack();

    setMinimumSize(getSize());

    GuiHelper.centerChildToParent(owner, this, true);
  }

  private JPanel createButtonsPanel() {
    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(10, 5, 5, 5));

    final JPanel leftPanel = new JPanel(new BorderLayout());
    leftPanel.add(setDefaultsButton, BorderLayout.WEST);

    final JPanel rightPanel = new JPanel(new GridLayout(1, 2, 5, 5));
    rightPanel.add(okButton);
    rightPanel.add(cancelButton);

    panel.add(leftPanel, BorderLayout.WEST);
    panel.add(rightPanel, BorderLayout.EAST);

    return panel;
  }

  private JPanel createOptionsPanel() {
    final JPanel panel = new JPanel(new GridLayout(1, 2, 5, 5));

    final JPanel leftPanel = new JPanel(new GridLayout(2, 1));
    leftPanel.setBorder(new TitledBorder(""));

    leftPanel.add(regEx);
    leftPanel.add(caseSensitive);

    final JPanel rightPanel = new JPanel(new GridLayout(2, 1));
    rightPanel.setBorder(new TitledBorder(""));

    rightPanel.add(visibleOnly);
    rightPanel.add(selectedOnly);

    panel.add(leftPanel);
    panel.add(rightPanel);

    return panel;
  }

  private void init() {
    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new TitledBorder(""));

    panel.add(createOptionsPanel(), BorderLayout.NORTH);
    panel.add(createButtonsPanel(), BorderLayout.SOUTH);

    add(panel, BorderLayout.CENTER);
  }

  @Override
  public void setVisible(final boolean visible) {
    if (visible) {
      originalRegEx = regEx.isSelected();
      originalCaseSensitive = caseSensitive.isSelected();
      originalVisibleOnly = visibleOnly.isSelected();
      originalSelectedOnly = selectedOnly.isSelected();
    }

    super.setVisible(visible);
  }

  private class InternalButtonListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getSource().equals(setDefaultsButton)) {
        searcher.setRegEx(false);
        searcher.setCaseSensitive(false);
        searcher.setOnlySelected(false);
        searcher.setOnlyVisible(false);

        regEx.setSelected(false);
        caseSensitive.setSelected(false);
        selectedOnly.setSelected(false);
        visibleOnly.setSelected(false);

        return;
      }

      if (event.getSource().equals(okButton)) {
        searcher.setRegEx(regEx.isSelected());
        searcher.setCaseSensitive(caseSensitive.isSelected());
        searcher.setOnlySelected(selectedOnly.isSelected());
        searcher.setOnlyVisible(visibleOnly.isSelected());
      }

      if (event.getSource().equals(cancelButton)) {
        regEx.setSelected(originalRegEx);
        caseSensitive.setSelected(originalCaseSensitive);
        visibleOnly.setSelected(originalVisibleOnly);
        selectedOnly.setSelected(originalSelectedOnly);
      }

      setVisible(false);
    }
  }
}
