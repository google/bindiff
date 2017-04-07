// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.scripting;

public class ScriptingLanguage implements Comparable<ScriptingLanguage> {
  private final String name;
  private final String version;

  public ScriptingLanguage(final String name, final String version) {
    this.name = name;
    this.version = version;
  }

  @Override
  public int compareTo(final ScriptingLanguage event) {
    return name.compareTo(event.name);
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name + " " + version;
  }
}
