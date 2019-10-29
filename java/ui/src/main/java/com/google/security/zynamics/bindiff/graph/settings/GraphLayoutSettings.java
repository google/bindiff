package com.google.security.zynamics.bindiff.graph.settings;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.config.GraphViewSettingsConfigItem;
import com.google.security.zynamics.bindiff.enums.ECircularLayoutStyle;
import com.google.security.zynamics.bindiff.enums.EGraphLayout;
import com.google.security.zynamics.bindiff.enums.ELayoutOrientation;
import com.google.security.zynamics.bindiff.enums.EOrthogonalLayoutStyle;
import com.google.security.zynamics.bindiff.graph.layout.LayoutCreator;
import com.google.security.zynamics.bindiff.log.Logger;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.settings.ILayoutSettings;
import y.layout.CanonicMultiStageLayouter;

public class GraphLayoutSettings implements ILayoutSettings {
  private EGraphLayout defaultGraphLayout;

  private boolean autoLayouting;

  private int visibilityWarningThreshold;

  private int animationSpeed;
  private final int lastAnimationSpeed;

  // Hierarchical tab
  private ELayoutOrientation hierarchicLayoutOrientation;
  private boolean hierarchicOrthogonalEdgeRouting;
  private int hierarchicLayoutMinLayerDistance;
  private int hierarchicLayoutMinNodeDistance;

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

    hierarchicOrthogonalEdgeRouting = initialSettings.getHierarchicalOrthogonalEdgeRouting();
    hierarchicLayoutOrientation =
        ELayoutOrientation.getEnum(initialSettings.getHierarchicalOrientation());
    hierarchicLayoutMinLayerDistance = initialSettings.getHierarchicalMinimumNodeDistance();
    hierarchicLayoutMinNodeDistance = initialSettings.getHierarchicalMinimumLayerDistance();

    orthogonalLayoutStyle = initialSettings.getOrthogonalLayoutStyle();
    orthogonalLayoutOrientation = initialSettings.getOrthogonalOrientation();
    orthogonalLayoutMinNodeDistance = initialSettings.getOrthogonalMinimumNodeDistance();

    updateLayouter();
  }

  private void setCurrentLayouter(final CanonicMultiStageLayouter layouter) {
    Preconditions.checkNotNull(layouter);

    currentLayouter = layouter;
  }

  protected void addListener(final IGraphSettingsChangedListener listener) {
    try {
      settingsListeners.addListener(listener);
    } catch (final IllegalStateException e) {
      Logger.logWarning("Listener is already listening.");
    }
  }

  protected void removeListener(final IGraphSettingsChangedListener listener) {
    try {
      settingsListeners.removeListener(listener);
    } catch (final IllegalStateException e) {
      Logger.logWarning("Listener was not listening.");
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
    return hierarchicOrthogonalEdgeRouting;
  }

  public ELayoutOrientation getHierarchicOrientation() {
    return hierarchicLayoutOrientation;
  }

  public long getMinimumCircularNodeDistance() {
    return circularLayoutMinNodeDistance;
  }

  public long getMinimumHierarchicLayerDistance() {
    return hierarchicLayoutMinLayerDistance;
  }

  public long getMinimumHierarchicNodeDistance() {
    return hierarchicLayoutMinNodeDistance;
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

  public void setCircularLayoutStyle(final ECircularLayoutStyle cirularLayout) {
    if (cirularLayout == circularLayoutStyle) {
      return;
    }

    circularLayoutStyle = cirularLayout;

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
    if (orientation == hierarchicLayoutOrientation) {
      return;
    }

    hierarchicLayoutOrientation = orientation;

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.hierarchicalLayoutOrientationChanged(this);
    }
  }

  public void setHierarchicOrthogonalEdgeRouting(final boolean orthogonal) {
    if (orthogonal == hierarchicOrthogonalEdgeRouting) {
      return;
    }

    hierarchicOrthogonalEdgeRouting = orthogonal;

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
    if (minLayerDistance == hierarchicLayoutMinLayerDistance) {
      return;
    }

    hierarchicLayoutMinLayerDistance = minLayerDistance;

    for (final IGraphSettingsChangedListener listener : settingsListeners) {
      listener.hierarchicalLayoutMinLayerDistanceChanged(this);
    }
  }

  public void setMinimumHierarchicNodeDistance(final int minNodeDistance) {
    if (minNodeDistance == hierarchicLayoutMinNodeDistance) {
      return;
    }

    hierarchicLayoutMinNodeDistance = minNodeDistance;

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
    if (getDefaultGraphLayout() == EGraphLayout.HIERARCHICAL) {
      setCurrentLayouter(LayoutCreator.getHierarchicalLayout(this));
    } else if (getDefaultGraphLayout() == EGraphLayout.ORTHOGONAL) {
      setCurrentLayouter(LayoutCreator.getOrthogonalLayout(this));
    } else {
      setCurrentLayouter(LayoutCreator.getCircularLayout(this));
    }
  }
}
