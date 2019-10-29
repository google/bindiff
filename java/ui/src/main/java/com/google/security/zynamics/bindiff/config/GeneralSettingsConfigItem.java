package com.google.security.zynamics.bindiff.config;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.logging.Level;
import javax.xml.xpath.XPathException;
import org.w3c.dom.Document;

/** Sets and gets the main settings via java */
public class GeneralSettingsConfigItem extends ConfigItem {

  private static final String CONFIG_FILE_FORMAT_VERSION = "/bindiff/@config-version";
  private static final int CONFIG_FILE_FORMAT_VERSION_DEFAULT = 6;
  private int configFileFormatVersion = CONFIG_FILE_FORMAT_VERSION_DEFAULT;

  private static final String SOCKET_PORT = "/bindiff/ui/@port";
  private static final int SOCKET_PORT_DEFAULT = 2000;
  private int socketPort = SOCKET_PORT_DEFAULT;

  private static final String IDA_DIRECTORY = "/bindiff/ida/@directory";
  private static final String IDA_DIRECTORY_DEFAULT = "";
  private String idaDirectory = IDA_DIRECTORY_DEFAULT;

  private static final String WORKSPACE_DIRECTORY = "/bindiff/preferences/workspace/@directory";
  private static final String WORKSPACE_DIRECTORY_DEFAULT = "";
  private String workspaceDirectory = WORKSPACE_DIRECTORY_DEFAULT;

  private static final String DEFAULT_WORKSPACE = "/bindiff/preferences/workspace/@default";
  private static final String DEFAULT_WORKSPACE_DEFAULT = "";
  private String defaultWorkspace = DEFAULT_WORKSPACE_DEFAULT;

  private static final String DIFF_ENGINE_PATH = "/bindiff/ui/@directory";
  private static final String DIFF_ENGINE_PATH_DEFAULT = "";
  private String diffEnginePath = DIFF_ENGINE_PATH_DEFAULT;

  private static final String ADD_EXISTING_DIFF_LAST_DIR =
      "/bindiff/preferences/history/entry[@for='add-existing-diff-dir']/@v";
  private static final String ADD_EXISTING_DIFF_LAST_DIR_DEFAULT = "";
  private String addExistingDiffLastDir = ADD_EXISTING_DIFF_LAST_DIR_DEFAULT;

  private static final String DIRECTORY_DIFF_LAST_PRIMARY_DIR =
      "/bindiff/preferences/history/entry[@for='directory-diff-primary-dir']/@v";
  private static final String DIRECTORY_DIFF_LAST_PRIMARY_DIR_DEFAULT = "";
  private String directoryDiffLastPrimaryDir = DIRECTORY_DIFF_LAST_PRIMARY_DIR_DEFAULT;

  private static final String DIRECTORY_DIFF_LAST_SECONDARY_DIR =
      "/bindiff/preferences/history/entry[@for='directory-diff-secondary-dir']/@v";
  private static final String DIRECTORY_DIFF_LAST_SECONDARY_DIR_DEFAULT = "";
  private String directoryDiffLastSecondaryDir = DIRECTORY_DIFF_LAST_SECONDARY_DIR_DEFAULT;

  private static final String RECENT_WORKSPACE_DIRECTORIES =
      "/bindiff/preferences/history/list[@for='workspace-dir']/entry/@v";
  private static final ImmutableList<String> RECENT_WORKSPACE_DIRECTORIES_DEFAULT =
      ImmutableList.of();
  private List<String> recentWorkspaceDirectories = RECENT_WORKSPACE_DIRECTORIES_DEFAULT;

  private static final String NEW_DIFF_LAST_PRIMARY_DIR =
      "/bindiff/preferences/history/entry[@for='new-diff-primary-dir']/@v";
  private static final String NEW_DIFF_LAST_PRIMARY_DIR_DEFAULT = "";
  private String newDiffLastPrimaryDir = NEW_DIFF_LAST_PRIMARY_DIR_DEFAULT;

