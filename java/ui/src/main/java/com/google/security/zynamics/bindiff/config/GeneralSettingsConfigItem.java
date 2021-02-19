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

package com.google.security.zynamics.bindiff.config;

import java.util.List;
import java.util.logging.Level;

/** Sets and gets the main settings via java */
public class GeneralSettingsConfigItem {

  public int getVersion() {
    return Config.getInstance().getVersion();
  }

  public void setVersion(final int version) {
    Config.getInstance().setVersion(version);
  }

  public String getDefaultWorkspace() {
    return Config.getInstance().getPreferences().getDefaultWorkspace();
  }

  public void setDefaultWorkspace(final String defaultWorkspace) {
    Config.getInstance().getPreferencesBuilder().setDefaultWorkspace(defaultWorkspace);
  }

  public String getBinDiffDirectory() {
    return Config.getInstance().getDirectory();
  }

  public void setBinDiffDirectory(final String diffEnginePath) {
    Config.getInstance().setDirectory(diffEnginePath);
  }

  public String getIdaDirectory() {
    return Config.getInstance().getIda().getDirectory();
  }

  public void setIdaDirectory(final String idaDirectory) {
    Config.getInstance().getIdaBuilder().setDirectory(idaDirectory);
  }

  public final String getWorkspaceDirectory() {
    return ""; // TODO(cblichmann): Implement/check
  }

  public void setWorkspaceDirectory(final String workspaceDirectory) {
    // TODO(cblichmann): Implement/check
    // Config.getInstance().getPreferencesBuilder().setWorkspace(workspaceDirectory);
  }

  public List<String> getRecentWorkspaceDirectories() {
    return Config.getInstance().getPreferences().getHistory().getWorkspaceDirList();
  }

  public void setRecentWorkspaceDirectories(final List<String> recentWorkspaceDirectories) {
    Config.getInstance()
        .getPreferencesBuilder()
        .getHistoryBuilder()
        .clearWorkspaceDir()
        .addAllWorkspaceDir(recentWorkspaceDirectories);
  }

  public final int getSocketPort() {
    return Config.getInstance().getUi().getPort();
  }

  public final void setSocketPort(final int socketPort) {
    Config.getInstance().getUiBuilder().setPort(socketPort);
  }

  public final String getNewDiffLastPrimaryDir() {
    return Config.getInstance().getPreferences().getHistory().getNewDiffPrimaryDir();
  }

  public final void setNewDiffLastPrimaryDir(final String newDiffLastPrimaryDir) {
    Config.getInstance()
        .getPreferencesBuilder()
        .getHistoryBuilder()
        .setNewDiffPrimaryDir(newDiffLastPrimaryDir);
  }

  public final String getNewDiffLastSecondaryDir() {
    return Config.getInstance().getPreferences().getHistory().getNewDiffSecondaryDir();
  }

  public final void setNewDiffLastSecondaryDir(final String newDiffLastSecondaryDir) {
    Config.getInstance()
        .getPreferencesBuilder()
        .getHistoryBuilder()
        .setNewDiffSecondaryDir(newDiffLastSecondaryDir);
  }

  public final String getDirectoryDiffLastPrimaryDir() {
    return Config.getInstance().getPreferences().getHistory().getDirectoryDiffPrimaryDir();
  }

  public final void setDirectoryDiffLastPrimaryDir(final String directoryDiffLastPrimaryDir) {
    Config.getInstance()
        .getPreferencesBuilder()
        .getHistoryBuilder()
        .setDirectoryDiffPrimaryDir(directoryDiffLastPrimaryDir);
  }

  public final String getDirectoryDiffLastSecondaryDir() {
    return Config.getInstance().getPreferences().getHistory().getDirectoryDiffSecondaryDir();
  }

  public final void setDirectoryDiffLastSecondaryDir(final String directoryDiffLastSecondaryDir) {
    Config.getInstance()
        .getPreferencesBuilder()
        .getHistoryBuilder()
        .setDirectoryDiffSecondaryDir(directoryDiffLastSecondaryDir);
  }

