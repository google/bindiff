package com.google.security.zynamics.bindiff.processes;

import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.exceptions.BinExportException;
import com.google.security.zynamics.zylib.io.FileUtils;
import com.google.security.zynamics.zylib.system.IdaException;
import com.google.security.zynamics.zylib.system.IdaHelpers;
import java.io.File;

public class ExportProcess {

  public static String getExportFilename(final String destFilename, final File outputDir) {
    return FileUtils.ensureTrailingSlash(outputDir.getPath()) + destFilename;
  }

  public static void startExportProcess(
      final File idaExe, final File outputFolder, final File fileToExport, final String targetName)
      throws BinExportException {
    try {
      IdaHelpers.createIdaProcess(
          BinDiffConfig.getInstance().getMainSettings().getIdaDirectory()
              + File.separatorChar
              + idaExe.getName(),
          fileToExport.getAbsolutePath(),
          outputFolder.getPath() + File.separator + targetName);
    } catch (final IdaException e) {
      throw new BinExportException(e);
    }
  }
}
