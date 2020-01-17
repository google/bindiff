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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class IoWriterUtils {
  public static void writeByteArrary(final OutputStream stream, final byte[] bytes)
      throws IOException {
    stream.write(bytes);
  }

  public static void writeIntegerValue(final OutputStream stream, final int value)
      throws IOException {
    stream.write(value & 0xFF);
    stream.write(value >>> 8 & 0xFF);
    stream.write(value >>> 16 & 0xFF);
    stream.write(value >>> 24);
  }

  public static void writeLongValue(final OutputStream stream, final long value)
      throws IOException {
    stream.write((int) (value & 0xFF));
    stream.write((int) (value >>> 8 & 0xFF));
    stream.write((int) (value >>> 16 & 0xFF));
    stream.write((int) (value >>> 24 & 0xFF));
    stream.write((int) (value >>> 32 & 0xFF));
    stream.write((int) (value >>> 40 & 0xFF));
    stream.write((int) (value >>> 48 & 0xFF));
    stream.write((int) (value >>> 56));
  }

  public static void writeNewZipFile(final String zipFile, final List<File> fileEntries)
      throws IOException {
    final byte[] buffer = new byte[1024];

    final ZipOutputStream zipOutStream = new ZipOutputStream(new FileOutputStream(zipFile));

    for (final File file : fileEntries) {
      final FileInputStream fileInputStream = new FileInputStream(file);

      zipOutStream.putNextEntry(new ZipEntry(file.getName()));

      int length;
      while ((length = fileInputStream.read(buffer)) > 0) {
        zipOutStream.write(buffer, 0, length);
      }

      zipOutStream.closeEntry();
      fileInputStream.close();
    }

    zipOutStream.finish();

    zipOutStream.close();
  }

  public static void writeShortValue(final OutputStream stream, final int value)
      throws IOException {
    if (value > Short.MAX_VALUE) {
      throw new IOException("Value out of range exception.");
    }

    stream.write((value & 0xFF));
    stream.write((value >>> 8));
  }

  public static void writeStringValue(final OutputStream stream, final String string)
      throws IOException {
    stream.write(string.getBytes());
  }
}