  private static final String NEW_DIFF_LAST_SECONDARY_DIR =
      "/bindiff/preferences/history/entry[@for='new-diff-secondary-dir']/@v";
  private static final String NEW_DIFF_LAST_SECONDARY_DIR_DEFAULT = "";
  private String newDiffLastSecondaryDir = NEW_DIFF_LAST_SECONDARY_DIR_DEFAULT;

  private static final String LOG_LEVEL = "/bindiff/log/@level";
  private static final Level LOG_LEVEL_DEFAULT = Level.INFO;
  private Level logLevel = LOG_LEVEL_DEFAULT;

  private boolean logVerbose = false;
  private boolean logInfo = false;
  private boolean logWarning = false;
  private boolean logSevere = false;
  private boolean logException = false;
  private boolean logStacktrace = false;

  private static final String CONSOLE_LOGGING = "/bindiff/log/@to-stderr";
  private static final boolean CONSOLE_LOGGING_DEFAULT = false;
  private boolean consoleLogging = CONSOLE_LOGGING_DEFAULT;

  private static final String FILE_LOGGING = "/bindiff/log/@to-file";
  private static final boolean FILE_LOGGING_DEFAULT = false;
  private boolean fileLogging = FILE_LOGGING_DEFAULT;

  private static final String LOG_FILE_LOCATION = "/bindiff/log/@directory";
  private static final String LOG_FILE_LOCATION_DEFAULT = "";
  private String logFileLocation = LOG_FILE_LOCATION_DEFAULT;

  private static final String SCREEN_WIDTH = "/bindiff/preferences/layout/window/@screenWidth";
  private static final int SCREEN_WIDTH_DEFAULT = 0;
  private int screenWidth = SCREEN_WIDTH_DEFAULT;

  private static final String SCREEN_HEIGHT = "/bindiff/preferences/layout/window/@screenHeight";
  private static final int SCREEN_HEIGHT_DEFAULT = 0;
  private int screenHeight = SCREEN_HEIGHT_DEFAULT;

  private static final String WINDOW_X_POS = "/bindiff/preferences/layout/window/@x";
  private static final int WINDOW_X_POS_DEFAULT = 0;
  private int windowXPos = WINDOW_X_POS_DEFAULT;

  private static final String WINDOW_Y_POS = "/bindiff/preferences/layout/window/@y";
  private static final int WINDOW_Y_POS_DEFAULT = 0;
  private int windowYPos = WINDOW_Y_POS_DEFAULT;

  private static final String WINDOW_WIDTH = "/bindiff/preferences/layout/window/@width";
  private static final int WINDOW_WIDTH_DEFAULT = 800;
  private int windowWidth = WINDOW_WIDTH_DEFAULT;

  private static final String WINDOW_HEIGHT = "/bindiff/preferences/layout/window/@height";
  private static final int WINDOW_HEIGHT_DEFAULT = 600;
  private int windowHeight = WINDOW_HEIGHT_DEFAULT;

  private static final String WINDOW_STATE_WAS_MAXIMIZED =
      "/bindiff/preferences/layout/window/@maximized";
  private static final boolean WINDOW_STATE_WAS_MAXIMIZED_DEFAULT = false;
  private boolean windowStateWasMaximized = WINDOW_STATE_WAS_MAXIMIZED_DEFAULT;

  private static final String WORKSPACE_TREE_DIVIDER_POSITION =
      "/bindiff/preferences/layout/divider-position/@v";
  private static final int WORKSPACE_TREE_DIVIDER_POSITION_DEFAULT = 200;
  private int workspaceTreeDividerPosition = WORKSPACE_TREE_DIVIDER_POSITION_DEFAULT;

