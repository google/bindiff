// Copyright 2019 Google LLC. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.binexport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import com.google.security.zynamics.BinExport.BinExport2;
import ghidra.app.util.DomainObjectService;
import ghidra.app.util.Option;
import ghidra.app.util.OptionException;
import ghidra.app.util.exporter.Exporter;
import ghidra.app.util.exporter.ExporterException;
import ghidra.framework.model.DomainObject;
import ghidra.program.model.address.AddressSetView;
import ghidra.program.model.listing.Program;
import ghidra.util.exception.CancelledException;
import ghidra.util.task.TaskMonitor;

/** Exports Ghidra disassembly data into BinExport v2 format. */
public class BinExportExporter extends Exporter {

  // Option names
  private final static String IDAPRO_COMPAT_OPTGROUP = "IDA Pro Compatibility";
  private final static String IDAPRO_COMPAT_OPT_SUBTRACT_IMAGEBASE =
      "Subtract Imagebase";
  private final static String IDAPRO_COMPAT_OPT_REMAP_MNEMONICS =
      "Remap mnemonics";

  private boolean subtractImagebase = false;
  private boolean remapMnemonics = false;

  public BinExportExporter() {
    super("Binary BinExport (v2) for BinDiff", "BinExport", null);
    log.appendMsg("BinExport 11 (c)2019 Google LLC.");
  }

  @Override
  public boolean export(File file, DomainObject domainObj,
      AddressSetView addrSet, TaskMonitor monitor)
      throws ExporterException, IOException {

    if (!(domainObj instanceof Program)) {
      log.appendMsg("Unsupported type: " + domainObj.getClass().getName());
      return false;
    }
    final var program = (Program) domainObj;

//    for (var entry : program.getMetadata().entrySet()) {
//      System.out.printf("%s: %s\n", entry.getKey(), entry.getValue());
//    }
//    System.out.println("-----------------");
//    for (var as : program.getAddressFactory().getAddressSpaces()) {
//      System.out.println(as.toString());
//    }
//    System.out.println("-----------------");

    monitor.setCancelEnabled(true);
    try {
      final var builder = new BinExport2Builder(program);
      if (remapMnemonics) {
        builder
            .setMnemonicMapper(new IdaProMnemonicMapper(program.getLanguage()));
      }
      if (subtractImagebase) {
        builder.setAddressOffset(program.getImageBase().getOffset());
      }
      final BinExport2 proto = builder.build(monitor);

      monitor.setMessage("Writing BinExport2 file");
      proto.writeTo(new FileOutputStream(file));
    } catch (final CancelledException e) {
      return false;
    }
    return true;
  }

  @Override
  public List<Option> getOptions(DomainObjectService domainObjectService) {
    return List.of(
        new Option(IDAPRO_COMPAT_OPTGROUP, IDAPRO_COMPAT_OPT_SUBTRACT_IMAGEBASE,
            Boolean.FALSE),
        new Option(IDAPRO_COMPAT_OPTGROUP, IDAPRO_COMPAT_OPT_REMAP_MNEMONICS,
            Boolean.TRUE));
  }

  @Override
  public void setOptions(List<Option> options) throws OptionException {
    for (final var option : options) {
      switch (option.getName()) {
        case IDAPRO_COMPAT_OPT_SUBTRACT_IMAGEBASE:
          subtractImagebase = (boolean) option.getValue();
          break;
        case IDAPRO_COMPAT_OPT_REMAP_MNEMONICS:
          remapMnemonics = (boolean) option.getValue();
          break;
      }
    }
  }
}
