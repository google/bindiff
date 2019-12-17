package com.google.security.zynamics.bindiff.socketserver;

import com.google.common.base.Preconditions;
import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.io.matches.FunctionDiffSocketXmlData;
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
import java.util.logging.Level;
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
    this.controller = Preconditions.checkNotNull(controller);
  }

  private void handleReceivedByteBuffer(final byte[] bytes) {
    logger.at(Level.INFO).log("Received byte stream from socket...");

    try {
      final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      final Document match = builder.parse(new ByteArrayInputStream(bytes));
      final XPath xpath = XPathFactory.newInstance().newXPath();
      final FunctionDiffSocketXmlData data = new FunctionDiffSocketXmlData();
      xpath.evaluate("/BinDiffMatch/@type", match);
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
      controller.openFunctionDiffView(data);
    } catch (final ParserConfigurationException e) {
      // Should not happen as the XML support ships with the JRE.
      throw new RuntimeException(e);
    } catch (final IOException e) {
      handleError(e, "Error reading from socket: " + e.getMessage());
    } catch (final SAXException | XPathException e) {
      handleError(e, "Failed to parse data from socket: " + e.getMessage());
    }
  }

  public void handleError(final Exception e, final String msg) {
    CMessageBox.showError(controller.getMainWindow(), msg);
    logger.at(Level.SEVERE).withCause(e).log(msg);
  }

  public void startListening() throws IOException {
    logger.at(Level.INFO).log("Starting local command server on port %d...", port);
    socket = new ServerSocket();
    socket.bind(new InetSocketAddress(InetAddress.getByName(null), port));
    new SocketListenerThread(this).start();
  }

  public ServerSocket getSocket() {
    return socket;
  }

  /** Simple thread class that receives command data. */
  static class SocketListenerThread extends Thread {
    private final SocketServer server;

    public SocketListenerThread(final SocketServer server) {
      this.server = Preconditions.checkNotNull(server);
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
