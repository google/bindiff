package com.google.security.zynamics.bindiff.graph.backgroundrendering;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.EGraph;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettingsChangedListenerAdapter;
import com.google.security.zynamics.bindiff.project.userview.CallGraphViewData;
import com.google.security.zynamics.bindiff.project.userview.FlowGraphViewData;
import com.google.security.zynamics.bindiff.project.userview.ViewData;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import y.view.Graph2DView;

public class BackgroundRendererManager {
  private final GraphSettings settings;

  private final InternalGraphSettingsChangedListener settingsChangedListener =
      new InternalGraphSettingsChangedListener();

  private final Graph2DView view;

  private final ImageBackgroundRenderer imageBackgroundRenderer;

  public BackgroundRendererManager(
      final ViewData viewData,
      final Graph2DView graph2DView,
      final EGraph graphType,
      final GraphSettings settings) {
    Preconditions.checkNotNull(viewData);
    Preconditions.checkNotNull(graphType);
    this.settings = Preconditions.checkNotNull(settings);
    this.view = graph2DView;
    this.imageBackgroundRenderer = new ImageBackgroundRenderer(viewData, view, graphType);

    settings.addListener(settingsChangedListener);
  }

  protected static String buildTitle(final ViewData viewData, final EGraph type) {
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

  public void addListeners() {
    settings.addListener(settingsChangedListener);
  }

  public void removeListener() {
    settings.removeListener(settingsChangedListener);
  }

  private class InternalGraphSettingsChangedListener extends GraphSettingsChangedListenerAdapter {
    @Override
    public void diffViewModeChanged(final GraphSettings settings) {
      imageBackgroundRenderer.update();
    }
  }
}
