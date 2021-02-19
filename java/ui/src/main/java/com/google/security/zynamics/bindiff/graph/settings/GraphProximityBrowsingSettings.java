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
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.zygraph.settings.IProximitySettings;
import com.google.security.zynamics.zylib.gui.zygraph.settings.IProximitySettingsListener;
import java.util.logging.Level;

public class GraphProximityBrowsingSettings implements IProximitySettings {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private boolean proximityBrowsing;
  private boolean proximityBrowsingFrozen;

  private int proximityBrowsingChildDepth;
  private int proximityBrowsingParentDepth;

  private int autoProximityBrowsingActivationThreshold;

  private final ListenerProvider<IProximitySettingsListener> zySettingsListeners =
      new ListenerProvider<>();

  private final ListenerProvider<IGraphSettingsChangedListener> settingsListeners =
      new ListenerProvider<>();

  public GraphProximityBrowsingSettings(final GraphViewSettingsConfigItem initialSettings) {
    proximityBrowsing = initialSettings.getProximityBrowsing();
    proximityBrowsingFrozen = false;

    proximityBrowsingChildDepth = initialSettings.getProximityBrowsingChildDepth();
    proximityBrowsingParentDepth = initialSettings.getProximityBrowsingParentDepth();

    autoProximityBrowsingActivationThreshold =
        initialSettings.getAutoProximityBrowsingActivationThreshold();
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
      logger.at(Level.WARNING).log("Listener was not listening.");
    }
  }

  @Override
  public void addListener(final IProximitySettingsListener listener) {
    zySettingsListeners.addListener(listener);
  }

  public int getAutoProximityBrowsingActivationThreshold() {
    return autoProximityBrowsingActivationThreshold;
  }

  @Override
  public boolean getProximityBrowsing() {
    return proximityBrowsing;
  }

  @Override
  public int getProximityBrowsingChildren() {
    return proximityBrowsingChildDepth;
  }

  @Override
  public boolean getProximityBrowsingFrozen() {
    return proximityBrowsingFrozen;
  }

  @Override
  public int getProximityBrowsingParents() {
    return proximityBrowsingParentDepth;
  }

  @Override
  public void removeListener(final IProximitySettingsListener listener) {
    zySettingsListeners.removeListener(listener);
  }

  public void setAutoProximityBrowsingActivationThreshold(final int threshold) {
    if (threshold == autoProximityBrowsingActivationThreshold) {
      return;
    }

    autoProximityBrowsingActivationThreshold = threshold;

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.autoProximityBrowsingActivationThresholdChanged(this);
    }
  }

  public void setProximityBrowsing(final boolean enable) {
    if (proximityBrowsing == enable) {
      return;
    }

    proximityBrowsing = enable;

    for (final IProximitySettingsListener listener : zySettingsListeners) {
      // How many listener are going to be notified here?
      listener.changedProximityBrowsing(enable);
    }

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.proximityBrowsingChanged(this);
    }
  }

  public void setProximityBrowsingChildren(final int childDepth) {
    if (childDepth == proximityBrowsingChildDepth) {
      return;
    }

    proximityBrowsingChildDepth = childDepth;

    for (final IProximitySettingsListener listener : zySettingsListeners) {
      listener.changedProximityBrowsingDepth(childDepth, getProximityBrowsingParents());
    }

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.proximityBrowsingChildDepthChanged(this);
    }
  }

  public void setProximityBrowsingFrozen(final boolean proximityBrowsingFrozen) {
    if (this.proximityBrowsingFrozen == proximityBrowsingFrozen) {
      return;
    }

    this.proximityBrowsingFrozen = proximityBrowsingFrozen;

    for (final IProximitySettingsListener listener : zySettingsListeners) {
      listener.changedProximityBrowsingFrozen(getProximityBrowsingFrozen());
    }

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.proximityBrowsingFrozenChanged(this);
    }
  }

  public void setProximityBrowsingParents(final int parentDepth) {
    if (parentDepth == proximityBrowsingParentDepth) {
      return;
    }

    proximityBrowsingParentDepth = parentDepth;

    for (final IProximitySettingsListener listener : zySettingsListeners) {
      listener.changedProximityBrowsingDepth(getProximityBrowsingChildren(), parentDepth);
    }

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.proximityBrowsingParentDepthChanged(this);
    }
  }
}
