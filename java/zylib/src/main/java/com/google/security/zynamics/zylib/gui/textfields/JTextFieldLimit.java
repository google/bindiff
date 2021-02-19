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
