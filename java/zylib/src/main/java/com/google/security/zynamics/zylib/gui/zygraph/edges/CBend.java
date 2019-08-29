package com.google.security.zynamics.zylib.gui.zygraph.edges;

import com.google.security.zynamics.zylib.general.ListenerProvider;
import java.util.Objects;

public class CBend {
  private double m_x;
  private double m_y;

  private final ListenerProvider<IBendListener> m_listeners = new ListenerProvider<IBendListener>();

  public CBend(final double x, final double y) {
    m_x = x;
    m_y = y;
  }

  public void addListener(final IBendListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public boolean equals(final Object object) {
    if (!(object instanceof CBend)) {
      return false;
    }

    final CBend rhs = (CBend) object;
    return (Double.compare(rhs.m_x, m_x) == 0) && (Double.compare(rhs.m_y, m_y) == 0);
  }

  @Override
  public int hashCode() {
    return Objects.hash(m_x, m_y);
  }

  public double getX() {
    return m_x;
  }

  public double getY() {
    return m_y;
  }

  public void removeListener(final IBendListener listener) {
    m_listeners.removeListener(listener);
  }

  public void setX(final double x) {
    if (Double.compare(m_x, x) == 0) {
      return;
    }

    m_x = x;

    for (final IBendListener listener : m_listeners) {
      listener.changedX(this, x);
    }
  }

  public void setY(final double y) {
    if (Double.compare(m_y, y) == 0) {
      return;
    }

    m_y = y;

    for (final IBendListener listener : m_listeners) {
      listener.changedY(this, y);
    }
  }
}
