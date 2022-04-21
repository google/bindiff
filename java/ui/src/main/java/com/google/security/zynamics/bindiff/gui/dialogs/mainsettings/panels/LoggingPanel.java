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

package com.google.security.zynamics.bindiff.gui.dialogs.mainsettings.panels;

import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.config.GeneralSettingsConfigItem;
import com.google.security.zynamics.bindiff.logging.Logger;
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
import java.util.logging.Level;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

public class LoggingPanel extends JPanel {
  private static final int LABEL_WIDTH = 275;
  private static final int ROW_HEIGHT = 25;
  private static final int NUMBER_OF_ROWS_PANEL_1 = 3;
  private static final int NUMBER_OF_ROWS_PANEL_2 = 6;

  private final JCheckBox consoleLogging = new JCheckBox();
  private final JCheckBox fileLogging = new JCheckBox();
  private final JComboBox<String> logLevel =
      new JComboBox<>(
          new String[] {"Debug" /* Index 0 */, "Info", "Warning", "Error", "Off" /* 4 */});

  private FileChooserPanel logFileLocationPanel;

  public LoggingPanel() {
    super(new BorderLayout());
    init();
  }

  private static String selectLogFileDirectory(final Container parent) {
    final DirectoryChooser chooser = new DirectoryChooser("Select Logger Directory");
    return chooser.showOpenDialog(parent) == DirectoryChooser.APPROVE_OPTION
        ? chooser.getSelectedFile().getAbsolutePath()
        : null;
  }

  private JPanel createLoggingDetailPanel() {
    final JPanel panel = new JPanel(new GridLayout(NUMBER_OF_ROWS_PANEL_2, 1, 5, 5));
    panel.setBorder(new TitledBorder("Detail"));

    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Log level:", LABEL_WIDTH, logLevel, ROW_HEIGHT));

    return panel;
  }

  private JPanel createLoggingPanel() {
    final BinDiffConfig config = BinDiffConfig.getInstance();
    final GeneralSettingsConfigItem settings = config.getMainSettings();

    String logFileLocation = settings.getLogFileLocation();
    if ("".equals(logFileLocation)) {
      logFileLocation = Logger.getDefaultLoggingDirectoryPath();
    }
    logFileLocationPanel =
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
            "Logger directory:", LABEL_WIDTH, this.logFileLocationPanel, ROW_HEIGHT));

    return panel;
  }

  private void init() {
    final JPanel mainPanel = new JPanel(new BorderLayout());
    final JPanel innerMainPanel = new JPanel(new GridBagLayout());

    final GridBagConstraints cbc = new GridBagConstraints();
    cbc.gridx = 0;
    cbc.anchor = GridBagConstraints.FIRST_LINE_START;
    cbc.weightx = 1;
    cbc.fill = GridBagConstraints.HORIZONTAL;

    cbc.gridy = 0;
    innerMainPanel.add(createLoggingPanel(), cbc);

    cbc.gridy = 1;
    innerMainPanel.add(createLoggingDetailPanel(), cbc);

    mainPanel.add(innerMainPanel, BorderLayout.NORTH);
    add(new JScrollPane(mainPanel));

    setCurrentValues();
  }

  public boolean getConsoleLogging() {
    return consoleLogging.isSelected();
  }

  public boolean getFileLogging() {
    return fileLogging.isSelected();
  }

  public String getLogFileLocation() {
    return logFileLocationPanel.getText();
  }

  private static int levelToIndex(Level level) {
    if (level.equals(Level.FINEST)) {
      return 0;
    }
    if (level.equals(Level.INFO)) {
      return 1;
    }
    if (level.equals(Level.WARNING)) {
      return 2;
    }
    if (level.equals(Level.SEVERE)) {
      return 3;
    }
    if (level.equals(Level.OFF)) {
      return 4;
    }
    return 1;
  }

  public Level getLogLevel() {
    switch (logLevel.getSelectedIndex()) {
      case 0:
        return Level.FINEST;
      case 2:
        return Level.WARNING;
      case 3:
        return Level.SEVERE;
      case 4:
        return Level.OFF;
      case 1:
      default:
        return Level.INFO;
    }
  }

  public void setCurrentValues() {
    final BinDiffConfig config = BinDiffConfig.getInstance();
    final GeneralSettingsConfigItem settings = config.getMainSettings();

    consoleLogging.setSelected(settings.getConsoleLogging());
    fileLogging.setSelected(settings.getFileLogging());

    logLevel.setSelectedIndex(levelToIndex(settings.getLogLevel()));
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
