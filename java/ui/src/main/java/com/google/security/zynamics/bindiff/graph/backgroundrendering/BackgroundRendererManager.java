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

package com.google.security.zynamics.bindiff.graph.backgroundrendering;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Joiner;
import com.google.security.zynamics.bindiff.enums.EGraph;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettingsChangedListenerAdapter;
import com.google.security.zynamics.bindiff.project.userview.CallGraphViewData;
import com.google.security.zynamics.bindiff.project.userview.FlowGraphViewData;
import com.google.security.zynamics.bindiff.project.userview.ViewData;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import y.view.Graph2DView;

/** Manages background drawing for graph views. */
public class BackgroundRendererManager extends GraphSettingsChangedListenerAdapter {
  private final GraphSettings settings;

  private final ImageBackgroundRenderer imageBackgroundRenderer;

  public BackgroundRendererManager(
      final ViewData viewData,
      final Graph2DView view,
      final EGraph graphType,
      final GraphSettings settings) {
    checkNotNull(viewData);
    checkNotNull(graphType);
    this.settings = checkNotNull(settings);
    imageBackgroundRenderer = new ImageBackgroundRenderer(viewData, view, graphType);
    view.setBackgroundRenderer(imageBackgroundRenderer);

    settings.addListener(this);
  }

  static String buildTitle(final ViewData viewData, final EGraph type) {
    if (viewData instanceof FlowGraphViewData) {
      final FlowGraphViewData data = (FlowGraphViewData) viewData;
      IAddress address;
      final Joiner joiner = Joiner.on(" ").skipNulls();
      switch (type) {
        case PRIMARY_GRAPH:
          address = data.getAddress(ESide.PRIMARY);
          return address == null ? "" : address + " " + data.getFunctionName(ESide.PRIMARY);
        case SECONDARY_GRAPH:
          address = data.getAddress(ESide.SECONDARY);
          return address == null ? "" : data.getFunctionName(ESide.SECONDARY) + " " + address;
        case COMBINED_GRAPH:
          final IAddress primaryAddress = data.getAddress(ESide.PRIMARY);
          final String primaryName = data.getFunctionName(ESide.PRIMARY);
          final IAddress secondaryAddress = data.getAddress(ESide.SECONDARY);
          final String secondaryName = data.getFunctionName(ESide.SECONDARY);
          return joiner.join(
              primaryAddress,
              primaryName,
              primaryAddress == null || secondaryAddress == null ? "" : "   vs   ",
              secondaryAddress,
              secondaryName);
        default:
      }
    } else if (viewData instanceof CallGraphViewData) {
      final CallGraphViewData data = (CallGraphViewData) viewData;
      switch (type) {
        case PRIMARY_GRAPH:
          return data.getImageName(ESide.PRIMARY);
        case SECONDARY_GRAPH:
          return data.getImageName(ESide.SECONDARY);
        case COMBINED_GRAPH:
          return String.format(
              "%s vs %s", data.getImageName(ESide.PRIMARY), data.getImageName(ESide.SECONDARY));
        default:
      }
    }
    return "";
  }

  public void removeListener() {
    settings.removeListener(this);
  }

  @Override
  public void diffViewModeChanged(final GraphSettings settings) {
    imageBackgroundRenderer.update();
  }
}
