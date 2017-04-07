// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.ZyTree;

import java.util.List;

public interface IZyTreeNode {
  List<? extends IZyTreeNode> getChildren();
}
