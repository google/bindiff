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

package com.google.security.zynamics.bindiff.gui.components.closeablebuttontab;

import com.google.common.base.Preconditions;
import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.gui.components.MessageBox;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicButtonUI;

/** A tab button similar in style to the one used in Google Chrome. */
public class TabButton extends JButton {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  // Colors from Google Chrome
  private static final Color CROSS_COLOR = new Color(0x5d6063);
  private static final Color CROSS_COLOR_ROLLOVER = new Color(0x5d6166);
  private static final Color CROSS_BACKGROUND_ROLLOVER = new Color(0xe8eaed);
  private static final Color CROSS_BACKGROUND_PRESSED = new Color(0xdadce0);

  private final ListenerProvider<ICloseTabButtonListener> closeTabButtonListeners =
      new ListenerProvider<>();

  private final InternalButtonListener buttonListener = new InternalButtonListener();

  private final InternalMouseListener mouseListener = new InternalMouseListener();

  private final JTabbedPane pane;

  private final TabButtonComponent tabButtonComponent;

  private final boolean enableClose;

  public TabButton(
      final JTabbedPane tabPane,
      final TabButtonComponent tabButtonComponent,
      final boolean enableClose) {
    Preconditions.checkNotNull(tabPane);
    Preconditions.checkNotNull(tabButtonComponent);

    pane = tabPane;
    this.tabButtonComponent = tabButtonComponent;
    this.enableClose = enableClose;

    final int size = 14;

    setPreferredSize(new Dimension(size, size));
    setToolTipText("Close View");

    // Make the button look the same for all Laf's
    setUI(new BasicButtonUI());

    // Make it transparent
    setContentAreaFilled(false);

    // No need to be focusable
    setFocusable(false);
    setBorderPainted(false);

    // Making nice rollover effect
    addMouseListener(mouseListener);
    // We use the same listener for all buttons
    setRolloverEnabled(true);

    // Close the proper tab by clicking the button
    addActionListener(buttonListener);
  }

  @Override
  protected void paintComponent(final Graphics g) {
    if (!enableClose) {
      super.paintComponent(g);
      return;
    }

    final Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHints(
        new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));

    final int w = getWidth();
    final int h = getHeight();
    final int delta = 4;

    Color crossColor = CROSS_COLOR;
    if (getModel().isRollover()) {
      g2.setColor(!getModel().isPressed() ? CROSS_BACKGROUND_ROLLOVER : CROSS_BACKGROUND_PRESSED);
      g2.fillArc(0, 0, w, h, 0, 360);
      crossColor = CROSS_COLOR_ROLLOVER;
    }
    g2.setColor(crossColor);
    g2.setStroke(new BasicStroke(1.5f));
    g2.drawLine(delta - 1, delta - 1, w - delta, h - delta);
    g2.drawLine(delta - 1, h - delta, w - delta, delta - 1);
    g2.dispose();
  }

  public void addListener(final ICloseTabButtonListener listener) {
    closeTabButtonListeners.addListener(listener);
  }

  public void removeListener(final ICloseTabButtonListener listener) {
    closeTabButtonListeners.removeListener(listener);
  }

  private class InternalButtonListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      final int i = pane.indexOfTabComponent(tabButtonComponent);
      if (i == -1) {
        return;
      }

      for (final ICloseTabButtonListener listener : closeTabButtonListeners) {
        try {
          if (!listener.closing(tabButtonComponent)) {
            return;
          }
        } catch (final Exception e) {
          logger.at(Level.SEVERE).withCause(e).log("An error occurred while closing the tab");
          MessageBox.showError(
              SwingUtilities.getWindowAncestor(TabButton.this),
              "An error occurred while closing the tab.");
        }
      }
    }
  }

  private static class InternalMouseListener extends MouseAdapter {
    @Override
    public void mouseEntered(final MouseEvent e) {
      final Component component = e.getComponent();
      if (component instanceof AbstractButton) {
        final AbstractButton button = (AbstractButton) component;
        button.setBorderPainted(false);
      }
    }

    @Override
    public void mouseExited(final MouseEvent e) {
      final Component component = e.getComponent();
      if (component instanceof AbstractButton) {
        final AbstractButton button = (AbstractButton) component;
        button.setBorderPainted(false);
      }
    }
  }
}
