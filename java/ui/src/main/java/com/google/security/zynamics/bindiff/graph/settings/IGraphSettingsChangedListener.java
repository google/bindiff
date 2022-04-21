// Copyright 2011-2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
