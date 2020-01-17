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
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import javax.swing.ImageIcon;

/** Utility class to load images from BinDiff package resources. */
public class ImageUtils {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private ImageUtils() {}

  public static Image getImage(final String imagePath) {
    final String pkgName = BinDiff.class.getPackage().getName().replace('.', '/');
    URL imageUrl = null;
    // Blaze packages up resource files differently than Maven. Search "ui" and "zylib" as well.
    for (String path :
        Arrays.asList(
            "",
            "/ui/src/main/resources/" + pkgName + "/",
            "/zylib/src/main/resources/" + pkgName + "/")) {
      imageUrl = BinDiff.class.getResource(path + imagePath);
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
