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

package com.google.security.zynamics.zylib.gui.zygraph.helpers;

import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class HtmlGenerator {
  private static Map<String, String> initLineCache = new HashMap<String, String>();

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

  public static String getHtml(final Collection<String> strings, final String fontname,
      final boolean boldFirstLine) {
    final StringBuilder html =
        new StringBuilder(String.format("<html><font face=\"%s\" size=\"3\" color=\"000000\">",
            fontname));

    boolean first = true;

    for (final String string : strings) {
      html.append(boldFirstLine && first ? "<b>" : "");
      html.append(escapeHtml(string));
      html.append(boldFirstLine && first ? "</b>" : "");
      html.append("<br>");

      first = false;
    }

    html.append("</font></html>");

    return html.toString();
  }

  public static String getHtml(final ZyLabelContent content, final String fontname,
      final boolean boldFirstLine) {
    return getHtml(content, fontname, boldFirstLine, false);
  }

  public static String getHtml(final ZyLabelContent content, final String fontname,
      final boolean boldFirstLine, final boolean eliminateEmptyLines) {
    if (!initLineCache.containsKey(fontname)) {
      initLineCache.put(fontname,
          String.format("<html><font face=\"%s\" size=\"3\" color=\"000000\">", fontname));
    }

    final StringBuilder html = new StringBuilder(initLineCache.get(fontname));

    final int tooltipMaxLength = content.getLineCount() >= 40 ? 40 : content.getLineCount();

    for (int i = 0; i < tooltipMaxLength; ++i) {
      final String text = content.getLineContent(i).getText();
      if (eliminateEmptyLines) {
        if ((text.length() < 1) || text.equals("\n") || text.equals("\r") || text.equals("\t")) {
          continue;
        }
      }

      html.append(boldFirstLine && (i == 0) ? "<b>" : "");
      html.append(escapeHtml(text));
      html.append(boldFirstLine && (i == 0) ? "</b>" : "");
      html.append("<br>");
    }
    if (content.getLineCount() > 40) {
      html.append("...");
      html.append("<br>");
    }

    html.append("</font></html>");

    return html.toString();
  }

  public static String getHtml(final ZyLabelContent contentOne, final ZyLabelContent contentTwo,
      final String fontname, final boolean boldFirstLine) {
    return getHtml(contentOne, contentTwo, fontname, boldFirstLine, false);
  }

  public static String getHtml(final ZyLabelContent contentOne, final ZyLabelContent contentTwo,
      final String fontname, final boolean boldFirstLine, final boolean eliminateEmptyLines) {
    final StringBuilder html =
        new StringBuilder(String.format("<html><font face=\"%s\" size=\"3\" color=\"000000\">",
            fontname));

    for (int i = 0; i < contentOne.getLineCount(); ++i) {
      final String text = contentOne.getLineContent(i).getText();
      if (eliminateEmptyLines) {
        if ((text.length() < 1) || text.equals("\n") || text.equals("\r") || text.equals("\t")) {
          continue;
        }
      }

      html.append(boldFirstLine && (i == 0) ? "<b>" : "");
      html.append(escapeHtml(contentOne.getLineContent(i).getText()));
      html.append(boldFirstLine && (i == 0) ? "</b>" : "");
      html.append("<br>");
    }

    html.append("</font>");
    html.append("<hr></hr>");
    html.append(String.format("<font face=\"%s\" size=\"3\" color=\"000000\">", fontname));

    for (int i = 0; i < contentTwo.getLineCount(); ++i) {
      final String text = contentTwo.getLineContent(i).getText();
      if (eliminateEmptyLines) {
        if ((text.length() < 1) || text.equals("\n") || text.equals("\r") || text.equals("\t")) {
          continue;
        }
      }

      html.append(boldFirstLine && (i == 0) ? "<b>" : "");
      html.append(escapeHtml(contentTwo.getLineContent(i).getText()));
      html.append(boldFirstLine && (i == 0) ? "</b>" : "");
      html.append("<br>");
    }

    html.append("</font></html>");

    return html.toString();
  }

}
