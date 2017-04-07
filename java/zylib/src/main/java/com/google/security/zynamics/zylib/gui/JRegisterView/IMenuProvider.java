// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.JRegisterView;

import javax.swing.JPopupMenu;

public interface IMenuProvider {
  JPopupMenu getRegisterMenu(int registerNumber);
}
