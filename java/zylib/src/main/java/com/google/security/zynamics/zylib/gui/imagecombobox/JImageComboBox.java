// Copyright 2011-2023 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.zylib.gui.imagecombobox;

import java.awt.Component;
import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

public class JImageComboBox extends JComboBox<ImageElement> {

  public JImageComboBox(final ImageElement[] items) {
    super(items);

    setRenderer(new ComboBoxRenderer(JLabel.CENTER));
  }

  public JImageComboBox(final ImageElement[] items, final int labelAlignment) {
    super(items);

    if ((labelAlignment != JLabel.LEFT)
        && (labelAlignment != JLabel.CENTER)
        && (labelAlignment != JLabel.RIGHT)) {
      throw new IllegalArgumentException("Error: Label alignment in invalid.");
    }

    setRenderer(new ComboBoxRenderer(labelAlignment));
  }

  private static class ComboBoxRenderer extends JLabel implements ListCellRenderer<Object> {
    private static final long serialVersionUID = 2728401247866641230L;

    private Font uhOhFont;

    public ComboBoxRenderer(final int alignment) {
      setOpaque(true);
      setHorizontalAlignment(alignment);
      setVerticalAlignment(CENTER);

      if (alignment == JLabel.LEFT) {
        setBorder(new EmptyBorder(0, 5, 0, 0));
      }
    }

    protected void setUhOhText(final String uhOhText, final Font normalFont) {
      if (uhOhFont == null) {
        // lazily create this font
        uhOhFont = normalFont.deriveFont(Font.ITALIC);
      }

      setFont(uhOhFont);
      setText(uhOhText);
    }

    /*
     * This method finds the image and text corresponding to the selected value and returns the
     * label, set up to display the text and image.
     */
    @Override
    public Component getListCellRendererComponent(
        final JList<?> list,
        final Object value,
        final int index,
        final boolean isSelected,
        final boolean cellHasFocus) {
      final ImageElement element = (ImageElement) value;

      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      } else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }

      if (element == null) {
        return this;
      }

      final ImageIcon icon = element.getIcon();
      final String text = element.getObject().toString();

      setIcon(icon);

      if (icon != null) {
        setText(text);
        setFont(list.getFont());
      } else {
        setUhOhText(text + " (no image available)", list.getFont());
      }

      return this;
    }
  }
}
