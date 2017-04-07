package com.google.security.zynamics.bindiff.config;

import javax.xml.xpath.XPathException;
import org.w3c.dom.Document;

/** This abstract class holds all graph related configuration settings. */
public abstract class GraphViewSettingsConfigItem extends ConfigItem {

  private static final String DEFAULT_GRAPH_LAYOUT = "DefaultGraphLayout/@value";
  private static final int DEFAULT_GRAPH_LAYOUT_DEFAULT = 0;
  private int defaultGraphLayout = DEFAULT_GRAPH_LAYOUT_DEFAULT;

  private static final String AUTO_LAYOUTING = "AutoLayouting/@value";
  private static final boolean AUTO_LAYOUTING_DEFAULT = true;
  private boolean autoLayouting = AUTO_LAYOUTING_DEFAULT;

  private static final String PROXIMITY_BROWSING = "ProximityBrowsing/@value";
  private static final boolean PROXIMITY_BROWSING_DEFAULT = true;
  private boolean proximityBrowsing = PROXIMITY_BROWSING_DEFAULT;

  private static final String PROXIMITY_BROWSING_CHILD_DEPTH = "ProximityBrowsingChildDepth/@value";
  private int proximityBrowsingChildDepth;

  private static final String PROXIMITY_BROWSING_PARENT_DEPTH =
      "ProximityBrowsingParentDepth/@value";
  private int proximityBrowsingParentDepth;

  private static final String AUTO_PROXIMITY_BROWSING_ACTIVATION_THRESHOLD =
      "AutoProximityBrowsingActivationThreshold/@value";
  private int autoProximityBrowsingActivationThreshold;

  private static final String VISIBILITY_WARNING_THRESHOLD = "VisibilityWarningThreshold/@value";
  private int visibilityWarningThreshold;

  private static final String DRAW_BENDS = "DrawBends/@value";
  private static final boolean DRAW_BENDS_DEFAULT = false;
  private boolean drawBends = DRAW_BENDS_DEFAULT;

  private static final String HIERARCHICAL_ORTHOGONAL_EDGEROUTING =
      "HierarchicalOrthogonalEdgeRouting/@value";
  private static final boolean HIERARCHICAL_ORTHOGONAL_EDGEROUTING_DEFAULT = false;
  private boolean hierarchicalOrthogonalEdgeRouting = HIERARCHICAL_ORTHOGONAL_EDGEROUTING_DEFAULT;

  private static final String HIERARCHICAL_ORIENTATION = "HierarchicalOrientation/@value";
  private int hierarchicalOrientation;

  private static final String HIERARCHICAL_MINIMUM_LAYER_DISTANCE =
      "HierarchicalMinimumLayerDistance/@value";
  private static final int HIERARCHICAL_MINIMUM_LAYER_DISTANCE_DEFAULT = 50;
  private int hierarchicalMinimumLayerDistance = HIERARCHICAL_MINIMUM_LAYER_DISTANCE_DEFAULT;

  private static final String HIERARCHICAL_MINIMUM_NODE_DISTANCE =
      "HierarchicalMinimumNodeDistance/@value";
  private static final int HIERARCHICAL_MINIMUM_NODE_DISTANCE_DEFAULT = 25;
  private int hierarchicalMinimumNodeDistance = HIERARCHICAL_MINIMUM_NODE_DISTANCE_DEFAULT;

  private static final String ORTHOGONAL_LAYOUT_STYLE = "OrthogonalLayoutStyle/@value";
  private static final int ORTHOGONAL_LAYOUT_STYLE_DEFAULT = 0;
  private int orthogonalLayoutStyle = ORTHOGONAL_LAYOUT_STYLE_DEFAULT;

  private static final String ORTHOGONAL_ORIENTATION = "OrthogonalOrientation/@value";
  private static final int ORTHOGONAL_ORIENTATION_DEFAULT = 0;
  private int orthogonalOrientation = ORTHOGONAL_ORIENTATION_DEFAULT;

  private static final String ORTHOGONAL_MINIMUM_NODE_DISTANCE =
      "OrthogonalMinimumNodeDistance/@value";
  private static final int ORTHOGONAL_MINIMUM_NODE_DISTANCE_DEFAULT = 50;
  private int orthogonalMinimumNodeDistance = ORTHOGONAL_MINIMUM_NODE_DISTANCE_DEFAULT;

  private static final String CIRCULAR_LAYOUT_STYLE = "CircularLayoutStyle/@value";
  private static final int CIRCULAR_LAYOUT_STYLE_DEFAULT = 1;
  private int circularLayoutStyle = CIRCULAR_LAYOUT_STYLE_DEFAULT;

