// Copyright 2011-2020 Google LLC
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

package com.google.security.zynamics.bindiff.gui.dialogs;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.components.TextComponentUtils;
import com.google.security.zynamics.bindiff.project.diff.DiffDirectories;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import com.google.security.zynamics.zylib.gui.CFileChooser;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.FileChooser.FileChooserPanel;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class NewDiffDialog extends BaseDialog {
  private static final int IDB_FILE_FILTER_INDEX = 0;
  private static final int TEXTFIELD_WITH = 500;
  private static final int LABEL_WITH = 180;
  private static final int ROW_HEIGHT = 25;

  private final File workspaceDir;

  private final InternalPrimaryFileChooserListener primaryFileListener =
      new InternalPrimaryFileChooserListener();
  private final InternalSecondaryFileChooserListener secondaryFileListener =
      new InternalSecondaryFileChooserListener();

  private final InternalButtonListener buttonListener = new InternalButtonListener();

  private final JButton diffButton = new JButton(buttonListener);
  private final JButton cancelButton = new JButton(buttonListener);

  private final FileChooserPanel primaryChooser =
      new FileChooserPanel("", primaryFileListener, "...", TEXTFIELD_WITH, ROW_HEIGHT, 0);
  private final FileChooserPanel secondaryChooser =
      new FileChooserPanel("", secondaryFileListener, "...", TEXTFIELD_WITH, ROW_HEIGHT, 0);

  private final JTextField destinationDirName =
      TextComponentUtils.addDefaultEditorActions(new JTextField());

  private boolean diffPressed = false;

  private File lastSelectedFile = null;

  public NewDiffDialog(final Window window, final File workspaceDir) {
    super(window, "New Diff");

    this.workspaceDir = Preconditions.checkNotNull(workspaceDir);
    Preconditions.checkArgument(workspaceDir.exists(), "Workspace directory doesn't exist.");

    init();
    pack();

    GuiHelper.centerChildToParent(window, this, true);

    setVisible(true);
  }

  private static File chooseFile(
      final Component component,
      final String title,
      final File startFolder,
      final int fileFilterIndex) {
    final CFileChooser chooser =
        new CFileChooser(
            new CFileChooser.FileExtension(
                String.format(
                    "IDA Pro Database/BinExport Binary (*.%s;*.%s;*.%s)",
                    Constants.IDB32_EXTENSION,
                    Constants.IDB64_EXTENSION,
                    Constants.BINDIFF_BINEXPORT_EXTENSION),
                Constants.IDB32_EXTENSION,
                Constants.IDB64_EXTENSION,
                Constants.BINDIFF_BINEXPORT_EXTENSION));

    chooser.setCurrentDirectory(startFolder);
    chooser.setFileFilter(fileFilterIndex);
    chooser.setApproveButtonText("Ok");
    chooser.setDialogTitle(title);

    if (chooser.showOpenDialog(component) == JFileChooser.APPROVE_OPTION) {
      return chooser.getSelectedFile();
    }
    return null;
  }

  private Component createButtonPanel() {
    diffButton.setText("Diff");
    cancelButton.setText("Cancel");

    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(10, 0, 5, 5));

    final JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
    buttonPanel.add(diffButton);
    buttonPanel.add(cancelButton);

    panel.add(buttonPanel, BorderLayout.EAST);

    return panel;
  }

  private JPanel createDestinationFolderPanel() {
    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(5, 0, 0, 0));

    final JPanel textFieldPanel = new JPanel(new BorderLayout());
    textFieldPanel.setBorder(new TitledBorder("Diff Destination"));

    destinationDirName.setPreferredSize(
        new Dimension(destinationDirName.getPreferredSize().width, ROW_HEIGHT));
    textFieldPanel.add(destinationDirName, BorderLayout.NORTH);

    panel.add(textFieldPanel, BorderLayout.CENTER);

    return panel;
  }

  private JPanel createFilesChooserPane() {
    final JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));

    final JPanel primaryPanel = new JPanel(new GridLayout(1, 1, 5, 5));
    final JPanel secondaryPanel = new JPanel(new GridLayout(1, 1, 5, 5));

    primaryPanel.setBorder(new TitledBorder("Primary Source"));
    secondaryPanel.setBorder(new TitledBorder("Secondary Source"));

    primaryPanel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Primary file:", LABEL_WITH, primaryChooser, ROW_HEIGHT));
    secondaryPanel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Secondary file:", LABEL_WITH, secondaryChooser, ROW_HEIGHT));

    panel.add(primaryPanel);
    panel.add(secondaryPanel);

    return panel;
  }

  private boolean isDiffExisting(final File destination) {
    for (final String fileName : destination.list()) {
      if (fileName.toLowerCase().endsWith(Constants.BINDIFF_MATCHES_DB_EXTENSION.toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  private void updateDestinationFolder() {
    final String dirName =
        DiffDirectories.getDiffDestinationDirectoryName(
            primaryChooser.getText(), secondaryChooser.getText());
    destinationDirName.setText(dirName);
  }

  private boolean validateDiffSources() {
    final File primaryIdb = getIdb(ESide.PRIMARY);
    final File primaryBinExport = getBinExportBinary(ESide.PRIMARY);
    if (primaryIdb == null && primaryBinExport == null) {
      CMessageBox.showInformation(this, "Primary source files not set or do not exist.");
      return false;
    }

    final File secondaryIdb = getIdb(ESide.SECONDARY);
    final File secondaryBinExport = getBinExportBinary(ESide.SECONDARY);
    if (secondaryIdb == null && secondaryBinExport == null) {
      CMessageBox.showInformation(this, "Secondary source files not set or do not exist.");
      return false;
    }

    final File destination = getDestinationDirectory();
    if (destination == null) {
      CMessageBox.showInformation(this, "Destination folder is not set.");
      return false;
    }
    if (destination.exists() && isDiffExisting(destination)) {
      CMessageBox.showInformation(this, "Diff file already exists in the workspace.");
      return false;
    }

    try {
      if (!destination.exists() && !destination.mkdirs()) {
        CMessageBox.showInformation(this, "Could not create destination folder.");
        return false;
      }
    } catch (final SecurityException e) {
      CMessageBox.showInformation(this, "Could not create destination folder.");
      return false;
    }
    return true;
  }

  private void init() {
    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));

    panel.add(createFilesChooserPane(), BorderLayout.NORTH);
    panel.add(createDestinationFolderPanel(), BorderLayout.CENTER);
    panel.add(createButtonPanel(), BorderLayout.SOUTH);

    add(panel, BorderLayout.CENTER);
  }

  public File getBinExportBinary(final ESide side) {
    final File binaryFile =
        new File(side == ESide.PRIMARY ? primaryChooser.getText() : secondaryChooser.getText());
    return binaryFile.exists() ? binaryFile : null;
  }

  public File getDestinationDirectory() {
    if (destinationDirName.getText().equals("")) {
      return null;
    }

    return new File(
        String.join("", workspaceDir.getPath(), File.separator, destinationDirName.getText()));
  }

  public boolean getDiffButtonPressed() {
    return diffPressed;
  }

  public File getIdb(final ESide side) {
    final String idbPath =
        side == ESide.PRIMARY ? primaryChooser.getText() : secondaryChooser.getText();

    if (idbPath.endsWith("." + Constants.IDB32_EXTENSION)
        || idbPath.endsWith("." + Constants.IDB64_EXTENSION)) {
      final File idbFile = new File(idbPath);

      if (idbFile.exists()) {
        return idbFile;
      }
    }

    return null;
  }

  private class InternalButtonListener extends AbstractAction {
    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getSource().equals(diffButton)) {
        if (!validateDiffSources()) {
          return;
        }

        diffPressed = true;
      }

      dispose();
    }
  }

  private class InternalPrimaryFileChooserListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      final BinDiffConfig config = BinDiffConfig.getInstance();

      final File file =
          NewDiffDialog.chooseFile(
              NewDiffDialog.this,
              "Choose Primary File",
              lastSelectedFile == null
                  ? new File(config.getMainSettings().getNewDiffLastPrimaryDir())
                  : lastSelectedFile,
              IDB_FILE_FILTER_INDEX);

      if (file != null) {
        primaryChooser.setText(file.getPath());
        lastSelectedFile = file.getParentFile();
        config.getMainSettings().setNewDiffLastPrimaryDir(lastSelectedFile.getPath());
      }

      updateDestinationFolder();
    }
  }

  private class InternalSecondaryFileChooserListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      final BinDiffConfig config = BinDiffConfig.getInstance();

      final File file =
          NewDiffDialog.chooseFile(
              NewDiffDialog.this,
              "Choose Secondary File",
              lastSelectedFile == null
                  ? new File(config.getMainSettings().getNewDiffLastSecondaryDir())
                  : lastSelectedFile,
              IDB_FILE_FILTER_INDEX);

      if (file != null) {
        secondaryChooser.setText(file.getPath());
        lastSelectedFile = file.getParentFile();
        config.getMainSettings().setNewDiffLastSecondaryDir(lastSelectedFile.getPath());
      }

      updateDestinationFolder();
    }
  }
}