  public final String getAddExistingDiffLastDir() {
    return Config.getInstance().getPreferences().getHistory().getAddExistingDiffDir();
  }

  public final void setAddExistingDiffLastDir(final String addExistingDiffLastDir) {
    Config.getInstance()
        .getPreferencesBuilder()
        .getHistoryBuilder()
        .setAddExistingDiffDir(addExistingDiffLastDir);
  }

  public final Level getLogLevel() {
    return Config.fromProtoLogLevel(Config.getInstance().getLog().getLevel());
  }

  public final void setLogLevel(final Level logLevel) {
    Config.getInstance().getLogBuilder().setLevel(Config.fromLogLevel(logLevel));
  }

  public final boolean getConsoleLogging() {
    return Config.getInstance().getLog().getToStderr();
  }

  public final void setConsoleLogging(final boolean consoleLogging) {
    Config.getInstance().getLogBuilder().setToStderr(consoleLogging);
  }

  public final boolean getFileLogging() {
    return Config.getInstance().getLog().getToFile();
  }

  public final void setFileLogging(final boolean fileLogging) {
    Config.getInstance().getLogBuilder().setToFile(fileLogging);
  }

  public final String getLogFileLocation() {
    return Config.getInstance().getLog().getDirectory();
  }

  public final void setLogFileLocation(final String logFileLocation) {
    Config.getInstance().getLogBuilder().setDirectory(logFileLocation);
  }

  public final boolean getWindowStateWasMaximized() {
    return Config.getInstance().getPreferences().getLayout().getMaximized();
  }

  public final void setWindowStateWasMaximized(final boolean windowStateWasMaximized) {
    Config.getInstance()
        .getPreferencesBuilder()
        .getLayoutBuilder()
        .setMaximized(windowStateWasMaximized);
  }

  public final int getWindowXPos() {
    return Config.getInstance().getPreferences().getLayout().getX();
  }

  public final void setWindowXPos(final int windowXPos) {
    Config.getInstance().getPreferencesBuilder().getLayoutBuilder().setX(windowXPos);
  }

  public final int getWindowYPos() {
    return Config.getInstance().getPreferences().getLayout().getY();
  }

  public final void setWindowYPos(final int windowYPos) {
    Config.getInstance().getPreferencesBuilder().getLayoutBuilder().setY(windowYPos);
  }

  public final int getWindowWidth() {
    return Config.getInstance().getPreferences().getLayout().getWidth();
  }

  public final void setWindowWidth(final int windowWidth) {
    Config.getInstance().getPreferencesBuilder().getLayoutBuilder().setWidth(windowWidth);
  }

  public final int getWindowHeight() {
    return Config.getInstance().getPreferences().getLayout().getHeight();
  }

  public final void setWindowHeight(final int windowHeight) {
    Config.getInstance().getPreferencesBuilder().getLayoutBuilder().setHeight(windowHeight);
  }

  public final int getScreenWidth() {
    return Config.getInstance().getPreferences().getLayout().getScreenWidth();
  }

  public final void setScreenWidth(final int screenWidth) {
    Config.getInstance().getPreferencesBuilder().getLayoutBuilder().setScreenWidth(screenWidth);
  }

  public final int getScreenHeight() {
    return Config.getInstance().getPreferences().getLayout().getScreenHeight();
  }

  public final void setScreenHeight(final int screenHeight) {
    Config.getInstance().getPreferencesBuilder().getLayoutBuilder().setScreenHeight(screenHeight);
  }

  public final int getWorkspaceTreeDividerPosition() {
    return Config.getInstance().getPreferences().getLayout().getDividerPosition();
  }

  public final void setWorkspaceTreeDividerPosition(final int workspaceTreeDividerPosition) {
    Config.getInstance()
        .getPreferencesBuilder()
        .getLayoutBuilder()
        .setDividerPosition(workspaceTreeDividerPosition);
  }
}
