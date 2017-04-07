// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.helpers;

import java.util.Collection;

public interface IDeletableGraph<NodeType> {
  void removeNodes(Collection<NodeType> nodes);
}
