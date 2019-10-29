package com.google.security.zynamics.bindiff.config;

import com.google.common.base.Ascii;
import com.google.security.zynamics.bindiff.enums.ECircularLayoutStyle;
import com.google.security.zynamics.bindiff.enums.EGraphLayout;
import com.google.security.zynamics.bindiff.enums.ELayoutOrientation;
import com.google.security.zynamics.bindiff.enums.EMouseAction;
import com.google.security.zynamics.bindiff.enums.EOrthogonalLayoutStyle;
import javax.xml.xpath.XPathException;
import org.w3c.dom.Document;

/** This abstract class holds all graph related configuration settings. */
public abstract class GraphViewSettingsConfigItem extends ConfigItem {

  private static final String DEFAULT_GRAPH_LAYOUT = "layout/@default";
  private static final EGraphLayout DEFAULT_GRAPH_LAYOUT_DEFAULT = EGraphLayout.HIERARCHICAL;
  private EGraphLayout defaultGraphLayout = DEFAULT_GRAPH_LAYOUT_DEFAULT;

  private static final String AUTO_LAYOUTING = "layout/@auto";
  private static final boolean AUTO_LAYOUTING_DEFAULT = true;
  private boolean autoLayouting = AUTO_LAYOUTING_DEFAULT;

  private static final String PROXIMITY_BROWSING = "proximity-browsing/@enabled";
  private static final boolean PROXIMITY_BROWSING_DEFAULT = true;
  private boolean proximityBrowsing = PROXIMITY_BROWSING_DEFAULT;

  private static final String PROXIMITY_BROWSING_CHILD_DEPTH = "proximity-browsing/@child-depth";
  private int proximityBrowsingChildDepth;

  private static final String PROXIMITY_BROWSING_PARENT_DEPTH = "proximity-browsing/@parent-depth";
  private int proximityBrowsingParentDepth;

  private static final String AUTO_PROXIMITY_BROWSING_ACTIVATION_THRESHOLD =
      "proximity-browsing/@auto-enable-threshold";
  private int autoProximityBrowsingActivationThreshold;

  private static final String VISIBILITY_WARNING_THRESHOLD =
      "proximity-browsing/@visibility-warn-threshold";
  private int visibilityWarningThreshold;

  private static final String DRAW_BENDS = "layout/@draw-bends";
  private static final boolean DRAW_BENDS_DEFAULT = false;
  private boolean drawBends = DRAW_BENDS_DEFAULT;

  private static final String HIERARCHICAL_ORTHOGONAL_EDGEROUTING = "hierarchical-layout/@style";
  private static final boolean HIERARCHICAL_ORTHOGONAL_EDGEROUTING_DEFAULT = false;
  private boolean hierarchicalOrthogonalEdgeRouting = HIERARCHICAL_ORTHOGONAL_EDGEROUTING_DEFAULT;

  private static final String HIERARCHICAL_ORIENTATION = "hierarchical-layout/@orientation";
  private int hierarchicalOrientation;

  private static final String HIERARCHICAL_MINIMUM_LAYER_DISTANCE =
      "hierarchical-layout/@min-layer-distance";
  private static final int HIERARCHICAL_MINIMUM_LAYER_DISTANCE_DEFAULT = 50;
  private int hierarchicalMinimumLayerDistance = HIERARCHICAL_MINIMUM_LAYER_DISTANCE_DEFAULT;

  private static final String HIERARCHICAL_MINIMUM_NODE_DISTANCE =
      "hierarchical-layout/@min-node-distance";
  private static final int HIERARCHICAL_MINIMUM_NODE_DISTANCE_DEFAULT = 25;
  private int hierarchicalMinimumNodeDistance = HIERARCHICAL_MINIMUM_NODE_DISTANCE_DEFAULT;

  private static final String ORTHOGONAL_LAYOUT_STYLE = "orthogonal-layout/@style";
  private static final EOrthogonalLayoutStyle ORTHOGONAL_LAYOUT_STYLE_DEFAULT =
      EOrthogonalLayoutStyle.NORMAL;
  private EOrthogonalLayoutStyle orthogonalLayoutStyle = ORTHOGONAL_LAYOUT_STYLE_DEFAULT;

