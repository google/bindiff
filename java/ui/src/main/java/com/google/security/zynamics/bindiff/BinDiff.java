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

package com.google.security.zynamics.bindiff;

import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.config.GeneralSettingsConfigItem;
import com.google.security.zynamics.bindiff.config.ThemeConfigItem;
import com.google.security.zynamics.bindiff.gui.window.MainWindow;
import com.google.security.zynamics.bindiff.logging.Logger;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.bindiff.socketserver.SocketServer;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import javax.swing.InputMap;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

/**
 * Launcher class for BinDiff.
 * Note: The name of this class is used directly as the application name on macOS.
 */
public class BinDiff {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private static String workspaceFileName = null;

  private BinDiff() {}

  static {
    // Use Apple L&F screen menu bar if available. This property must be set before any frames are
    // displayed. Setting the Apple-specific properties has no effect on other platforms.
    try {
      System.setProperty("apple.laf.useScreenMenuBar", "true");
      System.setProperty("com.apple.macos.useScreenMenuBar", "true");

      // Set application name
      System.setProperty(
          "com.apple.mrj.application.apple.menu.about.name", Constants.DEFAULT_WINDOW_TITLE);
    } catch (final SecurityException e) {
      // Do nothing. macOS integration will be sub-optimal but there's nothing we can meaningfully
      // do here.
    }
  }

  private static boolean initializeConfigFile() {
    final BinDiffConfig config = BinDiffConfig.getInstance();
    try {
      config.read();
      if (config.getMainSettings().getVersion() < Constants.CONFIG_FILEVERSION) {
        final int answer =
            CMessageBox.showYesNoWarning(
                null,
                "Your configuration file is obsolete. Do you want to overwrite it with a new "
                    + "default configuration file?");
        try {
          if (answer == JOptionPane.YES_OPTION) {
            BinDiffConfig.delete();
          }
        } catch (final IOException e) {
          // Logger isn't initialized, so no logging here
          CMessageBox.showError(null, "Couldn't delete configuration file.");
          return false;
        }
      }
      return true;
    } catch (final IOException e) {
      logger.at(Level.SEVERE).withCause(e).log("Error while parsing configuration file");
      CMessageBox.showError(null, e.getMessage());
      return false;
    }
  }

  private static void initializeTheme() {
    final ThemeConfigItem theme = BinDiffConfig.getInstance().getThemeSettings();

    final String[] allFonts =
        GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    Arrays.sort(allFonts);

    final Font uiFont = theme.getUiFont();
    if (Arrays.binarySearch(allFonts, uiFont.getFamily()) >= 0) {
      GuiHelper.setDefaultFont(uiFont);
    } else {
      logger.at(Level.WARNING).log("Font not installed/found: %s", uiFont.getFontName());
    }
    final Font codeFont = theme.getCodeFont();
    if (Arrays.binarySearch(allFonts, codeFont.getFamily()) >= 0) {
      GuiHelper.setMonospacedFont(codeFont);
    } else {
      logger.at(Level.WARNING).log("Font not installed/found: %s", codeFont.getFontName());
    }
    for (final String fontName :
        new String[] {
          "Button.font",
          "CheckBox.font",
          "CheckBoxMenuItem.font",
          "ColorChooser.font",
          "ComboBox.font",
          "DesktopIcon.font",
          "InternalFrame.font",
          "InternalFrame.titleFont",
          "Label.font",
          "List.font",
          "Menu.font",
          "MenuBar.font",
          "MenuItem.font",
          "OptionPane.font",
          "Panel.font",
          "PasswordField.font",
          "PopupMenu.font",
          "ProgressBar.font",
          "RadioButton.font",
          "RadioButtonMenuItem.font",
          "ScrollPane.font",
          "TabbedPane.font",
          "Table.font",
          "TableHeader.font",
          "Text.font",
          "TextArea.font",
          "TextField.font",
          "TitledBorder.font",
          "ToggleButton.font",
          "ToolBar.font",
          "ToolTip.font",
          "Tree.font",
          "Viewport.font"
        }) {
      UIManager.put(fontName, GuiHelper.getDefaultFont());
    }
  }

  private static void initializeGlobalTooltipDelays() {
    final ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
    toolTipManager.setDismissDelay(60000);
    toolTipManager.setInitialDelay(1250);
    toolTipManager.setReshowDelay(500);
  }

