// Copyright 2011-2024 Google LLC
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

package com.google.security.zynamics.bindiff.config;

import com.google.security.zynamics.bindiff.BinDiffProtos;
import com.google.security.zynamics.bindiff.BinDiffProtos.Config.UiPreferences.GraphLayoutOptions.CircularLayoutOptions.CircularLayoutStyle;
import com.google.security.zynamics.bindiff.BinDiffProtos.Config.UiPreferences.GraphLayoutOptions.GraphLayout;
import com.google.security.zynamics.bindiff.BinDiffProtos.Config.UiPreferences.GraphLayoutOptions.HierarchicalLayoutOptions.HierarchicalLayoutStyle;
import com.google.security.zynamics.bindiff.BinDiffProtos.Config.UiPreferences.GraphLayoutOptions.LayoutOrientation;
import com.google.security.zynamics.bindiff.BinDiffProtos.Config.UiPreferences.GraphLayoutOptions.MouseWheelAction;
import com.google.security.zynamics.bindiff.BinDiffProtos.Config.UiPreferences.GraphLayoutOptions.OrthogonalLayoutOptions.OrthogonalLayoutStyle;
import com.google.security.zynamics.bindiff.enums.ECircularLayoutStyle;
import com.google.security.zynamics.bindiff.enums.EGraphLayout;
import com.google.security.zynamics.bindiff.enums.ELayoutOrientation;
import com.google.security.zynamics.bindiff.enums.EMouseAction;
import com.google.security.zynamics.bindiff.enums.EOrthogonalLayoutStyle;

/** This abstract class holds all graph related configuration settings. */
public abstract class GraphViewSettingsConfigItem {

  protected abstract BinDiffProtos.Config.UiPreferences.GraphLayoutOptions getGraphLayoutOptions();

  protected abstract BinDiffProtos.Config.UiPreferences.GraphLayoutOptions.Builder
      getGraphLayoutOptionsBuilder();

  public final EGraphLayout getDefaultGraphLayout() {
    switch (getGraphLayoutOptions().getLayout()) {
      case ORTHOGONAL:
        return EGraphLayout.ORTHOGONAL;
      case CIRCULAR:
        return EGraphLayout.CIRCULAR;
      case GRAPH_LAYOUT_UNSPECIFIED:
      case UNRECOGNIZED:
      case HIERARCHICAL:
        break;
    }
    return EGraphLayout.HIERARCHICAL;
  }

  public final boolean getAutoLayouting() {
    return getGraphLayoutOptions().getAutoLayout();
  }

  public final boolean getProximityBrowsing() {
    return getGraphLayoutOptions().getProximityBrowsing().getEnabled();
  }

  public final int getProximityBrowsingChildDepth() {
    return getGraphLayoutOptions().getProximityBrowsing().getChildDepth();
  }

  public final int getProximityBrowsingParentDepth() {
    return getGraphLayoutOptions().getProximityBrowsing().getParentDepth();
  }

  public final int getAutoProximityBrowsingActivationThreshold() {
    return getGraphLayoutOptions().getProximityBrowsing().getAutoEnableThreshold();
  }

  public final int getVisibilityWarningThreshold() {
    return getGraphLayoutOptions().getProximityBrowsing().getVisibilityWarnThreshold();
  }

  public final boolean getDrawBends() {
    return getGraphLayoutOptions().getDrawBends();
  }

  public final boolean getHierarchicalOrthogonalEdgeRouting() {
    return getGraphLayoutOptions()
        .getHierarchicalOptions()
        .getStyle()
        .equals(HierarchicalLayoutStyle.ORTHOGONAL_EDGE_ROUTING);
  }

  public final ELayoutOrientation getHierarchicalOrientation() {
    switch (getGraphLayoutOptions().getHierarchicalOptions().getOrientation()) {
      case HORIZONTAL:
        return ELayoutOrientation.HORIZONTAL;
      case LAYOUT_ORIENTATION_UNSPECIFIED:
      case UNRECOGNIZED:
      case VERTICAL:
        break;
    }
    return ELayoutOrientation.VERTICAL;
  }

  public final int getHierarchicalMinimumLayerDistance() {
    return getGraphLayoutOptions().getHierarchicalOptions().getMinLayerDistance();
  }

  public final int getHierarchicalMinimumNodeDistance() {
    return getGraphLayoutOptions().getHierarchicalOptions().getMinNodeDistance();
  }

  public final EOrthogonalLayoutStyle getOrthogonalLayoutStyle() {
    switch (getGraphLayoutOptions().getOrthogonalOptions().getStyle()) {
      case DEFAULT:
        return EOrthogonalLayoutStyle.NORMAL;
      case ORTHOGONAL_LAYOUT_STYLE_UNSPECIFIED:
      case UNRECOGNIZED:
      case TREE:
        break;
    }
    return EOrthogonalLayoutStyle.TREE;
  }

