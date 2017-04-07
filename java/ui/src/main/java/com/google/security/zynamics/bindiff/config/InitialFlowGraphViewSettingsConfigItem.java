package com.google.security.zynamics.bindiff.config;

/** Settings class for flow graph views. */
public class InitialFlowGraphViewSettingsConfigItem extends GraphViewSettingsConfigItem {

  @Override
  protected int getProximityBrowsingChildDepthDefaultValue() {
    return 2;
  }

  @Override
  protected int getProximityBrowsingParentDepthDefaultValue() {
    return 2;
  }

  @Override
  protected int getAutoProximityBrowsingActivationThresholdDefaultValue() {
    return 200;
  }

  @Override
  protected int getVisibilityWarningThresholdDefaultValue() {
    return 300;
  }

  @Override
  protected int getHierarchicalOrientationDefaultValue() {
    return 1;
  }

  @Override
  protected String getXPath(final String relative) {
    return "/BinDiff/FlowGraph/" + relative;
  }
}
