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

package com.google.security.zynamics.bindiff.utils;

import java.awt.Color;

public class HtmlUtils {
  /**
   * Escapes the characters in a <code>String</code> using HTML entities.
   *
   * @param str the <code>String</code> to escape, may be <code>null</code>
   * @return a new escaped <code>String</code>, empty if <code>null</code> string input
   */
  public static String escapeHtml(final String str) {
    final StringBuffer buf = new StringBuffer();
    final int len = str == null ? 0 : str.length();

    for (int i = 0; i < len; i++) {
      final char ch = str.charAt(i);
      if (Character.isLetterOrDigit(ch)) {
        buf.append(ch);
      } else {
        buf.append("&#" + (int) ch + ";");
      }
    }
    return buf.toString();
  }

  public static String getHexColorValue(final Color c) {
    return Integer.toHexString(c.getRGB()).substring(2);
  }

  public static String insertOptionalWordBreaks(
      final String text,
      final int threshold,
      final int insertStep,
      final boolean encodeHtmlEntities) {
    String html = "";

    final String[] words = text.split(" ");

    boolean first = true;
    for (final String word : words) {
      String w = "";

      if (word.length() > threshold) {
        int pos = 0;
        for (; pos < word.length() - word.length() % insertStep - 1; pos += insertStep) {
          if (encodeHtmlEntities) {
            w += escapeHtml(word.substring(pos, pos + insertStep));
          } else {
            w += word.substring(pos, pos + insertStep);
          }
          w += "<span style=\"display:block;width:1px;float:left;\"> </span>";
        }

        w += word.substring(pos);
      } else {
        w = encodeHtmlEntities ? escapeHtml(word) : word;
      }

      html += (first ? "" : encodeHtmlEntities ? escapeHtml(" ") : " ") + w;

      first = false;
    }

    return html;
  }
}
