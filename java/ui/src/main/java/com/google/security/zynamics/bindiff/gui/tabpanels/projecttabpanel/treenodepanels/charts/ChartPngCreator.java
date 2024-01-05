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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.charts;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.jfree.chart.JFreeChart;

public class ChartPngCreator {
  private static BufferedImage draw(final JFreeChart chart, final int width, final int height) {
    final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    final Graphics2D gfx = img.createGraphics();
    try {
      chart.draw(gfx, new Rectangle2D.Double(0, 0, width, height));
    } finally {
      gfx.dispose();
    }

    return img;
  }

  public static void saveToFile(
      final JFreeChart chart, final String aFileName, final int width, final int height)
      throws FileNotFoundException, IOException {
    final BufferedImage image = draw(chart, width, height);
    ImageIO.write(image, "png", new File(aFileName));
  }
}
