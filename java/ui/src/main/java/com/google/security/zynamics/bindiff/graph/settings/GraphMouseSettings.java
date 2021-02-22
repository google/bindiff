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

package com.google.security.zynamics.bindiff.graph.settings;

import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.config.GraphViewSettingsConfigItem;
import com.google.security.zynamics.bindiff.enums.EMouseAction;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.zygraph.MouseWheelAction;
import com.google.security.zynamics.zylib.gui.zygraph.settings.IMouseSettings;
import java.util.logging.Level;

public class GraphMouseSettings implements IMouseSettings {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private EMouseAction mouseWheelAction;
  private int scrollSensitivity;
  private int zoomSensitivity;

  private final ListenerProvider<IGraphSettingsChangedListener> settingsListeners =
      new ListenerProvider<>();

  public GraphMouseSettings(final GraphViewSettingsConfigItem initialSettings) {
    mouseWheelAction = initialSettings.getMouseWheelAction();
    scrollSensitivity = initialSettings.getScrollSensitivity();
    zoomSensitivity = initialSettings.getZoomSensitivity();
  }

  protected void addListener(final IGraphSettingsChangedListener listener) {
    try {
      settingsListeners.addListener(listener);
    } catch (final Exception e) {
      logger.at(Level.WARNING).log("Listener is already listening");
    }
  }

  protected void removeListener(final IGraphSettingsChangedListener listener) {
    try {
      settingsListeners.removeListener(listener);
    } catch (final Exception e) {
      logger.at(Level.WARNING).log("Listener was not listening");
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

  public void setMouseWheelAction(final EMouseAction mouseWheelAction) {
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
