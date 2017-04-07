// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.scripting;

import java.io.Writer;

public interface IScriptConsole {
  void addListener(IScriptConsoleListener listener);

  String getOutput();

  Writer getWriter();

  void removeListener(IScriptConsoleListener listener);
}
