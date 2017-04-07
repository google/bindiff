package com.google.security.zynamics.bindiff.config;

import javax.xml.xpath.XPathException;
import org.w3c.dom.Document;

/** Sets and gets the main settings via java */
public class GeneralSettingsConfigItem extends ConfigItem {

  private static final String CONFIG_FILE_FORMAT_VERSION = "/BinDiff/@configVersion";
  private static final int CONFIG_FILE_FORMAT_VERSION_DEFAULT = 4;
  private int configFileFormatVersion = CONFIG_FILE_FORMAT_VERSION_DEFAULT;

  private static final String SOCKET_PORT = "/BinDiff/Gui/@port";
  private static final int SOCKET_PORT_DEFAULT = 2000;
  private int socketPort = SOCKET_PORT_DEFAULT;

  private static final String IDA_DIRECTORY = "/BinDiff/Ida/@directory";
  private static final String IDA_DIRECTORY_DEFAULT = "";
  private String idaDirectory = IDA_DIRECTORY_DEFAULT;

  private static final String WORKSPACE_DIRECTORY = "/BinDiff/Workspace/@directory";
  private static final String WORKSPACE_DIRECTORY_DEFAULT = "";
  private String workspaceDirectory = WORKSPACE_DIRECTORY_DEFAULT;

  private static final String DEFAULT_WORKSPACE = "/BinDiff/Workspace/@default";
  private static final String DEFAULT_WORKSPACE_DEFAULT = "";
  private String defaultWorkspace = DEFAULT_WORKSPACE_DEFAULT;

  private static final String DIFF_ENGINE_PATH = "/BinDiff/Engine/@path";
  private static final String DIFF_ENGINE_PATH_DEFAULT = "";
  private String diffEnginePath = DIFF_ENGINE_PATH_DEFAULT;

  private static final String ADD_EXISTING_DIFF_LAST_DIR =
      "/BinDiff/History/Entry[@key='AddExistingDiffLastDir']/@value";
  private static final String ADD_EXISTING_DIFF_LAST_DIR_DEFAULT = "";
  private String addExistingDiffLastDir = ADD_EXISTING_DIFF_LAST_DIR_DEFAULT;

  private static final String DIRECTORY_DIFF_LAST_PRIMARY_DIR =
      "/BinDiff/History/Entry[@key='DirectoryDiffLastPrimaryDir']/@value";
  private static final String DIRECTORY_DIFF_LAST_PRIMARY_DIR_DEFAULT = "";
  private String directoryDiffLastPrimaryDir = DIRECTORY_DIFF_LAST_PRIMARY_DIR_DEFAULT;

  private static final String DIRECTORY_DIFF_LAST_SECONDARY_DIR =
      "/BinDiff/History/Entry[@key='DirectoryDiffLastSecondaryDir']/@value";
  private static final String DIRECTORY_DIFF_LAST_SECONDARY_DIR_DEFAULT = "";
  private String directoryDiffLastSecondaryDir = DIRECTORY_DIFF_LAST_SECONDARY_DIR_DEFAULT;

  private static final String LAST_WORKSPACE_DIRECTORY_1 =
      "/BinDiff/History/Entry[@key='LastWorkspaceDir1']/@value";
  private static final String LAST_WORKSPACE_DIRECTORY_1_DEFAULT = "";
  private String lastWorkspaceDirectory1 = LAST_WORKSPACE_DIRECTORY_1_DEFAULT;

  private static final String LAST_WORKSPACE_DIRECTORY_2 =
      "/BinDiff/History/Entry[@key='LastWorkspaceDir2']/@value";
  private static final String LAST_WORKSPACE_DIRECTORY_2_DEFAULT = "";
  private String lastWorkspaceDirectory2 = LAST_WORKSPACE_DIRECTORY_2_DEFAULT;

  private static final String LAST_WORKSPACE_DIRECTORY_3 =
      "/BinDiff/History/Entry[@key='LastWorkspaceDir3']/@value";
  private static final String LAST_WORKSPACE_DIRECTORY_3_DEFAULT = "";
  private String lastWorkspaceDirectory3 = LAST_WORKSPACE_DIRECTORY_3_DEFAULT;

  private static final String LAST_WORKSPACE_DIRECTORY_4 =
      "/BinDiff/History/Entry[@key='LastWorkspaceDir4']/@value";
  private static final String LAST_WORKSPACE_DIRECTORY_4_DEFAULT = "";
  private String lastWorkspaceDirectory4 = LAST_WORKSPACE_DIRECTORY_4_DEFAULT;

  private static final String NEW_DIFF_LAST_PRIMARY_DIR =
      "/BinDiff/History/Entry[@key='NewDiffLastPrimaryDir']/@value";
  private static final String NEW_DIFF_LAST_PRIMARY_DIR_DEFAULT = "";
  private String newDiffLastPrimaryDir = NEW_DIFF_LAST_PRIMARY_DIR_DEFAULT;