  public final ELayoutOrientation getOrthogonalOrientation() {
    switch (getGraphLayoutOptions().getOrthogonalOptions().getOrientation()) {
      case HORIZONTAL:
        return ELayoutOrientation.HORIZONTAL;
      case LAYOUT_ORIENTATION_UNSPECIFIED:
      case UNRECOGNIZED:
      case VERTICAL:
        break;
    }
    return ELayoutOrientation.VERTICAL;
  }

  public final int getOrthogonalMinimumNodeDistance() {
    return getGraphLayoutOptions().getOrthogonalOptions().getMinNodeDistance();
  }

  public final ECircularLayoutStyle getCircularLayoutStyle() {
    switch (getGraphLayoutOptions().getCircularOptions().getStyle()) {
      case ISOLATED:
        return ECircularLayoutStyle.ISOLATED;
      case SINGLE_CIRCLE:
        return ECircularLayoutStyle.SINGLE_CYCLE;
      case CIRCULAR_LAYOUT_STYLE_UNSPECIFIED:
      case UNRECOGNIZED:
      case COMPACT:
        break;
    }
    return ECircularLayoutStyle.COMPACT;
  }

  public final int getCircularMinimumNodeDistance() {
    return getGraphLayoutOptions().getCircularOptions().getMinNodeDistance();
  }

  public final int getScrollSensitivity() {
    return getGraphLayoutOptions().getScrollSensitivity();
  }

  public final int getZoomSensitivity() {
    return getGraphLayoutOptions().getZoomSensitivity();
  }

  public final EMouseAction getMouseWheelAction() {
    switch (getGraphLayoutOptions().getWheelAction()) {
      case ZOOM:
        return EMouseAction.ZOOM;
      case MOUSE_WHEEL_ACTION_UNSPECIFIED:
      case UNRECOGNIZED:
      case SCROLL:
        break;
    }
    return EMouseAction.SCROLL;
  }

  public final boolean getViewSynchronization() {
    return getGraphLayoutOptions().getViewSynchronization();
  }

  public final boolean getShowScrollbars() {
    return getGraphLayoutOptions().getShowScrollbars();
  }

  public final int getAnimationSpeed() {
    return getGraphLayoutOptions().getAnimationSpeed();
  }

  public final void setDefaultGraphLayout(final EGraphLayout defaultGraphLayout) {
    GraphLayout layout=getGraphLayout(defaultGraphLayout);
    getGraphLayoutOptionsBuilder().setLayout(layout);
  }

  private GraphLayout getGraphLayout(EGraphLayout defaultGraphLayout) {
    GraphLayout layout = GraphLayout.HIERARCHICAL;
    switch (defaultGraphLayout) {
      case ORTHOGONAL:
        layout = GraphLayout.ORTHOGONAL;
        break;
      case CIRCULAR:
        layout = GraphLayout.CIRCULAR;
        break;
      case HIERARCHICAL:
        break;
    }
    return layout;
  }

  public final void setAutoLayouting(final boolean autoLayouting) {
    getGraphLayoutOptionsBuilder().setAutoLayout(autoLayouting);
  }

  public final void setProximityBrowsing(final boolean proximityBrowsing) {
    getGraphLayoutOptionsBuilder().getProximityBrowsingBuilder().setEnabled(proximityBrowsing);
  }

  public final void setProximityBrowsingChildDepth(final int proximityBrowsingChildDepth) {
    getGraphLayoutOptionsBuilder()
        .getProximityBrowsingBuilder()
        .setChildDepth(proximityBrowsingChildDepth);
  }

  public final void setProximityBrowsingParentDepth(final int proximityBrowsingParentDepth) {
    getGraphLayoutOptionsBuilder()
        .getProximityBrowsingBuilder()
        .setParentDepth(proximityBrowsingParentDepth);
  }

  public final void setAutoProximityBrowsingActivationThreshold(
      final int autoProximityBrowsingActivationThreshold) {
    getGraphLayoutOptionsBuilder()
        .getProximityBrowsingBuilder()
        .setAutoEnableThreshold(autoProximityBrowsingActivationThreshold);
  }

  public final void setVisibilityWarningThreshold(final int visibilityWarningThreshold) {
    getGraphLayoutOptionsBuilder()
        .getProximityBrowsingBuilder()
        .setVisibilityWarnThreshold(visibilityWarningThreshold);
  }

  public final void setDrawBends(final boolean drawBends) {
    getGraphLayoutOptionsBuilder().setDrawBends(drawBends);
  }

  public final void setHierarchicalOrthogonalEdgeRouting(
      final boolean hierarchicalOrthogonalEdgeRouting) {
    getGraphLayoutOptionsBuilder()
        .getHierarchicalOptionsBuilder()
        .setStyle(
            hierarchicalOrthogonalEdgeRouting
                ? HierarchicalLayoutStyle.ORTHOGONAL_EDGE_ROUTING
                : HierarchicalLayoutStyle.DEFAULT);
  }

  public final void setHierarchicalOrientation(final ELayoutOrientation hierarchicalOrientation) {
    LayoutOrientation orientation=getLayoutOrientation(hierarchicalOrientation);
    getGraphLayoutOptionsBuilder().getHierarchicalOptionsBuilder().setOrientation(orientation);
  }

