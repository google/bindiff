// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.settings;

import y.layout.CanonicMultiStageLayouter;

public interface ILayoutSettings {
  boolean getAnimateLayout();

  int getAnimateLayoutEdgeThreshold();

  int getAnimateLayoutNodeThreshold();

  boolean getAutomaticLayouting();

  CanonicMultiStageLayouter getCurrentLayouter();
}
