// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.JRegisterView;

import java.math.BigInteger;

public interface IRegisterModel {
  void addListener(IRegistersChangedListener registerView);

  int getNumberOfRegisters();

  RegisterInformationInternal[] getRegisterInformation();

  RegisterInformationInternal getRegisterInformation(int editedRegister);

  void setValue(String registerName, BigInteger editValue);

}
