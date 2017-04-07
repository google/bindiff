package com.google.security.zynamics.bindiff.utils;

import com.google.security.zynamics.bindiff.Launcher;
import com.google.security.zynamics.bindiff.log.Logger;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import javax.swing.ImageIcon;

public class ImageUtils {
  public static Image getImage(final String imagePath) {
    final URL imageUrl = Launcher.class.getResource(imagePath);
    if (imageUrl == null) {
      Logger.logWarning("Image resource not found: \"" + imagePath + "\"");
      return null;
    }

    return Toolkit.getDefaultToolkit().getImage(imageUrl);
  }

  public static ImageIcon getImageIcon(final String imagePath) {
    return new ImageIcon(getImage(imagePath));
  }
}
