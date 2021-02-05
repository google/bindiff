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

package com.google.security.zynamics.bindiff.processes;

import com.google.security.zynamics.bindiff.BinDiffProtos;
import com.google.security.zynamics.bindiff.config.Config;
import com.google.security.zynamics.bindiff.exceptions.BinExportException;
import com.google.security.zynamics.zylib.io.FileUtils;
import com.google.security.zynamics.zylib.system.IdaException;
import com.google.security.zynamics.zylib.system.IdaHelpers;
import java.io.File;

/** Static methods to help export IDA Pro databases to .BinExport. */
// TODO(cblichmann): Remove this functionality from Java and call "bindiff --export" instead.
public class ExportProcess {

  public static String getExportFilename(final String destFilename, final File outputDir) {
    return FileUtils.ensureTrailingSlash(outputDir.getPath()) + destFilename;
  }

  public static void startExportProcess(
      final File idaExe, final File outputFolder, final File fileToExport, final String targetName)
      throws BinExportException {
    try {
      final BinDiffProtos.Config.IdaProOptions ida = Config.getInstance().getIda();
      IdaHelpers.createIdaProcess(
          ida.getDirectory() + File.separatorChar + idaExe.getName(),
          fileToExport.getAbsolutePath(),
          outputFolder.getPath() + File.separator + targetName,
          ida.getBinexportX86NoreturnHeuristic());
    } catch (final IdaException e) {
      throw new BinExportException(e);
    }
  }
}
