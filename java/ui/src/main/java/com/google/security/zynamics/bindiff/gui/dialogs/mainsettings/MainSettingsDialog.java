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

package com.google.security.zynamics.bindiff.gui.dialogs.mainsettings;

import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.BinDiff;
import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.config.GeneralSettingsConfigItem;
import com.google.security.zynamics.bindiff.config.ThemeConfigItem;
import com.google.security.zynamics.bindiff.gui.dialogs.BaseDialog;
import com.google.security.zynamics.bindiff.gui.dialogs.mainsettings.panels.GeneralPanel;
import com.google.security.zynamics.bindiff.gui.dialogs.mainsettings.panels.LoggingPanel;
import com.google.security.zynamics.bindiff.gui.dialogs.mainsettings.panels.SyntaxHighlightingPanel;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.CPanelTwoButtons;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

/** General application settings dialog. */
public class MainSettingsDialog extends BaseDialog implements ActionListener {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private static final int DIALOG_WIDTH = 600;
  private static final int DIALOG_HEIGHT = 430;

  private final GeneralPanel generalPanel = new GeneralPanel();
  private final LoggingPanel loggingPanel = new LoggingPanel();
  private final SyntaxHighlightingPanel syntaxHighlightingPanel = new SyntaxHighlightingPanel();

  private final CPanelTwoButtons buttons = new CPanelTwoButtons(this, "Ok", "Cancel");

  private boolean cancelled = true;

  public MainSettingsDialog(final Window parent) {
    super(parent, "Main Settings");

    init();
    pack();

    setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
    setMinimumSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));

    GuiHelper.centerChildToParent(parent, this, true);
  }

  private void adoptChanges() {
    try {
      BinDiff.applyLoggingChanges();
    } catch (final SecurityException | IOException e) {
      final String message = "Couldn't create file logger";
      logger.at(Level.SEVERE).withCause(e).log(message);
      CMessageBox.showError(this, message);
    }
  }

  private void save() throws IOException {
    final BinDiffConfig config = BinDiffConfig.getInstance();
    final GeneralSettingsConfigItem mainSettings = config.getMainSettings();
    final ThemeConfigItem themeSettings = config.getThemeSettings();

    // General Panel
    mainSettings.setIdaDirectory(generalPanel.getIdaDirectory());
    mainSettings.setWorkspaceDirectory(generalPanel.getWorkspaceDirectory());

    // Logging Panel
    mainSettings.setConsoleLogging(loggingPanel.getConsoleLogging());
    mainSettings.setFileLogging(loggingPanel.getFileLogging());
    mainSettings.setLogFileLocation(loggingPanel.getLogFileLocation());
    mainSettings.setLogLevel(loggingPanel.getLogLevel());

    // Syntax Highlighting Panel
    themeSettings.setAddressColor(syntaxHighlightingPanel.getAddressColor());
    themeSettings.setMnemonicColor(syntaxHighlightingPanel.getMnemonicColor());
    themeSettings.setRegisterColor(syntaxHighlightingPanel.getRegisterColor());
    themeSettings.setOperatorColor(syntaxHighlightingPanel.getOperatorColor());
    themeSettings.setSizePrefixColor(syntaxHighlightingPanel.getSizePrefixColor());
    themeSettings.setDereferenceColor(syntaxHighlightingPanel.getDereferenceColor());
    themeSettings.setImmediateColor(syntaxHighlightingPanel.getImmediateColor());
    themeSettings.setOperatorSeparatorColor(syntaxHighlightingPanel.getOperandSeparatorColor());
    themeSettings.setCommentColor(syntaxHighlightingPanel.getCommentColor());
    themeSettings.setDefaultColor(syntaxHighlightingPanel.getDefaultColor());
    themeSettings.setSymbolColor(syntaxHighlightingPanel.getSymbolColor());
    themeSettings.setStackVariableColor(syntaxHighlightingPanel.getStackVariableColor());
    themeSettings.setGlobalVariableColor(syntaxHighlightingPanel.getGlobalVariableColor());
    themeSettings.setJumpLabelColor(syntaxHighlightingPanel.getJumpLabelColor());
    themeSettings.setFunctionColor(syntaxHighlightingPanel.getFunctionColor());
    themeSettings.apply();

    adoptChanges();
    config.write();
  }

  private void init() {
    final JTabbedPane tabbedPane = new JTabbedPane();

    tabbedPane.addTab("General", generalPanel);
    tabbedPane.addTab("Logging", loggingPanel);
    tabbedPane.addTab("Syntax Highlighting", syntaxHighlightingPanel);

    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(1, 1, 1, 1));

    panel.add(tabbedPane, BorderLayout.CENTER);
    panel.add(buttons, BorderLayout.SOUTH);

    add(panel, BorderLayout.CENTER);
  }

  @Override
  public void setVisible(final boolean visible) {
    generalPanel.setCurrentValues();
    loggingPanel.setCurrentValues();
    syntaxHighlightingPanel.setCurrentValues();

    super.setVisible(visible);
  }

  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    if (event.getSource() == buttons.getFirstButton()) {
      try {
        save();
        cancelled = false;
      } catch (final IOException e) {
        final String message = "Couldn't save main settings";
        logger.at(Level.SEVERE).withCause(e).log(message);
        CMessageBox.showError(this, message);
      }
    }
    dispose();
  }
}
