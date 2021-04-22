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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.config.GraphViewSettingsConfigItem;
import com.google.security.zynamics.bindiff.enums.ECircularLayoutStyle;
import com.google.security.zynamics.bindiff.enums.EGraphLayout;
import com.google.security.zynamics.bindiff.enums.ELayoutOrientation;
import com.google.security.zynamics.bindiff.enums.EOrthogonalLayoutStyle;
import com.google.security.zynamics.bindiff.graph.layout.LayoutCreator;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.settings.ILayoutSettings;
import y.layout.CanonicMultiStageLayouter;

public class GraphLayoutSettings implements ILayoutSettings {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private EGraphLayout defaultGraphLayout;

  private boolean autoLayouting;

  private int visibilityWarningThreshold;

  private int animationSpeed;
  private final int lastAnimationSpeed;

  // Hierarchical tab
  private ELayoutOrientation hierarchicalLayoutOrientation;
  private boolean hierarchicalOrthogonalEdgeRouting;
  private int hierarchicalLayoutMinLayerDistance;
  private int hierarchicalLayoutMinNodeDistance;

  // Orthogonal tab
  private int orthogonalLayoutMinNodeDistance;
  private ELayoutOrientation orthogonalLayoutOrientation;
  private EOrthogonalLayoutStyle orthogonalLayoutStyle;

  // Circular tab
  private ECircularLayoutStyle circularLayoutStyle;
  private int circularLayoutMinNodeDistance;

  private CanonicMultiStageLayouter currentLayouter;

  private final ListenerProvider<IGraphSettingsChangedListener> settingsListeners =
      new ListenerProvider<>();

  public GraphLayoutSettings(final GraphViewSettingsConfigItem initialSettings) {
    // Configurable initial settings.
    defaultGraphLayout = initialSettings.getDefaultGraphLayout();

    autoLayouting = initialSettings.getAutoLayouting();
    visibilityWarningThreshold = initialSettings.getVisibilityWarningThreshold();

    animationSpeed = initialSettings.getAnimationSpeed();
    lastAnimationSpeed = animationSpeed;

    circularLayoutStyle = initialSettings.getCircularLayoutStyle();
    circularLayoutMinNodeDistance = initialSettings.getCircularMinimumNodeDistance();

    hierarchicalOrthogonalEdgeRouting = initialSettings.getHierarchicalOrthogonalEdgeRouting();
    hierarchicalLayoutOrientation = initialSettings.getHierarchicalOrientation();
    hierarchicalLayoutMinLayerDistance = initialSettings.getHierarchicalMinimumNodeDistance();
    hierarchicalLayoutMinNodeDistance = initialSettings.getHierarchicalMinimumLayerDistance();

    orthogonalLayoutStyle = initialSettings.getOrthogonalLayoutStyle();
    orthogonalLayoutOrientation = initialSettings.getOrthogonalOrientation();
    orthogonalLayoutMinNodeDistance = initialSettings.getOrthogonalMinimumNodeDistance();

    updateLayouter();
  }

  private void setCurrentLayouter(final CanonicMultiStageLayouter layouter) {
    checkNotNull(layouter);

    currentLayouter = layouter;
  }

  protected void addListener(final IGraphSettingsChangedListener listener) {
    try {
      settingsListeners.addListener(listener);
    } catch (final IllegalStateException e) {
      logger.atWarning().log("Listener is already listening");
    }
  }

  protected void removeListener(final IGraphSettingsChangedListener listener) {
    try {
      settingsListeners.removeListener(listener);
    } catch (final IllegalStateException e) {
      logger.atWarning().log("Listener was not listening");
    }
  }

  @Override
  public boolean getAnimateLayout() {
    return animationSpeed > 0;
  }

  @Override
  public int getAnimateLayoutEdgeThreshold() {
    return 0; // not in use for BinDiff
  }

  @Override
  public int getAnimateLayoutNodeThreshold() {
    return 0; // not in use for BinDiff
  }

  @Override
  public boolean getAutomaticLayouting() {
    return autoLayouting;
  }

  public ECircularLayoutStyle getCircularLayoutStyle() {
    return circularLayoutStyle;
  }

  @Override
  public CanonicMultiStageLayouter getCurrentLayouter() {
    return currentLayouter;
  }

  public EGraphLayout getDefaultGraphLayout() {
    return defaultGraphLayout;
  }

  public boolean getHierarchicalOrthogonalEdgeRouting() {
    return hierarchicalOrthogonalEdgeRouting;
  }

  public ELayoutOrientation getHierarchicalOrientation() {
    return hierarchicalLayoutOrientation;
  }

  public long getMinimumCircularNodeDistance() {
    return circularLayoutMinNodeDistance;
  }

  public long getMinimumHierarchicLayerDistance() {
    return hierarchicalLayoutMinLayerDistance;
  }

