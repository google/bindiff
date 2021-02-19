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

package com.google.security.zynamics.bindiff.gui.dialogs.directorydiff;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.joining;

import com.google.common.collect.ImmutableList;
import com.google.security.zynamics.bindiff.BinDiffProtos.Config.UiPreferences.HistoryOptions;
import com.google.security.zynamics.bindiff.config.Config;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.dialogs.BaseDialog;
import com.google.security.zynamics.bindiff.gui.window.MainWindow;
import com.google.security.zynamics.bindiff.project.diff.DiffDirectories;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.bindiff.utils.BinDiffFileUtils;
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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class DirectoryDiffDialog extends BaseDialog {
  private static final int DLG_WIDTH = 800;
  private static final int DLG_HEIGHT = 400;

  private static final int MAX_LISTED_FILES = 16;

  private FileChooserPanel primaryDirChooser;
  private FileChooserPanel secondaryDirChooser;

  private JButton diffButton;

  private final IdbPairTable diffsTable;

  private final String workspacePath;

  private boolean diffButtonPressed = false;

  public DirectoryDiffDialog(final Window window, final File workspaceDir) {
    super(window, "Directory Diff");

    checkNotNull(workspaceDir);
    checkArgument(workspaceDir.exists(), "Workspace directory doesn't exist");

    workspacePath = workspaceDir.getPath();
    diffsTable = new IdbPairTable(workspaceDir, new IdbPairTableModel());

    init();
    pack();

    setPreferredSize(new Dimension(DLG_WIDTH, DLG_HEIGHT));
    setSize(new Dimension(DLG_WIDTH, DLG_HEIGHT));

    GuiHelper.centerChildToParent(window, this, true);
    SwingUtilities.invokeLater(
        () -> {
          updateTable();

          if (window instanceof MainWindow
              && ((MainWindow) window).getController().askDisassemblerDirectoryIfUnset()) {
            dispose();
          }
        });
    setVisible(true);
  }

  private File chooseFile(final Component component, final ESide side) {
    final CFileChooser fileChooser = new CFileChooser();

    final String title;
    final File startFolder;
    switch (side) {
      case PRIMARY:
        title = "Choose Primary Directory";
        startFolder = new File(secondaryDirChooser.getText()).getParentFile();
        break;
      case SECONDARY:
        title = "Choose Secondary Directory";
        startFolder = new File(primaryDirChooser.getText()).getParentFile();
        break;
      default:
        throw new AssertionError();
    }

    fileChooser.setCurrentDirectory(startFolder);
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fileChooser.setApproveButtonText("Ok");
    fileChooser.setDialogTitle(title);

    boolean done = false;
    while (!done) {
      if (fileChooser.showOpenDialog(component) == JFileChooser.APPROVE_OPTION) {
        final File selectedFile = fileChooser.getSelectedFile();
        if (!selectedFile.isDirectory()) {
          CMessageBox.showInformation(component, "The selected file must be a directory.");
          continue;
        }
        if (selectedFile
            .getPath()
            .equals(
                ESide.PRIMARY == side
                    ? secondaryDirChooser.getText()
                    : primaryDirChooser.getText())) {
          CMessageBox.showInformation(
              component, "Primary and secondary directory cannot be identical.");
          continue;
        }

        return selectedFile;
      }
      done = true;
    }
    return null;
  }

  private Component createButtonsPanel() {
    final JButton refreshButton =
        new JButton(
            new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent e) {
                updateTable();
              }
            });
    refreshButton.setText("Refresh");

    final JButton selectAllButton =
        new JButton(
            new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent e) {
                selectAll(true);
              }
            });
    selectAllButton.setText("Select All");

    final JButton selectNoneButton =
        new JButton(
            new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent e) {
                selectAll(false);
              }
            });
    selectNoneButton.setText("Select None");

    diffButton = new JButton(new DiffButtonListener());
    diffButton.setText("Diff");

    final JButton cancelButton =
        new JButton(
            new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent e) {
                dispose();
              }
            });
    cancelButton.setText("Cancel");

    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));

    final JPanel selectionButtonsPanel = new JPanel(new GridLayout(1, 3, 5, 5));
    selectionButtonsPanel.add(refreshButton);
    selectionButtonsPanel.add(selectAllButton);
    selectionButtonsPanel.add(selectNoneButton);

    final JPanel actionButtonsPanel = new JPanel(new GridLayout(1, 2, 5, 5));
    actionButtonsPanel.add(diffButton);
    actionButtonsPanel.add(cancelButton);

    panel.add(selectionButtonsPanel, BorderLayout.WEST);
    panel.add(actionButtonsPanel, BorderLayout.EAST);

    return panel;
  }

  private Component createFileChooserPanel() {
    final HistoryOptions.Builder history =
        Config.getInstance().getPreferencesBuilder().getHistoryBuilder();
    final InternalDirectoryChooserListener listener = new InternalDirectoryChooserListener();
    primaryDirChooser = new FileChooserPanel(history.getDirectoryDiffPrimaryDir(), listener);
    secondaryDirChooser = new FileChooserPanel(history.getDirectoryDiffSecondaryDir(), listener);

    final JPanel panel = new JPanel(new GridLayout(1, 2, 5, 5));

    final JPanel priPanel = new JPanel(new BorderLayout());
    priPanel.setBorder(new TitledBorder("Primary Directory"));
    priPanel.add(primaryDirChooser, BorderLayout.CENTER);

    final JPanel secPanel = new JPanel(new BorderLayout());
    secPanel.setBorder(new TitledBorder("Secondary Directory"));
    secPanel.add(secondaryDirChooser, BorderLayout.CENTER);

    panel.add(priPanel);
    panel.add(secPanel);

    return panel;
  }

  private Component createTablePanel() {
    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new TitledBorder("Found IDB Pairs"));

    final JScrollPane scrollPane = new JScrollPane(diffsTable);

    panel.add(scrollPane, BorderLayout.CENTER);

    return panel;
  }

  private void selectAll(final boolean select) {
    final IdbPairTableModel model = diffsTable.getTableModel();

    final List<DiffPairTableData> tableData = model.getTableData();
    for (final DiffPairTableData data : tableData) {
      data.getSelectionCheckBox().setSelected(select);
    }

    model.fireTableDataChanged();
  }

  private boolean validateSelectedDiffs() {
    final List<String> destCreationErrors = new ArrayList<>(MAX_LISTED_FILES);
    final List<String> sourceFileErrors = new ArrayList<>(MAX_LISTED_FILES);
    final List<String> destExistsErrors = new ArrayList<>(MAX_LISTED_FILES);

    final List<DiffPairTableData> idbPairs = getSelectedIdbPairs();
    if (idbPairs.size() == 0) {
      CMessageBox.showInformation(this, "Can't start diff process: No diff selected.");
      return false;
    }

    for (final DiffPairTableData data : idbPairs) {
      final File destinationDir = Path.of(workspacePath, data.getDestinationDirectory()).toFile();
      try {
        if (!destinationDir.mkdir()) {
          destCreationErrors.add(" - " + destinationDir);
        }
      } catch (final Exception e) {
        destCreationErrors.add(" - " + destinationDir);
      } finally {
        destinationDir.delete();
      }

      final String location = data.getIDBLocation();
      final Path primarySource =
          Path.of(getSourceBasePath(ESide.PRIMARY), location, data.getIDBName());
      final Path secondarySource =
          Path.of(getSourceBasePath(ESide.SECONDARY), location, data.getIDBName());
      if (!primarySource.toFile().exists()) {
        sourceFileErrors.add(" - " + primarySource);
      }
      if (!secondarySource.toFile().exists()) {
        sourceFileErrors.add(" - " + secondarySource);
      }
      if (destinationDir.exists()) {
        destExistsErrors.add(" - " + destinationDir);
      }
    }

    if (!destExistsErrors.isEmpty()) {
      final StringBuilder message =
          new StringBuilder()
              .append(
                  "Can't start diff process: "
                      + "Some or all of the destination folders already exist\n"
                      + "Please rename affected folders:\n")
              .append(destExistsErrors.stream().limit(MAX_LISTED_FILES).collect(joining("\n")));
      if (destExistsErrors.size() > MAX_LISTED_FILES) {
        message.append(" - ...\n");
      }
      CMessageBox.showInformation(this, message.toString());
      return false;
    }

    if (!destCreationErrors.isEmpty()) {
      final StringBuilder message =
          new StringBuilder()
              .append("Can't start diff process: Some destination folders could not be created:\n")
              .append(destCreationErrors.stream().limit(MAX_LISTED_FILES).collect(joining("\n")));
      if (destCreationErrors.size() > MAX_LISTED_FILES) {
        message.append(" - ...\n");
      }
      CMessageBox.showInformation(this, message.toString());
      return false;
    }

    if (!sourceFileErrors.isEmpty()) {
      final StringBuilder message =
          new StringBuilder()
              .append("Can't start diff process: Can't find one or more source files:\n")
              .append(sourceFileErrors.stream().limit(MAX_LISTED_FILES).collect(joining("\n")));
      if (sourceFileErrors.size() > MAX_LISTED_FILES) {
        message.append(" - ...\n");
      }
      CMessageBox.showInformation(this, message.toString());
      return false;
    }

    return true;
  }

  private void init() {
    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));

    panel.add(createFileChooserPanel(), BorderLayout.NORTH);
    panel.add(createTablePanel(), BorderLayout.CENTER);
    panel.add(createButtonsPanel(), BorderLayout.SOUTH);

    add(panel, BorderLayout.CENTER);
  }

  public boolean getDiffButtonPressed() {
    return diffButtonPressed;
  }

  public List<DiffPairTableData> getSelectedIdbPairs() {
    final List<DiffPairTableData> selectedDiffPairs = new ArrayList<>();

    final IdbPairTableModel model = diffsTable.getTableModel();

    for (final DiffPairTableData data : model.getTableData()) {
      if (data.getSelectionCheckBox().isSelected()) {
        selectedDiffPairs.add(data);
      }
    }

    return selectedDiffPairs;
  }

  public String getSourceBasePath(final ESide side) {
    return side == ESide.PRIMARY ? primaryDirChooser.getText() : secondaryDirChooser.getText();
  }

  private class DiffButtonListener extends AbstractAction {
    @Override
    public void actionPerformed(final ActionEvent event) {
      diffButtonPressed = true;
        if (diffsTable.isEditing()) {
          diffsTable.getCellEditor().stopCellEditing();
        }
      if (!validateSelectedDiffs()) {
          return;
        }

      Config.getInstance()
          .getPreferencesBuilder()
          .getHistoryBuilder()
          .setDirectoryDiffPrimaryDir(primaryDirChooser.getText())
          .setDirectoryDiffSecondaryDir(secondaryDirChooser.getText());
      dispose();
    }
  }

  private List<DiffPairTableData> findDiffPairs(
      final String primaryDirPath, final String secondaryDirPath) {
    final File primaryDir = new File(primaryDirPath);
    final File secondaryDir = new File(secondaryDirPath);
    if (!primaryDir.isDirectory() || !secondaryDir.isDirectory()) {
      return new ArrayList<>();
    }

    final List<DiffPairTableData> tableData = new ArrayList<>();
    final List<String> filter =
        ImmutableList.of(Constants.IDB32_EXTENSION, Constants.IDB64_EXTENSION);

    final List<String> primaryRelative =
        BinDiffFileUtils.findFiles(primaryDir, filter).stream()
            .map(f -> f.getPath().substring(primaryDir.getPath().length()))
            .collect(Collectors.toUnmodifiableList());
    final Set<String> secondaryRelative =
        BinDiffFileUtils.findFiles(secondaryDir, filter).stream()
            .map(f -> f.getPath().substring(secondaryDir.getPath().length()))
            .collect(Collectors.toUnmodifiableSet());

    for (final String fileName : primaryRelative) {
      if (secondaryRelative.contains(fileName)) {
        final File relativeFile = new File(fileName);
        final String destination =
            DiffDirectories.getDiffDestinationDirectoryName(
                Path.of(primaryDir.getPath(), fileName).toString(),
                Path.of(secondaryDir.getPath(), fileName).toString());
        tableData.add(
            new DiffPairTableData(relativeFile.getName(), relativeFile.getParent(), destination));
      }
    }

    return tableData;
  }

  private void updateTable() {
    final List<DiffPairTableData> tableData =
        findDiffPairs(primaryDirChooser.getText(), secondaryDirChooser.getText());
    diffsTable.setTableData(tableData);
    diffButton.setEnabled(!tableData.isEmpty());
  }

  private class InternalDirectoryChooserListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getSource().equals(primaryDirChooser.getButton())) {
        final File file = chooseFile(DirectoryDiffDialog.this, ESide.PRIMARY);
        if (file != null && file.isDirectory()) {
          primaryDirChooser.setText(file.getPath());
        }
      } else if (event.getSource().equals(secondaryDirChooser.getButton())) {
        final File file = chooseFile(DirectoryDiffDialog.this, ESide.SECONDARY);
        if (file != null && file.isDirectory()) {
          secondaryDirChooser.setText(file.getPath());
        }
      }
      updateTable();
    }
  }
}
