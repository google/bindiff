package com.google.security.zynamics.bindiff.graph.settings;

public interface IGraphSettingsChangedListener {
  void animationSpeedChanged(GraphDisplaySettings settings);

  void autoLayoutChanged(GraphLayoutSettings settings);

  void autoProximityBrowsingActivationThresholdChanged(GraphProximityBrowsingSettings settings);

  void circularLayoutMinNodeDistanceChanged(GraphLayoutSettings settings);

  void circularLayoutStyleChanged(GraphLayoutSettings settings);

  void diffViewModeChanged(GraphSettings settings);

  void drawBendsChanged(GraphSettings settings);

  void focusSideChanged(GraphSettings settings);

  void gradientBackgroundChanged(GraphDisplaySettings settings);

  void graphSyncChanged(GraphSettings settings);

  void hierarchicalLayoutMinLayerDistanceChanged(GraphLayoutSettings settings);

  void hierarchicalLayoutMinNodeDistanceChanged(GraphLayoutSettings settings);

  void hierarchicalLayoutOrientationChanged(GraphLayoutSettings settings);

  void hierarchicalOrthogonalEdgeRoutingChanged(GraphLayoutSettings settings);

  void layoutAnimationChanged(GraphLayoutSettings settings);

  void layoutChanged(GraphLayoutSettings settings);

  void mouseWheelActionChanged(GraphMouseSettings settings);

  void orthogonalLayoutMinNodeDistanceChanged(GraphLayoutSettings settings);

  void orthogonalLayoutOrientationChanged(GraphLayoutSettings settings);

  void orthogonalLayoutStyleChanged(GraphLayoutSettings settings);

  void proximityBrowsingChanged(GraphProximityBrowsingSettings settings);

  void proximityBrowsingChildDepthChanged(GraphProximityBrowsingSettings settings);

  void proximityBrowsingFrozenChanged(GraphProximityBrowsingSettings settings);

  void proximityBrowsingParentDepthChanged(GraphProximityBrowsingSettings settings);

  void scrollSensitivityChanged(GraphMouseSettings settings);

  void showScrollbarsChanged(GraphSettings settings);

  void visibilityWarningThresholdChanged(GraphLayoutSettings settings);

  void zoomSensitivityChanged(GraphMouseSettings settings);
}
