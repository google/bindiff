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

package com.google.security.zynamics.bindiff.graph.layout;

import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;

public class LayoutCommandHelper {
  public static void activateAutoLayout(final BinDiffGraph<?, ?> graph, final boolean autoLayout) {
    graph.getSettings().getLayoutSettings().setAutomaticLayouting(autoLayout);
  }

  public static boolean deactiveAutoLayout(final BinDiffGraph<?, ?> graph) {
    final boolean wasAutolayout = isAutolayout(graph);
    graph.getSettings().getLayoutSettings().setAutomaticLayouting(false);

    return wasAutolayout;
  }

  public static boolean hasSelectedNodes(final BinDiffGraph<?, ?> graph) {
    final GraphSettings settings = graph.getSettings();

    if (settings.isSync()) {
      return graph.getSuperGraph().getSelectedNodes().size() > 0;
    }

    return graph.getSelectedNodes().size() > 0;
  }

  public static boolean isAutolayout(final BinDiffGraph<?, ?> graph) {
    return graph.getSettings().getLayoutSettings().getAutomaticLayouting();
  }

  public static boolean isProximityBrowsing(final BinDiffGraph<?, ?> graph) {
    return graph.getSettings().getProximitySettings().getProximityBrowsing();
  }

  public static boolean isProximityBrowsingFrozen(final BinDiffGraph<?, ?> graph) {
    return graph.getSettings().getProximitySettings().getProximityBrowsingFrozen();
  }

  public static boolean isSnychron(final BinDiffGraph<?, ?> graph) {
    return graph.getSettings().isSync();
  }
}
