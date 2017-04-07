// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.disassembly;


@SuppressWarnings("hiding")
public interface IModule<ViewType extends IView<?, ?>, FunctionType extends IFunction> {
  IModuleConfiguration getConfiguration();

  IModuleContent<FunctionType, ViewType> getContent();
}
