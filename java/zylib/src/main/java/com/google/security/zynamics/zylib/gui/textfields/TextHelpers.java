// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.textfields;

import javax.swing.text.Element;
import javax.swing.text.JTextComponent;

public class TextHelpers {
  public static int getLineAtCaret(final JTextComponent component) {
    final int caretPosition = component.getCaretPosition();
    final Element root = component.getDocument().getDefaultRootElement();

    return root.getElementIndex(caretPosition) + 1;
  }

  public static int getNumberOfLines(final JTextComponent component) {
    final Element root = component.getDocument().getDefaultRootElement();

    return root.getElementCount();
  }

  public static void insert(final JTextComponent component, final int position, final String string) {
    final String old = component.getText();

    component.setText(old.substring(0, position) + string + old.substring(position));
  }

  public static void insert(final JTextComponent component, final String string) {
    final int start = component.getSelectionStart();

    insert(component, start, string);

    component.setSelectionStart(start + string.length());
    component.setSelectionEnd(start + string.length());
  }
}