  public long getMinimumHierarchicNodeDistance() {
    return hierarchicalLayoutMinNodeDistance;
  }

  public long getMinimumOrthogonalNodeDistance() {
    return orthogonalLayoutMinNodeDistance;
  }

  public ELayoutOrientation getOrthogonalLayoutOrientation() {
    return orthogonalLayoutOrientation;
  }

  public EOrthogonalLayoutStyle getOrthogonalLayoutStyle() {
    return orthogonalLayoutStyle;
  }

  public int getVisibilityWarningThreshold() {
    return visibilityWarningThreshold;
  }

  public void setAnimateLayout(final boolean animateLayout) {
    if (animateLayout == (animationSpeed > 0)) {
      return;
    }

    animationSpeed = lastAnimationSpeed;

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.layoutAnimationChanged(this);
    }
  }

  public void setAutomaticLayouting(final boolean autoLayouting) {
    if (this.autoLayouting == autoLayouting) {
      return;
    }

    this.autoLayouting = autoLayouting;

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.autoLayoutChanged(this);
    }
  }

  public void setCircularLayoutStyle(final ECircularLayoutStyle circularLayout) {
    if (circularLayout == circularLayoutStyle) {
      return;
    }

    circularLayoutStyle = circularLayout;

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.circularLayoutStyleChanged(this);
    }
  }

  public void setDefaultGraphLayout(final EGraphLayout layout) {
    if (layout == defaultGraphLayout) {
      return;
    }

    defaultGraphLayout = layout;
    updateLayouter();

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.layoutChanged(this);
    }
  }

  public void setHierarchicOrientation(final ELayoutOrientation orientation) {
    if (orientation == hierarchicalLayoutOrientation) {
      return;
    }

    hierarchicalLayoutOrientation = orientation;

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.hierarchicalLayoutOrientationChanged(this);
    }
  }

  public void setHierarchicalOrthogonalEdgeRouting(final boolean orthogonal) {
    if (orthogonal == hierarchicalOrthogonalEdgeRouting) {
      return;
    }

    hierarchicalOrthogonalEdgeRouting = orthogonal;

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.hierarchicalOrthogonalEdgeRoutingChanged(this);
    }
  }

  public void setMinimumCircularNodeDistance(final int minNodeDistance) {
    if (minNodeDistance == circularLayoutMinNodeDistance) {
      return;
    }

    circularLayoutMinNodeDistance = minNodeDistance;

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.circularLayoutMinNodeDistanceChanged(this);
    }
  }

  public void setMinimumHierarchicLayerDistance(final int minLayerDistance) {
    if (minLayerDistance == hierarchicalLayoutMinLayerDistance) {
      return;
    }

    hierarchicalLayoutMinLayerDistance = minLayerDistance;

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.hierarchicalLayoutMinLayerDistanceChanged(this);
    }
  }

  public void setMinimumHierarchicNodeDistance(final int minNodeDistance) {
    if (minNodeDistance == hierarchicalLayoutMinNodeDistance) {
      return;
    }

    hierarchicalLayoutMinNodeDistance = minNodeDistance;

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.hierarchicalLayoutMinNodeDistanceChanged(this);
    }
  }

  public void setMinimumOrthogonalNodeDistance(final int minNodeDistance) {
    if (minNodeDistance == orthogonalLayoutMinNodeDistance) {
      return;
    }

    orthogonalLayoutMinNodeDistance = minNodeDistance;

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.orthogonalLayoutMinNodeDistanceChanged(this);
    }
  }

  public void setOrthogonalLayoutOrientation(final ELayoutOrientation orientation) {
    if (orientation == orthogonalLayoutOrientation) {
      return;
    }

    orthogonalLayoutOrientation = orientation;

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.orthogonalLayoutOrientationChanged(this);
    }
  }

  public void setOrthogonalLayoutStyle(final EOrthogonalLayoutStyle orthogonalLayout) {
    if (orthogonalLayout == orthogonalLayoutStyle) {
      return;
    }

    orthogonalLayoutStyle = orthogonalLayout;

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.orthogonalLayoutStyleChanged(this);
    }
  }

  public void setVisibilityWarningThreshold(final int visibilityWarningThreshold) {
    if (this.visibilityWarningThreshold == visibilityWarningThreshold) {
      return;
    }

    this.visibilityWarningThreshold = visibilityWarningThreshold;

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.visibilityWarningThresholdChanged(this);
    }
  }

  public void updateLayouter() {
    switch (getDefaultGraphLayout()) {
      case HIERARCHICAL:
        setCurrentLayouter(LayoutCreator.getHierarchicalLayout(this));
        break;
      case ORTHOGONAL:
        setCurrentLayouter(LayoutCreator.getOrthogonalLayout(this));
        break;
      default:
        setCurrentLayouter(LayoutCreator.getCircularLayout(this));
    }
  }
}
