// Copyright 2011-2023 Google LLC
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

package com.google.security.zynamics.bindiff.config;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toCollection;

import com.google.common.flogger.FluentLogger;
import com.google.common.io.Files;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.google.security.zynamics.bindiff.BinDiffProtos;
import com.google.security.zynamics.bindiff.BinDiffProtos.Config.LogOptions.LogLevel;
import com.google.security.zynamics.bindiff.BinDiffProtos.Config.MatchingStep;
import com.google.security.zynamics.bindiff.BinDiffProtos.Config.UiPreferences.HistoryOptions;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.bindiff.utils.ResourceUtils;
import com.google.security.zynamics.zylib.system.SystemHelpers;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

/** JSON configuration parser. */
public class Config {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  public static final BinDiffProtos.Config DEFAULTS;
  private static BinDiffProtos.Config.Builder instance;

  private Config() {}

  static {
    try {
      DEFAULTS = loadFromJson(ResourceUtils.getResourceAsString("config/bindiff.json")).build();
    } catch (IOException e) {
      // Throw if the embedded default string does not parse or the resource cannot be found.
      throw new AssertionError(e);
    }
  }

  public static String getUserConfigFilename() {
    return SystemHelpers.getApplicationDataDirectory(Constants.PRODUCT_NAME)
        + Constants.CONFIG_FILENAME;
  }

  public static BinDiffProtos.Config.Builder getInstance() {
    if (instance != null) {
      return instance;
    }
    instance = BinDiffProtos.Config.newBuilder(DEFAULTS);

    try {
      var commonConfig =
          new File(
              SystemHelpers.getAllUsersApplicationDataDirectory(Constants.PRODUCT_NAME)
                  + Constants.CONFIG_FILENAME);
      mergeInto(loadFromFile(commonConfig).build(), instance);
    } catch (IOException e) {
      // Just log at a debug level
      logger.atFine().withCause(e).log("Cannot load per-machine config");
    }

    try {
      var userConfig = new File(getUserConfigFilename());
      mergeInto(loadFromFile(userConfig).build(), instance);
    } catch (IOException e) {
      // Just log at a debug level
      logger.atFine().withCause(e).log("Cannot load per-user config");
    }

    return instance;
  }

  private static void normalizeRecentWorkspaces(BinDiffProtos.Config.Builder config) {
    HistoryOptions.Builder history = config.getPreferencesBuilder().getHistoryBuilder();
    List<String> normalized =
        history.getRecentWorkspaceList().stream()
            .filter(workspace -> !workspace.strip().isEmpty() && new File(workspace).isFile())
            .distinct()
            .collect(Collectors.toList());
    history.clearRecentWorkspace();
    history.addAllRecentWorkspace(normalized);
  }

  public static BinDiffProtos.Config.Builder loadFromJson(String data)
      throws InvalidProtocolBufferException {
    var builder = BinDiffProtos.Config.newBuilder();
    JsonFormat.parser().ignoringUnknownFields().merge(data, builder);
    normalizeRecentWorkspaces(builder);
    return builder;
  }

  public static BinDiffProtos.Config.Builder loadFromFile(File file) throws IOException {
    return loadFromJson(Files.asCharSource(file, UTF_8).read());
  }

  public static String getAsJsonString(BinDiffProtos.ConfigOrBuilder config)
      throws InvalidProtocolBufferException {
    return JsonFormat.printer()
        .includingDefaultValueFields()
        .preservingProtoFieldNames()
        .print(config);
  }

  public static void saveUserConfig(BinDiffProtos.Config.Builder config) throws IOException {
    var userConfig = new File(getUserConfigFilename()).getCanonicalFile();
    userConfig.getParentFile().mkdirs(); // Ensure the config directory exists
    normalizeRecentWorkspaces(config);
    Files.asCharSink(userConfig, UTF_8).write(getAsJsonString(config));
  }

  public static void mergeInto(BinDiffProtos.Config from, BinDiffProtos.Config.Builder config) {
    // Keep this code in sync with the implementation in `config.cc`.

    // Copy and clear the problematic fields
    List<MatchingStep> functionMatching = config.getFunctionMatchingList();
    List<MatchingStep> basicBlockMatching = config.getBasicBlockMatchingList();
    config.clearFunctionMatching();
    config.clearBasicBlockMatching();

    // Let Protobuf handle the actual merge
    config.mergeFrom(from);

    HashSet<String> names =
        config.getFunctionMatchingList().stream()
            .map(MatchingStep::getName)
            .collect(toCollection(HashSet::new));
    if (names.isEmpty() || names.size() != config.getFunctionMatchingCount()) {
      // Duplicate or no algorithms, restore original list
      config.addAllFunctionMatching(functionMatching);
    }

    names =
        config.getBasicBlockMatchingList().stream()
            .map(MatchingStep::getName)
            .collect(toCollection(HashSet::new));
    if (names.isEmpty() || names.size() != config.getBasicBlockMatchingCount()) {
      config.addAllBasicBlockMatching(basicBlockMatching);
    }
  }

  public static Level fromProtoLogLevel(BinDiffProtos.Config.LogOptions.LogLevel level) {
    switch (level) {
      case DEBUG:
        return Level.FINEST;
      case INFO:
        return Level.INFO;
      case WARNING:
        return Level.WARNING;
      case ERROR:
        return Level.SEVERE;
      case OFF:
      case LOG_LEVEL_UNSPECIFIED:
      case UNRECOGNIZED:
        break;
    }
    return Level.OFF;
  }

  public static BinDiffProtos.Config.LogOptions.LogLevel fromLogLevel(Level level) {
    // Java     Level.intValue()   BinDiff
    // -----------------------------------
    // OFF      Integer.MAX_VALUE  OFF
    // SEVERE                1000  ERROR
    // WARNING                900  WARNING
    // INFO                   800  INFO
    // CONFIG                 700  INFO
    // FINE                   500  DEBUG
    // FINER                  400  DEBUG
    // FINEST                 300  DEBUG
    // ALL      Integer.MIN_VALUE  DEBUG
    var value = level.intValue();
    if (value < Level.CONFIG.intValue()) {
      return LogLevel.DEBUG;
    }
    if (value < Level.WARNING.intValue()) {
      return LogLevel.INFO;
    }
    if (value < Level.SEVERE.intValue()) {
      return LogLevel.WARNING;
    }
    if (value < Level.OFF.intValue()) {
      return LogLevel.ERROR;
    }
    return LogLevel.OFF;
  }

  public static String formatColor(Color color) {
    return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
  }
}
