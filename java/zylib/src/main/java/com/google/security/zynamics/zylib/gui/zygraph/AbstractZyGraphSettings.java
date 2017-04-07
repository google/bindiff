// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph;

import com.google.security.zynamics.zylib.gui.zygraph.settings.IDisplaySettings;
import com.google.security.zynamics.zylib.gui.zygraph.settings.IMouseSettings;
import com.google.security.zynamics.zylib.gui.zygraph.settings.IProximitySettings;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.settings.ILayoutSettings;

public abstract class AbstractZyGraphSettings {
  public abstract IDisplaySettings getDisplaySettings();

  public abstract ILayoutSettings getLayoutSettings();

  public abstract IMouseSettings getMouseSettings();

  public abstract IProximitySettings getProximitySettings();
}
