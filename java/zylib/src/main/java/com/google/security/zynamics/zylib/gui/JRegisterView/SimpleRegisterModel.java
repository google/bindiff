// Copyright 2011-2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.zylib.gui.JRegisterView;

import java.math.BigInteger;
import java.util.HashMap;

import com.google.common.base.Preconditions;

public class SimpleRegisterModel implements IRegisterModel {

  /**
   * Mapping between register names and register information objects.
   */
  private final HashMap<String, RegisterInformationInternal> registerMap =
      new HashMap<String, RegisterInformationInternal>();

  /**
   * Array of register information objects that describe the registers and their values.
   */
  private final RegisterInformationInternal[] registers;

  public SimpleRegisterModel(final RegisterInformation[] registers) {

    this.registers = new RegisterInformationInternal[registers.length];
    initializeRegisterInformation(registers);
  }

  /**
   * Initializes the components that keep track of register information.
   * 
   * @param passedRegisters
   */
  private void initializeRegisterInformation(final RegisterInformation[] passedRegisters) {

    for (int i = 0; i < passedRegisters.length; i++) {

      final RegisterInformation register = passedRegisters[i];

      Preconditions.checkNotNull(register.getRegisterName(),
          "Error: register.getRegisterName() argument can not be null");
      Preconditions.checkArgument(registerMap.containsKey(register.getRegisterName()),
          "Error: Duplicate register name " + register.getRegisterName());
      final RegisterInformationInternal internalRegister =
          new RegisterInformationInternal(register.getRegisterName(), register.getRegisterSize());

      this.registers[i] = internalRegister;
      this.registerMap.put(register.getRegisterName(), internalRegister);
    }
  }

  @Override
  public void addListener(final IRegistersChangedListener registerView) {
  }

  @Override
  public int getNumberOfRegisters() {
    return registers.length;
  }

  @Override
  public RegisterInformationInternal[] getRegisterInformation() {
    return registers;
  }

  @Override
  public RegisterInformationInternal getRegisterInformation(final int register) {
    return registers[register];
  }

  /**
   * Sets the value of a register.
   * 
   * @param register The name of the register.
   * @param value The value of the register.
   */
  @Override
  public void setValue(final String register, final BigInteger value) {
    Preconditions.checkNotNull(register, "Error: Argument register can't be null");
    Preconditions.checkArgument(registerMap.containsKey(register), "Error: Invalid register name");
    final RegisterInformationInternal registerInfo = registerMap.get(register);
    registerInfo.setValue(value);
    registerInfo.setModified(true);
  }
}
