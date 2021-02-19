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

public class CDecFormatter extends JFormattedTextField.AbstractFormatter {
  private static final long serialVersionUID = 8368311921001233300L;

  private final DecFilter filter = new DecFilter();

  private final int m_maxChar;

  private final int m_minNumber;
  private final int m_maxNumber;

  public CDecFormatter() {
    this(Integer.MAX_VALUE);
  }

  public CDecFormatter(final int maxChar) {
    m_maxChar = maxChar;

    m_minNumber = -1;
    m_maxNumber = -1;
  }

  public CDecFormatter(final int maxChar, final int minNumber, final int maxNumber) {
    m_maxChar = maxChar;
    m_minNumber = minNumber;
    m_maxNumber = maxNumber;
  }

  private boolean isValid(final String string, final int selected) {
    if (((getFormattedTextField().getText().length() - selected) + string.length()) > m_maxChar) {
      invalidEdit();
      return false;
    }

    for (int i = 0; i < string.length(); i++) {
      final char ch = string.charAt(i);
      if (Character.digit(ch, 10) == -1) {
        invalidEdit();
        return false;
      }
    }

    if ((m_minNumber != -1) && (m_maxNumber != -1)) {
      final int value = Integer.parseInt(getFormattedTextField().getText() + string);
      if ((value < m_minNumber) || (value > m_maxNumber)) {
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
    return s.equals("") ? 0 : Long.parseLong(s);
  }

  @Override
  public String valueToString(final Object o) {
    if (o == null) {
      return null;
    }
    return Long.toString((Long) o);
  }

  private class DecFilter extends DocumentFilter {
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
