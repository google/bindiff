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

import com.google.common.flogger.FluentLogger;
import java.io.IOException;

/** A class that is used to read and write the BinDiff configuration. */
public final class BinDiffConfig {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  /** This class is a singleton. */
  private static final BinDiffConfig instance = new BinDiffConfig();

  private final GeneralSettingsConfigItem mainSettings;
  private final DebugConfigItem debugSettings;
  private final ThemeConfigItem colorSettings;
  private final GraphViewSettingsConfigItem initialCallGraphSettings;
  private final GraphViewSettingsConfigItem initialFlowGraphSettings;

  /** Creates a new config object. */
  private BinDiffConfig() {
    mainSettings = new GeneralSettingsConfigItem();
    debugSettings = new DebugConfigItem();
    colorSettings = new ThemeConfigItem();
    initialCallGraphSettings = new InitialCallGraphViewSettingsConfigItem();
    initialFlowGraphSettings = new InitialFlowGraphViewSettingsConfigItem();
  }

  /** Returns the only valid instance of the configuration file. */
  public static BinDiffConfig getInstance() {
    return instance;
  }

  public GeneralSettingsConfigItem getMainSettings() {
    return mainSettings;
  }

  public DebugConfigItem getDebugSettings() {
    return debugSettings;
  }

  public ThemeConfigItem getThemeSettings() {
    return colorSettings;
  }

  public GraphViewSettingsConfigItem getInitialCallGraphSettings() {
    return initialCallGraphSettings;
  }

  public GraphViewSettingsConfigItem getInitialFlowGraphSettings() {
    return initialFlowGraphSettings;
  }

  /** Writes the configuration file to disk. */
  public void write() throws IOException {
    logger.atInfo().log("Saving configuration...");
    Config.saveUserConfig(Config.getInstance());
  }
}
