// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.scripting.console;

import com.google.security.zynamics.zylib.gui.GuiHelper;
import java.awt.Color;
import java.awt.Font;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * Document style for the syntax highlighted Python interpreter output.
 */
public class ConsoleStdoutDocument extends DefaultStyledDocument {
  private static final long serialVersionUID = 4657150237257401496L;

  private final SimpleAttributeSet outputAttrA = new SimpleAttributeSet();
  private final SimpleAttributeSet outputAttrB = new SimpleAttributeSet();
  private final SimpleAttributeSet outputErrAttr = new SimpleAttributeSet();

  boolean flipflop;
  int lastPosition;
  SimpleAttributeSet outputAttr;

  public ConsoleStdoutDocument() {
    final Font monospacedFont = GuiHelper.getMonospacedFont();
    final String monospacedFamily = monospacedFont.getFamily();
    final int monospacedSize = monospacedFont.getSize();

    StyleConstants.setFontFamily(outputAttrA, monospacedFamily);
    StyleConstants.setFontSize(outputAttrA, monospacedSize);
    StyleConstants.setForeground(outputAttrA, new Color((float) .4, (float) .4, (float) .4));

    StyleConstants.setFontFamily(outputAttrB, monospacedFamily);
    StyleConstants.setFontSize(outputAttrB, monospacedSize);
    StyleConstants.setForeground(outputAttrB, new Color((float) .1, (float) .1, (float) .1));

    StyleConstants.setFontFamily(outputErrAttr, monospacedFamily);
    StyleConstants.setFontSize(outputErrAttr, monospacedSize);
    StyleConstants.setForeground(outputErrAttr, new Color(1, (float) .2, (float) .2));

    lastPosition = 0;
    flipflop = false;
    outputAttr = outputAttrA;
  }

  public void append(final String output) {
    try {
      super.insertString(lastPosition, output, outputAttr);
      lastPosition += output.length();
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
  }

  public void appendErr(final String output) {
    try {
      super.insertString(lastPosition, output, outputErrAttr);
      lastPosition += output.length();
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
  }

  public void flip() {
    outputAttr = flipflop ? outputAttrA : outputAttrB;

    flipflop = !flipflop;
  }
}