  @Override
  public void load(final Document doc) throws XPathException {
    configFileFormatVersion =
        getInteger(doc, CONFIG_FILE_FORMAT_VERSION, CONFIG_FILE_FORMAT_VERSION_DEFAULT);
    diffEnginePath = getString(doc, DIFF_ENGINE_PATH, DIFF_ENGINE_PATH_DEFAULT);
    defaultWorkspace = getString(doc, DEFAULT_WORKSPACE, DEFAULT_WORKSPACE_DEFAULT);
    idaDirectory = getString(doc, IDA_DIRECTORY, IDA_DIRECTORY_DEFAULT);
    workspaceDirectory = getString(doc, WORKSPACE_DIRECTORY, WORKSPACE_DIRECTORY_DEFAULT);
    recentWorkspaceDirectories =
        getStrings(doc, RECENT_WORKSPACE_DIRECTORIES, RECENT_WORKSPACE_DIRECTORIES_DEFAULT);
    socketPort = getInteger(doc, SOCKET_PORT, SOCKET_PORT_DEFAULT);
    newDiffLastPrimaryDir =
        getString(doc, NEW_DIFF_LAST_PRIMARY_DIR, NEW_DIFF_LAST_PRIMARY_DIR_DEFAULT);
    newDiffLastSecondaryDir =
        getString(doc, NEW_DIFF_LAST_SECONDARY_DIR, NEW_DIFF_LAST_SECONDARY_DIR_DEFAULT);
    directoryDiffLastPrimaryDir =
        getString(doc, DIRECTORY_DIFF_LAST_PRIMARY_DIR, DIRECTORY_DIFF_LAST_PRIMARY_DIR_DEFAULT);
    directoryDiffLastSecondaryDir =
        getString(
            doc, DIRECTORY_DIFF_LAST_SECONDARY_DIR, DIRECTORY_DIFF_LAST_SECONDARY_DIR_DEFAULT);
    addExistingDiffLastDir =
        getString(doc, ADD_EXISTING_DIFF_LAST_DIR, ADD_EXISTING_DIFF_LAST_DIR_DEFAULT);

    // getLevel() maps "debug", "info", "warning", "error" and "off" to standard Java log levels.
    logLevel = getLevel(doc, LOG_LEVEL, LOG_LEVEL_DEFAULT);
    final int logIntValue = logLevel.intValue();
    logVerbose = logIntValue <= Level.ALL.intValue();
    logInfo = logIntValue <= Level.INFO.intValue();
    logWarning = logIntValue <= Level.WARNING.intValue();
    logSevere = logIntValue <= Level.SEVERE.intValue();
    logException = logIntValue <= Level.SEVERE.intValue();
    logStacktrace = logIntValue <= Level.SEVERE.intValue();

    consoleLogging = getBoolean(doc, CONSOLE_LOGGING, CONSOLE_LOGGING_DEFAULT);
    fileLogging = getBoolean(doc, FILE_LOGGING, FILE_LOGGING_DEFAULT);
    logFileLocation = getString(doc, LOG_FILE_LOCATION, LOG_FILE_LOCATION_DEFAULT);

    windowStateWasMaximized =
        getBoolean(doc, WINDOW_STATE_WAS_MAXIMIZED, WINDOW_STATE_WAS_MAXIMIZED_DEFAULT);
    windowXPos = getInteger(doc, WINDOW_X_POS, WINDOW_X_POS_DEFAULT);
    windowYPos = getInteger(doc, WINDOW_Y_POS, WINDOW_Y_POS_DEFAULT);
    windowWidth = getInteger(doc, WINDOW_WIDTH, WINDOW_WIDTH_DEFAULT);
    windowHeight = getInteger(doc, WINDOW_HEIGHT, WINDOW_HEIGHT_DEFAULT);
    screenWidth = getInteger(doc, SCREEN_WIDTH, SCREEN_WIDTH_DEFAULT);
    screenHeight = getInteger(doc, SCREEN_HEIGHT, SCREEN_HEIGHT_DEFAULT);
    workspaceTreeDividerPosition =
        getInteger(doc, WORKSPACE_TREE_DIVIDER_POSITION, WORKSPACE_TREE_DIVIDER_POSITION_DEFAULT);
  }

