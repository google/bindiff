package com.google.security.zynamics.bindiff.graph.settings;

import com.google.security.zynamics.bindiff.config.GraphViewSettingsConfigItem;
import com.google.security.zynamics.bindiff.enums.EDiffViewMode;
import com.google.security.zynamics.bindiff.enums.EGraphSynchronization;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.log.Logger;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.zygraph.AbstractZyGraphSettings;

public class GraphSettings extends AbstractZyGraphSettings {
  public static final int MAX_SELECTION_UNDO_CACHE = 30;

  private final ListenerProvider<IGraphSettingsChangedListener> settingsListeners =
      new ListenerProvider<>();

  private final GraphLayoutSettings layoutSettings;
  private final GraphProximityBrowsingSettings proximitySettings;
  private final GraphDisplaySettings displaySettings;
  private final GraphMouseSettings mouseSettings;

  private boolean drawBends;
  private boolean showScrollbars;

  private ESide focusSide;
  private EDiffViewMode diffViewMode;
  private EGraphSynchronization graphSync;

  public GraphSettings(final GraphViewSettingsConfigItem initialSettings) {
    layoutSettings = new GraphLayoutSettings(initialSettings);
    proximitySettings = new GraphProximityBrowsingSettings(initialSettings);
    displaySettings = new GraphDisplaySettings(initialSettings);
    mouseSettings = new GraphMouseSettings(initialSettings);

    drawBends = initialSettings.getDrawBends();
    showScrollbars = initialSettings.getShowScrollbars();

    graphSync =
        initialSettings.getViewSynchronization()
            ? EGraphSynchronization.SYNC
            : EGraphSynchronization.ASYNC;
    focusSide = ESide.PRIMARY;
    diffViewMode = EDiffViewMode.NORMAL_VIEW;
  }

  public void addListener(final IGraphSettingsChangedListener listener) {
    settingsListeners.addListener(listener);

    layoutSettings.addListener(listener);
    proximitySettings.addListener(listener);
    displaySettings.addListener(listener);
    mouseSettings.addListener(listener);
  }

  public EDiffViewMode getDiffViewMode() {
    return diffViewMode;
  }

  @Override
  public GraphDisplaySettings getDisplaySettings() {
    return displaySettings;
  }

  public boolean getDrawBends() {
    return drawBends;
  }

  public ESide getFocus() {
    return focusSide;
  }

  public EGraphSynchronization getGraphSyncMode() {
    return graphSync;
  }

  @Override
  public GraphLayoutSettings getLayoutSettings() {
    return layoutSettings;
  }

  @Override
  public GraphMouseSettings getMouseSettings() {
    return mouseSettings;
  }

  @Override
  public GraphProximityBrowsingSettings getProximitySettings() {
    return proximitySettings;
  }

  public boolean getShowScrollbars() {
    return showScrollbars;
  }

  public boolean isAsync() {
    return EGraphSynchronization.ASYNC == getGraphSyncMode();
  }

  public boolean isSync() {
    return EGraphSynchronization.SYNC == getGraphSyncMode();
  }

  public void removeListener(final IGraphSettingsChangedListener listener) {
    try {
      layoutSettings.removeListener(listener);
      proximitySettings.removeListener(listener);
      displaySettings.removeListener(listener);
      mouseSettings.removeListener(listener);
    } catch (final Exception e) {
      Logger.logWarning("Listener was not listening.");
    }
  }

  public void setDiffViewMode(final EDiffViewMode diffViewMode) {
    this.diffViewMode = diffViewMode;

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.diffViewModeChanged(this);
    }
  }

  public void setDrawBends(final boolean drawBends) {
    if (this.drawBends == drawBends) {
      return;
    }

    this.drawBends = drawBends;

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.drawBendsChanged(this);
    }
  }

  public void setFocusSide(final ESide side) {
    if (focusSide == side) {
      return;
    }

    focusSide = side;

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.focusSideChanged(this);
    }
  }

  public void setGraphSyncMode(final EGraphSynchronization graphSync) {
    if (this.graphSync == graphSync) {
      return;
    }

    this.graphSync = graphSync;

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.graphSyncChanged(this);
    }
  }

  public void setShowScrollbars(final boolean show) {
    if (showScrollbars == show) {
      return;
    }

    showScrollbars = show;

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.showScrollbarsChanged(this);
    }
  }
}
