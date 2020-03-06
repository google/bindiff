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

package com.google.security.zynamics.bindiff.graph.helpers;

import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class GraphColorizer {
  private static final LinkedHashSet<Color> recentColors = new LinkedHashSet<>();

  public static void colorizeInvisibleNodes(final BinDiffGraph<?, ?> graph, final Color color) {
    for (final ZyGraphNode<?> node : graph.getNodes()) {
      if (!node.isVisible()) {
        node.getRawNode().setColor(color);
      }
    }
  }

  public static void colorizeSelectedNodes(final BinDiffGraph<?, ?> graph, final Color color) {
    for (final ZyGraphNode<?> node : graph.getSelectedNodes()) {
      node.getRawNode().setColor(color);
    }
  }

  public static void colorizeUnselectedNodes(final BinDiffGraph<?, ?> graph, final Color color) {
    for (final ZyGraphNode<?> node : graph.getNodes()) {
      if (!node.isSelected()) {
        node.getRawNode().setColor(color);
      }
    }
  }

  public static List<Color> getRecentColors() {
    return new ArrayList<>(recentColors);
  }

  public static void setRecentColors(final List<Color> colors) {
    recentColors.clear();
    recentColors.addAll(colors);
  }
}
