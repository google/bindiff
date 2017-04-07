// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.image;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;

public class ImageHelper {
  public static Image filterImage(final Image inImage, final ImageFilter filter) {
    final ImageProducer imageProducer = new FilteredImageSource(inImage.getSource(), filter);
    return Toolkit.getDefaultToolkit().createImage(imageProducer);
  }
}