  private static final String ORTHOGONAL_ORIENTATION = "orthogonal-layout/@orientation";
  private static final ELayoutOrientation ORTHOGONAL_ORIENTATION_DEFAULT =
      ELayoutOrientation.VERTICAL;
  private ELayoutOrientation orthogonalOrientation = ORTHOGONAL_ORIENTATION_DEFAULT;

  private static final String ORTHOGONAL_MINIMUM_NODE_DISTANCE =
      "orthogonal-layout/@min-node-distance";
  private static final int ORTHOGONAL_MINIMUM_NODE_DISTANCE_DEFAULT = 50;
  private int orthogonalMinimumNodeDistance = ORTHOGONAL_MINIMUM_NODE_DISTANCE_DEFAULT;

  private static final String CIRCULAR_LAYOUT_STYLE = "circular-layout/@style";
  private static final ECircularLayoutStyle CIRCULAR_LAYOUT_STYLE_DEFAULT =
      ECircularLayoutStyle.COMPACT;
  private ECircularLayoutStyle circularLayoutStyle = CIRCULAR_LAYOUT_STYLE_DEFAULT;

  private static final String CIRCULAR_MINIMUM_NODE_DISTANCE = "circular-layout/@min-node-distance";
  private static final int CIRCULAR_MINIMUM_NODE_DISTANCE_DEFAULT = 50;
  private int circularMinimumNodeDistance = CIRCULAR_MINIMUM_NODE_DISTANCE_DEFAULT;

  private static final String SCROLL_SENSITIVITY = "control/@scroll-sensitivity";
  private static final int SCROLL_SENSITIVITY_DEFAULT = 4;
  private int scrollSensitivity = SCROLL_SENSITIVITY_DEFAULT;

  private static final String ZOOM_SENSITIVITY = "control/@zoom-sensitivity";
  private static final int ZOOM_SENSITIVITY_DEFAULT = 4;
  private int zoomSensitivity = ZOOM_SENSITIVITY_DEFAULT;

  private static final String MOUSE_WHEEL_ACTION = "control/@wheel-action";
  private static final EMouseAction MOUSE_WHEEL_ACTION_DEFAULT = EMouseAction.SCROLL;
  private EMouseAction mouseWheelAction = MOUSE_WHEEL_ACTION_DEFAULT;

  private static final String VIEW_SYNCHRONIZATION = "control/@view-synchronization";
  private static final boolean VIEW_SYNCHRONIZATION_DEFAULT = true;
  private boolean viewSynchronization = VIEW_SYNCHRONIZATION_DEFAULT;

  private static final String SHOW_SCROLLBARS = "control/@show-scrollbars";
  private static final boolean SHOW_SCROLLBARS_DEFAULT = false;
  private boolean showScrollbars = SHOW_SCROLLBARS_DEFAULT;

  private static final String ANIMATION_SPEED = "layout/@animation-speed";
  private static final int ANIMATION_SPEED_DEFAULT = 5;
  private int animationSpeed = ANIMATION_SPEED_DEFAULT;

  GraphViewSettingsConfigItem() {
    proximityBrowsingChildDepth = getProximityBrowsingChildDepthDefaultValue();
    proximityBrowsingParentDepth = getProximityBrowsingParentDepthDefaultValue();
    autoProximityBrowsingActivationThreshold =
        getAutoProximityBrowsingActivationThresholdDefaultValue();
    visibilityWarningThreshold = getVisibilityWarningThresholdDefaultValue();
    hierarchicalOrientation = getHierarchicalOrientationDefaultValue();
  }

  protected abstract String getXPath(final String relative);

  protected abstract int getProximityBrowsingChildDepthDefaultValue();

  protected abstract int getProximityBrowsingParentDepthDefaultValue();

