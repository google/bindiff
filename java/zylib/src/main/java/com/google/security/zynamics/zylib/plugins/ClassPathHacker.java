// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.plugins;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class ClassPathHacker {
  private static final Class<?>[] parameters = new Class[] {URL.class};

  /* File.toURL() was deprecated, so use File.toURI().toURL() */
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
      /* Class was uncheched, so used URLClassLoader.class instead */
      final Method method = URLClassLoader.class.getDeclaredMethod("addURL", parameters);
      method.setAccessible(true);
      method.invoke(sysloader, new Object[] {u});

      // TODO: Reminder for SP (Log this without using System.out.print
      // System.out.println("Dynamically added " + u.toString() + " to classLoader");
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }
}
