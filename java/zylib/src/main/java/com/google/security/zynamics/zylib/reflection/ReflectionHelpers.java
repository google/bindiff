// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionHelpers {
  public static Object getField(final Class<?> c, final Object object, final String field)
      throws IllegalArgumentException, IllegalAccessException, SecurityException,
      NoSuchFieldException {
    final Field chap = c.getDeclaredField(field);
    chap.setAccessible(true);

    return chap.get(object);
  }

  public static Object getField(final Object object, final String field)
      throws IllegalArgumentException, IllegalAccessException, SecurityException,
      NoSuchFieldException {
    final Class<?> c = object.getClass();

    final Field chap = c.getDeclaredField(field);
    chap.setAccessible(true);

    return chap.get(object);
  }

  public static Method getMethod(final Object object, final String methodName,
      final Class<?>... arguments) throws IllegalArgumentException, SecurityException,
      NoSuchMethodException {
    final Class<?> c = object.getClass();

    final Class<?>[] parameterTypes = new Class<?>[arguments.length];

    int counter = 0;

    for (final Class<?> argument : arguments) {
      parameterTypes[counter++] = argument;
    }

    final Method method = c.getDeclaredMethod(methodName, parameterTypes);

    return method;
  }

  public static Object getMethod(final Object object, final String methodName,
      final Object... arguments) throws IllegalArgumentException, SecurityException,
      NoSuchMethodException {
    final Class<?> c = object.getClass();

    final Class<?>[] parameterTypes = new Class<?>[arguments.length];

    int counter = 0;

    for (final Object argument : arguments) {
      parameterTypes[counter++] = argument.getClass();
    }

    final Method method = c.getDeclaredMethod(methodName, parameterTypes);

    return method;
  }

  public static Object getStaticField(final Class<?> c, final String field)
      throws IllegalArgumentException, IllegalAccessException, SecurityException,
      NoSuchFieldException {
    final Field chap = c.getDeclaredField(field);
    chap.setAccessible(true);

    return chap.get(null);
  }
}