  protected abstract int getAutoProximityBrowsingActivationThresholdDefaultValue();

  protected abstract int getVisibilityWarningThresholdDefaultValue();

  protected abstract int getHierarchicalOrientationDefaultValue();

  @Override
  public void load(final Document doc) throws XPathException {
    switch (Ascii.toLowerCase(getString(doc, getXPath(DEFAULT_GRAPH_LAYOUT), ""))) {
      case "hierarchical":
        defaultGraphLayout = EGraphLayout.HIERARCHICAL;
        break;
      case "orthogonal":
        defaultGraphLayout = EGraphLayout.ORTHOGONAL;
        break;
      case "circular":
        defaultGraphLayout = EGraphLayout.CIRCULAR;
        break;
      default: // Keep default
    }

    autoLayouting = getBoolean(doc, getXPath(AUTO_LAYOUTING), AUTO_LAYOUTING_DEFAULT);
    proximityBrowsing = getBoolean(doc, getXPath(PROXIMITY_BROWSING), PROXIMITY_BROWSING_DEFAULT);
    proximityBrowsingChildDepth =
        getInteger(
            doc,
            getXPath(PROXIMITY_BROWSING_CHILD_DEPTH),
            getProximityBrowsingChildDepthDefaultValue());
    proximityBrowsingParentDepth =
        getInteger(
            doc,
            getXPath(PROXIMITY_BROWSING_PARENT_DEPTH),
            getProximityBrowsingParentDepthDefaultValue());
    autoProximityBrowsingActivationThreshold =
        getInteger(
            doc,
            getXPath(AUTO_PROXIMITY_BROWSING_ACTIVATION_THRESHOLD),
            getAutoProximityBrowsingActivationThresholdDefaultValue());
    visibilityWarningThreshold =
        getInteger(
            doc,
            getXPath(VISIBILITY_WARNING_THRESHOLD),
            getVisibilityWarningThresholdDefaultValue());
    drawBends = getBoolean(doc, getXPath(DRAW_BENDS), DRAW_BENDS_DEFAULT);

    switch (Ascii.toLowerCase(getString(doc, getXPath(HIERARCHICAL_ORTHOGONAL_EDGEROUTING), ""))) {
      case "orthogonal-edge-routing":
        hierarchicalOrthogonalEdgeRouting = true;
        break;
      case "default": // fall through
      default: // Keep default
    }

    hierarchicalOrientation =
        getInteger(
            doc, getXPath(HIERARCHICAL_ORIENTATION), getHierarchicalOrientationDefaultValue());
    hierarchicalMinimumLayerDistance =
        getInteger(
            doc,
            getXPath(HIERARCHICAL_MINIMUM_LAYER_DISTANCE),
            HIERARCHICAL_MINIMUM_LAYER_DISTANCE_DEFAULT);
    hierarchicalMinimumNodeDistance =
        getInteger(
            doc,
            getXPath(HIERARCHICAL_MINIMUM_NODE_DISTANCE),
            HIERARCHICAL_MINIMUM_NODE_DISTANCE_DEFAULT);

    switch (Ascii.toLowerCase(getString(doc, getXPath(ORTHOGONAL_LAYOUT_STYLE), ""))) {
      case "normal":
        orthogonalLayoutStyle = EOrthogonalLayoutStyle.NORMAL;
        break;
      case "tree":
        orthogonalLayoutStyle = EOrthogonalLayoutStyle.TREE;
        break;
      default: // Keep default
    }
    switch (Ascii.toLowerCase(getString(doc, getXPath(ORTHOGONAL_ORIENTATION), ""))) {
      case "vertical":
        orthogonalOrientation = ELayoutOrientation.VERTICAL;
        break;
      case "horizontal":
        orthogonalOrientation = ELayoutOrientation.HORIZONTAL;
        break;
      default: // Keep default
    }
    orthogonalMinimumNodeDistance =
        getInteger(
            doc,
            getXPath(ORTHOGONAL_MINIMUM_NODE_DISTANCE),
            ORTHOGONAL_MINIMUM_NODE_DISTANCE_DEFAULT);

    switch (Ascii.toLowerCase(getString(doc, getXPath(CIRCULAR_LAYOUT_STYLE), ""))) {
      case "compact":
        circularLayoutStyle = ECircularLayoutStyle.COMPACT;
        break;
      case "isolated":
        circularLayoutStyle = ECircularLayoutStyle.ISOLATED;
        break;
      case "single-cycle":
        circularLayoutStyle = ECircularLayoutStyle.SINGLE_CYCLE;
        break;
      default: // Keep default
    }
    circularMinimumNodeDistance =
        getInteger(
            doc, getXPath(CIRCULAR_MINIMUM_NODE_DISTANCE), CIRCULAR_MINIMUM_NODE_DISTANCE_DEFAULT);
    scrollSensitivity = getInteger(doc, getXPath(SCROLL_SENSITIVITY), SCROLL_SENSITIVITY_DEFAULT);
    zoomSensitivity = getInteger(doc, getXPath(ZOOM_SENSITIVITY), ZOOM_SENSITIVITY_DEFAULT);

    try {
      mouseWheelAction =
          EMouseAction.valueOf(
              Ascii.toUpperCase(
                  getString(
                      doc, getXPath(MOUSE_WHEEL_ACTION), MOUSE_WHEEL_ACTION_DEFAULT.toString())));
    } catch (IllegalArgumentException e) {
      /* Keep default */
    }

    viewSynchronization =
        getBoolean(doc, getXPath(VIEW_SYNCHRONIZATION), VIEW_SYNCHRONIZATION_DEFAULT);
    showScrollbars = getBoolean(doc, getXPath(SHOW_SCROLLBARS), SHOW_SCROLLBARS_DEFAULT);
    animationSpeed = getInteger(doc, getXPath(ANIMATION_SPEED), ANIMATION_SPEED_DEFAULT);
  }

