package com.google.security.zynamics.zylib.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public class GuiHelper implements WindowStateListener {
  public static final int DEFAULT_FONTSIZE = 12;

  public static final Font MONOSPACED_FONT =
      new Font(Font.MONOSPACED, Font.PLAIN, DEFAULT_FONTSIZE);

  public static final Font DEFAULT_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, DEFAULT_FONTSIZE);

  // Fields needed by the applyWindowFix() method and its workaround.
  private static final GuiHelper instance = new GuiHelper();
  private Field metacityWindowManager = null;
  private Field awtWindowManager = null;
  private boolean needsWindowFix = false;

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
   * @param window the window to apply the fix for.
   */
  public static final void applyWindowFix(Window window) {
    if (!instance.needsWindowFix) {
      return;
    }
    window.removeWindowStateListener(instance);
    window.addWindowStateListener(instance);
    // Apply fix first for the current window state.
    instance.windowStateChanged(null);
  }

  /** Centers the child component relative to its parent component. */
  public static final void centerChildToParent(
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

  public static final void centerOnScreen(final Window frame) {
    frame.setLocationRelativeTo(null);
  }

  public static Component findComponentAt(final Container c, final Point sp) {
    final Point cp = new Point(sp.x, sp.y);
    SwingUtilities.convertPointFromScreen(cp, c);

    if (!c.contains(cp.x, cp.y)) {
      return c;
    }

    final int ncomponents = c.getComponentCount();
    final Component component[] = c.getComponents();
    for (int i = 0; i < ncomponents; i++) {
      final Component comp = component[i];
      final Point loc = comp.getLocation();
      if ((comp.contains(cp.x - loc.x, cp.y - loc.y)) && (comp.isVisible() == true)) {
        // found a component that intersects the point, see if there
        // is a deeper possibility.
        if (comp instanceof Container) {
          final Container child = (Container) comp;
          final Component deeper = findComponentAt(child, sp);
          if (deeper != null) {
            return deeper;
          }
        } else {
          return comp;
        }
      }
    }

    return c;
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

  public static String getDefaultFont() {
    return DEFAULT_FONT.getName();
  }

  public static String getMonospaceFont() {
    return MONOSPACED_FONT.getName();
  }

  public interface ComponentFilter {
    boolean accept(JComponent comp);
  }
}
