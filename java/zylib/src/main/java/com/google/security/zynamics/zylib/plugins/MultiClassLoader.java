// Copyright 2011-2024 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.zylib.plugins;

import java.util.Hashtable;

/**
 * A simple test class loader capable of loading from multiple sources, such as local files or a
 * URL.
 *
 * <p>This class is derived from an article by Chuck McManis
 * http://www.javaworld.com/javaworld/jw-10-1996/indepth.src.html with large modifications.
 *
 * <p>Note that this has been updated to use the non-deprecated version of defineClass() -- JDM.
 *
 * @author Jack Harich - 8/18/97
 * @author John D. Mitchell - 99.03.04
 */
@Deprecated
public abstract class MultiClassLoader extends ClassLoader {

  // ---------- Fields --------------------------------------
  private final Hashtable<String, Class<?>> classes = new Hashtable<String, Class<?>>();
  private char classNameReplacementChar;

  protected boolean monitorOn = false;
  protected boolean sourceMonitorOn = true;

  // ---------- Initialization ------------------------------
  public MultiClassLoader() {}

  // --- Std
  protected static void print(final String text) {
    System.out.println(text);
  }

  protected String formatClassName(final String className) {
    if (classNameReplacementChar == '\u0000') {
      // '/' is used to map the package to the path
      return className.replace('.', '/') + ".class";
    } else {
      // Replace '.' with custom char, such as '_'
      return className.replace('.', classNameReplacementChar) + ".class";
    }
  }

  // ---------- Protected Methods ---------------------------
  protected abstract byte[] loadClassBytes(String className);

  protected void monitor(final String text) {
    if (monitorOn) {
      print(text);
    }
  }

  // ---------- Superclass Overrides ------------------------
  /**
   * This is a simple version for external clients since they will always want the class resolved
   * before it is returned to them.
   */
  @Override
  public Class<?> loadClass(final String className) throws ClassNotFoundException {
    return loadClass(className, true);
  }

  // ---------- Abstract Implementation ---------------------
  @Override
  public synchronized Class<?> loadClass(final String className, final boolean resolveIt)
      throws ClassNotFoundException {

    Class<?> result;
    byte[] classBytes;
    monitor(">> MultiClassLoader.loadClass(" + className + ", " + resolveIt + ")");

    // ----- Check our local cache of classes
    result = classes.get(className);
    if (result != null) {
      monitor(">> returning cached result.");
      return result;
    }

    // ----- Check with the primordial class loader
    try {
      result = super.findSystemClass(className);
      monitor(">> returning system class (in CLASSPATH).");
      return result;
    } catch (final ClassNotFoundException e) {
      monitor(">> Not a system class.");
    }

    // ----- Try to load it from preferred source
    // Note loadClassBytes() is an abstract method
    classBytes = loadClassBytes(className);
    if (classBytes == null) {
      throw new ClassNotFoundException();
    }

    // ----- Define it (parse the class file)
    result = defineClass(className, classBytes, 0, classBytes.length);
    if (result == null) {
      throw new ClassFormatError();
    }

    // ----- Resolve if necessary
    if (resolveIt) {
      resolveClass(result);
    }

    // Done
    classes.put(className, result);
    monitor(">> Returning newly loaded class.");
    return result;
  }

  // ---------- Public Methods ------------------------------
  /**
   * This optional call allows a class name such as "COM.test.Hello" to be changed to
   * "COM_test_Hello", which is useful for storing classes from different packages in the same
   * retrival directory. In the above example the char would be '_'.
   */
  public void setClassNameReplacementChar(final char replacement) {
    classNameReplacementChar = replacement;
  }
} // End class
