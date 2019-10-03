package com.google.security.zynamics.bindiff.utils;

import com.google.security.zynamics.bindiff.Launcher;
import com.google.security.zynamics.bindiff.log.Logger;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Arrays;
import javax.swing.ImageIcon;

public class ImageUtils {
  public static Image getImage(final String imagePath) {
    final String pkgName = Launcher.class.getPackage().getName().replace('.', '/');
    URL imageUrl = null;
    // Blaze packages up resource files differently than Maven. Search "ui" and "zylib" as well.
    for (String path :
        Arrays.asList(
            "",
            "/ui/src/main/resources/" + pkgName + "/",
            "/zylib/src/main/resources/" + pkgName + "/")) {
      imageUrl = Launcher.class.getResource(path + imagePath);
      if (imageUrl != null) {
        break;
      }
    }

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
