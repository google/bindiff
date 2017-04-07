// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.edges;

public interface IBendListener {
  void changedX(CBend bend, double x);

  void changedY(CBend bend, double y);
}
