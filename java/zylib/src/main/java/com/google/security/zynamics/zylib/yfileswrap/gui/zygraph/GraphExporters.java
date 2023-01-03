// Copyright 2011-2023 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph;

import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import y.io.IOHandler;
import y.io.ImageIoOutputHandler;
import y.io.ImageOutputHandler;
import y.io.ViewPortConfigurator;
import y.view.Graph2D;
import y.view.Graph2DView;
import yext.svg.io.SVGIOHandler;

public class GraphExporters {
  private static void configureExportView(final Graph2DView exportView) {
    final ViewPortConfigurator vpc = new ViewPortConfigurator();

    // Register the graph to be exported with the configurator instance.
    // Depending on the other settings (see below) the graph will be used to
    // determine the image size, for example.
    vpc.setGraph2D(exportView.getGraph2D());

    // The complete graph should be exported, hence set the clipping type
    // accordingly.
    vpc.setClipType(ViewPortConfigurator.CLIP_GRAPH);
    // The graph's bounding box should determine the size of the image.
    vpc.setSizeType(ViewPortConfigurator.SIZE_USE_ORIGINAL);

    // Configure the export view using mainly default values, i.e., zoom level
    // 100%, and 15 pixel margin around the graph's bounding box.
    vpc.configure(exportView);
  }

  private static void exportGraphPartToImageFileFormat(
      final Graph2D graph, final IOHandler ioh, final String outFile) throws IOException {
    writeGraphToFile(graph, ioh, outFile);
  }

  private static void exportGraphToImageFileFormat(
      final Graph2D graph, final IOHandler ioh, final String outFile, final Graph2DView exportView)
      throws IOException {
    final Graph2DView originalView = replaceCurrentWithExportView(graph, exportView);

    configureExportView((Graph2DView) graph.getCurrentView());

    try {
      writeGraphToFile(graph, ioh, outFile);
    } finally {
      restoreOriginalView(graph, originalView);
    }
  }

  private static Graph2DView replaceCurrentWithExportView(
      final Graph2D graph, final Graph2DView exportView) {
    // Save the currently active view.
    final Graph2DView originalView = (Graph2DView) graph.getCurrentView();

    // Use the Graph2DRenderer instance of the currently active view. (Optional.)
    exportView.setGraph2DRenderer(originalView.getGraph2DRenderer());

    // Replace the currently active view containing the graph with the "export"
    // view.
    graph.setCurrentView(exportView);

    return originalView;
  }

  private static void restoreOriginalView(final Graph2D graph, final Graph2DView originalView) {
    // Remove the "export" view from the graph.
    graph.removeView(graph.getCurrentView());
    // Reset the current view to the originally active view.
    graph.setCurrentView(originalView);
  }

  private static void writeGraphToFile(
      final Graph2D graph, final IOHandler ioh, final String outFile) throws IOException {
    ioh.write(graph, outFile);
  }

  public static ImageOutputHandler createPNGOutputHandler() {
    // Use the services of Java Image I/O API to see whether there is an image
    // writer registered that is capable of writing the PNG graphics file format.
    final Iterator<ImageWriter> it = ImageIO.getImageWritersBySuffix("png");
    final ImageWriter iw = it.hasNext() ? it.next() : null;

    // Return an image output handler that serves as an adapter to this image
    // writer.
    return iw == null ? null : new ImageIoOutputHandler(iw);
  }

  /**
   * Exports a graph to a GIF file.
   *
   * @param zygraph The graph to export.
   * @param filename The name of the JPEG file.
   * @throws IOException Thrown if saving the file fails.
   */
  public static void exportAllAsGIF(final AbstractZyGraph<?, ?> zygraph, final String filename)
      throws IOException {
    final y.io.GIFIOHandler gif = new y.io.GIFIOHandler();
    gif.setAntialiasingEnabled(true);

    final Graph2DView view = zygraph.getView();
    exportGraphToImageFileFormat(
        view.getGraph2D(), gif, filename, gif.createDefaultGraph2DView(view.getGraph2D()));
  }

  /**
   * Exports a graph to a JPEG file.
   *
   * @param zygraph The graph to export.
   * @param filename The name of the JPEG file.
   * @throws IOException Thrown if saving the file fails.
   */
  public static void exportAllAsJPEG(final AbstractZyGraph<?, ?> zygraph, final String filename)
      throws IOException {
    final y.io.JPGIOHandler jpg = new y.io.JPGIOHandler();
    jpg.setAntialiasingEnabled(true);
    jpg.setQuality(0.9f);
    final Graph2DView view = zygraph.getView();
    exportGraphToImageFileFormat(
        view.getGraph2D(), jpg, filename, jpg.createDefaultGraph2DView(view.getGraph2D()));
  }

  // Warning on raw types on the following methods are suppressed. We need to use the raw type to
  // allow these methods to be called without introducing dependencies on yFiles.
  public static boolean exportAllAsPNG(final AbstractZyGraph<?, ?> zygraph, final String filename)
      throws IOException {
    final Graph2DView view = zygraph.getView();
    final ImageOutputHandler png = createPNGOutputHandler();

    exportGraphToImageFileFormat(
        view.getGraph2D(), png, filename, png.createDefaultGraph2DView(view.getGraph2D()));
    return true;
  }

  public static boolean exportAllAsSVG(final AbstractZyGraph<?, ?> zygraph, final String filename)
      throws IOException {
    final Graph2DView view = zygraph.getView();
    final SVGIOHandler svg = new SVGIOHandler();

    exportGraphToImageFileFormat(
        view.getGraph2D(), svg, filename, svg.createDefaultGraph2DView(view.getGraph2D()));
    return true;
  }

  public static void exportPartAsGIF(final AbstractZyGraph<?, ?> zygraph, final String filename)
      throws IOException {
    final Graph2DView view = zygraph.getView();
    final y.io.GIFIOHandler jpg = new y.io.GIFIOHandler();
    jpg.setAntialiasingEnabled(true);

    exportGraphPartToImageFileFormat(view.getGraph2D(), jpg, filename);
  }

  public static void exportPartAsJPEG(final AbstractZyGraph<?, ?> zygraph, final String filename)
      throws IOException {
    Graph2DView view = zygraph.getView();
    final y.io.JPGIOHandler jpg = new y.io.JPGIOHandler();
    jpg.setAntialiasingEnabled(true);
    jpg.setQuality(0.9f);
    exportGraphPartToImageFileFormat(view.getGraph2D(), jpg, filename);
  }

  public static void exportPartAsPNG(final AbstractZyGraph<?, ?> zygraph, final String filename)
      throws IOException {
    Graph2DView view = zygraph.getView();
    final ImageOutputHandler png = createPNGOutputHandler();
    png.setAntialiasingEnabled(true);

    exportGraphPartToImageFileFormat(view.getGraph2D(), png, filename);
  }

  public static void exportPartAsSVG(final AbstractZyGraph<?, ?> zygraph, final String filename)
      throws IOException {
    Graph2DView view = zygraph.getView();
    final SVGIOHandler svg = new SVGIOHandler();

    exportGraphPartToImageFileFormat(view.getGraph2D(), svg, filename);
  }
}
