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

package com.google.security.zynamics.zylib.gui;

import javax.swing.JFormattedTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import com.google.common.base.Preconditions;

/**
 * Hex formatter that can be used to validate input for hexadecimal text fields. Taken from
 * http://forum.java.sun.com/thread.jspa?threadID=436233&messageID=3169976
 * 
 * Slightly modified.
 */
public class CHexFormatter extends JFormattedTextField.AbstractFormatter {
  private static final long serialVersionUID = 6996845563062947862L;

  private final HexFilter filter = new HexFilter();

  private int maxSize = Integer.MAX_VALUE;

  public CHexFormatter() {
  }

  public CHexFormatter(final int maxSize) {
    Preconditions.checkArgument(maxSize > 0, "Error: Maximum input size must be positive");
    this.maxSize = maxSize;
  }

  private boolean isValid(final String string, final int replaced) {
    if (((getFormattedTextField().getText().length() - replaced) + string.length()) > maxSize) {
      invalidEdit();
      return false;
    }

    for (int i = 0; i < string.length(); i++) {
      final char ch = string.charAt(i);

      if (Character.digit(ch, 16) == -1) {
        invalidEdit();
        return false;
      }
    }

    return true;
  }

  @Override
  protected DocumentFilter getDocumentFilter() {
    return filter;
  }

  @Override
  public Object stringToValue(final String s) {
    return s;
  }

  @Override
  public String valueToString(final Object o) {
    return (String) o;
  }

  private class HexFilter extends DocumentFilter {
    @Override
    public void insertString(final DocumentFilter.FilterBypass fb, final int offset,
        final String string, final AttributeSet attr) throws BadLocationException {
      if (isValid(string, 0)) {
        super.insertString(fb, offset, string, attr);
      }
    }

    @Override
    public void replace(final DocumentFilter.FilterBypass fb, final int offset, final int length,
        final String string, final AttributeSet attr) throws BadLocationException {
      if (isValid(string, length)) {
        super.replace(fb, offset, length, string, attr);
      }
    }
  }
}