  @Override
  public void store(final Document doc) throws XPathException {
    setString(doc, DIFF_ENGINE_PATH, diffEnginePath);
    setString(doc, DEFAULT_WORKSPACE, defaultWorkspace);
    setInteger(doc, CONFIG_FILE_FORMAT_VERSION, configFileFormatVersion);
    setString(doc, IDA_DIRECTORY, idaDirectory);
    setString(doc, WORKSPACE_DIRECTORY, workspaceDirectory);
    setStrings(doc, RECENT_WORKSPACE_DIRECTORIES, recentWorkspaceDirectories);
    setInteger(doc, SOCKET_PORT, socketPort);
    setString(doc, NEW_DIFF_LAST_PRIMARY_DIR, newDiffLastPrimaryDir);
    setString(doc, NEW_DIFF_LAST_SECONDARY_DIR, newDiffLastSecondaryDir);
    setString(doc, DIRECTORY_DIFF_LAST_PRIMARY_DIR, directoryDiffLastPrimaryDir);
    setString(doc, DIRECTORY_DIFF_LAST_SECONDARY_DIR, directoryDiffLastSecondaryDir);
    setString(doc, ADD_EXISTING_DIFF_LAST_DIR, addExistingDiffLastDir);

    if (logVerbose) {
      setLevel(doc, LOG_LEVEL, Level.ALL);
    } else if (logInfo) {
      setLevel(doc, LOG_LEVEL, Level.INFO);
    } else if (logWarning) {
      setLevel(doc, LOG_LEVEL, Level.WARNING);
    } else if (logSevere || logException || logStacktrace) {
      setLevel(doc, LOG_LEVEL, Level.SEVERE);
    } else {
      setLevel(doc, LOG_LEVEL, Level.OFF);
    }

    setBoolean(doc, CONSOLE_LOGGING, consoleLogging);
    setBoolean(doc, FILE_LOGGING, fileLogging);
    setString(doc, LOG_FILE_LOCATION, logFileLocation);

    setBoolean(doc, WINDOW_STATE_WAS_MAXIMIZED, windowStateWasMaximized);
    setInteger(doc, WINDOW_X_POS, windowXPos);
    setInteger(doc, WINDOW_Y_POS, windowYPos);
    setInteger(doc, WINDOW_WIDTH, windowWidth);
    setInteger(doc, WINDOW_HEIGHT, windowHeight);
    setInteger(doc, SCREEN_WIDTH, screenWidth);
    setInteger(doc, SCREEN_HEIGHT, screenHeight);
    setInteger(doc, WORKSPACE_TREE_DIVIDER_POSITION, workspaceTreeDividerPosition);
  }

  public int getVersion() {
    return configFileFormatVersion;
  }

  public void setVersion(final int configFileFormatVersion) {
    this.configFileFormatVersion = configFileFormatVersion;
  }

  public String getDefaultWorkspace() {
    return defaultWorkspace;
  }

  public void setDefaultWorkspace(final String defaultWorkspace) {
    this.defaultWorkspace = defaultWorkspace;
  }

  public String getDiffEnginePath() {
    return diffEnginePath;
  }

  public void setDiffEnginePath(final String diffEnginePath) {
    this.diffEnginePath = diffEnginePath;
  }

  public final String getIdaDirectory() {
    return idaDirectory;
  }

  public void setIdaDirectory(final String idaDirectory) {
    this.idaDirectory = idaDirectory;
  }

  public final String getWorkspaceDirectory() {
    return workspaceDirectory;
  }

  public void setWorkspaceDirectory(final String workspaceDirectory) {
    this.workspaceDirectory = workspaceDirectory;
  }

  public List<String> getRecentWorkspaceDirectories() {
    return recentWorkspaceDirectories;
  }

  public void setRecentWorkspaceDirectories(final List<String> recentWorkspaceDirectories) {
    this.recentWorkspaceDirectories = recentWorkspaceDirectories;
  }

  public final int getSocketPort() {
    return socketPort;
  }

  public final void setSocketPort(final int socketPort) {
    this.socketPort = socketPort;
  }

  public final String getNewDiffLastPrimaryDir() {
    return newDiffLastPrimaryDir;
  }

  public final void setNewDiffLastPrimaryDir(final String newDiffLastPrimaryDir) {
    this.newDiffLastPrimaryDir = newDiffLastPrimaryDir;
  }

  public final String getNewDiffLastSecondaryDir() {
    return newDiffLastSecondaryDir;
  }

  public final void setNewDiffLastSecondaryDir(final String newDiffLastSecondaryDir) {
    this.newDiffLastSecondaryDir = newDiffLastSecondaryDir;
  }