  @Override
  public void store(final Document doc) throws XPathException {
    switch (defaultGraphLayout) {
      case HIERARCHICAL:
        setString(doc, getXPath(DEFAULT_GRAPH_LAYOUT), "hierarchical");
        break;
      case ORTHOGONAL:
        setString(doc, getXPath(DEFAULT_GRAPH_LAYOUT), "orthogonal");
        break;
      case CIRCULAR:
        setString(doc, getXPath(DEFAULT_GRAPH_LAYOUT), "circular");
        break;
    }

    setBoolean(doc, getXPath(AUTO_LAYOUTING), autoLayouting);
    setBoolean(doc, getXPath(PROXIMITY_BROWSING), proximityBrowsing);
    setInteger(doc, getXPath(PROXIMITY_BROWSING_CHILD_DEPTH), proximityBrowsingChildDepth);
    setInteger(doc, getXPath(PROXIMITY_BROWSING_PARENT_DEPTH), proximityBrowsingParentDepth);
    setInteger(
        doc,
        getXPath(AUTO_PROXIMITY_BROWSING_ACTIVATION_THRESHOLD),
        autoProximityBrowsingActivationThreshold);
    setInteger(doc, getXPath(VISIBILITY_WARNING_THRESHOLD), visibilityWarningThreshold);
    setBoolean(doc, getXPath(DRAW_BENDS), drawBends);

    setString(
        doc,
        getXPath(HIERARCHICAL_ORTHOGONAL_EDGEROUTING),
        hierarchicalOrthogonalEdgeRouting ? "orthogonal-edge-routing" : "default");

    setInteger(doc, getXPath(HIERARCHICAL_ORIENTATION), hierarchicalOrientation);
    setInteger(
        doc, getXPath(HIERARCHICAL_MINIMUM_LAYER_DISTANCE), hierarchicalMinimumLayerDistance);
    setInteger(doc, getXPath(HIERARCHICAL_MINIMUM_NODE_DISTANCE), hierarchicalMinimumNodeDistance);

    switch (orthogonalLayoutStyle) {
      case NORMAL:
        setString(doc, getXPath(ORTHOGONAL_LAYOUT_STYLE), "default");
        break;
      case TREE:
        setString(doc, getXPath(ORTHOGONAL_LAYOUT_STYLE), "tree");
        break;
    }
    switch (orthogonalOrientation) {
      case VERTICAL:
        setString(doc, getXPath(ORTHOGONAL_ORIENTATION), "vertical");
        break;
      case HORIZONTAL:
        setString(doc, getXPath(ORTHOGONAL_ORIENTATION), "horizontal");
        break;
    }
    setInteger(doc, getXPath(ORTHOGONAL_MINIMUM_NODE_DISTANCE), orthogonalMinimumNodeDistance);

    switch (circularLayoutStyle) {
      case COMPACT:
        setString(doc, getXPath(CIRCULAR_LAYOUT_STYLE), "compact");
        break;
      case ISOLATED:
        setString(doc, getXPath(CIRCULAR_LAYOUT_STYLE), "isolated");
        break;
      case SINGLE_CYCLE:
        setString(doc, getXPath(CIRCULAR_LAYOUT_STYLE), "single-cycle");
        break;
    }
    setInteger(doc, getXPath(CIRCULAR_MINIMUM_NODE_DISTANCE), circularMinimumNodeDistance);
    setInteger(doc, getXPath(SCROLL_SENSITIVITY), scrollSensitivity);
    setInteger(doc, getXPath(ZOOM_SENSITIVITY), zoomSensitivity);
    setString(doc, getXPath(MOUSE_WHEEL_ACTION), Ascii.toLowerCase(mouseWheelAction.toString()));
    setBoolean(doc, getXPath(VIEW_SYNCHRONIZATION), viewSynchronization);
    setBoolean(doc, getXPath(SHOW_SCROLLBARS), showScrollbars);
    setInteger(doc, getXPath(ANIMATION_SPEED), animationSpeed);
  }

