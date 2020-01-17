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

package com.google.security.zynamics.zylib.gui.scripting.console;

import com.google.security.zynamics.zylib.gui.scripting.SyntaxDocument;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;


public class ConsoleHelpers {
  private final SyntaxDocument document;

  private final MutableAttributeSet normal;

  public ConsoleHelpers(final SyntaxDocument document, final MutableAttributeSet normal) {
    this.document = document;
    this.normal = normal;
  }

  /**
   * Convert a global position in the document to a position within the containing element.
   * 
   * @param pos the global document position which to convert to a position within the element
   *        containing it.
   * @return the position within the element
   */
  public int getCaretOffsetInLine(final int pos) {
    return pos - document.getParagraphElement(pos).getStartOffset();
  }

  /**
   * Get the text of the element containing the given position.
   * 
   * @param pos the global document position at which to find the element containing it.
   * @return the text contained within the element
   */
  public String getCurrentLine(final int pos) {
    final Element element = document.getParagraphElement(pos);
    String line = "";

    try {
      line =
          document.getText(element.getStartOffset(),
              element.getEndOffset() - element.getStartOffset());
    } catch (final BadLocationException e) {
      System.out.println("Bad location!");
      e.printStackTrace();
    }

    return line;
  }

  /**
   * Get the global document position of the start of an element.
   * 
   * @param position the global document position at which to find the element containing it.
   * @return the global start position of the element
   */
  public int getLineStartOffset(final int position) {
    return document.getParagraphElement(position).getStartOffset();
  }

  public MutableAttributeSet getNormal() {
    return normal;
  }

  /**
   * Return all the characters forming a word (alpha-chars) from the given position backwards, up to
   * the when the first delimiter is found.
   * 
   * @param position location within the document from where to retrieve a word
   */
  public String getWord(final int position) {
    int offset = position;
    final Element element = document.getParagraphElement(offset);
    String elementText;

    try {
      elementText =
          document.getText(element.getStartOffset(),
              element.getEndOffset() - element.getStartOffset());
    } catch (final Exception excp) {
      return "";
    }

    final int elementTextLength = elementText.length();
    if (elementTextLength == 0) {
      return "";
    }

    int i = 0;
    if (element.getStartOffset() > 0) {
      offset = offset - element.getStartOffset();
    }

    for (i = offset - 1; i >= 0; i--) {
      final char c = elementText.charAt(i);
      if (document.isDelimiter("" + c) || (i == 0)) {
        return elementText.substring(i + 1, offset).trim();
      }
    }
    return "";
  }
}
