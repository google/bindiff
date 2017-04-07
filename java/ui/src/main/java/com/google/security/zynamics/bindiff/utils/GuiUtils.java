package com.google.security.zynamics.bindiff.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Window;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
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

  public static JCheckBoxMenuItem buildCheckBoxMenuItem(
      final String name, final AbstractAction action) {
    final JCheckBoxMenuItem item = new JCheckBoxMenuItem(name);
    item.addActionListener(action);

    return item;
  }

  public static JCheckBoxMenuItem buildCheckBoxMenuItem(
      final String name, final char mnemonic, final AbstractAction action) {
    final JCheckBoxMenuItem item = new JCheckBoxMenuItem(name);
    item.setMnemonic(mnemonic);
    item.addActionListener(action);

    return item;
  }

  public static JCheckBoxMenuItem buildCheckBoxMenuItem(
      final String name,
      final char mnemonic,
      final int keyEvent,
      final int modifier,
      final AbstractAction action) {
    final JCheckBoxMenuItem item = buildCheckBoxMenuItem(name, mnemonic, action);
    item.setAccelerator(KeyStroke.getKeyStroke(keyEvent, modifier));

    return item;
  }

  public static JMenu buildMenu(final String name, final char mnemonic) {
    final JMenu menu = new JMenu(name);
    menu.setMnemonic(mnemonic);

    return menu;
  }

  public static JMenuItem buildMenuItem(final String text, final AbstractAction action) {
    return buildMenuItem(text, (char) -1, -1, 0, action, true);
  }

  public static JMenuItem buildMenuItem(
      final String text, final AbstractAction action, final boolean enable) {
    return buildMenuItem(text, (char) -1, -1, 0, action, enable);
  }

  public static JMenuItem buildMenuItem(
      final String text, final char mnemonic, final AbstractAction action) {
    return buildMenuItem(text, mnemonic, -1, 0, action, true);
  }

  public static JMenuItem buildMenuItem(
      final String text, final char mnemonic, final AbstractAction action, final boolean enable) {
    return buildMenuItem(text, mnemonic, -1, 0, action, enable);
  }

  public static JMenuItem buildMenuItem(
      final String text, final char mnemonic, final char accelerator, final AbstractAction action) {
    final JMenuItem result = buildMenuItem(text, mnemonic, action);
    result.setAccelerator(KeyStroke.getKeyStroke(accelerator));
    return result;
  }

  public static JMenuItem buildMenuItem(
      final String text,
      final char mnemonic,
      final int keyCode,
      final int modifier,
      final AbstractAction action) {
    return buildMenuItem(text, mnemonic, keyCode, modifier, action, true);
  }

  public static JMenuItem buildMenuItem(
      final String text,
      final char mnemonic,
      final int keyCode,
      final int modifier,
      final AbstractAction action,
      final boolean enable) {
    final JMenuItem result = new JMenuItem(text, mnemonic);
    if (keyCode >= 0) {
      result.setAccelerator(KeyStroke.getKeyStroke(keyCode, modifier));
    }
    result.setEnabled(enable);
    result.addActionListener(action);

    return result;
  }

  public static JMenuItem buildMenuItem(
      final String text, final Icon icon, final AbstractAction action) {
    final JMenuItem item = buildMenuItem(text, (char) -1, -1, 0, action, true);
    item.setIcon(icon);

    return item;
  }

  public static JMenuItem buildMenuItem(
      final String text, final int keyCode, final int modifier, final AbstractAction action) {
    return buildMenuItem(text, (char) -1, keyCode, modifier, action, true);
  }

  public static JRadioButtonMenuItem buildRadioButtonMenuItem(
      final String name, final char mnemonic, final AbstractAction action) {
    final JRadioButtonMenuItem item = new JRadioButtonMenuItem(name);
    item.setMnemonic(mnemonic);
    item.addActionListener(action);

    return item;
  }

  public static JRadioButtonMenuItem buildRadioButtonMenuItem(
      final String name,
      final char mnemonic,
      final int keyEvent,
      final int modifier,
      final AbstractAction action) {
    final JRadioButtonMenuItem item = buildRadioButtonMenuItem(name, mnemonic, action);
    item.setAccelerator(KeyStroke.getKeyStroke(keyEvent, modifier));

    return item;
  }

  public static <T extends Component> T createHorizontalNamedComponent(
      final JPanel parent,
      final String labelText,
      final T component,
      final int panelWidth,
      final int panelHeight,
      final boolean isLast) {
    component.setPreferredSize(new Dimension(panelWidth, panelHeight));

    final JPanel rowPanel = new JPanel(new BorderLayout());
    rowPanel.setBorder(new EmptyBorder(0, 2, isLast ? 2 : 0, 2));

    rowPanel.add(new JLabel(labelText), BorderLayout.CENTER);
    rowPanel.add(component, BorderLayout.EAST);

    parent.add(rowPanel);

    return component;
  }

  public static JPanel createHorizontalNamedComponentPanel(
      final String labelText,
      final int labelWidth,
      final JComponent component,
      final int panelHeight) {
    final JPanel panel = new JPanel(new BorderLayout());

    final JLabel descriptionLabel = new JLabel(labelText);

    descriptionLabel.setPreferredSize(new Dimension(labelWidth, panelHeight));
    component.setPreferredSize(new Dimension(component.getPreferredSize().width, panelHeight));

    panel.add(descriptionLabel, BorderLayout.WEST);
    panel.add(component, BorderLayout.CENTER);

    panel.setPreferredSize(new Dimension(panel.getPreferredSize().width, panelHeight));

    return panel;
  }

  public static JPanel createHorizontalNamedLabelPanel(
      final String labelText,
      final int labelWidth,
      final JLabel valueLabel,
      final int panelHeight) {
    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(0, 3, 2, 3));
    panel.setBackground(Color.WHITE);

    final JLabel descriptionLabel = new JLabel(labelText);
    descriptionLabel.setPreferredSize(new Dimension(labelWidth, panelHeight));
    panel.setPreferredSize(new Dimension(0, panelHeight));

    valueLabel.setForeground(new Color(0, 0, 160));
    valueLabel.setMinimumSize(new Dimension(0, 0));

    panel.add(descriptionLabel, BorderLayout.WEST);
    panel.add(valueLabel, BorderLayout.CENTER);

    return panel;
  }

  public static Component createHorizontalNamedLabelPanel(
      final String labelText,
      final int labelWidth,
      final JTextField textfield,
      final int panelHeight) {

    textfield.setEditable(false);
    textfield.setBorder(null);
    textfield.setForeground(new Color(0, 0, 160));
    textfield.setBackground(Color.WHITE);

    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(0, 3, 2, 3));
    panel.setBackground(Color.WHITE);

    final JLabel descriptionLabel = new JLabel(labelText);
    descriptionLabel.setPreferredSize(new Dimension(labelWidth, panelHeight));
    panel.setPreferredSize(new Dimension(0, panelHeight));

    panel.add(descriptionLabel, BorderLayout.WEST);
    panel.add(textfield, BorderLayout.CENTER);

    return panel;
  }

  public static void setWindowIcons(
      final Window window,
      final String iconPath16x16,
      final String iconPath32x32,
      final String iconPath48x48) {
    final ArrayList<Image> imageList = new ArrayList<>();
    imageList.add(ImageUtils.getImage(iconPath16x16));
    imageList.add(ImageUtils.getImage(iconPath32x32));
    imageList.add(ImageUtils.getImage(iconPath48x48));

    window.setIconImages(imageList);
  }

  public static void updateLater(final JComponent component) {
    EventQueue.invokeLater(
        new Runnable() {
          @Override
          public void run() {
            component.updateUI();
          }
        });
  }
}
