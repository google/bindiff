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

import com.google.security.zynamics.bindiff.gui.components.TextComponentUtils;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.FileChooser.FileChooserPanel;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.io.DirectoryChooser;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/** Dialog to export the current view into a variety of formats. */
public class ExportViewDialog extends BaseDialog {
  private static final int DIALOG_WIDTH = 600;
  private static final int DIALOG_HEIGHT = 263;

  private static final int LABEL_WIDTH = 200;
  private static final int ROW_HEIGHT = 25;

  private final Window window;
  private final String description;

  private final FileChooserPanel destinationChooserPanel;

  private final JTextField primaryImageName;
  private final JTextField secondaryImageName;
  private final JTextField combinedImageName;

  private final JComboBox<String> imageFormat;
  private final JComboBox<String> captureRegion;

  private final JButton okButton = new JButton("Ok");
  private final JButton cancelButton = new JButton("Cancel");

  private final ActionListener buttonListener = new InternalButtonListener();

  private boolean okPressed = false;

  public ExportViewDialog(
      final Window parent,
      final String title,
      final String description,
      final File defaultDirectory,
      final String defaultFileName) {
    super(parent, title);

    window = parent;
    this.description = description;

    okButton.addActionListener(buttonListener);
    cancelButton.addActionListener(buttonListener);

    ActionListener directoryChooserListener = new InternalDestinationDirectoryListener();
    destinationChooserPanel =
        new FileChooserPanel(
            defaultDirectory.getPath(), directoryChooserListener, "...", 0, ROW_HEIGHT, 0);

    primaryImageName =
        TextComponentUtils.addDefaultEditorActions(new JTextField("primary_" + defaultFileName));
    secondaryImageName =
        TextComponentUtils.addDefaultEditorActions(new JTextField("secondary_" + defaultFileName));
    combinedImageName =
        TextComponentUtils.addDefaultEditorActions(new JTextField("combined_" + defaultFileName));

    imageFormat = new JComboBox<>();
    imageFormat.addItem("PNG");
    imageFormat.addItem("JPEG");
    imageFormat.addItem("GIF");
    imageFormat.addItem("SVG");

    captureRegion = new JComboBox<>();
    captureRegion.addItem("Graph");
    captureRegion.addItem("View");

    init();

    setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
    setMinimumSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));

    GuiHelper.centerChildToParent(parent, this, true);
  }

  private boolean confirmOverwrites() {
    String filesToOverwrite = "";

    final File primaryFile = getPrimaryImageFile();
    if (primaryFile.exists()) {
      filesToOverwrite += primaryFile.getPath() + "\n";
    }

    final File secondaryFile = getSecondaryImageFile();
    if (secondaryFile.exists()) {
      filesToOverwrite += secondaryFile.getPath() + "\n";
    }

    final File combinedFile = getCombinedImageFile();
    if (combinedFile.exists()) {
      filesToOverwrite += combinedFile.getPath() + "\n";
    }

    if (!filesToOverwrite.equals("")) {
      return CMessageBox.showYesNoQuestion(
              this, String.format("%s\n These files already exist. Overwrite?", filesToOverwrite))
          == JOptionPane.YES_OPTION;
    }

    return true;
  }

  private File getImageFile(final String fileName) {
    String path =
        destinationChooserPanel
            .getText()
            .replace('\\', File.separatorChar)
            .replace('/', File.separatorChar);
    if (!path.endsWith(File.separator)) {
      path += File.separator;
    }

    final String[] fileExtensions = {".png", ".jpeg", ".gif", ".svg"};
    path += fileName + fileExtensions[imageFormat.getSelectedIndex()];

    return new File(path);
  }

  private String selectDirectory(final Window parent) {
    final DirectoryChooser selecter = new DirectoryChooser("Choose Destination Directory");
    selecter.setCurrentDirectory(new File(destinationChooserPanel.getText()));

    if (selecter.showOpenDialog(parent) == DirectoryChooser.APPROVE_OPTION) {
      return selecter.getSelectedFile().getAbsolutePath();
    }

    return null;
  }

  private boolean validatePaths() {
    try {
      final File primaryFile = getPrimaryImageFile();
      if (primaryFile.createNewFile()) {
        // TODO: This can fail
        primaryFile.delete();
      }

      final File secondaryFile = getSecondaryImageFile();
      if (secondaryFile.createNewFile()) {
        // TODO: This can fail
        secondaryFile.delete();
      }

      final File combinedFile = getCombinedImageFile();
      if (combinedFile.createNewFile()) {
        // TODO: This can fail
        combinedFile.delete();
      }
    } catch (final Exception e) {
      return false;
    }

    return true;
  }

  private void init() {
    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));

    final JPanel setterPanel = new JPanel(new GridLayout(6, 1, 3, 3));
    setterPanel.setBorder(new TitledBorder(description));

    setterPanel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Destination directory:", LABEL_WIDTH, destinationChooserPanel, ROW_HEIGHT));
    setterPanel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Primary image name:", LABEL_WIDTH, primaryImageName, ROW_HEIGHT));
    setterPanel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Secondary image name:", LABEL_WIDTH, secondaryImageName, ROW_HEIGHT));
    setterPanel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Combined image name:", LABEL_WIDTH, combinedImageName, ROW_HEIGHT));
    setterPanel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Image file format:", LABEL_WIDTH, imageFormat, ROW_HEIGHT));
    setterPanel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Clip Area:", LABEL_WIDTH, captureRegion, ROW_HEIGHT));

    final JPanel southPanel = new JPanel(new BorderLayout());
    southPanel.setBorder(new EmptyBorder(8, 5, 5, 5));

    final JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);

    southPanel.add(buttonPanel, BorderLayout.EAST);

    panel.add(setterPanel, BorderLayout.NORTH);
    panel.add(southPanel, BorderLayout.SOUTH);

    add(panel, BorderLayout.CENTER);

    pack();
  }

  @Override
  public void dispose() {
    okButton.removeActionListener(buttonListener);
    cancelButton.removeActionListener(buttonListener);
    super.dispose();
  }

  public File getCombinedImageFile() {
    return getImageFile(combinedImageName.getText());
  }

  public File getPrimaryImageFile() {
    return getImageFile(primaryImageName.getText());
  }

  public File getSecondaryImageFile() {
    return getImageFile(secondaryImageName.getText());
  }

  public boolean isCaptureAll() {
    return captureRegion.getSelectedIndex() == 0;
  }

  public boolean isCapturePart() {
    return captureRegion.getSelectedIndex() == 1;
  }

  public boolean isGIF() {
    return imageFormat.getSelectedIndex() == 2;
  }

  public boolean isJPEG() {
    return imageFormat.getSelectedIndex() == 1;
  }

  public boolean isOkPressed() {
    return okPressed;
  }

  public boolean isPNG() {
    return imageFormat.getSelectedIndex() == 0;
  }

  public boolean isSVG() {
    return imageFormat.getSelectedIndex() == 3;
  }

  private class InternalButtonListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getSource().equals(okButton)) {
        if (!validatePaths()) {
          CMessageBox.showInformation(
              window, "Illegal image file names. Please enter valid names.");
          return;
        }
        if (!confirmOverwrites()) {
          return;
        }

        okPressed = true;
      }
      dispose();
    }
  }

  private class InternalDestinationDirectoryListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      final String directoryPath = selectDirectory(window);
      if (directoryPath != null) {
        destinationChooserPanel.setText(directoryPath);
      }
    }
  }
}
