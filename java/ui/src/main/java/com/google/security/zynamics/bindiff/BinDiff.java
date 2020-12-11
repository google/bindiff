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

import static com.google.common.base.StandardSystemProperty.JAVA_VERSION;

import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.config.GeneralSettingsConfigItem;
import com.google.security.zynamics.bindiff.config.ThemeConfigItem;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.window.MainWindow;
import com.google.security.zynamics.bindiff.logging.Logger;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.bindiff.socketserver.SocketServer;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.io.FileUtils;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import javax.swing.InputMap;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Launcher class for BinDiff. Note: The name of this class is used directly as the application name
 * on macOS.
 */
public class BinDiff {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private static final Option HELP =
      Option.builder().longOpt("help").desc("Print this message").build();
  private static final Option CONSOLE_LOGGING =
      Option.builder("c").longOpt("logtostderr").desc("Enable console logging").build();
  private static final Option FILE_LOGGING =
      Option.builder("f").longOpt("log_file").hasArg().argName("FILE").desc("Log to file").build();
  private static final Option DEBUG_MENU =
      Option.builder().longOpt("debug_menu").desc("Display the \"Debug\" menu").build();

  private static boolean desktopIntegrationDone = false;

  private BinDiff() {}

  static {
    try {
      // Set the default for JRE managed UI scaling.
      if (System.getProperty("sun.java2d.uiScale.enabled") == null) {
        System.setProperty("sun.java2d.uiScale.enabled", "true");
      }

      // Use Apple L&F screen menu bar if available. This property must be set before any frames
      // are displayed. Setting the Apple-specific properties has no effect on other platforms.
      System.setProperty("apple.laf.useScreenMenuBar", "true");
      System.setProperty("com.apple.macos.useScreenMenuBar", "true");
      System.setProperty("com.apple.mrj.application.live-resize", "true");

      // Set application name
      System.setProperty(
          "com.apple.mrj.application.apple.menu.about.name", Constants.DEFAULT_WINDOW_TITLE);
    } catch (final SecurityException e) {
      // Do nothing. macOS integration will be sub-optimal but there's nothing we can meaningfully
      // do here.
    }
  }

  public static boolean isDesktopIntegrationDone() {
    return desktopIntegrationDone;
  }

