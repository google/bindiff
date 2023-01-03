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

  public static void insert(
      final JTextComponent component, final int position, final String string) {
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
