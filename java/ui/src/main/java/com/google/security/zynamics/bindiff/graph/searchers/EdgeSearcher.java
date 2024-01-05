// Copyright 2011-2024 Google LLC
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

package com.google.security.zynamics.bindiff.graph.searchers;

import com.google.security.zynamics.zylib.gui.zygraph.realizers.CStyleRunData;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EdgeSearcher {
  public static ArrayList<SearchResult> search(
      final ZyGraphEdge<?, ?, ?> edge,
      String searchString,
      final boolean regEx,
      final boolean caseSensitive) {
    final ArrayList<SearchResult> results = new ArrayList<>();

    if (searchString.isEmpty() || edge.getRealizer().labelCount() != 1) {
      return results;
    }

    final ZyLabelContent content = edge.getRealizer().getEdgeLabelContent();

    int lineCounter = 0;

    for (final ZyLineContent lineContent : content) {
      String lineText = lineContent.getText();

      if (!caseSensitive) {
        lineText = lineText.toLowerCase();
      }

      int startPosition = 0;

      do {
        if (regEx) {
          Pattern pattern;

          if (caseSensitive) {
            pattern = Pattern.compile(searchString);
          } else {
            pattern = Pattern.compile(searchString, Pattern.CASE_INSENSITIVE);
          }

          final Matcher matcher = pattern.matcher(lineText);
          final boolean found = matcher.find(startPosition);
          if (found) {
            final int start = matcher.start();
            final int end = matcher.end();

            if (start != end) {
              final List<CStyleRunData> backgroundStyleRun =
                  lineContent.getBackgroundStyleRunData(start, end);

              // Something was found
              results.add(
                  new SearchResult(
                      edge,
                      lineCounter,
                      start,
                      end - start,
                      lineText,
                      backgroundStyleRun,
                      Color.BLACK));
            }

            startPosition = end;

            if (start == end) {
              startPosition++;
            }

            if (matcher.end() == lineText.length()) {
              break;
            }
          } else {
            break;
          }
        } else {
          if (!caseSensitive) {
            searchString = searchString.toLowerCase();
          }

          final int index = lineText.indexOf(searchString, startPosition);

          if (index != -1) {
            final List<CStyleRunData> backgroundStyleRun =
                lineContent.getBackgroundStyleRunData(index, index + searchString.length() - 1);

            results.add(
                new SearchResult(
                    edge,
                    lineCounter,
                    index,
                    searchString.length(),
                    lineText,
                    backgroundStyleRun,
                    Color.BLACK));
            startPosition = index + searchString.length();
          } else {
            break;
          }
        }
      } while (true);

      ++lineCounter;
    }

    return results;
  }
}
