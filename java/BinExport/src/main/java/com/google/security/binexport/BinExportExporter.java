// Copyright 2011-2019 Google LLC. All Rights Reserved.
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

package com.google.security.binexport;

import com.google.protobuf.ByteString;
import com.google.security.zynamics.BinExport.BinExport2;
import ghidra.app.util.DomainObjectService;
import ghidra.app.util.Option;
import ghidra.app.util.OptionException;
import ghidra.app.util.exporter.Exporter;
import ghidra.app.util.exporter.ExporterException;
import ghidra.framework.model.DomainObject;
import ghidra.program.model.address.AddressSetView;
import ghidra.program.model.block.BasicBlockModel;
import ghidra.program.model.block.CodeBlock;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.Listing;
import ghidra.program.model.listing.Program;
import ghidra.program.model.mem.MemoryAccessException;
import ghidra.program.model.symbol.RefType;
import ghidra.util.exception.CancelledException;
import ghidra.util.task.TaskMonitor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;
import java.util.function.ToIntFunction;

/** Exports Ghidra disassembly data into BinExport v2 format. */
public class BinExportExporter extends Exporter {

  public BinExportExporter() {
    super("Binary BinExport (v2) for BinDiff", "BinExport", null);
    log.appendMsg("BinExport 11 (c)2019 Google LLC.");
  }

  private void buildMetaInformation(BinExport2.Builder builder, Program program) {
    builder
        .getMetaInformationBuilder()
        .setExecutableName(program.getExecutablePath())
        // TODO(cblichmann): Now that we have SHA256 in Ghidra, use that.
        .setExecutableId(program.getExecutableMD5())
        .setArchitectureName(program.getLanguageID().toString())
        .setTimestamp(System.currentTimeMillis() / 1000);
  }

  private void buildMnemonics(
      BinExport2.Builder builder, Listing listing, Map<String, Integer> mnemonics) {
    final var mnemonicHist = new HashMap<String, Integer>();
    for (final var instr : listing.getInstructions(true)) {
      mnemonicHist.merge(instr.getMnemonicString(), 1, Integer::sum);
    }
    final var mnemonicList = new Vector<Map.Entry<String, Integer>>();
    mnemonicList.addAll(mnemonicHist.entrySet());
    mnemonicList.sort(
        Comparator.comparingInt((ToIntFunction<Entry<String, Integer>>) Entry::getValue)
            .reversed()
            .thenComparing(Entry::getKey));
    for (final var mnemonic : mnemonicList) {
      builder.addMnemonicBuilder().setName(mnemonic.getKey());
      mnemonics.put(mnemonic.getKey(), builder.getMnemonicBuilderList().size());
    }
  }

  private void buildInstructions(
      BinExport2.Builder builder,
      Listing listing,
      Map<String, Integer> mnemonics,
      Map<Long, Integer> instructionIndices) {
    Instruction prevInstr = null;
    long prevAddress = 0;
    int prevSize = 0;
    for (final var instr : listing.getInstructions(true)) {
      // TODO(cblichmann): Check whether getAddressableWordOffset() is the right method.
      final long address = instr.getAddress().getAddressableWordOffset();

      final var instrBuilder = builder.addInstructionBuilder();
      // Write the full instruction address iff:
      // - there is no previous instruction
      // - the previous instruction doesn't have code flow into the current one
      // - the previous instruction overlaps the current one
      // - the current instruction is a function entry point
      if (prevInstr == null
          || !prevInstr.hasFallthrough()
          || prevAddress + prevSize != address
          || listing.getFunctionAt(instr.getAddress()) != null) {
        instrBuilder.setAddress(address);
      }
      try {
        final var bytes = instr.getBytes();
        instrBuilder.setRawBytes(ByteString.copyFrom(bytes));
        prevSize = bytes.length;
      } catch (final MemoryAccessException e) {
        // Leave raw bytes empty
      }
      instrBuilder.setMnemonicIndex(mnemonics.get(instr.getMnemonicString()));
      instructionIndices.put(address, builder.getInstructionBuilderList().size());

      // TODO(cblichmann): Set operand indices

      // Export call targets.
      for (final var ref : instr.getReferenceIteratorTo()) {
        final var refType = ref.getReferenceType();
        if (refType != RefType.COMPUTED_CALL
            || refType != RefType.CONDITIONAL_COMPUTED_CALL
            || refType != RefType.UNCONDITIONAL_CALL
            || refType != RefType.CONDITIONAL_CALL) {
          continue;
        }
        instrBuilder.addCallTarget(ref.getToAddress().getAddressableWordOffset());
      }

      prevInstr = instr;
      prevAddress = address;
    }
  }

  private void buildBasicBlocks(
      BinExport2.Builder builder,
      BasicBlockModel bbModel,
      Map<Long, Integer> instructionIndices) {}

    @Override
  public boolean export(
      File file, DomainObject domainObj, AddressSetView addrSet, TaskMonitor monitor)
      throws ExporterException, IOException {

    if (!(domainObj instanceof Program)) {
      log.appendMsg("Unsupported type: " + domainObj.getClass().getName());
      return false;
    }
    final var program = (Program) domainObj;
    final var bbModel = new BasicBlockModel(program, true);

    monitor.setCancelEnabled(true);
    monitor.setIndeterminate(true);
    try {
      monitor.setMessage("Starting export");
      final var builder = BinExport2.newBuilder();
      buildMetaInformation(builder, program);

      final var listing = program.getListing();

      final var mnemonics = new TreeMap<String, Integer>(); // Mnemonic to index
      buildMnemonics(builder, listing, mnemonics);

      final var instructionIndices = new TreeMap<Long, Integer>(); // Address to index
      buildInstructions(builder, listing, mnemonics, instructionIndices);
      buildBasicBlocks(builder, bbModel, instructionIndices);

      final var funcManager = program.getFunctionManager();
      for (final var func : funcManager.getFunctions(true)) {
        log.appendMsg("BinExport: " + func.getName());

        for (final var bbIter = bbModel.getCodeBlocksContaining(func.getBody(), monitor);
            bbIter.hasNext(); ) {
          final CodeBlock bb = bbIter.next();

          log.appendMsg("BinExport: " + func.getName() + " bb start {");
          for (final var instr : listing.getInstructions(bb, true)) {
            log.appendMsg("BinExport: " + func.getName() + "  " + instr.toString());
          }
          log.appendMsg("BinExport: " + func.getName() + "          }");
        }
      }

      monitor.setMessage("Writing BinExport2 file");
      final BinExport2 proto = builder.build();
      proto.writeTo(new FileOutputStream(file));
    } catch (final CancelledException e) {
      return false;
    }
    return true;
  }

  @Override
  public List<Option> getOptions(DomainObjectService domainObjectService) {
    return EMPTY_OPTIONS;
  }

  @Override
  public void setOptions(List<Option> options) throws OptionException {
    // No options to set
  }
}