  public final String getDirectoryDiffLastPrimaryDir() {
    return directoryDiffLastPrimaryDir;
  }

  public final void setDirectoryDiffLastPrimaryDir(final String directoryDiffLastPrimaryDir) {
    this.directoryDiffLastPrimaryDir = directoryDiffLastPrimaryDir;
  }

  public final String getDirectoryDiffLastSecondaryDir() {
    return directoryDiffLastSecondaryDir;
  }

  public final void setDirectoryDiffLastSecondaryDir(final String directoryDiffLastSecondaryDir) {
    this.directoryDiffLastSecondaryDir = directoryDiffLastSecondaryDir;
  }

  public final String getAddExistingDiffLastDir() {
    return addExistingDiffLastDir;
  }

  public final void setAddExistingDiffLastDir(final String addExistingDiffLastDir) {
    this.addExistingDiffLastDir = addExistingDiffLastDir;
  }

  public final Level getLogLevel() {
    return logLevel;
  }

  public final void setLogLevel(final Level logLevel) {
    this.logLevel = logLevel;
  }

  public final boolean getConsoleLogging() {
    return consoleLogging;
  }

  public final void setConsoleLogging(final boolean consoleLogging) {
    this.consoleLogging = consoleLogging;
  }

  public final boolean getFileLogging() {
    return fileLogging;
  }

  public final void setFileLogging(final boolean fileLogging) {
    this.fileLogging = fileLogging;
  }

  public final String getLogFileLocation() {
    return logFileLocation;
  }

  public final void setLogFileLocation(final String logFileLocation) {
    this.logFileLocation = logFileLocation;
  }

  public final boolean getLogVerbose() {
    return logVerbose;
  }

  public final void setLogVerbose(final boolean logVerbose) {
    this.logVerbose = logVerbose;
  }

  public final boolean getLogInfo() {
    return logInfo;
  }

  public final void setLogInfo(final boolean logInfo) {
    this.logInfo = logInfo;
  }

  public final boolean getLogWarning() {
    return logWarning;
  }

  public final void setLogWarning(final boolean logWarning) {
    this.logWarning = logWarning;
  }

  public final boolean getLogSevere() {
    return logSevere;
  }

  public final void setLogSevere(final boolean logSevere) {
    this.logSevere = logSevere;
  }

  public final boolean getLogException() {
    return logException;
  }

  public final void setLogException(final boolean logException) {
    this.logException = logException;
  }

  public final boolean getLogStacktrace() {
    return logStacktrace;
  }

  public final void setLogStacktrace(final boolean logStacktrace) {
    this.logStacktrace = logStacktrace;
  }

  public final boolean getWindowStateWasMaximized() {
    return windowStateWasMaximized;
  }

  public final void setWindowStateWasMaximized(final boolean windowStateWasMaximized) {
    this.windowStateWasMaximized = windowStateWasMaximized;
  }

  public final int getWindowXPos() {
    return windowXPos;
  }

  public final void setWindowXPos(final int windowXPos) {
    this.windowXPos = windowXPos;
  }

  public final int getWindowYPos() {
    return windowYPos;
  }

  public final void setWindowYPos(final int windowYPos) {
    this.windowYPos = windowYPos;
  }

  public final int getWindowWidth() {
    return windowWidth;
  }

  public final void setWindowWidth(final int windowWidth) {
    this.windowWidth = windowWidth;
  }

  public final int getWindowHeight() {
    return windowHeight;
  }

  public final void setWindowHeight(final int windowHeight) {
    this.windowHeight = windowHeight;
  }

  public final int getScreenWidth() {
    return screenWidth;
  }

  public final void setScreenWidth(final int screenWidth) {
    this.screenWidth = screenWidth;
  }

  public final int getScreenHeight() {
    return screenHeight;
  }

  public final void setScreenHeight(final int screenHeight) {
    this.screenHeight = screenHeight;
  }

  public final int getWorkspaceTreeDividerPosition() {
    return workspaceTreeDividerPosition;
  }

  public final void setWorkspaceTreeDividerPosition(final int workspaceTreeDividerPosition) {
    this.workspaceTreeDividerPosition = workspaceTreeDividerPosition;
  }
}
