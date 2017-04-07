// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui;

import com.google.security.zynamics.zylib.io.FileUtils;

import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;


/**
 * A custom sub-class of JLabel that displays file names with path ellipses. It also features a
 * "Copy Path" popup menu item that copies the full path to clipboard.
 * 
 * @author cblichmann@google.com (Christian Blichmann)
 */
public class CPathLabel extends JLabel {
  /** Holds the shortened string */
  protected String m_textEllipsis = null;

  protected JPopupMenu m_popup;

  public CPathLabel() {
    this("", null, LEADING);
  }

  public CPathLabel(final Icon image) {
    this(null, image, CENTER);
  }

  public CPathLabel(final Icon image, final int horizontalAlignment) {
    this(null, image, horizontalAlignment);
  }

  public CPathLabel(final String text) {
    this(text, null, LEADING);
  }

  public CPathLabel(final String text, final Icon icon, final int horizontalAlignment) {
    super(text, icon, horizontalAlignment);

    m_popup = new JPopupMenu();
    final JMenuItem copyPathMenuItem = new JMenuItem("Copy Path", 'C');
    copyPathMenuItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final StringSelection data = new StringSelection(CPathLabel.super.getText());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(data, data);
      }
    });
    m_popup.add(copyPathMenuItem);

    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(final ComponentEvent e) {
        updatePathEllipsis();
      }
    });

    addMouseListener(new MouseAdapter() {
      private void handlePopupEvent(final MouseEvent e) {
        if (e.isPopupTrigger()) {
          m_popup.show(e.getComponent(), e.getX(), e.getY());
        }
      }

      @Override
      public void mousePressed(final MouseEvent e) {
        handlePopupEvent(e);
      }

      @Override
      public void mouseReleased(final MouseEvent e) {
        handlePopupEvent(e);
      }
    });
  }

  public CPathLabel(final String text, final int horizontalAlignment) {
    this(text, null, horizontalAlignment);
  }

  /**
   * Updates the shortened path string depending on the current width.
   */
  protected void updatePathEllipsis() {
    if (getGraphics() == null) {
      return;
    }
    final FontMetrics fm = getGraphics().getFontMetrics();
    m_textEllipsis = super.getText();
    int maxlen = m_textEllipsis.length();

    String newValue = m_textEllipsis;
    final int width = getWidth();
    while ((maxlen >= 12) && (fm.stringWidth(newValue) > width)) {
      newValue = FileUtils.getPathEllipsis(m_textEllipsis, maxlen);
      maxlen--;
    }
    m_textEllipsis = newValue;
  }

  @Override
  public String getText() {
    return m_textEllipsis;
  }

  @Override
  public void repaint() {
    updatePathEllipsis();
    super.repaint();
  }
}
