// Copyright 2011-2021 Google LLC
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

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.ImmutableList;
import com.google.common.flogger.FluentLogger;
import com.google.common.io.ByteSource;
import com.google.security.zynamics.bindiff.BinDiff;
import com.google.security.zynamics.zylib.io.FileUtils;
import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BaseMultiResolutionImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import javax.swing.ImageIcon;

/** Utility class to load images from BinDiff package resources. */
public class ResourceUtils {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private static final String BINDIFF_PACKAGE =
      BinDiff.class.getPackage().getName().replace('.', '/');

  // Blaze/Bazel packages up resource files differently than Maven. Search "ui" and "zylib" as well.
  private static final ImmutableList<String> SEARCH_PATHS =
      ImmutableList.of(
          "",
          "/ui/src/main/resources/" + BINDIFF_PACKAGE + "/",
          "/zylib/src/main/resources/" + BINDIFF_PACKAGE + "/");

  private ResourceUtils() {}

  public static Image getImage(final String imagePath) {
    return getImage(imagePath, null);
  }

  public static Image getImage(final String imagePath, final Component component) {
    URL imageUrl = null;
    URL imageUrl2x = null;
    for (String path : SEARCH_PATHS) {
      // Try to find a 2x scaled version of the image.
      imageUrl = BinDiff.class.getResource(path + imagePath);
      imageUrl2x =
          BinDiff.class.getResource(
              path
                  + BinDiffFileUtils.removeFileExtension(imagePath)
                  + "@2."
                  + FileUtils.getFileExtension(new File(imagePath)));
      if (imageUrl != null) {
        break;
      }
    }
    if (imageUrl == null) {
      logger.at(Level.WARNING).log("Image resource not found: \"%s\"", imagePath);
      return null;
    }

    final Toolkit toolkit =
        component != null ? component.getToolkit() : Toolkit.getDefaultToolkit();
    final Image image = toolkit.getImage(imageUrl);
    final Image image2x = imageUrl2x != null ? toolkit.getImage(imageUrl2x) : null;

    if (component != null) {
      // Ensure all resolution variants are loaded. While Swing also uses a MediaTracker to track
      // image load state, it does so inconsistently, sometimes leading to "Invalid Image variant"
      // errors during paint. Waiting for these images should be fast, as they are part of the JAR.
      final MediaTracker mt = new MediaTracker(component);
      mt.addImage(image, 0);
      if (image2x != null) {
        mt.addImage(image2x, 1);
      }
      try {
        mt.waitForAll();
      } catch (final InterruptedException e) {
        logger.at(Level.WARNING).withCause(e).log(
            "Interrupted while loading image resource: \"%s\"", imagePath);
      }
    }

    if (image2x == null) {
      return image;
    }
    return new BaseMultiResolutionImage(image, image2x);
  }

  public static ImageIcon getImageIcon(final String imagePath) {
    return getImageIcon(imagePath, null);
  }

  public static ImageIcon getImageIcon(final String imagePath, final Component component) {
    return new ImageIcon(getImage(imagePath, component));
  }

  public static InputStream getResource(final String resourcePath) {
    for (final String path : SEARCH_PATHS) {
      final InputStream stream = BinDiff.class.getResourceAsStream(path + resourcePath);
      if (stream != null) {
        return stream;
      }
    }
    logger.at(Level.WARNING).log("Resource not found: \"%s\"", resourcePath);
    return null;
  }

  public static String getResourceAsString(final String resourcePath) throws IOException {
    final InputStream stream = getResource(resourcePath);
    if (stream == null) {
      return null;
    }
    final ByteSource source =
        new ByteSource() {
          @Override
          public InputStream openStream() {
            return stream;
          }
        };
    return source.asCharSource(UTF_8).read();
  }
}