  private static final String CIRCULAR_MINIMUM_NODE_DISTANCE = "CircularMinimumNodeDistance/@value";
  private static final int CIRCULAR_MINIMUM_NODE_DISTANCE_DEFAULT = 50;
  private int circularMinimumNodeDistance = CIRCULAR_MINIMUM_NODE_DISTANCE_DEFAULT;

  private static final String SCROLL_SENSITIVITY = "ScrollSensitivity/@value";
  private static final int SCROLL_SENSITIVITY_DEFAULT = 4;
  private int scrollSensitivity = SCROLL_SENSITIVITY_DEFAULT;

  private static final String ZOOM_SENSITIVITY = "ZoomSensitivity/@value";
  private static final int ZOOM_SENSITIVITY_DEFAULT = 4;
  private int zoomSensitivity = ZOOM_SENSITIVITY_DEFAULT;

  private static final String MOUSE_WHEEL_ACTION = "MouseWheelAction/@value";
  private static final int MOUSE_WHEEL_ACTION_DEFAULT = 4;
  private int mouseWheelAction = MOUSE_WHEEL_ACTION_DEFAULT;

  private static final String VIEW_SYNCHRONIZATION = "ViewSynchronization/@value";
  private static final boolean VIEW_SYNCHRONIZATION_DEFAULT = true;
  private boolean viewSynchronization = VIEW_SYNCHRONIZATION_DEFAULT;

  private static final String GRADIENT_BACKGROUND = "GradientBackground/@value";
  private static final boolean GRADIENT_BACKGROUND_DEFAULT = false;
  private boolean gradientBackground = GRADIENT_BACKGROUND_DEFAULT;

  private static final String SHOW_SCROLLBARS = "ShowScrollbars/@value";
  private static final boolean SHOW_SCROLLBARS_DEFAULT = false;
  private boolean showScrollbars = SHOW_SCROLLBARS_DEFAULT;

  private static final String LAYOUT_ANIMATION = "LayoutAnimation/@value";
  private static final boolean LAYOUT_ANIMATION_DEFAULT = true;
  private boolean layoutAnimation = LAYOUT_ANIMATION_DEFAULT;

