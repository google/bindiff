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

package com.google.security.zynamics.zylib.gui.ColorPanel;

import com.google.security.zynamics.zylib.general.Convert;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.CColorChooser;
import com.google.security.zynamics.zylib.resources.Constants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;


public class ColorPanel extends JPanel {
  private final ListenerProvider<IColorPanelListener> m_listeners =
      new ListenerProvider<IColorPanelListener>();

  private boolean m_editable = true;

  private final boolean m_showColorText;

  private final JLabel m_textLabel = new JLabel("");

  private Set<Color> m_defaultColors = null;

  public ColorPanel(final Color color) {
    this(color, false, false);
  }

  public ColorPanel(final Color color, final boolean editable) {
    this(color, editable, false);
  }

  public ColorPanel(final Color color, final boolean editable, final boolean showColorText) {
    super(new BorderLayout());

    m_editable = editable;
    m_showColorText = showColorText;

    setColor(color);
    setBackground(color);

    m_textLabel.setHorizontalAlignment(SwingConstants.CENTER);
    add(m_textLabel, BorderLayout.CENTER);

    setBorder(new LineBorder(Color.BLACK));
    setPreferredSize(new Dimension(100, 20));
    addMouseListener(new InternalListener());

    updateUI();
  }

  public ColorPanel(final Color color, final boolean editable, final boolean showColorText,
      final Set<Color> defaultColors) {
    this(color, editable, showColorText);

    m_defaultColors = defaultColors;

    setColor(color);
  }

  private void chooseColor() {
    final Color col =
        CColorChooser.showDialog(this, Constants.COLOR_CHOOSER, getBackground(),
            m_defaultColors == null ? null : m_defaultColors.toArray(new Color[0]));
    if (col != null) {
      setColor(col);
    }
  }

  private void setTextColor(final Color color) {
    if (m_showColorText) {
      final float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
      if (hsb[2] > 0.65) {
        final Color dcolor = new Color(32, 32, 32);
        m_textLabel.setForeground(dcolor);
      } else {
        final Color bcolor = new Color(222, 222, 222);
        m_textLabel.setForeground(bcolor);
      }

      updateUI();
    }
  }

  public void addListener(final IColorPanelListener listener) {
    m_listeners.addListener(listener);
  }

  public Color getColor() {
    return getBackground();
  }

  public void removeListener(final IColorPanelListener listener) {
    m_listeners.removeListener(listener);
  }

  public void setColor(final Color color) {
    setBackground(color);
    if (m_showColorText) {
      setTextColor(color);
      m_textLabel.setText(String.format("#%s (%d, %d, %d)", Convert.colorToHexString(color)
          .toUpperCase(), color.getRed(), color.getGreen(), color.getBlue()));
      m_textLabel.updateUI();
    }

    for (final IColorPanelListener listener : m_listeners) {
      listener.changedColor(this);
    }
  }

  public void setEditable(final boolean editable) {
    m_editable = editable;
  }

  public void setText(final String text) {
    if (m_showColorText) {
      m_textLabel.setText(text);
    }
  }

  private class InternalListener extends MouseAdapter {
    @Override
    public void mouseClicked(final MouseEvent event) {
      if (m_editable && (event.getButton() == MouseEvent.BUTTON1) && (event.getClickCount() == 1)) {
        chooseColor();
      }
    }
  }
}