  private static final String NEW_DIFF_LAST_SECONDARY_DIR =
      "/BinDiff/History/Entry[@key='NewDiffLastSecondaryDir']/@value";
  private static final String NEW_DIFF_LAST_SECONDARY_DIR_DEFAULT = "";
  private String newDiffLastSecondaryDir = NEW_DIFF_LAST_SECONDARY_DIR_DEFAULT;

  private static final String CONSOLE_LOGGING = "/BinDiff/Logging/ConsoleLogging/@value";
  private static final boolean CONSOLE_LOGGING_DEFAULT = false;
  private boolean consoleLogging = CONSOLE_LOGGING_DEFAULT;

  private static final String FILE_LOGGING = "/BinDiff/Logging/FileLogging/@value";
  private static final boolean FILE_LOGGING_DEFAULT = false;
  private boolean fileLogging = FILE_LOGGING_DEFAULT;

  private static final String LOG_FILE_LOCATION = "/BinDiff/Logging/LogFileLocation/@value";
  private static final String LOG_FILE_LOCATION_DEFAULT = "";
  private String logFileLocation = LOG_FILE_LOCATION_DEFAULT;

  private static final String LOG_VERBOSE = "/BinDiff/Logging/LogVerbose/@value";
  private static final boolean LOG_VERBOSE_DEFAULT = false;
  private boolean logVerbose = LOG_VERBOSE_DEFAULT;

  private static final String LOG_INFO = "/BinDiff/Logging/LogInfo/@value";
  private static final boolean LOG_INFO_DEFAULT = false;
  private boolean logInfo = LOG_INFO_DEFAULT;

  private static final String LOG_WARNING = "/BinDiff/Logging/LogWarning/@value";
  private static final boolean LOG_WARNING_DEFAULT = false;
  private boolean logWarning = LOG_WARNING_DEFAULT;

  private static final String LOG_SEVERE = "/BinDiff/Logging/LogSevere/@value";
  private static final boolean LOG_SEVERE_DEFAULT = false;
  private boolean logSevere = LOG_SEVERE_DEFAULT;

  private static final String LOG_EXCEPTION = "/BinDiff/Logging/LogException/@value";
  private static final boolean LOG_EXCEPTION_DEFAULT = false;
  private boolean logException = LOG_EXCEPTION_DEFAULT;

  private static final String LOG_STACKTRACE = "/BinDiff/Logging/LogStacktrace/@value";
  private static final boolean LOG_STACKTRACE_DEFAULT = false;
  private boolean logStacktrace = LOG_STACKTRACE_DEFAULT;

  private static final String SCREEN_WIDTH = "/BinDiff/Layout/Screen/@width";
  private static final int SCREEN_WIDTH_DEFAULT = 0;
  private int screenWidth = SCREEN_WIDTH_DEFAULT;

  private static final String SCREEN_HEIGHT = "/BinDiff/Layout/Screen/@height";
  private static final int SCREEN_HEIGHT_DEFAULT = 0;
  private int screenHeight = SCREEN_HEIGHT_DEFAULT;

  private static final String WINDOW_X_POS = "/BinDiff/Layout/Window/@x";
  private static final int WINDOW_X_POS_DEFAULT = 0;
  private int windowXPos = WINDOW_X_POS_DEFAULT;

  private static final String WINDOW_Y_POS = "/BinDiff/Layout/Window/@y";
  private static final int WINDOW_Y_POS_DEFAULT = 0;
  private int windowYPos = WINDOW_Y_POS_DEFAULT;

  private static final String WINDOW_WIDTH = "/BinDiff/Layout/Window/@width";
  private static final int WINDOW_WIDTH_DEFAULT = 800;
  private int windowWidth = WINDOW_WIDTH_DEFAULT;

  private static final String WINDOW_HEIGHT = "/BinDiff/Layout/Window/@height";
  private static final int WINDOW_HEIGHT_DEFAULT = 600;
  private int windowHeight = WINDOW_HEIGHT_DEFAULT;

  private static final String WINDOW_STATE_WAS_MAXIMIZED = "/BinDiff/Layout/Window/@maximized";
  private static final boolean WINDOW_STATE_WAS_MAXIMIZED_DEFAULT = false;
  private boolean windowStateWasMaximized = WINDOW_STATE_WAS_MAXIMIZED_DEFAULT;

