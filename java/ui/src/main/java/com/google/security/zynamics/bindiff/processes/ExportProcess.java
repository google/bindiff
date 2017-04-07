package com.google.security.zynamics.bindiff.processes;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.exceptions.BinExportException;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.zylib.io.FileUtils;
import com.google.security.zynamics.zylib.system.IdaException;
import com.google.security.zynamics.zylib.system.IdaHelpers;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class ExportProcess {
  private static File createIDAScript(final File directory, final String targetName)
      throws BinExportException {
    final File idcFile =
        new File(directory.getPath() + File.separator + Constants.IDA_EXPORT_SCRIPT_NAME);

    PrintWriter pw = null;
    try {
      pw =
          new PrintWriter(
              new BufferedWriter(new OutputStreamWriter(new FileOutputStream(idcFile), UTF_8)));
      pw.println("#include <idc.idc>");
      pw.println("static main() {");
      pw.println("  Batch(0);");
      pw.println("  Wait();");
      pw.println(
          String.format(
              "  Exit(%s(\"%s\"));",
              Constants.IDA_EXPORTER_IDC_COMMAND,
              (directory.getPath() + File.separator + targetName).replace("\\", "\\\\")));
      pw.println("}");
      if (pw.checkError()) {
        throw new BinExportException("Couldn't write IDA export script.");
      }
    } catch (final FileNotFoundException e) {
      throw new BinExportException(e, "Couldn't create IDA export script file.");
    } finally {
      if (pw != null) {
        pw.close();
      }
    }

    return idcFile;
  }

  public static String getExportedFileName(final String destFileName, final File outputDir)
      throws BinExportException {
    try {
      final String exportFileName =
          FileUtils.ensureTrailingSlash(outputDir.getPath()) + destFileName;

      return exportFileName;
    } catch (final Exception e) {
      // FIXME: Never catch all exceptions!
      throw new BinExportException(
          e, "Couldn't start exporting process. File name generation failed.");
    }
  }

  public static void startExportProcess(
      final File idaExe, final File outputFolder, final File fileToExport, final String targetName)
      throws BinExportException {
    final File exporterIdcPath = createIDAScript(outputFolder, targetName);

    try {
      IdaHelpers.createIdaProcess(
          BinDiffConfig.getInstance().getMainSettings().getIdaDirectory()
              + File.separatorChar
              + idaExe.getName(),
          exporterIdcPath,
          fileToExport.getAbsolutePath(),
          "" // Output directory not used here, IDC script takes care of that
          );
    } catch (final IdaException e) {
      throw new BinExportException(e);
    } finally {
      exporterIdcPath.delete();
    }
  }
}