  public final EGraphLayout getDefaultGraphLayout() {
    return defaultGraphLayout;
  }

  public final boolean getAutoLayouting() {
    return autoLayouting;
  }

  public final boolean getProximityBrowsing() {
    return proximityBrowsing;
  }

  public final int getProximityBrowsingChildDepth() {
    return proximityBrowsingChildDepth;
  }

  public final int getProximityBrowsingParentDepth() {
    return proximityBrowsingParentDepth;
  }

  public final int getAutoProximityBrowsingActivationThreshold() {
    return autoProximityBrowsingActivationThreshold;
  }

  public final int getVisibilityWarningThreshold() {
    return visibilityWarningThreshold;
  }

  public final boolean getDrawBends() {
    return drawBends;
  }

  public final boolean getHierarchicalOrthogonalEdgeRouting() {
    return hierarchicalOrthogonalEdgeRouting;
  }

  public final int getHierarchicalOrientation() {
    return hierarchicalOrientation;
  }

  public final int getHierarchicalMinimumLayerDistance() {
    return hierarchicalMinimumLayerDistance;
  }

  public final int getHierarchicalMinimumNodeDistance() {
    return hierarchicalMinimumNodeDistance;
  }

  public final EOrthogonalLayoutStyle getOrthogonalLayoutStyle() {
    return orthogonalLayoutStyle;
  }

  public final ELayoutOrientation getOrthogonalOrientation() {
    return orthogonalOrientation;
  }

  public final int getOrthogonalMinimumNodeDistance() {
    return orthogonalMinimumNodeDistance;
  }

  public final ECircularLayoutStyle getCircularLayoutStyle() {
    return circularLayoutStyle;
  }

  public final int getCircularMinimumNodeDistance() {
    return circularMinimumNodeDistance;
  }

  public final int getScrollSensitivity() {
    return scrollSensitivity;
  }

  public final int getZoomSensitivity() {
    return zoomSensitivity;
  }

  public final EMouseAction getMouseWheelAction() {
    return mouseWheelAction;
  }

