package com.google.security.zynamics.bindiff.graph.settings;

import com.google.security.zynamics.bindiff.config.GraphViewSettingsConfigItem;
import com.google.security.zynamics.bindiff.log.Logger;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.zygraph.settings.IProximitySettings;
import com.google.security.zynamics.zylib.gui.zygraph.settings.IProximitySettingsListener;

public class GraphProximityBrowsingSettings implements IProximitySettings {
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
