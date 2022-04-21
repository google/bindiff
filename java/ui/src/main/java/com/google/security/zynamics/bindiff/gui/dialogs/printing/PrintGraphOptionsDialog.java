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

package com.google.security.zynamics.bindiff.gui.dialogs.printing;

import com.google.security.zynamics.bindiff.gui.components.TextComponentUtils;
import com.google.security.zynamics.bindiff.gui.dialogs.BaseDialog;
import com.google.security.zynamics.bindiff.resources.Colors;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import com.google.security.zynamics.zylib.gui.CDecFormatter;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.ColorPanel.ColorPanel;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import y.view.Graph2DPrinter;

/** Dialog for settings print options before printing the current graph view. */
public class PrintGraphOptionsDialog extends BaseDialog {
  private static final int LABEL_WIDTH = 125;
  private static final int ROW_HEIGHT = 25;

  private final Graph2DPrinter printer;
  private boolean okPressed = false;

  private final ActionListener buttonListener = new InteneralButtonListener();

  private final JButton okButton = new JButton("Ok");
  private final JButton cancelButton = new JButton("Cancel");

  private final JFormattedTextField posterRows =
      TextComponentUtils.addDefaultEditorActions(new JFormattedTextField(new CDecFormatter(2)));
  private final JFormattedTextField posterCols =
      TextComponentUtils.addDefaultEditorActions(new JFormattedTextField(new CDecFormatter(2)));
  private final JComboBox<String> posterCoords = new JComboBox<>();
  private final JComboBox<String> clipArea = new JComboBox<>();

  private final JTextField titleTextField =
      TextComponentUtils.addDefaultEditorActions(new JTextField());
  private final ColorPanel titleBarColor = new ColorPanel(Colors.GRAY192, true, true);
  private final ColorPanel titleTextColor = new ColorPanel(Color.BLACK, true, true);
  private final JFormattedTextField fontSize =
      TextComponentUtils.addDefaultEditorActions(new JFormattedTextField(new CDecFormatter(2)));

  public PrintGraphOptionsDialog(final Window parent, final Graph2DPrinter printer) {
    super(parent, "Print Options");

    this.printer = printer;

    okButton.addActionListener(buttonListener);
    cancelButton.addActionListener(buttonListener);

    init();

    pack();

    GuiHelper.centerChildToParent(parent, this, true);

    setVisible(true);
  }

  private JPanel createButtonsPanel() {
    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(10, 10, 10, 10));

    final JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);

    panel.add(buttonPanel, BorderLayout.EAST);

    return panel;
  }

  private JPanel createGeneralTabPanel() {
    posterRows.setText(String.valueOf(printer.getPosterRows()));
    posterCols.setText(String.valueOf(printer.getPosterColumns()));

    posterCoords.addItem("Show");
    posterCoords.addItem("Hide");
    posterCoords.setSelectedIndex(1);

    clipArea.addItem("Graph");
    clipArea.addItem("View");

    final JPanel panel = new JPanel(new GridLayout(4, 1, 3, 3));
    panel.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY), new EmptyBorder(5, 5, 5, 5)));

    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Poster Rows", LABEL_WIDTH, posterRows, ROW_HEIGHT));
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Poster Columns", LABEL_WIDTH, posterCols, ROW_HEIGHT));
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Add Poster Coords", LABEL_WIDTH, posterCoords, ROW_HEIGHT));
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Clip Area", LABEL_WIDTH, clipArea, ROW_HEIGHT));

    return panel;
  }

  private JPanel createTitleTabPanel() {
    final JPanel panel = new JPanel(new GridLayout(4, 1, 3, 3));
    panel.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY), new EmptyBorder(5, 5, 5, 5)));

    fontSize.setText("13");

    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Text", LABEL_WIDTH, titleTextField, ROW_HEIGHT));
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Text Color", LABEL_WIDTH, titleTextColor, ROW_HEIGHT));
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Titlebar Color", LABEL_WIDTH, titleBarColor, ROW_HEIGHT));
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Font Size", LABEL_WIDTH, fontSize, ROW_HEIGHT));

    return panel;
  }

  private void init() {
    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(1, 1, 1, 1));

    final JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab("General", createGeneralTabPanel());
    tabbedPane.addTab("Title", createTitleTabPanel());

    panel.add(tabbedPane, BorderLayout.NORTH);
    panel.add(createButtonsPanel(), BorderLayout.SOUTH);

    add(panel, BorderLayout.CENTER);
  }

  @Override
  public void dispose() {
    okButton.addActionListener(buttonListener);
    cancelButton.addActionListener(buttonListener);

    super.dispose();
  }

  public boolean isOkPressed() {
    return okPressed;
  }

  private final class InteneralButtonListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getSource() == okButton) {
        if (Integer.parseInt(posterRows.getText()) < 0) {
          CMessageBox.showInformation(
              PrintGraphOptionsDialog.this, "Poster rows value must be greater than 0.");

          return;
        }
        if (Integer.parseInt(posterCols.getText()) < 0) {
          CMessageBox.showInformation(
              PrintGraphOptionsDialog.this, "Poster columns value must be greater than 0.");

          return;
        }
        if (Integer.parseInt(fontSize.getText()) < 0) {
          CMessageBox.showInformation(
              PrintGraphOptionsDialog.this, "Font size value must be greater than 0.");

          return;
        }

        okPressed = true;

        printer.setPosterRows(Integer.parseInt(posterRows.getText()));
        printer.setPosterColumns(Integer.parseInt(posterCols.getText()));
        printer.setClipType(
            clipArea.getSelectedIndex() == 0
                ? Graph2DPrinter.CLIP_GRAPH
                : Graph2DPrinter.CLIP_VIEW);
        printer.setPrintPosterCoords(posterCoords.getSelectedIndex() == 0);

        final Graph2DPrinter.DefaultTitleDrawable dtd = new Graph2DPrinter.DefaultTitleDrawable();
        dtd.setText(titleTextField.getText());
        dtd.setTitleBarColor(titleTextColor.getColor());
        dtd.setTextColor(titleBarColor.getColor());
        dtd.setFont(new Font("Dialog", Font.PLAIN, Integer.parseInt(fontSize.getText())));
        printer.setTitleDrawable(dtd);
      }

      dispose();
    }
  }
}
