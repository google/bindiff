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
public class MainSettingsDialog extends BaseDialog {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private static final int DIALOG_WIDTH = 600;
  private static final int DIALOG_HEIGHT = 430;

  private final GeneralPanel generalPanel = new GeneralPanel();
  private final LoggingPanel loggingPanel = new LoggingPanel();
  private final SyntaxHighlightingPanel syntaxHighlightingPanel = new SyntaxHighlightingPanel();

  private final CPanelTwoButtons buttons =
      new CPanelTwoButtons(new InternalButtonListener(), "Ok", "Cancel");

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
      logger.at(Level.SEVERE).withCause(e).log("Couldn't create file logger");
      CMessageBox.showError(this, "Couldn't create file logger.");
    }
  }

  private void save() throws IOException {
    final BinDiffConfig config = BinDiffConfig.getInstance();
    final GeneralSettingsConfigItem mainSettings = config.getMainSettings();
    final ThemeConfigItem colorSettings = config.getThemeSettings();

    // General Panel
    mainSettings.setIdaDirectory(generalPanel.getIDADirectory());
    mainSettings.setWorkspaceDirectory(generalPanel.getWorkspaceDirectory());

    // Logging Panel
    mainSettings.setConsoleLogging(loggingPanel.getConsoleLogging());
    mainSettings.setFileLogging(loggingPanel.getFileLogging());
    mainSettings.setLogFileLocation(loggingPanel.getLogFileLocation());
    mainSettings.setLogLevel(loggingPanel.getLogLevel());

    // Syntax Highlighting Panel
    colorSettings.setAddressColor(syntaxHighlightingPanel.getAddressColor());
    colorSettings.setMnemonicColor(syntaxHighlightingPanel.getMnemonicColor());
    colorSettings.setRegisterColor(syntaxHighlightingPanel.getRegisterColor());
    colorSettings.setOperatorColor(syntaxHighlightingPanel.getOperatorColor());
    colorSettings.setSizePrefixColor(syntaxHighlightingPanel.getSizePrefixColor());
    colorSettings.setDereferenceColor(syntaxHighlightingPanel.getDereferenceColor());
    colorSettings.setImmediateColor(syntaxHighlightingPanel.getImmediateColor());
    colorSettings.setOperatorSeparatorColor(syntaxHighlightingPanel.getOperandSeparatorColor());
    colorSettings.setCommentColor(syntaxHighlightingPanel.getCommentColor());
    colorSettings.setDefaultColor(syntaxHighlightingPanel.getDefaultColor());
    colorSettings.setSymbolColor(syntaxHighlightingPanel.getSymbolColor());
    colorSettings.setStackVariableColor(syntaxHighlightingPanel.getStackVariableColor());
    colorSettings.setGlobalVariableColor(syntaxHighlightingPanel.getGlobalVariableColor());
    colorSettings.setJumpLabelColor(syntaxHighlightingPanel.getJumpLabelColor());
    colorSettings.setFunctionColor(syntaxHighlightingPanel.getFunctionColor());

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

  private class InternalButtonListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getSource() == buttons.getFirstButton()) {
        try {
          save();
        } catch (final IOException e) {
          logger.at(Level.SEVERE).withCause(e).log("Couldn't save main settings");
          CMessageBox.showError(MainSettingsDialog.this, "Couldn't save main settings.");
        }
      }

      dispose();
    }
  }
}
