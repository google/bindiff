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

package com.google.security.zynamics.zylib.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.Window;
import javax.swing.JComponent;
import javax.swing.JLabel;

/** UI utility class to centralize default font settings and window placement. */
public class GuiHelper {
  public static final int DEFAULT_FONTSIZE = 13;

  private static final GuiHelper instance = new GuiHelper();

  // Application-global font settings
  private Font defaultFont = new Font(Font.SANS_SERIF, Font.PLAIN, DEFAULT_FONTSIZE);

  private Font monospacedFont = new Font(Font.MONOSPACED, Font.PLAIN, DEFAULT_FONTSIZE);
  private FontMetrics monospacedFontMetrics = null;

  private GuiHelper() {
    /* Private constructor to prevent public instantiation. */
  }

  /** Centers the child component relative to its parent component. */
  public static void centerChildToParent(Component parent, Component child, boolean bStayOnScreen) {
    int x = (parent.getX() + (parent.getWidth() / 2)) - (child.getWidth() / 2);
    int y = (parent.getY() + (parent.getHeight() / 2)) - (child.getHeight() / 2);
    if (bStayOnScreen) {
      Toolkit tk = Toolkit.getDefaultToolkit();
      var ss = new Dimension(tk.getScreenSize());
      if ((x + child.getWidth()) > ss.getWidth()) {
        x = (int) (ss.getWidth() - child.getWidth());
      }
      if ((y + child.getHeight()) > ss.getHeight()) {
        y = (int) (ss.getHeight() - child.getHeight());
      }
      if (x < 0) {
        x = 0;
      }
      if (y < 0) {
        y = 0;
      }
    }
    child.setLocation(x, y);
  }

  public static void centerOnScreen(final Window frame) {
    frame.setLocationRelativeTo(null);
  }

  public static JComponent findComponentByPredicate(JComponent container, ComponentFilter pred) {
    for (Component c : container.getComponents()) {
      if (!(c instanceof JComponent)) {
        continue;
      }

      if (pred.accept((JComponent) c)) {
        return (JComponent) c;
      }

      JComponent result = findComponentByPredicate((JComponent) c, pred);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  public static void setDefaultFont(Font font) {
    instance.defaultFont = font;
  }

  public static Font getDefaultFont() {
    return instance.defaultFont;
  }

  public static void setMonospacedFont(Font font) {
    instance.monospacedFont = font;
    instance.monospacedFontMetrics = new JLabel().getFontMetrics(font);
  }

  public static Font getMonospacedFont() {
    return instance.monospacedFont;
  }

  public static FontMetrics getMonospacedFontMetrics() {
    return instance.monospacedFontMetrics;
  }

  public interface ComponentFilter {
    boolean accept(JComponent comp);
  }
}
