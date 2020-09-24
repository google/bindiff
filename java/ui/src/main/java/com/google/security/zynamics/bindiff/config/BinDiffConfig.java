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

package com.google.security.zynamics.bindiff.config;

import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.zylib.system.SystemHelpers;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathException;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

/** A class that is used to read and write the BinDiff configuration. */
public final class BinDiffConfig {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  /** This class is a singleton. */
  private static final BinDiffConfig instance = new BinDiffConfig();

  private final GeneralSettingsConfigItem mainSettings;
  private final DebugConfigItem debugSettings;
  private final ThemeConfigItem colorSettings;
  private final GraphViewSettingsConfigItem initialCallGraphSettings;
  private final GraphViewSettingsConfigItem initialFlowGraphSettings;

  /** Creates a new config file object. */
  private BinDiffConfig() {
    mainSettings = new GeneralSettingsConfigItem();
    debugSettings = new DebugConfigItem();
    colorSettings = new ThemeConfigItem();
    initialCallGraphSettings = new InitialCallGraphViewSettingsConfigItem();
    initialFlowGraphSettings = new InitialFlowGraphViewSettingsConfigItem();
  }

  /** Returns the only valid instance of the configuration file. */
  public static BinDiffConfig getInstance() {
    return instance;
  }

  /** Deletes the config file. */
  public static void delete() throws IOException {
    try {
      Files.delete(FileSystems.getDefault().getPath(getConfigFileName()));
    } catch (final SecurityException e) {
      throw new IOException("Couldn't delete config file: " + e.getMessage(), e);
    }
  }

  public GeneralSettingsConfigItem getMainSettings() {
    return mainSettings;
  }

  public DebugConfigItem getDebugSettings() {
    return debugSettings;
  }

  public ThemeConfigItem getThemeSettings() {
    return colorSettings;
  }

  public GraphViewSettingsConfigItem getInitialCallGraphSettings() {
    return initialCallGraphSettings;
  }

  public GraphViewSettingsConfigItem getInitialFlowGraphSettings() {
    return initialFlowGraphSettings;
  }

  /** Reads the configuration file from disk. */
  public void read() throws IOException {
    File configFile = new File(getConfigFileName()).getCanonicalFile();
    if (!configFile.exists()) {
      configFile = new File(getMachineConfigFileName());
    }

    final DocumentBuilder builder;
    try {
      builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    } catch (final ParserConfigurationException e) {
      // Should not happen as the XML support ships with the JRE.
      throw new RuntimeException(e);
    }

    final Document config;
    try {
      config = configFile.exists() ? builder.parse(configFile.getPath()) : builder.newDocument();
      mainSettings.load(config);
      initialCallGraphSettings.load(config);
      initialFlowGraphSettings.load(config);
      colorSettings.load(config);
    } catch (final SAXException | XPathException e) {
      throw new IOException("Failed to parse configuration file: " + e.getMessage(), e);
    }
  }

  /** Writes the configuration file to disk. */
  public void write() throws IOException {
    logger.at(Level.INFO).log("Saving configuration...");

    // Ensure that the configuration directory exists.
    final File configDir = new File(getConfigurationDirectory());
    if (!configDir.exists()) {
      configDir.mkdirs();
    }

    final DocumentBuilder builder;
    try {
      builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    } catch (final ParserConfigurationException e) {
      // Should not happen as the XML support ships with the JRE.
      throw new RuntimeException(e);
    }

    final File configFile = new File(getConfigFileName()).getCanonicalFile();
    final Document config;
    try {
      config = configFile.exists() ? builder.parse(configFile.getPath()) : builder.newDocument();
      // Store the settings.
      mainSettings.store(config);
      initialCallGraphSettings.store(config);
      initialFlowGraphSettings.store(config);
      colorSettings.store(config);
    } catch (final XPathException e) {
      throw new IOException("Failed to store configuration file: " + e.getMessage(), e);
    } catch (SAXException e) {
      throw new IOException("Failed to validate configuration file: " + e.getMessage(), e);
    }

    final DOMImplementationLS dom;
    try {
      dom =
          (DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS");
    } catch (final ClassNotFoundException
        | InstantiationException
        | IllegalAccessException
        | ClassCastException e) {
      // Should not happen as the XML support ships with the JRE.
      throw new RuntimeException(e);
    }
    final LSOutput output = dom.createLSOutput();
    output.setByteStream(new FileOutputStream(configFile));
    final LSSerializer writer = dom.createLSSerializer();
    writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
    writer.write(config, output);
  }

  /** Gets fully qualified path to the configuration file. */
  public static String getConfigFileName() {
    return getConfigurationDirectory() + Constants.CONFIG_FILENAME;
  }

  /** Gets the directory where the settings configuration file is stored. */
  public static String getConfigurationDirectory() {
    return SystemHelpers.getApplicationDataDirectory(Constants.PRODUCT_NAME);
  }

  /** Gets the application directory for use by all users for the machine. */
  public static final String getMachineConfigFileName() {
    return SystemHelpers.getAllUsersApplicationDataDirectory(Constants.PRODUCT_NAME)
        + Constants.CONFIG_FILENAME;
  }
}
