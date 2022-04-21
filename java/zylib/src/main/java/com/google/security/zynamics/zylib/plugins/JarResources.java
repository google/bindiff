// Copyright 2011-2022 Google LLC
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

package com.google.security.zynamics.zylib.plugins;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * JarResources: JarResources maps all resources included in a Zip or Jar file. Additionaly, it
 * provides a method to extract one as a blob.
 */
public final class JarResources {
  // jar resource mapping tables
  private final Hashtable<String, Integer> htSizes = new Hashtable<String, Integer>();

  private final Hashtable<String, byte[]> htJarContents = new Hashtable<String, byte[]>();
  // a jar file
  private final String jarFileName;

  // external debug flag
  public boolean debugOn = false;

  /**
   * creates a JarResources. It extracts all resources from a Jar into an internal hashtable, keyed
   * by resource names.
   * 
   * @param jarFileName a jar or zip file
   */
  public JarResources(final String jarFileName) {
    this.jarFileName = jarFileName;
    init();
  }

  /**
   * Dumps a zip entry into a string.
   * 
   * @param ze a ZipEntry
   */
  private String dumpZipEntry(final ZipEntry ze) {
    final StringBuffer sb = new StringBuffer();
    if (ze.isDirectory()) {
      sb.append("d ");
    } else {
      sb.append("f ");
    }

    if (ze.getMethod() == ZipEntry.STORED) {
      sb.append("stored   ");
    } else {
      sb.append("defalted ");
    }

    sb.append(ze.getName());
    sb.append("\t");
    sb.append("" + ze.getSize());
    if (ze.getMethod() == ZipEntry.DEFLATED) {
      sb.append("/" + ze.getCompressedSize());
    }

    return sb.toString();
  }

  /** initializes internal hash tables with Jar file resources. */
  private void init() {
    try {
      // extracts just sizes only.
      final ZipFile zf = new ZipFile(jarFileName);
      final Enumeration<? extends ZipEntry> e = zf.entries();
      while (e.hasMoreElements()) {
        final ZipEntry ze = e.nextElement();

        if (debugOn) {
          System.out.println(dumpZipEntry(ze));
        }

        htSizes.put(ze.getName(), (int) ze.getSize());
      }
      zf.close();

      // extract resources and put them into the hashtable.
      final FileInputStream fis = new FileInputStream(jarFileName);
      final BufferedInputStream bis = new BufferedInputStream(fis);
      final ZipInputStream zis = new ZipInputStream(bis);
      ZipEntry ze = null;
      while ((ze = zis.getNextEntry()) != null) {
        if (ze.isDirectory()) {
          continue;
        }

        if (debugOn) {
          System.out.println("ze.getName()=" + ze.getName() + "," + "getSize()=" + ze.getSize());
        }

        int size = (int) ze.getSize();
        // -1 means unknown size.
        if (size == -1) {
          size = htSizes.get(ze.getName()).intValue();
        }

        final byte[] b = new byte[size];
        int rb = 0;
        int chunk = 0;
        while ((size - rb) > 0) {
          chunk = zis.read(b, rb, size - rb);
          if (chunk == -1) {
            break;
          }
          rb += chunk;
        }

        // add to internal resource hashtable
        htJarContents.put(ze.getName(), b);

        if (debugOn) {
          System.out.println(ze.getName() + "  rb=" + rb + ",size=" + size + ",csize="
              + ze.getCompressedSize());
        }
      }
      zis.close();
    } catch (final NullPointerException e) {
      System.out.println("done.");
    } catch (final FileNotFoundException e) {
      e.printStackTrace();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Extracts a jar resource as a blob.
   * 
   * @param name a resource name.
   */
  public byte[] getResource(final String name) {
    return htJarContents.get(name);
  }

} // End of JarResources class.
