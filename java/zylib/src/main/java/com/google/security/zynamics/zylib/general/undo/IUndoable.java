// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.general.undo;

public interface IUndoable {

  String getDescription();

  String getSubCommandDescription(int i);

  int getSubCommands();

  void revertToSnapshot();

  void undo();
}
