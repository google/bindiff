// Copyright 2011-2021 Google LLC
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

package com.google.security.zynamics.bindiff.socketserver;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.io.matches.DiffRequestMessage;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/** Socket handler that listens for commands to display flow graphs. */
public final class SocketServer {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final int port;
  private ServerSocket socket;

  private final WorkspaceTabPanelFunctions controller;

  public SocketServer(final int port, final WorkspaceTabPanelFunctions controller) {
    this.port = port;
    this.socket = null;
    this.controller = checkNotNull(controller);
  }

  private void handleReceivedByteBuffer(final byte[] bytes) {
    logger.atInfo().log("Received byte stream from socket...");

    try {
      final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      final Document match = builder.parse(new ByteArrayInputStream(bytes));
      final XPath xpath = XPathFactory.newInstance().newXPath();
      final DiffRequestMessage data = new DiffRequestMessage();
      final boolean flowGraphMatch =
          "flow_graph".equals(xpath.evaluate("/BinDiffMatch/@type", match));
      data.setMatchesDBPath(xpath.evaluate("/BinDiffMatch/Database/@path", match));
      data.setBinExportPath(xpath.evaluate("/BinDiffMatch/Primary/@path", match), ESide.PRIMARY);
      data.setFunctionAddress(
          Long.parseUnsignedLong(xpath.evaluate("/BinDiffMatch/Primary/@address", match)),
          ESide.PRIMARY);
      data.setBinExportPath(
          xpath.evaluate("/BinDiffMatch/Secondary/@path", match), ESide.SECONDARY);
      data.setFunctionAddress(
          Long.parseUnsignedLong(xpath.evaluate("/BinDiffMatch/Secondary/@address", match)),
          ESide.SECONDARY);
      if (flowGraphMatch) {
        controller.openFunctionDiffView(data);
      } else {
        controller.openCallGraphDiffView(data);
      }
    } catch (final ParserConfigurationException e) {
      // Should not happen as the XML support ships with the JRE.
      throw new RuntimeException(e);
    } catch (final IOException e) {
      handleError(e, "Error reading from socket: " + e.getMessage());
    } catch (final SAXException | XPathException e) {
      handleError(e, "Failed to parse data from socket: " + e.getMessage());
    }
  }

  private void handleError(final Exception e, final String msg) {
    CMessageBox.showError(controller.getMainWindow(), msg);
    logger.atSevere().withCause(e).log("%s", msg);
  }

  public void startListening() throws IOException {
    logger.atInfo().log("Starting local command server on port %d...", port);
    socket = new ServerSocket();
    socket.bind(new InetSocketAddress(InetAddress.getByName(null), port));
    new SocketListenerThread(this).start();
  }

  ServerSocket getSocket() {
    return socket;
  }

  /** Simple thread class that receives command data. */
  static class SocketListenerThread extends Thread {
    private final SocketServer server;

    public SocketListenerThread(final SocketServer server) {
      this.server = checkNotNull(server);
    }

    private static byte[] receiveBoundedBytes(final InputStream steam) throws IOException {
      int toRead = 4; // 32-bit integer
      byte[] sizeBytes = new byte[toRead];
      if (steam.read(sizeBytes, 0, toRead) != toRead) {
        throw new IOException("Premature end of stream");
      }
      toRead =
          Math.min(
              ByteBuffer.wrap(sizeBytes).order(ByteOrder.LITTLE_ENDIAN).getInt(),
              1 << 20 /* 1 MiB */);
      byte[] byteData = new byte[toRead];
      if (steam.read(byteData, 0, toRead) != toRead) {
        throw new IOException("Unexpected end of stream");
      }
      return byteData;
    }

    @Override
    public void run() {
      while (!interrupted()) {
        try (final Socket socket = server.getSocket().accept();
            final InputStream in = socket.getInputStream()) {
          server.handleReceivedByteBuffer(receiveBoundedBytes(in));
        } catch (final IOException | SecurityException e) {
          server.handleError(e, e.getMessage());
        }
      }
    }
  }
}