  private static void initializeConfigFile() {
    final BinDiffConfig config = BinDiffConfig.getInstance();
    if (config.getMainSettings().getVersion() < Constants.CONFIG_FILEVERSION) {
      final int answer =
          CMessageBox.showYesNoWarning(
              null,
              "Your configuration file is obsolete. Do you want to overwrite it with a new "
                  + "default configuration file?");
      if (answer == JOptionPane.YES_OPTION) {
        try {
          Files.delete(FileSystems.getDefault().getPath(BinDiffConfig.getConfigFileName()));
        } catch (final IOException | SecurityException e) {
          // Logger isn't initialized yet, so no logging here
          CMessageBox.showError(
              null,
              "Couldn't delete old configuration file. "
                  + "BinDiff won't be able to save its configuration.");
        }
      }
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

  /**
   * Parses command line arguments.
   *
   * @param args the arguments, as passed into {@link #main(String[])}.
   * @return a list of positional arguments without any options or {@code null} on error.
   */
  private static String[] parseCommandLine(final String[] args) {
    final Options flags =
        new Options()
            .addOption(HELP)
            .addOption(CONSOLE_LOGGING)
            .addOption(FILE_LOGGING)
            .addOption(DEBUG_MENU);
    final CommandLine cmd;
    final String[] positional;
    try {
      cmd = new DefaultParser().parse(flags, args);
      positional = cmd.getArgs();
      if (positional.length > 1) {
        throw new ParseException("More than one workspace file specified");
      }
    } catch (final ParseException e) {
      logger.at(Level.WARNING).withCause(e).log("Invalid command line arguments");
      return null;
    }

    if (cmd.hasOption(HELP.getLongOpt())) {
      new HelpFormatter().printHelp("bindiff --ui [OPTION]... [WORKSPACE]", flags);
      System.exit(0);
    }

    Logger.setConsoleLogging(cmd.hasOption(CONSOLE_LOGGING.getLongOpt()));
    if (cmd.hasOption(FILE_LOGGING.getLongOpt())) {
      try {
        final String logDir = cmd.getOptionValue(FILE_LOGGING.getLongOpt());
        Logger.setFileHandler(
            new FileHandler(logDir.isEmpty() ? Logger.getDefaultLoggingDirectoryPath() : logDir));
      } catch (final IOException e) {
        logger.at(Level.WARNING).withCause(e).log("Could not create log file handler");
      }
    }

    if (cmd.hasOption(DEBUG_MENU.getLongOpt())) {
      BinDiffConfig.getInstance().getDebugSettings().setShowMenu(true);
    }

    return positional;
  }

  public static void applyLoggingChanges() throws IOException {
    logger.at(Level.INFO).log("Applying logger changes...");
    final GeneralSettingsConfigItem settings = BinDiffConfig.getInstance().getMainSettings();
    Logger.setConsoleLogging(settings.getConsoleLogging());

    final String path = FileUtils.ensureTrailingSlash(settings.getLogFileLocation());
    if (!new File(path).isDirectory()) {
      throw new IOException("Not a directory: " + path);
    }

    Logger.setFileHandler(new FileHandler(path + Constants.LOG_FILE_NAME));
    Logger.setLogLevel(settings.getLogLevel());
  }

  private static void initializeSocketServer(
      final Component window, final WorkspaceTabPanelFunctions controller) {
    // Wrapped in invokeLater to make sure we start the thread after the
    // UI is ready. This is done because the SocketServer relies on
    // a valid parent window for the workspace tree.
    SwingUtilities.invokeLater(
        () -> {
          final int port = Constants.getSocketPort();
          try {
            new SocketServer(port, controller).startListening();
          } catch (final IOException e) {
            CMessageBox.showError(
                window, String.format("Could not listen on port %s. BinDiff exits.", port));
            System.exit(1);
          }
        });
  }

  private static void initializeDesktop(
      final Desktop desktop, final WorkspaceTabPanelFunctions controller) {
    // The API below needs at least JDK9. Since Google's language level is still at Java 8, fall
    // back to using reflection.
    // Note that any failures are silently ignored. Like with the menu bar integration in the
    // static initializer of this class, the app won't feel native, but will be fully functional.
    // TODO(cblichmann): Once Java 11 is the default, replace the reflective calls.
    try {
      final Class<?> aboutHandlerClass = Class.forName("java.awt.desktop.AboutHandler");
      final Object aboutHandler =
          Proxy.newProxyInstance(
              aboutHandlerClass.getClassLoader(),
              new java.lang.Class<?>[] {aboutHandlerClass},
              (proxy, method, args) -> {
                controller.showAboutDialog();
                return null;
              });
      Desktop.class
          .getDeclaredMethod("setAboutHandler", aboutHandlerClass)
          .invoke(desktop, aboutHandler);

      final Class<?> preferencesHandlerClass = Class.forName("java.awt.desktop.PreferencesHandler");
      final Object preferencesHandler =
          Proxy.newProxyInstance(
              preferencesHandlerClass.getClassLoader(),
              new java.lang.Class<?>[] {preferencesHandlerClass},
              (proxy, method, args) -> {
                controller.showMainSettingsDialog();
                return null;
              });
      Desktop.class
          .getDeclaredMethod("setPreferencesHandler", preferencesHandlerClass)
          .invoke(desktop, preferencesHandler);

      final Class<?> quitHandlerClass = Class.forName("java.awt.desktop.QuitHandler");
      final Object quitHandler =
          Proxy.newProxyInstance(
              quitHandlerClass.getClassLoader(),
              new java.lang.Class<?>[] {quitHandlerClass},
              (proxy, method, args) -> {
                controller.exitBinDiff();
                return null;
              });
      Desktop.class
          .getDeclaredMethod("setQuitHandler", quitHandlerClass)
          .invoke(desktop, quitHandler);

      desktopIntegrationDone = true;
    } catch (final ReflectiveOperationException e) {
      logger.at(Level.WARNING).withCause(e).log();
    }
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

          // The config file must be initialized before the logger
          initializeConfigFile();
          initializeLogging();

          logger.at(Level.INFO).log(
              "Starting %s (Java %s)", Constants.PRODUCT_NAME_VERSION, JAVA_VERSION.value());

          initializeTheme();
          final String[] positional = parseCommandLine(args);
          initializeGlobalTooltipDelays();
          initializeStandardHotKeys();

          final Workspace workspace = new Workspace();
          final MainWindow window = new MainWindow(workspace);
          window.setVisible(true);
          GuiHelper.applyWindowFix(window);

          final WorkspaceTabPanelFunctions controller =
              window.getController().getTabPanelManager().getWorkspaceTabPanel().getController();

          initializeSocketServer(window, controller);

          if (Desktop.isDesktopSupported()) {
            initializeDesktop(Desktop.getDesktop(), controller);
          }

          if (positional != null && positional.length > 0) {
            controller.loadWorkspace(positional[0]);
          }
        });
  }
}
