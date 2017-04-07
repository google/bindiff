// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.realizers;

public interface IZyLineEditor {
  void recreateLabelLines(ZyLabelContent labelContent, Object persistantModel);

  void refreshSize(ZyLabelContent labelContent, Object persistantModel);
}
