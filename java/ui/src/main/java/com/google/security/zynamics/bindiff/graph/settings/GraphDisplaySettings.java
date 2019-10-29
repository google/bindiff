package com.google.security.zynamics.bindiff.graph.settings;

import com.google.security.zynamics.bindiff.config.GraphViewSettingsConfigItem;
import com.google.security.zynamics.bindiff.log.Logger;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.zygraph.settings.IDisplaySettings;
import com.google.security.zynamics.zylib.gui.zygraph.settings.IDisplaySettingsListener;

public class GraphDisplaySettings implements IDisplaySettings {
  private int animationSpeed;

  private final ListenerProvider<IDisplaySettingsListener> zySettingsListeners =
      new ListenerProvider<>();

  private final ListenerProvider<IGraphSettingsChangedListener> settingsListeners =
      new ListenerProvider<>();

  public GraphDisplaySettings(final GraphViewSettingsConfigItem initialSettings) {
    animationSpeed = initialSettings.getAnimationSpeed();
  }

  protected void addListener(final IGraphSettingsChangedListener listener) {
    try {
      settingsListeners.addListener(listener);
    } catch (final RuntimeException e) {
      Logger.logWarning("Listener is already listening.");
    }
  }

  protected void removeListener(final IGraphSettingsChangedListener listener) {
    try {
      settingsListeners.removeListener(listener);
    } catch (final RuntimeException e) {
      Logger.logWarning("Listener was not listening.");
    }
  }

  @Override
  public void addListener(final IDisplaySettingsListener listener) {
    zySettingsListeners.addListener(listener);
  }

  @Override
  public int getAnimationSpeed() {
    return animationSpeed;
  }

  @Override
  public boolean getMagnifyingGlassMode() {
    // Always return false, we want to get rid of the magnifying glass
    return false;
  }

  @Override
  public void removeListener(final IDisplaySettingsListener listener) {
    zySettingsListeners.removeListener(listener);
  }

  public void setAnimationSpeed(final int value) {
    if (animationSpeed == value) {
      return;
    }
    animationSpeed = value;

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.animationSpeedChanged(this);
    }
  }

  @Override
  public void setMagnifyingGlassMode(final boolean value) {
    // Do nothing, we want to get rid of the magnifying glass
  }
}
