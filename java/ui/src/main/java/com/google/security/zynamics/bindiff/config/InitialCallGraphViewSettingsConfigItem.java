package com.google.security.zynamics.bindiff.config;

/** Settings class for call graph views. */
public class InitialCallGraphViewSettingsConfigItem extends GraphViewSettingsConfigItem {

  @Override
  protected int getProximityBrowsingChildDepthDefaultValue() {
    return 1;
  }

  @Override
  protected int getProximityBrowsingParentDepthDefaultValue() {
    return 1;
  }

  @Override
  protected int getAutoProximityBrowsingActivationThresholdDefaultValue() {
    return 300;
  }

  @Override
  protected int getVisibilityWarningThresholdDefaultValue() {
    return 400;
  }

  @Override
  protected int getHierarchicalOrientationDefaultValue() {
    return 0;
  }

  @Override
  protected String getXPath(final String relative) {
    return "/bindiff/preferences/call-graph/" + relative;
  }
}
