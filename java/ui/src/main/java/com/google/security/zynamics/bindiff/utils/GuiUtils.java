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

package com.google.security.zynamics.bindiff.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

public class GuiUtils {
  private GuiUtils() {
    /* Prevent instantiation */
  }

  public static JCheckBoxMenuItem buildCheckBoxMenuItem(String name, ActionListener action) {
    JCheckBoxMenuItem item = new JCheckBoxMenuItem(name);
    item.addActionListener(action);

    return item;
  }

  public static JCheckBoxMenuItem buildCheckBoxMenuItem(
      String name, char mnemonic, ActionListener action) {
    var item = new JCheckBoxMenuItem(name);
    item.setMnemonic(mnemonic);
    item.addActionListener(action);
    return item;
  }

  public static JCheckBoxMenuItem buildCheckBoxMenuItem(
      String name, char mnemonic, int keyEvent, int modifier, ActionListener action) {
    JCheckBoxMenuItem item = buildCheckBoxMenuItem(name, mnemonic, action);
    item.setAccelerator(KeyStroke.getKeyStroke(keyEvent, modifier));
    return item;
  }

  public static JMenuItem buildMenuItem(String text, ActionListener action) {
    return buildMenuItem(text, (char) -1, -1, 0, action, true);
  }

  public static JMenuItem buildMenuItem(String text, ActionListener action, boolean enable) {
    return buildMenuItem(text, (char) -1, -1, 0, action, enable);
  }

  public static JMenuItem buildMenuItem(String text, char mnemonic, ActionListener action) {
    return buildMenuItem(text, mnemonic, -1, 0, action, true);
  }

  public static JMenuItem buildMenuItem(
      String text, char mnemonic, ActionListener action, boolean enable) {
    return buildMenuItem(text, mnemonic, -1, 0, action, enable);
  }

  public static JMenuItem buildMenuItem(
      String text, char mnemonic, char accelerator, ActionListener action) {
    JMenuItem result = buildMenuItem(text, mnemonic, action);
    result.setAccelerator(KeyStroke.getKeyStroke(accelerator));
    return result;
  }

  public static JMenuItem buildMenuItem(
      String text, char mnemonic, int keyCode, int modifier, ActionListener action) {
    return buildMenuItem(text, mnemonic, keyCode, modifier, action, true);
  }

  public static JMenuItem buildMenuItem(
      String text,
      char mnemonic,
      int keyCode,
      int modifier,
      ActionListener action,
      boolean enable) {
    var result = new JMenuItem(text, mnemonic);
    if (keyCode >= 0) {
      result.setAccelerator(KeyStroke.getKeyStroke(keyCode, modifier));
    }
    result.setEnabled(enable);
    result.addActionListener(action);

    return result;
  }

  public static JMenuItem buildMenuItem(String text, Icon icon, ActionListener action) {
    var item = buildMenuItem(text, (char) -1, -1, 0, action, true);
    item.setIcon(icon);
    return item;
  }

  public static JMenuItem buildMenuItem(
      String text, int keyCode, int modifier, ActionListener action) {
    return buildMenuItem(text, (char) -1, keyCode, modifier, action, true);
  }

  public static JRadioButtonMenuItem buildRadioButtonMenuItem(
      String name, char mnemonic, ActionListener action) {
    var item = new JRadioButtonMenuItem(name);
    item.setMnemonic(mnemonic);
    item.addActionListener(action);
    return item;
  }

  public static JRadioButtonMenuItem buildRadioButtonMenuItem(
      String name, char mnemonic, int keyEvent, int modifier, ActionListener action) {
    JRadioButtonMenuItem item = buildRadioButtonMenuItem(name, mnemonic, action);
    item.setAccelerator(KeyStroke.getKeyStroke(keyEvent, modifier));
    return item;
  }

  public static <T extends Component> T createHorizontalNamedComponent(
      JPanel parent,
      String labelText,
      T component,
      int panelWidth,
      int panelHeight,
      boolean isLast) {
    component.setPreferredSize(new Dimension(panelWidth, panelHeight));

    var rowPanel = new JPanel(new BorderLayout());
    rowPanel.setBorder(new EmptyBorder(0, 2, isLast ? 2 : 0, 2));

    rowPanel.add(new JLabel(labelText), BorderLayout.CENTER);
    rowPanel.add(component, BorderLayout.EAST);

    parent.add(rowPanel);
    return component;
  }

  public static JPanel createHorizontalNamedComponentPanel(
      String labelText, int labelWidth, JComponent component, int panelHeight) {
    var panel = new JPanel(new BorderLayout());

    var descriptionLabel = new JLabel(labelText);
    descriptionLabel.setPreferredSize(new Dimension(labelWidth, panelHeight));
    component.setPreferredSize(new Dimension(component.getPreferredSize().width, panelHeight));

    panel.add(descriptionLabel, BorderLayout.WEST);
    panel.add(component, BorderLayout.CENTER);

    panel.setPreferredSize(new Dimension(panel.getPreferredSize().width, panelHeight));

    return panel;
  }

  public static JPanel createHorizontalNamedLabelPanel(
      String labelText, int labelWidth, JLabel valueLabel, int panelHeight) {
    var panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(0, 3, 2, 3));
    panel.setBackground(Color.WHITE);

    var descriptionLabel = new JLabel(labelText);
    descriptionLabel.setPreferredSize(new Dimension(labelWidth, panelHeight));
    panel.setPreferredSize(new Dimension(0, panelHeight));

    valueLabel.setForeground(new Color(0, 0, 160));
    valueLabel.setMinimumSize(new Dimension(0, 0));

    panel.add(descriptionLabel, BorderLayout.WEST);
    panel.add(valueLabel, BorderLayout.CENTER);

    return panel;
  }

  public static Component createHorizontalNamedLabelPanel(
      String labelText, int labelWidth, JTextField textField, int panelHeight) {

    textField.setEditable(false);
    textField.setBorder(null);
    textField.setForeground(new Color(0, 0, 160));
    textField.setBackground(Color.WHITE);

    var panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(0, 3, 2, 3));
    panel.setBackground(Color.WHITE);

    var descriptionLabel = new JLabel(labelText);
    descriptionLabel.setPreferredSize(new Dimension(labelWidth, panelHeight));
    panel.setPreferredSize(new Dimension(0, panelHeight));

    panel.add(descriptionLabel, BorderLayout.WEST);
    panel.add(textField, BorderLayout.CENTER);

    return panel;
  }

  public static void setWindowIcons(
      Window window, String iconPath16x16, String iconPath32x32, String iconPath48x48) {
    var imageList = new ArrayList<Image>();
    imageList.add(ResourceUtils.getImage(iconPath16x16));
    imageList.add(ResourceUtils.getImage(iconPath32x32));
    imageList.add(ResourceUtils.getImage(iconPath48x48));

    window.setIconImages(imageList);
  }
}