  public final boolean getViewSynchronization() {
    return viewSynchronization;
  }

  public final boolean getShowScrollbars() {
    return showScrollbars;
  }

  public final int getAnimationSpeed() {
    return animationSpeed;
  }

  public final void setDefaultGraphLayout(final EGraphLayout defaultGraphLayout) {
    this.defaultGraphLayout = defaultGraphLayout;
  }

  public final void setAutoLayouting(final boolean autoLayouting) {
    this.autoLayouting = autoLayouting;
  }

  public final void setProximityBrowsing(final boolean proximityBrowsing) {
    this.proximityBrowsing = proximityBrowsing;
  }

  public final void setProximityBrowsingChildDepth(final int proximityBrowsingChildDepth) {
    this.proximityBrowsingChildDepth = proximityBrowsingChildDepth;
  }

  public final void setProximityBrowsingParentDepth(final int proximityBrowsingParentDepth) {
    this.proximityBrowsingParentDepth = proximityBrowsingParentDepth;
  }

  public final void setAutoProximityBrowsingActivationThreshold(
      final int autoProximityBrowsingActivationThreshold) {
    this.autoProximityBrowsingActivationThreshold = autoProximityBrowsingActivationThreshold;
  }

  public final void setVisibilityWarningThreshold(final int visibilityWarningThreshold) {
    this.visibilityWarningThreshold = visibilityWarningThreshold;
  }

  public final void setDrawBends(final boolean drawBends) {
    this.drawBends = drawBends;
  }

  public final void setHierarchicalOrthogonalEdgeRouting(
      final boolean hierarchicalOrthogonalEdgeRouting) {
    this.hierarchicalOrthogonalEdgeRouting = hierarchicalOrthogonalEdgeRouting;
  }

  public final void setHierarchicalOrientation(final int hierarchicalOrientation) {
    this.hierarchicalOrientation = hierarchicalOrientation;
  }

  public final void setHierarchicalMinimumLayerDistance(
      final int hierarchicalMinimumLayerDistance) {
    this.hierarchicalMinimumLayerDistance = hierarchicalMinimumLayerDistance;
  }

  public final void setHierarchicalMinimumNodeDistance(final int hierarchicalMinimumNodeDistance) {
    this.hierarchicalMinimumNodeDistance = hierarchicalMinimumNodeDistance;
  }

  public final void setOrthogonalLayoutStyle(final EOrthogonalLayoutStyle orthogonalLayoutStyle) {
    this.orthogonalLayoutStyle = orthogonalLayoutStyle;
  }

  public final void setOrthogonalOrientation(final ELayoutOrientation orthogonalOrientation) {
    this.orthogonalOrientation = orthogonalOrientation;
  }

  public final void setOrthogonalMinimumNodeDistance(final int orthogonalMinimumNodeDistance) {
    this.orthogonalMinimumNodeDistance = orthogonalMinimumNodeDistance;
  }

  public final void setCircularLayoutStyle(final ECircularLayoutStyle circularLayoutStyle) {
    this.circularLayoutStyle = circularLayoutStyle;
  }

  public final void setCircularMinimumNodeDistance(final int circularMinimumNodeDistance) {
    this.circularMinimumNodeDistance = circularMinimumNodeDistance;
  }

  public final void setScrollSensitivity(final int scrollSensitivity) {
    this.scrollSensitivity = scrollSensitivity;
  }

  public final void setZoomSensitivity(final int zoomSensitivity) {
    this.zoomSensitivity = zoomSensitivity;
  }

  public final void setMouseWheelAction(final EMouseAction mouseWheelAction) {
    this.mouseWheelAction = mouseWheelAction;
  }

  public final void setViewSynchronization(final boolean viewSynchronization) {
    this.viewSynchronization = viewSynchronization;
  }

  public final void setShowScrollbars(final boolean showScrollbars) {
    this.showScrollbars = showScrollbars;
  }

  public final void setAnimationSpeed(final int animationSpeed) {
    this.animationSpeed = animationSpeed;
  }
}
