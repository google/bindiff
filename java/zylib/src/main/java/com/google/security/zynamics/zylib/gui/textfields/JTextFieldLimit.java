// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.textfields;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

public class JTextFieldLimit extends DefaultStyledDocument {
  private static final long serialVersionUID = -8124048672190684534L;

  private final int limit;

  public JTextFieldLimit() {
    this(30000);
  }

  public JTextFieldLimit(final int limit) {
    this.limit = limit;
  }

  @Override
  public void insertString(final int offset, final String str, final AttributeSet attr)
      throws BadLocationException {
    if (str == null) {
      return;
    }

    if ((getLength() + str.length()) <= limit) {
      super.insertString(offset, str, attr);
    }
  }
}
