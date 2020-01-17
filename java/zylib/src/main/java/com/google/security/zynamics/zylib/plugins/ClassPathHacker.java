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

package com.google.security.zynamics.zylib.plugins;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class ClassPathHacker {
  public static void addFile(final File f) {
    try {
      addURL(f.toURI().toURL());
    } catch (final MalformedURLException e) {
      // Should never happen
      assert false : "Malformed URL from toURI()";
    }
  }

  public static void addFile(final String s) {
    final File f = new File(s);
    addFile(f);
  }

  public static void addURL(final URL u) {
    final URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    try {
      /* Class was unchecked, so use URLClassLoader.class instead */
      final Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
      method.setAccessible(true);
      method.invoke(sysloader, new Object[] {u});

      // TODO: Reminder for SP (Log this without using System.out.print
      // System.out.println("Dynamically added " + u.toString() + " to classLoader");
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }
}
