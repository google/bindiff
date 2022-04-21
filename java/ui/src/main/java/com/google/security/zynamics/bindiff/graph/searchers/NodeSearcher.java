// Copyright 2011-2022 Google LLC
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
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NodeSearcher {
  public static ArrayList<SearchResult> search(
      final ZyGraphNode<?> zyNode,
      String searchString,
      final boolean regEx,
      final boolean caseSensitive) {
    final ArrayList<SearchResult> results = new ArrayList<>();

    if (searchString.isEmpty()) {
      return results;
    }

    final IZyNodeRealizer nodeRealizer = zyNode.getRealizer();
    final ZyLabelContent node = nodeRealizer.getNodeContent();

    int lineCounter = 0;

    for (final ZyLineContent lineContent : node) {
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
              // Something was found
              final List<CStyleRunData> backgroundColorStyleRun =
                  lineContent.getBackgroundStyleRunData(start, end);
              final Color borderColor = nodeRealizer.getRealizer().getLineColor();

              results.add(
                  new SearchResult(
                      zyNode,
                      lineCounter,
                      start,
                      end - start,
                      lineText,
                      backgroundColorStyleRun,
                      borderColor));
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
            final List<CStyleRunData> backgroundColorStyleRun =
                lineContent.getBackgroundStyleRunData(index, index + searchString.length() - 1);
            final Color borderColor = nodeRealizer.getRealizer().getLineColor();

            results.add(
                new SearchResult(
                    zyNode,
                    lineCounter,
                    index,
                    searchString.length(),
                    lineText,
                    backgroundColorStyleRun,
                    borderColor));
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
