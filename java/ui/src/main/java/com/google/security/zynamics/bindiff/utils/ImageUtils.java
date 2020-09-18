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

import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.BinDiff;
import com.google.security.zynamics.zylib.io.FileUtils;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import javax.swing.ImageIcon;

/** Utility class to load images from BinDiff package resources. */
public class ImageUtils {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private ImageUtils() {}

  public static boolean isScaledDisplay() {
    final GraphicsConfiguration config =
        GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .getDefaultConfiguration();
    return !config.getDefaultTransform().isIdentity();
  }

  public static Image getImage(final String imagePath) {
    final boolean scaledDisplay = isScaledDisplay();
    final String pkgName = BinDiff.class.getPackage().getName().replace('.', '/');
    URL imageUrl = null;
    // Blaze packages up resource files differently than Maven. Search "ui" and "zylib" as well.
    for (String path :
        Arrays.asList(
            "",
            "/ui/src/main/resources/" + pkgName + "/",
            "/zylib/src/main/resources/" + pkgName + "/")) {
      if (scaledDisplay) {
        // On scaled displays, try to find a 2x scaled version of the image first.
        final File imageFilePath = new File(imagePath);
        imageUrl =
            BinDiff.class.getResource(
                path
                    + FileUtils.getFileBasename(imageFilePath)
                    + "@2."
                    + FileUtils.getFileExtension(imageFilePath));
      }
      if (imageUrl == null) {
        imageUrl = BinDiff.class.getResource(path + imagePath);
      }
      if (imageUrl != null) {
        break;
      }
    }

    if (imageUrl == null) {
      logger.at(Level.WARNING).log("Image resource not found: \"%s\"", imagePath);
      return null;
    }

    return Toolkit.getDefaultToolkit().getImage(imageUrl);
  }

  public static ImageIcon getImageIcon(final String imagePath) {
    return new ImageIcon(getImage(imagePath));
  }
}
