package com.google.security.zynamics.bindiff.graph.settings;

import com.google.security.zynamics.bindiff.config.GraphViewSettingsConfigItem;
import com.google.security.zynamics.bindiff.enums.EMouseAction;
import com.google.security.zynamics.bindiff.log.Logger;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.zygraph.MouseWheelAction;
import com.google.security.zynamics.zylib.gui.zygraph.settings.IMouseSettings;

public class GraphMouseSettings implements IMouseSettings {
  private EMouseAction mouseWheelAction;
  private int scrollSensitivity;
  private int zoomSensitivity;

  private final ListenerProvider<IGraphSettingsChangedListener> settingsListeners =
      new ListenerProvider<>();

  public GraphMouseSettings(final GraphViewSettingsConfigItem initialSettings) {
    mouseWheelAction = EMouseAction.getEnum(initialSettings.getMouseWheelAction());
    scrollSensitivity = initialSettings.getScrollSensitivity();
    zoomSensitivity = initialSettings.getZoomSensitivity();
  }

  protected void addListener(final IGraphSettingsChangedListener listener) {
    try {
      settingsListeners.addListener(listener);
    } catch (final Exception e) {
      Logger.logWarning("Listener is already listening.");
    }
  }

  protected void removeListener(final IGraphSettingsChangedListener listener) {
    try {
      settingsListeners.removeListener(listener);
    } catch (final Exception e) {
      Logger.logWarning("Listener was not listening.");
    }
  }

  @Override
  public MouseWheelAction getMouseWheelAction() {
    return mouseWheelAction == EMouseAction.ZOOM ? MouseWheelAction.ZOOM : MouseWheelAction.SCROLL;
  }

  @Override
  public int getScrollSensitivity() {
    return scrollSensitivity;
  }

  @Override
  public int getZoomSensitivity() {
    return zoomSensitivity;
  }

  public void setMousewheelAction(final EMouseAction mouseWheelAction) {
    if (this.mouseWheelAction == mouseWheelAction) {
      return;
    }

    this.mouseWheelAction = mouseWheelAction;

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.mouseWheelActionChanged(this);
    }
  }

  public void setScrollSensitivity(final int sensitivity) {
    if (sensitivity == scrollSensitivity) {
      return;
    }

    scrollSensitivity = sensitivity;

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.scrollSensitivityChanged(this);
    }
  }

  public void setZoomSensitivity(final int sensitivity) {
    if (sensitivity == zoomSensitivity) {
      return;
    }

    zoomSensitivity = sensitivity;

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.zoomSensitivityChanged(this);
    }
  }
}
