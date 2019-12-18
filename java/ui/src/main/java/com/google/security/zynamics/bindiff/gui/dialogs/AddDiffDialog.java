package com.google.security.zynamics.bindiff.gui.dialogs;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.database.MatchesDatabase;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.components.TextComponentUtils;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.resources.Colors;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.CFileChooser;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.gui.FileChooser.FileChooserPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class AddDiffDialog extends BaseDialog {
  private static final int TEXTFIELD_WIDTH = 500;
  private static final int LABEL_WIDTH = 180;
  private static final int ROW_HEIGHT = 25;

  private final Workspace workspace;

  private final InternalDiffChooserListener diffChooserListener = new InternalDiffChooserListener();
  private final InternalButtonListener buttonListener = new InternalButtonListener();

  private final JButton addButton = new JButton(buttonListener);
  private final JButton cancelButton = new JButton(buttonListener);

  private final FileChooserPanel diffChooser =
      new FileChooserPanel("", diffChooserListener, "...", TEXTFIELD_WIDTH, ROW_HEIGHT, 0);

  private final JTextField primaryExportBinary =
      TextComponentUtils.addDefaultEditorActions(new JTextField());
  private final JTextField secondaryExportBinary =
      TextComponentUtils.addDefaultEditorActions(new JTextField());

  private final JTextField destinationDirName =
      TextComponentUtils.addDefaultEditorActions(new JTextField());

  private boolean diffPressed = false;

  public AddDiffDialog(final Window window, final Workspace workspace) {
    super(window, "Add Existing Diff");

    this.workspace = Preconditions.checkNotNull(workspace);

    init();
    pack();

    GuiHelper.centerChildToParent(window, this, true);

    setVisible(true);
  }

  private static File chooseFile(
      final Component component, final String title, final File startFolder) {
    final CFileChooser chooser =
        new CFileChooser(Constants.BINDIFF_MATCHES_DB_EXTENSION, "BinDiff Matches Database");
    chooser.setCurrentDirectory(startFolder);
    chooser.setApproveButtonText("Ok");
    chooser.setDialogTitle(title);

    if (chooser.showOpenDialog(component) == JFileChooser.APPROVE_OPTION) {
      return chooser.getSelectedFile();
    }

    return null;
  }

  private JPanel createButtonPanel() {
    addButton.setText("Add");
    cancelButton.setText("Cancel");

    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(10, 0, 5, 5));

    final JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
    buttonPanel.add(addButton);
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

    panel.add(textFieldPanel, BorderLayout.NORTH);

    return panel;
  }

  private JPanel createExportSourcesPanel() {
    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new TitledBorder("Export Binaries"));

    final JPanel sourcesPanel = new JPanel(new GridLayout(2, 1, 5, 5));
    sourcesPanel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Primary Call Graph:", LABEL_WIDTH, primaryExportBinary, ROW_HEIGHT));
    sourcesPanel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Secondary Call Graph:", LABEL_WIDTH, secondaryExportBinary, ROW_HEIGHT));

    panel.add(sourcesPanel, BorderLayout.NORTH);

    return panel;
  }

  private JPanel createFileChooserPane() {
    final JPanel panel = new JPanel(new BorderLayout());

    final JPanel chooserPanel = new JPanel(new BorderLayout());
    chooserPanel.setBorder(new TitledBorder("Diff Matches Database:"));
    chooserPanel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Choose Diff:", LABEL_WIDTH, diffChooser, ROW_HEIGHT),
        BorderLayout.NORTH);

    panel.add(chooserPanel, BorderLayout.NORTH);
    panel.add(createExportSourcesPanel(), BorderLayout.CENTER);

    return panel;
  }

  private Pair<String, String> getIDBName(final File databaseFile) throws SQLException {
    try (final MatchesDatabase database = new MatchesDatabase(databaseFile)) {

      final String[] idbNames = database.getIDBNames();

      return new Pair<>(idbNames[0], idbNames[1]);
    }
  }

  private void updatedExportedSources() {
    final File matchesBinary = new File(diffChooser.getText());
    final Pair<String, String> namesPair;

    try {
      namesPair = getIDBName(matchesBinary);
    } catch (final SQLException e) {
      CMessageBox.showInformation(
          this, "Couldn't load necessary diff information: " + e.getMessage());
      return;
    }

    final String primaryName = namesPair.first();
    final String secondaryName = namesPair.second();
    if (primaryName == null
        || "".equals(primaryName)
        || secondaryName == null
        || "".equals(secondaryName)) {
      CMessageBox.showInformation(this, "Couldn't load necessary diff information.");

      return;
    }

    final String primaryBinExport =
        String.join(
            "",
            matchesBinary.getParent(),
            File.separator,
            primaryName,
            ".",
            Constants.BINDIFF_BINEXPORT_EXTENSION);
    final String secondaryBinExport =
        String.join(
            "",
            matchesBinary.getParent(),
            File.separator,
            secondaryName,
            ".",
            Constants.BINDIFF_BINEXPORT_EXTENSION);

    if (!new File(primaryBinExport).exists()) {
      primaryExportBinary.setBackground(Colors.TABLE_CELL_PRIMARY_DEFAULT_BACKGROUND);
    } else {
      primaryExportBinary.setBackground(Colors.GRAY240);
    }

    if (!new File(secondaryBinExport).exists()) {
      secondaryExportBinary.setBackground(Colors.TABLE_CELL_PRIMARY_DEFAULT_BACKGROUND);
    } else {
      secondaryExportBinary.setBackground(Colors.GRAY240);
    }

    primaryExportBinary.setText(primaryBinExport);
    secondaryExportBinary.setText(secondaryBinExport);

    final String destinationDirName = String.format("%s vs %s", primaryName, secondaryName);

    this.destinationDirName.setText(destinationDirName);
  }

  private boolean validateDiffSources() {
    final File matchesBinary = getMatchesBinary();

    final File primaryBinExport = getBinExportBinary(ESide.PRIMARY);
    final File secondaryBinExport = getBinExportBinary(ESide.SECONDARY);

    final File destination = getDestinationDirectory();

    String errorMsg = "";

    if (matchesBinary == null) {
      errorMsg = "Can't add diff to workspace. Can't find matches binary.";
    } else if (primaryBinExport == null) {
      errorMsg = "Can't add diff to workspace. Can't find primary BinExport binary.";
    } else if (secondaryBinExport == null) {
      errorMsg = "Can't add diff to workspace. Can't find secondary BinExport binary.";
    } else if (destination == null) {
      errorMsg = "Can't add diff to workspace. Destination folder is not defined.";
    } else if (workspace.containsDiff(matchesBinary.getPath())) {
      errorMsg = "Diff is already added to the workspace.";
    } else if (destination.exists() && destination.list().length != 0) {
      if (!matchesBinary.getParent().equals(destination.getPath())) {
        errorMsg = "Can't add diff to workspace. Destination folder already exists in workspace.";
      } else if (workspace.containsDiff(matchesBinary.getPath())) {
        errorMsg = "Can't add diff to workspace. Diff is already part of the current workspace.";
      }
    } else {
      try {
        if (!destination.mkdir()) {
          errorMsg = "Can't add diff to workspace. Destination folder cannot be created.";
        }
      } catch (final Exception e) {
        // FIXME: Never catch all exceptions!
        errorMsg = "Can't add diff to workspace. Destination folder cannot be created.";
      } finally {
        // TODO: This can fail
        destination.delete();
      }
    }

    if (!errorMsg.equals("")) {
      CMessageBox.showInformation(this, errorMsg);
    }

    return errorMsg.equals("");
  }

  private void init() {
    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));

    panel.add(createFileChooserPane(), BorderLayout.NORTH);
    panel.add(createDestinationFolderPanel(), BorderLayout.CENTER);
    panel.add(createButtonPanel(), BorderLayout.SOUTH);

    primaryExportBinary.setEditable(false);
    secondaryExportBinary.setEditable(false);
    primaryExportBinary.setBorder(new LineBorder(Color.GRAY));
    secondaryExportBinary.setBorder(new LineBorder(Color.GRAY));

    add(panel, BorderLayout.CENTER);
  }

  public boolean getAddButtonPressed() {
    return diffPressed;
  }

  public File getBinExportBinary(final ESide side) {
    final String binaryPath =
        side == ESide.PRIMARY ? primaryExportBinary.getText() : secondaryExportBinary.getText();

    if (binaryPath.endsWith("." + Constants.BINDIFF_BINEXPORT_EXTENSION)) {
      final File binaryFile = new File(binaryPath);

      if (binaryFile.exists()) {
        return binaryFile;
      }
    }

    return null;
  }

  public File getDestinationDirectory() {
    if (destinationDirName.getText().equals("")) {
      return null;
    }

    return new File(
        String.join(
            "", workspace.getWorkspaceDirPath(), File.separator, destinationDirName.getText()));
  }

  public File getMatchesBinary() {
    final File matchesBinary = new File(diffChooser.getText());

    if (matchesBinary.exists()) {
      return matchesBinary;
    }

    return null;
  }

  private class InternalButtonListener extends AbstractAction {
    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getSource().equals(addButton)) {
        if (!validateDiffSources()) {
          return;
        }

        diffPressed = true;
      }

      dispose();
    }
  }

  private class InternalDiffChooserListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      final BinDiffConfig config = BinDiffConfig.getInstance();

      final File file =
          AddDiffDialog.chooseFile(
              AddDiffDialog.this,
              "Choose Diff",
              new File(config.getMainSettings().getAddExistingDiffLastDir()));

      if (file != null && file.exists()) {
        diffChooser.setText(file.getPath());
        config.getMainSettings().setAddExistingDiffLastDir(file.getParent());

        updatedExportedSources();
      }
    }
  }
}
