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
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;

public class GuiHelper implements WindowStateListener {
  public static final int DEFAULT_FONTSIZE = 13;

  private static final GuiHelper instance = new GuiHelper();

  // Fields needed by the applyWindowFix() method and its workaround.
  private Field metacityWindowManager = null;
  private Field awtWindowManager = null;
  private boolean needsWindowFix = false;

  // Application-global font settings
  private Font defaultFont = new Font(Font.SANS_SERIF, Font.PLAIN, DEFAULT_FONTSIZE);
  private FontMetrics defaultFontMetrics = null;

  private Font monospacedFont = new Font(Font.MONOSPACED, Font.PLAIN, DEFAULT_FONTSIZE);
  private FontMetrics monospacedFontMetrics = null;

  /** Private constructor to prevent public instantiation. */
  private GuiHelper() {
    // See http://hg.netbeans.org/core-main/rev/409566c2aa65, this implements a rather ugly
    // reflection-based workaround for a super annoying JDK bug with certain window managers. When
    // this bug hits, menus won't respond normally to mouse events but are offset by a few hundred
    // pixels.
    final List<String> linuxDesktops = Arrays.asList("gnome", "gnome-shell", "mate", "cinnamon");
    final String desktop = System.getenv("DESKTOP_SESSION");
    if (desktop != null && linuxDesktops.contains(desktop.toLowerCase())) {
      try {
        final Class<?> xwm = Class.forName("sun.awt.X11.XWM");
        awtWindowManager = xwm.getDeclaredField("awt_wmgr");
        awtWindowManager.setAccessible(true);
        final Field otherWindowManager = xwm.getDeclaredField("OTHER_WM");
        otherWindowManager.setAccessible(true);
        if (awtWindowManager.get(null).equals(otherWindowManager.get(null))) {
          metacityWindowManager = xwm.getDeclaredField("METACITY_WM");
          metacityWindowManager.setAccessible(true);
          needsWindowFix = true;
        }
      } catch (final ClassNotFoundException | NoSuchFieldException | SecurityException
          | IllegalArgumentException | IllegalAccessException e) {
        // Ignore
      }
    }
  }

  @Override
  public void windowStateChanged(WindowEvent e) {
    try {
      awtWindowManager.set(null, metacityWindowManager.get(null));
    } catch (IllegalArgumentException | IllegalAccessException e1) {
      // Ignore
    }
  }

  /**
   * Adds a work around for a weird menu-offset JDK bug under certain window managers.
   *
   * @param window the window to apply the fix for.
   */
  public static void applyWindowFix(Window window) {
    if (!instance.needsWindowFix) {
      return;
    }
    window.removeWindowStateListener(instance);
    window.addWindowStateListener(instance);
    // Apply fix first for the current window state.
    instance.windowStateChanged(null);
  }

  /** Centers the child component relative to its parent component. */
  public static void centerChildToParent(
      final Component parent, final Component child, final boolean bStayOnScreen) {
    int x = (parent.getX() + (parent.getWidth() / 2)) - (child.getWidth() / 2);
    int y = (parent.getY() + (parent.getHeight() / 2)) - (child.getHeight() / 2);
    if (bStayOnScreen) {
      final Toolkit tk = Toolkit.getDefaultToolkit();
      final Dimension ss = new Dimension(tk.getScreenSize());
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

  public static JComponent findComponentByPredicate(
      final JComponent container, final ComponentFilter pred) {
    for (final Component c : container.getComponents()) {
      if (!(c instanceof JComponent)) {
        continue;
      }

      if (pred.accept((JComponent) c)) {
        return (JComponent) c;
      }

      final JComponent result = findComponentByPredicate((JComponent) c, pred);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  public static void setDefaultFont(Font font) {
    instance.defaultFont = font;
    instance.defaultFontMetrics = new JLabel().getFontMetrics(font);
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