  private LayoutOrientation getLayoutOrientation(ELayoutOrientation hierarchicalOrientation) {
    LayoutOrientation orientation = LayoutOrientation.HORIZONTAL;
    switch (hierarchicalOrientation) {
      case HORIZONTAL:
        break;
      case VERTICAL:
        orientation = LayoutOrientation.VERTICAL;
        break;
    }
    return orientation;
  }

  public final void setHierarchicalMinimumLayerDistance(
      final int hierarchicalMinimumLayerDistance) {
    getGraphLayoutOptionsBuilder()
        .getHierarchicalOptionsBuilder()
        .setMinLayerDistance(hierarchicalMinimumLayerDistance);
  }

  public final void setHierarchicalMinimumNodeDistance(final int hierarchicalMinimumNodeDistance) {
    getGraphLayoutOptionsBuilder()
        .getHierarchicalOptionsBuilder()
        .setMinNodeDistance(hierarchicalMinimumNodeDistance);
  }

  public final void setOrthogonalLayoutStyle(final EOrthogonalLayoutStyle orthogonalLayoutStyle) {
    OrthogonalLayoutStyle style=getOrthogonalLayoutStyle(orthogonalLayoutStyle);
    getGraphLayoutOptionsBuilder().getOrthogonalOptionsBuilder().setStyle(style);
  }

  private OrthogonalLayoutStyle getOrthogonalLayoutStyle(EOrthogonalLayoutStyle orthogonalLayoutStyle) {
    OrthogonalLayoutStyle style = OrthogonalLayoutStyle.DEFAULT;
    switch (orthogonalLayoutStyle) {
      case NORMAL:
        break;
      case TREE:
        style = OrthogonalLayoutStyle.TREE;
        break;
    }
    return style;
  }

  public final void setOrthogonalOrientation(final ELayoutOrientation orthogonalOrientation) {
    LayoutOrientation orientation = LayoutOrientation.HORIZONTAL;
    switch (orthogonalOrientation) {
      case HORIZONTAL:
        break;
      case VERTICAL:
        orientation = LayoutOrientation.VERTICAL;
        break;
    }
    getGraphLayoutOptionsBuilder().getOrthogonalOptionsBuilder().setOrientation(orientation);
  }

  public final void setOrthogonalMinimumNodeDistance(final int orthogonalMinimumNodeDistance) {
    getGraphLayoutOptionsBuilder()
        .getOrthogonalOptionsBuilder()
        .setMinNodeDistance(orthogonalMinimumNodeDistance);
  }

  public final void setCircularLayoutStyle(final ECircularLayoutStyle circularLayoutStyle) {
      CircularLayoutStyle style=getCircularLayoutStyle(circularLayoutStyle);
      getGraphLayoutOptionsBuilder().getCircularOptionsBuilder().setStyle(style);
  }

    private CircularLayoutStyle getCircularLayoutStyle(ECircularLayoutStyle circularLayoutStyle) {
        CircularLayoutStyle style = CircularLayoutStyle.COMPACT;
        switch (circularLayoutStyle) {
          case COMPACT:
            break;
          case ISOLATED:
            style = CircularLayoutStyle.ISOLATED;
            break;
          case SINGLE_CYCLE:
            style = CircularLayoutStyle.SINGLE_CIRCLE;
            break;
        }
        return style;
    }

    public final void setCircularMinimumNodeDistance(final int circularMinimumNodeDistance) {
    getGraphLayoutOptionsBuilder()
        .getCircularOptionsBuilder()
        .setMinNodeDistance(circularMinimumNodeDistance);
  }

  public final void setScrollSensitivity(final int scrollSensitivity) {
    getGraphLayoutOptionsBuilder().setScrollSensitivity(scrollSensitivity);
  }

  public final void setZoomSensitivity(final int zoomSensitivity) {
    getGraphLayoutOptionsBuilder().setZoomSensitivity(zoomSensitivity);
  }

  public final void setMouseWheelAction(final EMouseAction mouseWheelAction) {
    MouseWheelAction action=getMouseWheelAction(mouseWheelAction);
    getGraphLayoutOptionsBuilder().setWheelAction(action);
  }

  private MouseWheelAction getMouseWheelAction(EMouseAction mouseWheelAction) {
    MouseWheelAction action = MouseWheelAction.SCROLL;
    switch (mouseWheelAction) {
      case ZOOM:
        action = MouseWheelAction.ZOOM;
        break;
      case SCROLL:
        break;
    }
    return action;
  }

  public final void setViewSynchronization(final boolean viewSynchronization) {
    getGraphLayoutOptionsBuilder().setViewSynchronization(viewSynchronization);
  }

  public final void setShowScrollbars(final boolean showScrollbars) {
    getGraphLayoutOptionsBuilder().setShowScrollbars(showScrollbars);
  }

  public final void setAnimationSpeed(final int animationSpeed) {
    getGraphLayoutOptionsBuilder().setAnimationSpeed(animationSpeed);
  }
}