  private static void initializeLogging() {
    final GeneralSettingsConfigItem settings = BinDiffConfig.getInstance().getMainSettings();

    FileHandler fileHandler;
    try {
      fileHandler = new FileHandler(Logger.getLoggingFilePath(settings.getLogFileLocation()));
      Logger.setFileHandler(fileHandler);
    } catch (final IOException | SecurityException e) {
      CMessageBox.showWarning(
          null, "Failed to initialize file logger. Could not create log file handler.");
    }

    // Do not move these two lines before setFileHandler
    Logger.setConsoleLogging(settings.getConsoleLogging());
    Logger.setFileLogging(settings.getFileLogging());

    Logger.setLogLevel(settings.getLogLevel());
  }

  private static void initializeStandardHotKeys() {
    final InputMap splitPaneMap = (InputMap) UIManager.get("SplitPane.ancestorInputMap");
    for (int functionKey = KeyEvent.VK_F1; functionKey <= KeyEvent.VK_F12; functionKey++) {
      splitPaneMap.remove(KeyStroke.getKeyStroke(functionKey, 0));
    }
  }

  private static void parseCommandLine(final String[] args) {
    for (final String arg : args) {
      if ("-c".equals(arg)) {
        Logger.setConsoleLogging(true);
      } else if ("-f".equals(arg)) {
        final FileHandler filehandler;
        try {
          filehandler = new FileHandler(Logger.getDefaultLoggingDirectoryPath());
        } catch (final IOException e) {
          logger.at(Level.WARNING).withCause(e).log("Could not create log file handler");
          continue;
        }
        Logger.setFileHandler(filehandler);
      } else if (workspaceFileName == null && !arg.startsWith("-")) {
        workspaceFileName = arg;
      }
    }
  }

  public static void applyLoggingChanges() throws IOException {
    logger.at(Level.INFO).log("Applying logger changes...");
    final GeneralSettingsConfigItem settings = BinDiffConfig.getInstance().getMainSettings();
    Logger.setConsoleLogging(settings.getConsoleLogging());

    String path = settings.getLogFileLocation();
    if (path != null && path.length() > 1) {
      final char chr = path.charAt(path.length() - 1);
      if (!File.separator.equals(String.valueOf(chr))) {
        path += File.separator;
      }
    }
    if (path == null || !new File(path).exists()) {
      throw new IOException();
    }

    Logger.setFileHandler(new FileHandler(path + Constants.LOG_FILE_NAME));
    Logger.setLogLevel(settings.getLogLevel());
  }

  private static void initializeSocketServer(final MainWindow window) {
    // Wrapped in invokeLater to make sure we start the thread after the
    // UI is ready. This is done because the SocketServer relies on
    // a valid parent window for the workspace tree.
    SwingUtilities.invokeLater(
        () -> {
          final int port = Constants.getSocketPort();
          try {
            new SocketServer(
                    port,
                    // TODO(cblichmann): Get rid of this madness and do proper MVC
                    window
                        .getController()
                        .getTabPanelManager()
                        .getWorkspaceTabPanel()
                        .getController())
                .startListening();
          } catch (final IOException e) {
            CMessageBox.showError(
                window, String.format("Could not listen on port %s. BinDiff exits.", port));
            System.exit(1);
          }
        });
  }

  /**
   * Program entry point. Sets up the configuration file, initializes the logging system and creates
   * the UI.
   *
   * @param args command line arguments.
   */
  public static void main(final String[] args) {
    SwingUtilities.invokeLater(
        () -> {
          // Set default window title for message boxes
          System.setProperty(
              CMessageBox.DEFAULT_WINDOW_TITLE_PROPERTY, Constants.DEFAULT_WINDOW_TITLE);

          // The config file must be initialized before the logger is initialized
          if (!initializeConfigFile()) {
            return;
          }
          initializeLogging();

          logger.at(Level.INFO).log("Starting %s", Constants.PRODUCT_NAME_VERSION);

          initializeTheme();
          parseCommandLine(args);
          initializeGlobalTooltipDelays();
          initializeStandardHotKeys();

          final Workspace workspace = new Workspace();
          final MainWindow window = new MainWindow(workspace);

          workspace.setParentWindow(window);
          window.setVisible(true);
          GuiHelper.applyWindowFix(window);

          initializeSocketServer(window);

          if (workspaceFileName != null) {
            // TODO(cblichmann): Either do proper MVC or use dependency injection instead.
            window
                .getController()
                .getTabPanelManager()
                .getWorkspaceTabPanel()
                .getController()
                .loadWorkspace(workspaceFileName);
          }
        });
  }
}
