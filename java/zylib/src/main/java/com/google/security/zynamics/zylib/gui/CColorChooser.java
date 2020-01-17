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

package com.google.security.zynamics.zylib.gui;

import com.google.security.zynamics.zylib.gui.ColorPanel.ColorPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.colorchooser.DefaultColorSelectionModel;


/**
 * @author cblichmann@google.com (Christian Blichmann)
 */
public class CColorChooser extends JColorChooser {
  final Color[] m_recentColors;

  public CColorChooser() {
    this(new DefaultColorSelectionModel(Color.WHITE), null);
  }

  public CColorChooser(final Color initialColor) {
    this(new DefaultColorSelectionModel(initialColor), null);
  }

  public CColorChooser(final Color initialColor, final Color[] recentColors) {
    this(new DefaultColorSelectionModel(initialColor), recentColors);
  }

  public CColorChooser(final ColorSelectionModel model) {
    this(model, null);
  }

  public CColorChooser(final ColorSelectionModel model, final Color[] recentColors) {
    super(model);

    // Remove first chooser panel ("Swatches")
    // TODO(cblichmann): Revisit this for JDK > 1.7
    final AbstractColorChooserPanel[] panels = getChooserPanels();
    if (panels.length > 0) {
      removeChooserPanel(panels[0]);
    }

    m_recentColors = recentColors;
    if (m_recentColors != null) {
      addChooserPanel(new RecentColorsColorChooserPanel());
    }
  }

  public static JDialog createDialog(final Component parent, final String title,
      final boolean modal, final CColorChooser chooserPane, final ActionListener okListener,
      final ActionListener cancelListener) throws HeadlessException {
    // Just forward to ancestor
    return JColorChooser
        .createDialog(parent, title, modal, chooserPane, okListener, cancelListener);
  }

  public static Color showDialog(final Component parent, final String title,
      final Color initialColor) throws HeadlessException {
    return showDialog(parent, title, initialColor, null);
  }

  public static Color showDialog(final Component parent, final String title,
      final Color initialColor, final Color[] recentColors) throws HeadlessException {
    final CColorChooser pane = new CColorChooser(initialColor, recentColors);
    final SelectedColorActionListener ok = pane.new SelectedColorActionListener();
    final JDialog dlg = createDialog(parent, title, true, pane, ok, null);

    dlg.setVisible(true);
    dlg.dispose();

    return ok.getColor();
  }

  class RecentColorsColorChooserPanel extends AbstractColorChooserPanel {
    @Override
    protected void buildChooser() {
      final JPanel colorList = new JPanel();

      colorList.setLayout(new GridLayout(m_recentColors.length, 1, 1, 1));

      for (final Color col : m_recentColors) {
        final ColorPanel cp = new ColorPanel(col, false, true);
        cp.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(final MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON1) {
              return;
            }
            getColorSelectionModel().setSelectedColor(col);
          }
        });
        cp.setPreferredSize(new Dimension(200, 25));

        final JPanel p = new JPanel();
        p.setBorder(new EmptyBorder(1, 1, 1, 1));
        p.add(cp, BorderLayout.PAGE_START);

        colorList.add(cp);
      }

      final GridBagConstraints c = new GridBagConstraints();
      c.gridx = 0;
      c.gridy = 0;
      c.anchor = GridBagConstraints.FIRST_LINE_START;
      c.weightx = 1;
      c.fill = GridBagConstraints.HORIZONTAL;

      final JPanel innerPanel = new JPanel(new GridBagLayout());
      innerPanel.add(colorList, c);

      final JPanel outerPanel = new JPanel(new BorderLayout());
      outerPanel.add(innerPanel, BorderLayout.NORTH);

      final JScrollPane sp = new JScrollPane(outerPanel);
      sp.setBorder(null);
      sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      add(sp, BorderLayout.CENTER);
    }

    @Override
    public String getDisplayName() {
      return "Recent Colors";
    }

    @Override
    public Icon getLargeDisplayIcon() {
      /* Return null by default */
      return null;
    }

    @Override
    public Icon getSmallDisplayIcon() {
      /* Return null by default */
      return null;
    }

    @Override
    public void updateChooser() {
      /* Do nothing here */
    }
  }

  class SelectedColorActionListener implements ActionListener {
    Color m_color;

    @Override
    public void actionPerformed(final ActionEvent e) {
      m_color = CColorChooser.this.getColor();
    }

    public Color getColor() {
      return m_color;
    }
  }
}
