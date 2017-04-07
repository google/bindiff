package com.google.security.zynamics.bindiff.config;

import java.awt.Color;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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

  protected static String getString(
      final Document doc, final String expression, final String defaultValue)
      throws XPathExpressionException {
    final String result = xpath.evaluate(expression, doc);
    return !result.isEmpty() ? result : defaultValue;
  }

  protected static void setString(final Document doc, final String expression, final String value)
      throws XPathExpressionException {
    if (!expression.startsWith("/")) {
      throw new IllegalArgumentException("Expression must be an XPath AbsoluteLocationPath");
    }

    final String[] components = expression.substring(1).split("/");
    final StringBuffer path = new StringBuffer();
    Node lastNode = (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);
    if (lastNode == null) {
      for (int i = 0; i < components.length; i++) {
        final String component = components[i];
        path.append('/');
        path.append(component);
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

        final String[] pieces = component.split("\\[|=|\\]");
        final Element elem = doc.createElement(pieces[0]);
        lastNode = lastNode != null ? lastNode.appendChild(elem) : doc.appendChild(elem);

        if (pieces.length == 3) {
          final String attrName = pieces[1];
          final String attrValue = pieces[2];
          elem.setAttribute(attrName.substring(1), attrValue.substring(1, attrValue.length() - 1));
        }
      }
    }
    if (lastNode != null) {
      lastNode.setTextContent(value);
    }
  }
}
