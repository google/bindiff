// Copyright 2011-2021 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.bindiff.gui.dialogs.printing;

import com.google.security.zynamics.bindiff.gui.dialogs.BaseDialog;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import y.view.Graph2DPrinter;
import y.view.Graph2DView;
import y.view.PrintPreviewPanel;

public class PrintGraphPreviewDialog extends BaseDialog {
  private final ActionListener buttonListener = new InternalButtonListener();

  private final PrinterJob printJob;
  private final Graph2DPrinter printer;
  private final Graph2DView view;

  private final JButton cancelButton;

  private CGraph2DPrintPreviewPanel hiddenPrinterPreviewPanel;

  private JButton pageSetupButton;
  private JButton printButton;
  private JButton zoomInButton;
  private JButton zoomOutButton;
  private final JButton optionsButton;

  private JComboBox<String> zoomComboBox;

  private JScrollPane previewPane;

  public PrintGraphPreviewDialog(final JFrame parent, final Graph2DView view) {
    super(parent, "Print View");

    this.view = view;
    printJob = PrinterJob.getPrinterJob();
    printer = new Graph2DPrinter(view);

    cancelButton = new JButton();
    cancelButton.addActionListener(buttonListener);
    optionsButton = new JButton();
    optionsButton.addActionListener(buttonListener);

    extractComponents();

    printButton.addActionListener(buttonListener);

    init();

    pack();

    GuiHelper.centerChildToParent(parent, this, true);

    setVisible(true);
  }

  private void extractComponents() {
    hiddenPrinterPreviewPanel =
        new CGraph2DPrintPreviewPanel(printJob, printer, printJob.defaultPage());
  }

  private void init() {
    pageSetupButton.setText("Page Setup");
    printButton.setText("Print");
    zoomOutButton.setText("Zoom In");
    zoomInButton.setText("Zoom Out");
    optionsButton.setText("Options");
    cancelButton.setText("Cancel");

    final JPanel panel = new JPanel(new BorderLayout());

    final JPanel topPanel = new JPanel(new BorderLayout());
    final JPanel topButtonsPanel = new JPanel(new GridLayout(1, 4, 5, 5));
    topButtonsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    topButtonsPanel.add(zoomComboBox);
    topButtonsPanel.add(zoomOutButton);
    topButtonsPanel.add(zoomInButton);
    topButtonsPanel.add(optionsButton);
    topPanel.add(topButtonsPanel, BorderLayout.NORTH);

    final JPanel previewPanel = new JPanel(new BorderLayout());
    previewPanel.setBorder(new EmptyBorder(0, 5, 0, 4));
    previewPanel.add(previewPane, BorderLayout.CENTER);

    final JPanel bottomPanel = new JPanel(new BorderLayout());
    bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
    final JPanel bottomLeftPanel = new JPanel(new BorderLayout());
    bottomLeftPanel.add(pageSetupButton, BorderLayout.WEST);
    final JPanel bottomRightPanel = new JPanel(new GridLayout(1, 2, 5, 5));
    bottomRightPanel.add(printButton);
    bottomRightPanel.add(cancelButton);
    bottomPanel.add(bottomLeftPanel, BorderLayout.WEST);
    bottomPanel.add(bottomRightPanel, BorderLayout.EAST);

    panel.add(topPanel, BorderLayout.NORTH);
    panel.add(previewPanel, BorderLayout.CENTER);
    panel.add(bottomPanel, BorderLayout.SOUTH);

    add(panel, BorderLayout.CENTER);
  }

  @Override
  public void dispose() {
    cancelButton.removeActionListener(buttonListener);
    optionsButton.removeActionListener(buttonListener);

    printButton.removeActionListener(buttonListener);

    super.dispose();
  }

  private class CGraph2DPrintPreviewPanel extends PrintPreviewPanel {
    @SuppressWarnings("unchecked") // Suppresses JComboBox cast.
    public CGraph2DPrintPreviewPanel(
        final PrinterJob printJob, final Graph2DPrinter printer, final PageFormat pageFormat) {
      super(
          printJob,
          printer,
          printer.getPosterColumns(),
          printer.getPosterColumns() * printer.getPosterRows(),
          pageFormat);

      final JButton[] buttons = new JButton[4];

      int buttonIndex = 0;

      for (int i = 0; i < getComponentCount(); ++i) {
        final Component c1 = getComponent(i);

        for (int j = 0; j < ((JPanel) c1).getComponentCount(); ++j) {
          final Component c2 = ((JPanel) c1).getComponent(j);

          if (c2 instanceof JButton) {
            buttons[buttonIndex++] = (JButton) c2;
          } else if (c2 instanceof JComboBox) {
            zoomComboBox = (JComboBox<String>) c2;
          } else if (c2 instanceof JScrollPane) {
            previewPane = (JScrollPane) c2;
          }
        }
      }

      pageSetupButton = buttons[0];
      printButton = buttons[1];
      zoomOutButton = buttons[2];
      zoomInButton = buttons[3];
    }
  }

  private class InternalButtonListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getSource() == cancelButton || event.getSource() == printButton) {
        dispose();
      } else if (event.getSource() == optionsButton) {
        final PrintGraphOptionsDialog dlg =
            new PrintGraphOptionsDialog(PrintGraphPreviewDialog.this, printer);

        if (dlg.isOkPressed()) {
          hiddenPrinterPreviewPanel.setPages(
              0, printer.getPosterColumns(), printer.getPosterColumns() * printer.getPosterRows());
        }
      }
    }
  }
}
