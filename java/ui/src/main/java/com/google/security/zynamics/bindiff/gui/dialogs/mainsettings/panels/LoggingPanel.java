package com.google.security.zynamics.bindiff.gui.dialogs.mainsettings.panels;

import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.config.GeneralSettingsConfigItem;
import com.google.security.zynamics.bindiff.log.Logger;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import com.google.security.zynamics.zylib.gui.FileChooser.FileChooserPanel;
import com.google.security.zynamics.zylib.io.DirectoryChooser;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

public class LoggingPanel extends JPanel {
  private static final int LABEL_WIDTH = 275;
  private static final int ROW_HEIGHT = 25;
  private static final int NUMBER_OF_ROWS_PANEL_1 = 3;
  private static final int NUMBER_OF_ROWS_PANEL_2 = 6;

  private final JComboBox<String> consoleLogging = new JComboBox<>();
  private final JComboBox<String> fileLogging = new JComboBox<>();
  private final JComboBox<String> logVerbose = new JComboBox<>();
  private final JComboBox<String> logInfo = new JComboBox<>();
  private final JComboBox<String> logWarning = new JComboBox<>();
  private final JComboBox<String> logSevere = new JComboBox<>();
  private final JComboBox<String> logExceptions = new JComboBox<>();
  private final JComboBox<String> logStacktrace = new JComboBox<>();

  private FileChooserPanel logFileLocationPanel;

  public LoggingPanel() {
    super(new BorderLayout());
    init();
  }

  private static String selectLogFileDirectory(final Container parent) {
    final DirectoryChooser selecter = new DirectoryChooser("Select Log Directory");

    if (selecter.showOpenDialog(parent) == DirectoryChooser.APPROVE_OPTION) {
      return selecter.getSelectedFile().getAbsolutePath();
    }

    return null;
  }

  private JPanel createLoggingDetailPanel() {
    logVerbose.addItem("On");
    logVerbose.addItem("Off");

    logInfo.addItem("On");
    logInfo.addItem("Off");

    logWarning.addItem("On");
    logWarning.addItem("Off");

    logSevere.addItem("On");
    logSevere.addItem("Off");

    logExceptions.addItem("On");
    logExceptions.addItem("Off");

    logStacktrace.addItem("On");
    logStacktrace.addItem("Off");

    final JPanel panel = new JPanel(new GridLayout(NUMBER_OF_ROWS_PANEL_2, 1, 5, 5));
    panel.setBorder(new TitledBorder("Detail"));

    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Verbose logging:", LABEL_WIDTH, logVerbose, ROW_HEIGHT));
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Log infos:", LABEL_WIDTH, logInfo, ROW_HEIGHT));
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Log warnings:", LABEL_WIDTH, logWarning, ROW_HEIGHT));
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Log severe warnings:", LABEL_WIDTH, logSevere, ROW_HEIGHT));
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Log exceptions:", LABEL_WIDTH, logExceptions, ROW_HEIGHT));
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Log stacktraces:", LABEL_WIDTH, logStacktrace, ROW_HEIGHT));

    return panel;
  }

  private JPanel createLoggingPanel() {
    final BinDiffConfig config = BinDiffConfig.getInstance();
    final GeneralSettingsConfigItem settings = config.getMainSettings();

    consoleLogging.addItem("On");
    consoleLogging.addItem("Off");
    fileLogging.addItem("On");
    fileLogging.addItem("Off");

    String logFileLocation = settings.getLogFileLocation();
    if ("".equals(logFileLocation)) {
      logFileLocation = Logger.getDefaultLoggingDirectoryPath();
    }
    this.logFileLocationPanel =
        new FileChooserPanel(
            logFileLocation, new InternalLogFileDirectoryListener(), "...", 0, ROW_HEIGHT, 0);

    final JPanel panel = new JPanel(new GridLayout(NUMBER_OF_ROWS_PANEL_1, 1, 5, 5));
    panel.setBorder(new TitledBorder("Activation"));

    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Console logging:", LABEL_WIDTH, consoleLogging, ROW_HEIGHT));
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "File logging:", LABEL_WIDTH, fileLogging, ROW_HEIGHT));
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Log directory:", LABEL_WIDTH, this.logFileLocationPanel, ROW_HEIGHT));

    return panel;
  }

  private void init() {
    final JPanel mainPanel = new JPanel(new BorderLayout());
    final JPanel innerMainPanel = new JPanel(new GridBagLayout());
    final GridBagConstraints cbc = new GridBagConstraints();

    cbc.gridx = 0;
    cbc.gridy = 0;
    cbc.anchor = GridBagConstraints.FIRST_LINE_START;
    cbc.weightx = 1;
    cbc.fill = GridBagConstraints.HORIZONTAL;

    innerMainPanel.add(createLoggingPanel(), cbc);

    cbc.gridy = 1;

    innerMainPanel.add(createLoggingDetailPanel(), cbc);

    mainPanel.add(innerMainPanel, BorderLayout.NORTH);

    add(new JScrollPane(mainPanel));

    setCurrentValues();
  }

  public boolean getConsoleLogging() {
    return consoleLogging.getSelectedIndex() == 0;
  }

  public boolean getFileLogging() {
    return fileLogging.getSelectedIndex() == 0;
  }

  public boolean getLogException() {
    return logExceptions.getSelectedIndex() == 0;
  }

  public String getLogFileLocation() {
    return logFileLocationPanel.getText();
  }

  public boolean getLogInfo() {
    return logInfo.getSelectedIndex() == 0;
  }

  public boolean getLogSevere() {
    return logSevere.getSelectedIndex() == 0;
  }

  public boolean getLogStacktrace() {
    return logStacktrace.getSelectedIndex() == 0;
  }

  public boolean getLogVerbose() {
    return logVerbose.getSelectedIndex() == 0;
  }

  public boolean getLogWarning() {
    return logWarning.getSelectedIndex() == 0;
  }

  public void setCurrentValues() {
    final BinDiffConfig config = BinDiffConfig.getInstance();
    final GeneralSettingsConfigItem settings = config.getMainSettings();

    consoleLogging.setSelectedIndex(settings.getConsoleLogging() ? 0 : 1);
    fileLogging.setSelectedIndex(settings.getFileLogging() ? 0 : 1);

    logVerbose.setSelectedIndex(settings.getLogVerbose() ? 0 : 1);
    logInfo.setSelectedIndex(settings.getLogInfo() ? 0 : 1);
    logWarning.setSelectedIndex(settings.getLogWarning() ? 0 : 1);
    logSevere.setSelectedIndex(settings.getLogSevere() ? 0 : 1);
    logExceptions.setSelectedIndex(settings.getLogException() ? 0 : 1);
    logStacktrace.setSelectedIndex(settings.getLogStacktrace() ? 0 : 1);
  }

  private class InternalLogFileDirectoryListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      final String workspaceDirectoryPath = selectLogFileDirectory(getParent());

      if (workspaceDirectoryPath != null) {
        logFileLocationPanel.setText(workspaceDirectoryPath);
      }
    }
  }
}
