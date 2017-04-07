package com.google.security.zynamics.bindiff.socketserver;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.io.matches.FunctionDiffSocketXmlData;
import com.google.security.zynamics.bindiff.log.Logger;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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

public final class SocketServer {
  private final int port;
  private ServerSocket socket;

  private final WorkspaceTabPanelFunctions controller;

  public SocketServer(final int port, final WorkspaceTabPanelFunctions controller) {
    this.port = port;
    this.socket = null;
    this.controller = Preconditions.checkNotNull(controller);
  }

  public void handleReceivedByteBuffer(final byte[] bytes) {
    Logger.logInfo("Received byte stream from socket...");

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

    if (e != null) {
      Logger.logException(e, msg);
    } else {
      Logger.logSevere(msg);
    }
  }

  public void handleWarning(final Exception e, final String msg) {
    CMessageBox.showWarning(controller.getMainWindow(), msg);

    if (e != null) {
      Logger.logException(e, msg);
    } else {
      Logger.logWarning(msg);
    }
  }

  public void startListening() throws IOException {
    socket = new ServerSocket(port);
    Logger.logInfo("Starting IDA plugin socket socket listener thread...");
    new SocketListenerThread(this).start();
  }

  public ServerSocket getSocket() {
    return socket;
  }

  public int getPort() {
    return port;
  }

  /** Simple thread class that receives command data from the IDA Pro plugin. */
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
        try (final Socket clientSocket = server.getSocket().accept();
            final InputStream in = clientSocket.getInputStream()) {
          server.handleReceivedByteBuffer(receiveBoundedBytes(in));
        } catch (final IOException | SecurityException e) {
          server.handleError(e, e.getMessage());
        }
      }
    }
  }
}