  private static final String ANIMATION_SPEED = "AnimationSpeed/@value";
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
    defaultGraphLayout =
        getInteger(doc, getXPath(DEFAULT_GRAPH_LAYOUT), DEFAULT_GRAPH_LAYOUT_DEFAULT);
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
    hierarchicalOrthogonalEdgeRouting =
        getBoolean(
            doc,
            getXPath(HIERARCHICAL_ORTHOGONAL_EDGEROUTING),
            HIERARCHICAL_ORTHOGONAL_EDGEROUTING_DEFAULT);
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
    orthogonalLayoutStyle =
        getInteger(doc, getXPath(ORTHOGONAL_LAYOUT_STYLE), ORTHOGONAL_LAYOUT_STYLE_DEFAULT);
    orthogonalOrientation =
        getInteger(doc, getXPath(ORTHOGONAL_ORIENTATION), ORTHOGONAL_ORIENTATION_DEFAULT);
    orthogonalMinimumNodeDistance =
        getInteger(
            doc,
            getXPath(ORTHOGONAL_MINIMUM_NODE_DISTANCE),
            ORTHOGONAL_MINIMUM_NODE_DISTANCE_DEFAULT);
    circularLayoutStyle =
        getInteger(doc, getXPath(CIRCULAR_LAYOUT_STYLE), CIRCULAR_LAYOUT_STYLE_DEFAULT);
    circularMinimumNodeDistance =
        getInteger(
            doc, getXPath(CIRCULAR_MINIMUM_NODE_DISTANCE), CIRCULAR_MINIMUM_NODE_DISTANCE_DEFAULT);
    scrollSensitivity = getInteger(doc, getXPath(SCROLL_SENSITIVITY), SCROLL_SENSITIVITY_DEFAULT);
    zoomSensitivity = getInteger(doc, getXPath(ZOOM_SENSITIVITY), ZOOM_SENSITIVITY_DEFAULT);
    mouseWheelAction = getInteger(doc, getXPath(MOUSE_WHEEL_ACTION), MOUSE_WHEEL_ACTION_DEFAULT);
    viewSynchronization =
        getBoolean(doc, getXPath(VIEW_SYNCHRONIZATION), VIEW_SYNCHRONIZATION_DEFAULT);
    gradientBackground =
        getBoolean(doc, getXPath(GRADIENT_BACKGROUND), GRADIENT_BACKGROUND_DEFAULT);
    showScrollbars = getBoolean(doc, getXPath(SHOW_SCROLLBARS), SHOW_SCROLLBARS_DEFAULT);
    layoutAnimation = getBoolean(doc, getXPath(LAYOUT_ANIMATION), LAYOUT_ANIMATION_DEFAULT);
    animationSpeed = getInteger(doc, getXPath(ANIMATION_SPEED), ANIMATION_SPEED_DEFAULT);
  }

  @Override
  public void store(final Document doc) throws XPathException {
    setInteger(doc, getXPath(DEFAULT_GRAPH_LAYOUT), defaultGraphLayout);
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
    setBoolean(
        doc, getXPath(HIERARCHICAL_ORTHOGONAL_EDGEROUTING), hierarchicalOrthogonalEdgeRouting);
    setInteger(doc, getXPath(HIERARCHICAL_ORIENTATION), hierarchicalOrientation);
    setInteger(
        doc, getXPath(HIERARCHICAL_MINIMUM_LAYER_DISTANCE), hierarchicalMinimumLayerDistance);
    setInteger(doc, getXPath(HIERARCHICAL_MINIMUM_NODE_DISTANCE), hierarchicalMinimumNodeDistance);
    setInteger(doc, getXPath(ORTHOGONAL_LAYOUT_STYLE), orthogonalLayoutStyle);
    setInteger(doc, getXPath(ORTHOGONAL_ORIENTATION), orthogonalOrientation);
    setInteger(doc, getXPath(ORTHOGONAL_MINIMUM_NODE_DISTANCE), orthogonalMinimumNodeDistance);
    setInteger(doc, getXPath(CIRCULAR_LAYOUT_STYLE), circularLayoutStyle);
    setInteger(doc, getXPath(CIRCULAR_MINIMUM_NODE_DISTANCE), circularMinimumNodeDistance);
    setInteger(doc, getXPath(SCROLL_SENSITIVITY), scrollSensitivity);
    setInteger(doc, getXPath(ZOOM_SENSITIVITY), zoomSensitivity);
    setInteger(doc, getXPath(MOUSE_WHEEL_ACTION), mouseWheelAction);
    setBoolean(doc, getXPath(VIEW_SYNCHRONIZATION), viewSynchronization);
    setBoolean(doc, getXPath(GRADIENT_BACKGROUND), gradientBackground);
    setBoolean(doc, getXPath(SHOW_SCROLLBARS), showScrollbars);
    setBoolean(doc, getXPath(LAYOUT_ANIMATION), layoutAnimation);
    setInteger(doc, getXPath(ANIMATION_SPEED), animationSpeed);
  }

  public final int getDefaultGraphLayout() {
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

  public final int getOrthogonalLayoutStyle() {
    return orthogonalLayoutStyle;
  }

  public final int getOrthogonalOrientation() {
    return orthogonalOrientation;
  }

  public final int getOrthogonalMinimumNodeDistance() {
    return orthogonalMinimumNodeDistance;
  }

  public final int getCircularLayoutStyle() {
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

  public final int getMouseWheelAction() {
    return mouseWheelAction;
  }

  public final boolean getViewSynchronization() {
    return viewSynchronization;
  }

  public final boolean getGradientBackground() {
    return gradientBackground;
  }

  public final boolean getShowScrollbars() {
    return showScrollbars;
  }

  public final boolean getLayoutAnimation() {
    return layoutAnimation;
  }

  public final int getAnimationSpeed() {
    return animationSpeed;
  }

  public final void setDefaultGraphLayout(final int defaultGraphLayout) {
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

  public final void setOrthogonalLayoutStyle(final int orthogonalLayoutStyle) {
    this.orthogonalLayoutStyle = orthogonalLayoutStyle;
  }

  public final void setOrthogonalOrientation(final int orthogonalOrientation) {
    this.orthogonalOrientation = orthogonalOrientation;
  }

  public final void setOrthogonalMinimumNodeDistance(final int orthogonalMinimumNodeDistance) {
    this.orthogonalMinimumNodeDistance = orthogonalMinimumNodeDistance;
  }

  public final void setCircularLayoutStyle(final int circularLayoutStyle) {
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

  public final void setMouseWheelAction(final int mouseWheelAction) {
    this.mouseWheelAction = mouseWheelAction;
  }

  public final void setViewSynchronization(final boolean viewSynchronization) {
    this.viewSynchronization = viewSynchronization;
  }

  public final void setGradientBackground(final boolean gradientBackground) {
    this.gradientBackground = gradientBackground;
  }

  public final void setShowScrollbars(final boolean showScrollbars) {
    this.showScrollbars = showScrollbars;
  }

  public final void setLayoutAnimation(final boolean layoutAnimation) {
    this.layoutAnimation = layoutAnimation;
  }

  public final void setAnimationSpeed(final int animationSpeed) {
    this.animationSpeed = animationSpeed;
  }
}
