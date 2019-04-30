// Copyright 2011-2019 Google LLC. All Rights Reserved.
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;
import java.util.function.ToIntFunction;
import org.python.icu.impl.duration.impl.DataRecord.EGender;
import com.google.protobuf.ByteString;
import com.google.security.zynamics.BinExport.BinExport2;
import ghidra.app.util.DomainObjectService;
import ghidra.app.util.Option;
import ghidra.app.util.OptionException;
import ghidra.app.util.exporter.Exporter;
import ghidra.app.util.exporter.ExporterException;
import ghidra.framework.model.DomainObject;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSetView;
import ghidra.program.model.block.BasicBlockModel;
import ghidra.program.model.block.CodeBlock;
import ghidra.program.model.block.CodeBlockReference;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.Program;
import ghidra.program.model.mem.MemoryAccessException;
import ghidra.program.model.symbol.FlowType;
import ghidra.program.model.symbol.RefType;
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

  private long addressOffset = 0;

  public BinExportExporter() {
    super("Binary BinExport (v2) for BinDiff", "BinExport", null);
    log.appendMsg("BinExport 11 (c)2019 Google LLC.");
  }

  private void buildMetaInformation(BinExport2.Builder builder,
      Program program) {
    // Ghidra uses a quad format like x86:LE:32:default, BinExport just keeps
    // the processor and address size.
    final String[] quad = program.getLanguageID().toString().split(":", 4);
    final String arch = quad[0] + "-" + quad[2];

    builder.getMetaInformationBuilder()
        .setExecutableName(program.getExecutablePath())
        // TODO(cblichmann): Now that we have SHA256 in Ghidra, use that.
        .setExecutableId(program.getExecutableMD5()).setArchitectureName(arch)
        .setTimestamp(System.currentTimeMillis() / 1000);
  }

  private String getInstructionMnemonic(Instruction instr) {
    // IDA uses lowercase instruction mnemonics for some architecture (notably
    // X86). If remapping is enabled, convert the instruction's mnemonic to
    // lowercase.
    // TODO(cblichmann): Implement a more sophisticated scheme that tries
    // harder to do what IDA does.
    final String mnemonic = instr.getMnemonicString();
    return remapMnemonics ? mnemonic.toLowerCase() : mnemonic;
  }

  private long getMappedAddress(Address address) {
    return address.getOffset() - addressOffset;
  }

  private long getMappedAddress(Instruction instr) {
    return getMappedAddress(instr.getAddress());
  }

  private void buildMnemonics(BinExport2.Builder builder, Program program,
      Map<String, Integer> mnemonics) {
    final var listing = program.getListing();

    final var mnemonicHist = new HashMap<String, Integer>();
    for (final var instr : listing.getInstructions(true)) {
      mnemonicHist.merge(getInstructionMnemonic(instr), 1, Integer::sum);
    }
    final var mnemonicList = new Vector<Map.Entry<String, Integer>>();
    mnemonicList.addAll(mnemonicHist.entrySet());
    mnemonicList.sort(Comparator
        .comparingInt((ToIntFunction<Entry<String, Integer>>) Entry::getValue)
        .reversed().thenComparing(Entry::getKey));
    int id = 0;
    for (final var mnemonic : mnemonicList) {
      builder.addMnemonicBuilder().setName(mnemonic.getKey());
      mnemonics.put(mnemonic.getKey(), id++);
    }
  }

  private void buildInstructions(BinExport2.Builder builder, Program program,
      Map<String, Integer> mnemonics, Map<Long, Integer> instructionIndices) {
    final var listing = program.getListing();

    Instruction prevInstr = null;
    long prevAddress = 0;
    int prevSize = 0;
    int id = 0;
    for (final var instr : listing.getInstructions(true)) {
      final long address = getMappedAddress(instr);

      final var instrBuilder = builder.addInstructionBuilder();
      // Write the full instruction address iff:
      // - there is no previous instruction
      // - the previous instruction doesn't have code flow into the current one
      // - the previous instruction overlaps the current one
      // - the current instruction is a function entry point
      if (prevInstr == null || !prevInstr.hasFallthrough()
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
      int mnemonicIndex = mnemonics.get(getInstructionMnemonic(instr));
      if (mnemonicIndex != 0) {
        // Only store if different from default value
        instrBuilder.setMnemonicIndex(mnemonicIndex);
      }
      instructionIndices.put(address, id++);

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
        instrBuilder.addCallTarget(getMappedAddress(ref.getToAddress()));
      }

      prevInstr = instr;
      prevAddress = address;
    }
  }

  private void buildBasicBlocks(BinExport2.Builder builder, Program program,
      BasicBlockModel bbModel, Map<Long, Integer> instructionIndices,
      Map<Long, Integer> basicBlockIndices) throws CancelledException {
    final var listing = program.getListing();

    int id = 0;
    for (final var bbIter = bbModel.getCodeBlocks(TaskMonitor.DUMMY); bbIter
        .hasNext();) {
      final CodeBlock bb = bbIter.next();

      final var protoBb = builder.addBasicBlockBuilder();

      int instructionIndex = 0;
      int beginIndex = -1;
      int endIndex = -1;
      for (final var instr : listing.getInstructions(bb, true)) {
        instructionIndex = instructionIndices.get(getMappedAddress(instr));
        if (beginIndex < 0) {
          beginIndex = instructionIndex;
          endIndex = beginIndex + 1;
        } else if (instructionIndex != endIndex) {
          // Sequence is broken, store an interval
          final var indexRange =
              protoBb.addInstructionIndexBuilder().setBeginIndex(beginIndex);
          if (endIndex != beginIndex + 1) {
            // Omit end index in the single instruction interval case
            indexRange.setEndIndex(endIndex);
          }
          beginIndex = instructionIndex;
          endIndex = beginIndex + 1;
        } else {
          // Sequence is unbroken, remember endIndex
          endIndex = instructionIndex + 1;
        }
      }
      final var indexRange =
          protoBb.addInstructionIndexBuilder().setBeginIndex(beginIndex);
      if (endIndex != beginIndex + 1) {
        // Like above, Omit end index in the single instruction interval case
        indexRange.setEndIndex(endIndex);
      }
      basicBlockIndices.put(getMappedAddress(bb.getFirstStartAddress()), id++);
    }
  }

  private void buildFlowGraphs(BinExport2.Builder builder, Program program,
      BasicBlockModel bbModel, Map<Long, Integer> basicBlockIndices)
      throws CancelledException {
    final var listing = program.getListing();

    for (final var func : program.getFunctionManager().getFunctions(true)) {
      var bbIter =
          bbModel.getCodeBlocksContaining(func.getBody(), TaskMonitor.DUMMY);
      if (!bbIter.hasNext()) {
        continue; // Skip empty flow graphs, they only exist as call graph nodes
      }
      final var flowGraph = builder.addFlowGraphBuilder();
      while (bbIter.hasNext()) {
        final CodeBlock bb = bbIter.next();
        System.out.printf(":: %08X:\n", bb.getFirstStartAddress().getOffset());
        final long bbAddress = getMappedAddress(bb.getFirstStartAddress());
        final int id = basicBlockIndices.get(bbAddress);
        if (bbAddress == getMappedAddress(func.getEntryPoint())) {
          flowGraph.setEntryBasicBlockIndex(id);
        }
        flowGraph.addBasicBlockIndex(id);

        final long bbLastInstrAddress =
            getMappedAddress(listing.getInstructionBefore(bb.getMaxAddress()));
        final var edges = new Vector<BinExport2.FlowGraph.Edge>();
        var lastFlow = RefType.INVALID;
        for (final var bbRefIter =
            bb.getDestinations(TaskMonitor.DUMMY); bbRefIter.hasNext();) {
          final CodeBlockReference bbRef = bbRefIter.next();
          // BinExport2 only stores flow from the very last instruction of a
          // basic block.
          if (getMappedAddress(bbRef.getReferent()) != bbLastInstrAddress) {
            continue;
          }
          // System.out.printf("=> %08X: %30s: %08X -> %08X (%08X -> %08X)\n",
          // bb.getFirstStartAddress().getOffset(),
          // bbRef.getFlowType().toString(), bbRef.getReferent().getOffset(),
          // bbRef.getReference().getOffset(),
          // bbRef.getSourceAddress().getOffset(),
          // bbRef.getDestinationAddress().getOffset());
          final var edge = BinExport2.FlowGraph.Edge.newBuilder();
          final var flowType = bbRef.getFlowType();
          if (flowType == RefType.CONDITIONAL_JUMP
              || lastFlow == RefType.CONDITIONAL_JUMP) {
            edge.setType(flowType == RefType.CONDITIONAL_JUMP
                ? BinExport2.FlowGraph.Edge.Type.CONDITION_TRUE
                : BinExport2.FlowGraph.Edge.Type.CONDITION_FALSE);
          } else if (flowType != RefType.UNCONDITIONAL_JUMP) {
            continue;
          }
          edge.setSourceBasicBlockIndex(id);
          edge.setTargetBasicBlockIndex(basicBlockIndices
              .get(getMappedAddress(bbRef.getDestinationAddress())));
          edges.add(edge.build());

          lastFlow = flowType;
        }
        flowGraph.addAllEdge(edges);
        // System.out.printf("---\n");
      }
      assert flowGraph.getEntryBasicBlockIndex() > 0;
    }
  }

  private void buildCallGraph(BinExport2.Builder builder, Program program) {
    final var callGraph = builder.getCallGraphBuilder();
    for (final var func : program.getFunctionManager().getFunctions(true)) {
      final var vertex = callGraph.addVertexBuilder()
          .setAddress(getMappedAddress(func.getEntryPoint()));
      // TODO(cblichmann): Imported/library
      if (func.isThunk()) {
        // Only store if different from default value (NORMAL)
        vertex.setType(BinExport2.CallGraph.Vertex.Type.THUNK);
      }
      // TODO(cblichmann): Check for artificial names
      // Mangled name always needs to be set.
      vertex.setMangledName(func.getName());
    }
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
    final var bbModel = new BasicBlockModel(program, true);

    if (subtractImagebase) {
      addressOffset = program.getImageBase().getOffset();
    }

    monitor.setCancelEnabled(true);
    monitor.setIndeterminate(true);
    try {
      monitor.setMessage("Starting export");
      final var builder = BinExport2.newBuilder();
      buildMetaInformation(builder, program);

      // buildExpressions()
      // buildOperands()
      monitor.setMessage("Computing mnemonic histogram");
      final var mnemonics = new TreeMap<String, Integer>(); // Mnemonic to index
      buildMnemonics(builder, program, mnemonics);
      monitor.setMessage("Exporting instructions");
      final var instructionIndices = new TreeMap<Long, Integer>(); // Address to index
      buildInstructions(builder, program, mnemonics, instructionIndices);
      monitor.setMessage("Exporting basic block structure");
      final var basicBlockIndices = new HashMap<Long, Integer>(); // Basic block address to index
      buildBasicBlocks(builder, program, bbModel, instructionIndices,
          basicBlockIndices);
      // buildComments()
      // buildStrings()
      // buildDataReferences()
      monitor.setMessage("Exporting flow graphs");
      buildFlowGraphs(builder, program, bbModel, basicBlockIndices);
      buildCallGraph(builder, program);
      // buildSections();

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
    return List.of(
        new Option(IDAPRO_COMPAT_OPTGROUP, IDAPRO_COMPAT_OPT_SUBTRACT_IMAGEBASE,
            Boolean.FALSE),
        new Option(IDAPRO_COMPAT_OPTGROUP, IDAPRO_COMPAT_OPT_REMAP_MNEMONICS,
            Boolean.FALSE));
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
