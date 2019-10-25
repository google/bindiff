package com.google.security.zynamics.bindiff.gui.dialogs.directorydiff;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.dialogs.BaseDialog;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class DirectoryDiffDialog extends BaseDialog {
  private static final int DLG_WIDTH = 800;
  private static final int DLG_HEIGHT = 400;

  private static final int MAX_LISTED_FILES = 16;

  private final InternalDirectoryChooserListener dirChooserListener =
      new InternalDirectoryChooserListener();

  private final InternalButtonListener buttonListener = new InternalButtonListener();

  private final FileChooserPanel primaryDirChooser = new FileChooserPanel("", dirChooserListener);
  private final FileChooserPanel secondaryDirChooser = new FileChooserPanel("", dirChooserListener);

  private final JButton selectAllButton = new JButton(buttonListener);
  private final JButton deselectAllButton = new JButton(buttonListener);
  private final JButton diffButton = new JButton(buttonListener);
  private final JButton cancelButton = new JButton(buttonListener);

  private final IdbPairTable diffsTable;

  private final String workspacePath;

  private boolean diffButtonPressed = true;

  public DirectoryDiffDialog(final Window window, final File workspaceDir) {
    super(window, "Directory Diff");

    Preconditions.checkNotNull(workspaceDir);
    Preconditions.checkArgument(workspaceDir.exists(), "Workspace directory doesn't exist");

    workspacePath = workspaceDir.getPath();
    diffsTable = new IdbPairTable(workspaceDir, new IdbPairTableModel());

    init();
    pack();

    setPreferredSize(new Dimension(DLG_WIDTH, DLG_HEIGHT));
    setSize(new Dimension(DLG_WIDTH, DLG_HEIGHT));

    GuiHelper.centerChildToParent(window, this, true);

    setVisible(true);
  }

  private File chooseFile(final Component component, final ESide side) {
    final CFileChooser openFileDlg = new CFileChooser();

    final String title;
    final File startFolder;
    if (side == ESide.PRIMARY) {
      title = "Choose Primary Directory";
      startFolder = new File(secondaryDirChooser.getText()).getParentFile();
      if (startFolder != null) {
        BinDiffConfig.getInstance()
            .getMainSettings()
            .setDirectoryDiffLastPrimaryDir(startFolder.getPath());
      }
    } else if (side == ESide.SECONDARY) {
      title = "Choose Secondary Directory";
      startFolder = new File(primaryDirChooser.getText()).getParentFile();
      if (startFolder != null) {
        BinDiffConfig.getInstance()
            .getMainSettings()
            .setDirectoryDiffLastSecondaryDir(startFolder.getPath());
      }
    } else {
      assert false;
      throw new RuntimeException();
    }

    openFileDlg.setCurrentDirectory(startFolder);
    openFileDlg.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    openFileDlg.setApproveButtonText("Ok");
    openFileDlg.setDialogTitle(title);

    boolean done = false;

    while (!done) {
      if (JFileChooser.APPROVE_OPTION == openFileDlg.showOpenDialog(component)) {
        final File selectedFile = openFileDlg.getSelectedFile();

        if (!selectedFile.exists()) {
          CMessageBox.showInformation(component, "The selected file does not exist.");

          continue;
        } else if (!selectedFile.isDirectory()) {
          CMessageBox.showInformation(component, "The selected file must be a directory.");

          continue;
        } else if (selectedFile
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
      } else {
        done = true;
      }
    }

    return null;
  }

  private Component createButtonsPanel() {
    selectAllButton.setText("Select All");
    deselectAllButton.setText("Deselect All");
    diffButton.setText("Diff");
    cancelButton.setText("Cancel");

    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));

    final JPanel buttonPanel_a = new JPanel(new GridLayout(1, 2, 5, 5));
    buttonPanel_a.add(selectAllButton);
    buttonPanel_a.add(deselectAllButton);

    final JPanel buttonPanel_b = new JPanel(new GridLayout(1, 2, 5, 5));
    buttonPanel_b.add(diffButton);
    buttonPanel_b.add(cancelButton);

    panel.add(buttonPanel_a, BorderLayout.WEST);
    panel.add(buttonPanel_b, BorderLayout.EAST);

    return panel;
  }

  private Component createFileChooserPanel() {
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

  private String cutErrorMessage(final String msg) {
    String val = "";

    int foundIndex = 0;
    int fromIndex = 0;
    int counter = 0;

    while (foundIndex != -1) {
      foundIndex = msg.indexOf("\n", fromIndex);

      if (fromIndex != -1) {
        counter++;

        fromIndex = foundIndex + 1;

        if (counter >= MAX_LISTED_FILES) {
          val = msg.substring(0, fromIndex);
          val += "...";

          return val;
        }
      }
    }

    return msg;
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
    String errorMsg_A = "";
    String errorMsg_B = "";
    String errorMsg_C = "";

    final List<DiffPairTableData> idbPairs = getSelectedIdbPairs();

    if (idbPairs.size() == 0) {
      CMessageBox.showInformation(this, "Can't start diff process. There is no diff selected.");

      return false;
    }

    for (final DiffPairTableData data : idbPairs) {
      final String destination =
          String.format("%s%s%s", workspacePath, File.separator, data.getDestinationDirectory());

      final File destinationFile = new File(destination);

      try {
        if (!destinationFile.mkdir()) {
          errorMsg_A += String.format(" - %s", destination + "\n");
        }
      } catch (final Exception e) {
        // FIXME: Never catch all exceptions!
        errorMsg_A += String.format(" - %s", destination + "\n");
      } finally {
        // TODO: This can fail
        destinationFile.delete();
      }

      String location = data.getIDBLocation();
      if (!location.endsWith(File.separator)) {
        location += File.separator;
      }

      final String primarySource =
          String.format("%s%s%s", getSourceBasePath(ESide.PRIMARY), location, data.getIDBName());

      final String secondarySource =
          String.format("%s%s%s", getSourceBasePath(ESide.SECONDARY), location, data.getIDBName());

      final File primarySourceFile = new File(primarySource);
      final File secondarySourceFile = new File(secondarySource);

      if (!primarySourceFile.exists()) {
        errorMsg_B += String.format(" - %s", primarySource + "\n");
      }
      if (!secondarySourceFile.exists()) {
        errorMsg_B += String.format(" - %s", secondarySource + "\n");
      }

      if (destinationFile.exists()) {
        errorMsg_C += String.format(" - %s", destination + "\n");
      }
    }

    errorMsg_A = cutErrorMessage(errorMsg_A);
    errorMsg_B = cutErrorMessage(errorMsg_B);
    errorMsg_C = cutErrorMessage(errorMsg_C);

    if (!errorMsg_C.equals("")) {
      errorMsg_C =
          String.format(
              "%s\n\n%s",
              "Can't start diff process. Some diff destination folders already exist.\n"
                  + "Please rename affected destination folders.",
              errorMsg_C);

      CMessageBox.showInformation(this, errorMsg_C);

      return false;
    }

    if (!errorMsg_A.equals("")) {
      errorMsg_A =
          String.format(
              "%s\n\n%s",
              "Can't start diff process. Some destination folders cannot be created.", errorMsg_A);

      CMessageBox.showInformation(this, errorMsg_A);

      return false;
    }

    if (!errorMsg_B.equals("")) {
      errorMsg_B =
          String.format(
              "%s\n\n%s", "Can't start diff process. Can't find some source files.", errorMsg_B);

      CMessageBox.showInformation(this, errorMsg_B);

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

  private class InternalButtonListener extends AbstractAction {
    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getSource().equals(selectAllButton)) {
        selectAll(true);
      } else if (event.getSource().equals(deselectAllButton)) {
        selectAll(false);
      } else if (event.getSource().equals(diffButton)) {
        if (diffsTable.isEditing()) {
          diffsTable.getCellEditor().stopCellEditing();
        }

        if (validateSelectedDiffs()) {
          diffButtonPressed = true;
        } else {
          return;
        }

        dispose();
      } else {
        diffButtonPressed = false;

        dispose();
      }
    }
  }

  private class InternalDirectoryChooserListener implements ActionListener {
    private List<DiffPairTableData> findDiffPairs() {
      final List<DiffPairTableData> tableData = new ArrayList<>();

      final File primaryFile = new File(primaryDirChooser.getText());
      final File secondaryFile = new File(secondaryDirChooser.getText());

      final List<String> extensionFilter = new ArrayList<>();
      extensionFilter.add(Constants.IDB32_EXTENSION);
      extensionFilter.add(Constants.IDB64_EXTENSION);

      if (primaryFile.exists() && secondaryFile.exists()) {
        final List<String> primaryFiles = BinDiffFileUtils.findFiles(primaryFile, extensionFilter);
        final List<String> primaryCuttedPaths = new ArrayList<>();

        for (final String path : primaryFiles) {
          final String cuttedPath = path.substring(primaryFile.getPath().length());

          primaryCuttedPaths.add(cuttedPath);
        }

        final List<String> secondaryFiles =
            BinDiffFileUtils.findFiles(secondaryFile, extensionFilter);
        final Set<String> secondaryCuttedPaths = new HashSet<>();

        for (final String path : secondaryFiles) {
          final String cuttedPath = path.substring(secondaryFile.getPath().length());

          secondaryCuttedPaths.add(cuttedPath);
        }

        for (final String primaryCuttedPath : primaryCuttedPaths) {
          if (secondaryCuttedPaths.contains(primaryCuttedPath)) {
            final File cuttedFile = new File(primaryCuttedPath);

            final String fileName = cuttedFile.getName();
            final String location = cuttedFile.getParent();

            final String primaryFilePath =
                String.format("%s%s%s", primaryFile, File.separator, fileName);
            final String secondaryFilePath =
                String.format("%s%s%s", secondaryFile, File.separator, fileName);

            final String destination =
                DiffDirectories.getDiffDestinationDirectoryName(primaryFilePath, secondaryFilePath);

            final DiffPairTableData entry = new DiffPairTableData(fileName, location, destination);

            tableData.add(entry);
          }
        }
      }

      return tableData;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      List<DiffPairTableData> tableData = new ArrayList<>();

      if (event.getSource().equals(primaryDirChooser.getButton())) {
        final File choosenFile =
            DirectoryDiffDialog.this.chooseFile(DirectoryDiffDialog.this, ESide.PRIMARY);

        if (choosenFile != null && choosenFile.exists()) {
          primaryDirChooser.setText(choosenFile.getPath());
        }

        if (!secondaryDirChooser.getText().equals("")) {
          tableData = findDiffPairs();
        }
      } else if (event.getSource().equals(secondaryDirChooser.getButton())) {
        final File choosenFile =
            DirectoryDiffDialog.this.chooseFile(DirectoryDiffDialog.this, ESide.SECONDARY);

        if (choosenFile != null && choosenFile.exists()) {
          secondaryDirChooser.setText(choosenFile.getPath());
        }

        if (!primaryDirChooser.getText().equals("")) {
          tableData = findDiffPairs();
        }
      }

      diffsTable.setTableData(tableData);
    }
  }
}