  private static final String WORKSPACE_TREE_DIVIDER_POSITION =
      "/BinDiff/Layout/WorkspaceTreeDividerPosition/@value";
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
    lastWorkspaceDirectory1 =
        getString(doc, LAST_WORKSPACE_DIRECTORY_1, LAST_WORKSPACE_DIRECTORY_1_DEFAULT);
    lastWorkspaceDirectory2 =
        getString(doc, LAST_WORKSPACE_DIRECTORY_2, LAST_WORKSPACE_DIRECTORY_2_DEFAULT);
    lastWorkspaceDirectory3 =
        getString(doc, LAST_WORKSPACE_DIRECTORY_3, LAST_WORKSPACE_DIRECTORY_3_DEFAULT);
    lastWorkspaceDirectory4 =
        getString(doc, LAST_WORKSPACE_DIRECTORY_4, LAST_WORKSPACE_DIRECTORY_4_DEFAULT);
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

    consoleLogging = getBoolean(doc, CONSOLE_LOGGING, CONSOLE_LOGGING_DEFAULT);
    fileLogging = getBoolean(doc, FILE_LOGGING, FILE_LOGGING_DEFAULT);
    logFileLocation = getString(doc, LOG_FILE_LOCATION, LOG_FILE_LOCATION_DEFAULT);
    logVerbose = getBoolean(doc, LOG_VERBOSE, LOG_VERBOSE_DEFAULT);
    logInfo = getBoolean(doc, LOG_INFO, LOG_INFO_DEFAULT);
    logWarning = getBoolean(doc, LOG_WARNING, LOG_WARNING_DEFAULT);
    logSevere = getBoolean(doc, LOG_SEVERE, LOG_SEVERE_DEFAULT);
    logException = getBoolean(doc, LOG_EXCEPTION, LOG_EXCEPTION_DEFAULT);
    logStacktrace = getBoolean(doc, LOG_STACKTRACE, LOG_STACKTRACE_DEFAULT);

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
    getString(doc, DIFF_ENGINE_PATH, diffEnginePath);
    setString(doc, DEFAULT_WORKSPACE, defaultWorkspace);
    setInteger(doc, CONFIG_FILE_FORMAT_VERSION, configFileFormatVersion);
    setString(doc, IDA_DIRECTORY, idaDirectory);
    setString(doc, WORKSPACE_DIRECTORY, workspaceDirectory);
    getString(doc, LAST_WORKSPACE_DIRECTORY_1, lastWorkspaceDirectory1);
    getString(doc, LAST_WORKSPACE_DIRECTORY_2, lastWorkspaceDirectory2);
    getString(doc, LAST_WORKSPACE_DIRECTORY_3, lastWorkspaceDirectory3);
    getString(doc, LAST_WORKSPACE_DIRECTORY_4, lastWorkspaceDirectory4);
    setInteger(doc, SOCKET_PORT, socketPort);
    setString(doc, NEW_DIFF_LAST_PRIMARY_DIR, newDiffLastPrimaryDir);
    setString(doc, NEW_DIFF_LAST_SECONDARY_DIR, newDiffLastSecondaryDir);
    setString(doc, DIRECTORY_DIFF_LAST_PRIMARY_DIR, directoryDiffLastPrimaryDir);
    setString(doc, DIRECTORY_DIFF_LAST_SECONDARY_DIR, directoryDiffLastSecondaryDir);
    setString(doc, ADD_EXISTING_DIFF_LAST_DIR, addExistingDiffLastDir);

    setBoolean(doc, CONSOLE_LOGGING, consoleLogging);
    setBoolean(doc, FILE_LOGGING, fileLogging);
    setString(doc, LOG_FILE_LOCATION, logFileLocation);
    setBoolean(doc, LOG_VERBOSE, logVerbose);
    setBoolean(doc, LOG_INFO, logInfo);
    setBoolean(doc, LOG_WARNING, logWarning);
    setBoolean(doc, LOG_SEVERE, logSevere);
    setBoolean(doc, LOG_EXCEPTION, logException);
    setBoolean(doc, LOG_STACKTRACE, logStacktrace);

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

  public String getLastWorkspaceDirectory1() {
    return lastWorkspaceDirectory1;
  }

  public void setLastWorkspaceDirectory1(final String lastWorkspaceDirectory1) {
    this.lastWorkspaceDirectory1 = lastWorkspaceDirectory1;
  }

  public String getLastWorkspaceDirectory2() {
    return lastWorkspaceDirectory2;
  }

  public void setLastWorkspaceDirectory2(final String lastWorkspaceDirectory2) {
    this.lastWorkspaceDirectory2 = lastWorkspaceDirectory2;
  }

  public String getLastWorkspaceDirectory3() {
    return lastWorkspaceDirectory3;
  }

  public void setLastWorkspaceDirectory3(final String lastWorkspaceDirectory3) {
    this.lastWorkspaceDirectory3 = lastWorkspaceDirectory3;
  }

  public String getLastWorkspaceDirectory4() {
    return lastWorkspaceDirectory4;
  }

  public void setLastWorkspaceDirectory4(final String lastWorkspaceDirectory4) {
    this.lastWorkspaceDirectory4 = lastWorkspaceDirectory4;
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
