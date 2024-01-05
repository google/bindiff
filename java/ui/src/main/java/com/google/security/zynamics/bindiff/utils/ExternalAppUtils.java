// Copyright 2011-2024 Google LLC
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

package com.google.security.zynamics.bindiff.utils;

import com.google.common.base.Ascii;
import com.google.security.zynamics.bindiff.BinDiffProtos.Config.IdaProOptions;
import com.google.security.zynamics.bindiff.config.Config;
import com.google.security.zynamics.bindiff.exceptions.DifferException;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.zylib.io.FileUtils;
import com.google.security.zynamics.zylib.system.IdaHelpers;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExternalAppUtils {

  private ExternalAppUtils() {}

  public static File getBinDiffEngine() throws DifferException, FileNotFoundException {
    String rootPath = Config.getInstance().getDirectory();
    if ("".equals(rootPath)) {
      rootPath = FileUtils.findLocalRootPath(ExternalAppUtils.class);
    }
    for (final String path :
        new String[] {
          "bin", // Binary directory under "directory" setting, default case
          "", // Engine exists next to UI JAR file or "bin" is set as "directory"
          Paths.get("..", "MacOS", "bin").toString() // macOS, relative to "app" bundle dir
        }) {
      final File bindiffEngine =
          Path.of(rootPath, path, Constants.BINDIFF_ENGINE_EXECUTABLE).toFile();
      if (!bindiffEngine.exists()) {
        continue;
      }
      if (!bindiffEngine.canExecute()) {
        throw new DifferException(
            "BinDiff engine is not marked executable: " + bindiffEngine.getPath());
      }
      return bindiffEngine;
    }
    throw new FileNotFoundException("BinDiff engine not found in configured path: " + rootPath);
  }

  public static File getIdaExe(final File inFile) {
    final String extension = FileUtils.getFileExtension(inFile);
    final IdaProOptions ida = Config.getInstance().getIda();

    String idaExe;
    if (Ascii.equalsIgnoreCase(extension, Constants.IDB64_EXTENSION)) {
      idaExe = IdaHelpers.IDA64_EXECUTABLE;
    } else if (Ascii.equalsIgnoreCase(extension, Constants.IDB32_EXTENSION)) {
      idaExe = IdaHelpers.IDA32_EXECUTABLE;
    } else {
      return null;
    }
    return Paths.get(ida.getDirectory(), idaExe).toFile();
  }
}
