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
    final String extension = CFileUtils.getFileExtension(inFile.getPath());
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
