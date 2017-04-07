// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph;

import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;

public interface ILabelEditableContentListener {
  void editableContentChanged(ZyLabelContent labelContent);

  // void editModeEnded(ZyLabelContent labelContent);
}
