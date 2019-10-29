package com.google.security.zynamics.bindiff.config;

import com.google.common.base.Ascii;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Configuration items that can load from/store to an XML document. */
public abstract class ConfigItem {
  protected static XPath xpath = XPathFactory.newInstance().newXPath();

  /**
   * Loads configuration data from the specified XML document
   *
   * @param doc the XML document to load from
   */
  public abstract void load(final Document doc) throws XPathException;

  /**
   * Saves configuration data to the specified XML document.
   *
   * @param doc the XML document to save to
   */
  public abstract void store(final Document doc) throws XPathException;

  protected static boolean getBoolean(
      final Document doc, final String expression, final boolean defaultValue)
      throws XPathExpressionException {
    return Boolean.valueOf(getString(doc, expression, String.valueOf(defaultValue)));
  }

  protected static void setBoolean(final Document doc, final String expression, final boolean value)
      throws XPathExpressionException {
    setString(doc, expression, String.valueOf(value));
  }

  protected static Color getColor(
      final Document doc, final String expression, final Color defaultValue)
      throws XPathExpressionException {
    try {
      return Color.decode(getString(doc, expression, String.valueOf(defaultValue.getRGB())));
    } catch (final NumberFormatException e) {
      return defaultValue;
    }
  }

  protected static void setColor(final Document doc, final String expression, final Color value)
      throws XPathExpressionException {
    setString(
        doc,
        expression,
        String.format("#%02x%02x%02x", value.getRed(), value.getGreen(), value.getBlue()));
  }

  protected static int getInteger(
      final Document doc, final String expression, final int defaultValue)
      throws XPathExpressionException {
    try {
      return Integer.valueOf(getString(doc, expression, String.valueOf(defaultValue)));
    } catch (final NumberFormatException e) {
      return defaultValue;
    }
  }

  protected static void setInteger(final Document doc, final String expression, final int value)
      throws XPathExpressionException {
    setString(doc, expression, String.valueOf(value));
  }

  protected static Level getLevel(
      final Document doc, final String expression, final Level defaultValue) throws XPathException {
    switch (Ascii.toLowerCase(getString(doc, expression, ""))) {
      case "debug":
        return Level.ALL;
      case "info":
        return Level.INFO;
      case "warning":
        return Level.WARNING;
      case "error":
        return Level.SEVERE;
      case "off":
        return Level.OFF;
      default:
        return defaultValue;
    }
  }

  protected static void setLevel(final Document doc, final String expression, final Level value)
      throws XPathException {
    if (Level.ALL.equals(value)) {
      setString(doc, expression, "debug");
    } else if (Level.INFO.equals(value)) {
      setString(doc, expression, "info");
    } else if (Level.WARNING.equals(value)) {
      setString(doc, expression, "warning");
    } else if (Level.SEVERE.equals(value)) {
      setString(doc, expression, "error");
    } else if (Level.OFF.equals(value)) {
      setString(doc, expression, "off");
    }
  }

  protected static long getLong(
      final Document doc, final String expression, final long defaultValue)
      throws XPathExpressionException {
    try {
      return Long.valueOf(getString(doc, expression, String.valueOf(defaultValue)));
    } catch (final NumberFormatException e) {
      return defaultValue;
    }
  }

  protected void setLong(final String expression, final Document doc, final long value)
      throws XPathExpressionException {
    setString(doc, expression, String.valueOf(value));
  }

  protected static List<String> getStrings(
      final Document doc, final String expression, final List<String> defaultValue)
      throws XPathExpressionException {
    final NodeList nodes = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);
    final List<String> result = new ArrayList<>(nodes.getLength());
    for (int i = 0; i < nodes.getLength(); i++) {
      result.add(nodes.item(i).getNodeValue());
    }
    return !result.isEmpty() ? result : defaultValue;
  }

  private static void doSetStrings(
      final Document doc, final String expression, final List<String> values, boolean singleString)
      throws XPathExpressionException {
    if (!expression.startsWith("/")) {
      throw new IllegalArgumentException("Expression must be an XPath AbsoluteLocationPath");
    }

    final List<String> components = Splitter.on('/').splitToList(expression.substring(1));
    // For setStrings() to be meaningful, the path needs to contain at least the root element and a
    // child element template.
    if (components.size() < 2) {
      throw new IllegalArgumentException("AbsoluteLocationPath too short");
    }
    String elemName = null;
    String attrName = null;
    int lastCompPos = components.size() - 1;
    final String lastComp = components.get(lastCompPos);
    if (lastComp.startsWith("@")) {
      attrName = lastComp.substring(1);
      if (!singleString) {
        lastCompPos--;
      }
    }
    // If the last is an attribute, there needs to be an additional child element, as there cannot
    // be multiple root elements in XML.
    if (lastCompPos < 0) {
      throw new IllegalArgumentException("AbsoluteLocationPath too short");
    }
    if (!singleString) {
      elemName =
          Iterables.get(Splitter.onPattern("\\[|=|\\]").split(components.get(lastCompPos)), 0);
      lastCompPos--;
    }

    final StringBuilder newExpr = new StringBuilder(components.get(0));
    for (int i = 1; i <= lastCompPos; i++) {
      newExpr.append('/').append(components.get(i));
    }

    Node lastNode = (Node) xpath.evaluate(newExpr.toString(), doc, XPathConstants.NODE);
    final StringBuilder path = new StringBuilder();
    if (lastNode == null) {
      for (int i = 0; i <= lastCompPos; i++) {
        final String component = components.get(i);
        path.append('/').append(component);
        final Node node = (Node) xpath.evaluate(path.toString(), doc, XPathConstants.NODE);
        if (node != null) {
          lastNode = node;
          continue;
        }
        if (component.startsWith("@")) {
          final Node attr = doc.createAttribute(component.substring(1));
          lastNode.getAttributes().setNamedItem(attr);
          lastNode = attr;
          break;
        }

        final List<String> pieces = Splitter.onPattern("\\[|=|\\]").splitToList(component);
        final Element elem = doc.createElement(pieces.get(0));
        lastNode = lastNode != null ? lastNode.appendChild(elem) : doc.appendChild(elem);
        if (pieces.size() == 3) {
          final String aName = pieces.get(1);
          final String aValue = pieces.get(2);
          elem.setAttribute(aName.substring(1), aValue.substring(1, aValue.length() - 1));
        }
      }
    }

    if (lastNode != null) {
      if (singleString) {
        if (!values.isEmpty()) {
          lastNode.setTextContent(values.get(0));
        }
        return;
      }
      lastNode.setTextContent(""); // Clear children
      for (final String value : values) {
        final Element elem = doc.createElement(elemName);
        final Node child;
        if (attrName == null) {
          child = elem;
        } else {
          child = doc.createAttribute(attrName);
          elem.getAttributes().setNamedItem(child);
        }
        child.setTextContent(value);
        lastNode.appendChild(elem);
      }
    }
  }

  protected static void setStrings(
      final Document doc, final String expression, final List<String> values)
      throws XPathExpressionException {
    doSetStrings(doc, expression, values, false);
  }

  protected static String getString(
      final Document doc, final String expression, final String defaultValue)
      throws XPathExpressionException {
    final String result = xpath.evaluate(expression, doc);
    return !result.isEmpty() ? result : defaultValue;
  }

  protected static void setString(final Document doc, final String expression, final String value)
      throws XPathExpressionException {
    doSetStrings(doc, expression, Arrays.asList(value), true);
  }
}
