// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.disassembly;

import com.google.security.zynamics.zylib.ZyTree.IZyTreeNode;

import java.util.List;


public interface IOperandTreeNode extends IZyTreeNode {
  @Override
  List<? extends IOperandTreeNode> getChildren();

  List<IReference> getReferences();

  IReplacement getReplacement();

  ExpressionType getType();

  String getValue();
}
