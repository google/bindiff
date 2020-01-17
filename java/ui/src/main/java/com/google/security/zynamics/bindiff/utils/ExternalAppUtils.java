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

package com.google.security.zynamics.bindiff.utils;

import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.config.GeneralSettingsConfigItem;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.zylib.io.FileUtils;
import com.google.security.zynamics.zylib.system.IdaHelpers;
import java.io.File;

public class ExternalAppUtils {
  public static String getCommandLineDiffer() {
    String enginePath = BinDiffConfig.getInstance().getMainSettings().getDiffEnginePath();
    if (enginePath == null || enginePath.isEmpty()) {
      enginePath = FileUtils.findLocalRootPath(ExternalAppUtils.class);
    }
    return FileUtils.ensureTrailingSlash(enginePath) + Constants.BINDIFF_ENGINE_EXECUTABLE;
  }

  public static File getIdaExe(final File inFile) {
    final String extension = FileUtils.getFileExtension(inFile);
    final GeneralSettingsConfigItem settings = BinDiffConfig.getInstance().getMainSettings();

    if (extension.equalsIgnoreCase(Constants.IDB32_EXTENSION)) {
      return new File(
          settings.getIdaDirectory() + File.separatorChar + IdaHelpers.IDA32_EXECUTABLE);
    }
    if (extension.equalsIgnoreCase(Constants.IDB64_EXTENSION)) {
      return new File(
          settings.getIdaDirectory() + File.separatorChar + IdaHelpers.IDA64_EXECUTABLE);
    }
    return null;
  }
}
