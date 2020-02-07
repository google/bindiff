// Copyright 2019-2020 Google LLC
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

import com.google.protobuf.ByteString;
import com.google.security.zynamics.BinExport.BinExport2;
import com.google.security.zynamics.BinExport.BinExport2.Builder;
import ghidra.program.model.address.Address;
import ghidra.program.model.block.BasicBlockModel;
import ghidra.program.model.block.CodeBlock;
import ghidra.program.model.block.CodeBlockReference;
import ghidra.program.model.lang.Register;
import ghidra.program.model.listing.CodeUnitFormat;
import ghidra.program.model.listing.CodeUnitFormatOptions;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.Listing;
import ghidra.program.model.listing.Parameter;
import ghidra.program.model.listing.Program;
import ghidra.program.model.listing.Variable;
import ghidra.program.model.listing.VariableFilter;
import ghidra.program.model.mem.MemoryAccessException;
import ghidra.program.model.mem.MemoryBlock;
import ghidra.program.model.scalar.Scalar;
import ghidra.program.model.symbol.FlowType;
import ghidra.program.model.symbol.RefType;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.Symbol;
import ghidra.util.exception.CancelledException;
import ghidra.util.task.TaskMonitor;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;
import java.util.function.ToIntFunction;

/**
 * Java implementation of the BinExport2 writer class for Ghidra using a builder
 * pattern.
 *
 * @author Christian Blichmann
 */
public class BinExport2Builder {
  private final Builder builder = BinExport2.newBuilder();

  private TaskMonitor monitor;

  private final Program program;
  private final Listing listing;
  private final BasicBlockModel bbModel;

  private MnemonicMapper mnemonicMapper = new IdentityMnemonicMapper();
  private long addressOffset = 0;

  public BinExport2Builder(Program ghidraProgram) {
    program = ghidraProgram;
    listing = program.getListing();
    bbModel = new BasicBlockModel(program, true);
  }

  public BinExport2Builder setMnemonicMapper(MnemonicMapper mapper) {
    mnemonicMapper = mapper;
    return this;
  }

  public BinExport2Builder setAddressOffset(long offset) {
    return this;
  }

  private long getMappedAddress(Address address) {
    return address.getOffset() - addressOffset;
  }

  private long getMappedAddress(Instruction instr) {
    return getMappedAddress(instr.getAddress());
  }

  private void buildMetaInformation() {
    monitor.setIndeterminate(true);
    monitor.setMessage("Exporting meta data");

    // Ghidra uses a quad format like x86:LE:32:default, BinExport just keeps
    // the processor and address size.
    final String[] quad = program.getLanguageID().toString().split(":", 4);
    // TODO(cblichmann): Canonicalize architecture names
    final String arch = quad[0] + "-" + quad[2];

    builder.getMetaInformationBuilder()
        .setExecutableName(new File(program.getExecutablePath()).getName())
        .setExecutableId(program.getExecutableSHA256())
        .setArchitectureName(arch)
        .setTimestamp(System.currentTimeMillis() / 1000);
  }

  private void buildExpressions(Map<String, Integer> expressionIndices) {
    final var cuf = new CodeUnitFormat(new CodeUnitFormatOptions());
    int id = 0;
    for (final Instruction instr : listing.getInstructions(true)) {
      for (int i = 0; i < instr.getNumOperands(); i++) {
        final String opRep = cuf.getOperandRepresentationString(instr, i);
        if (expressionIndices.putIfAbsent(opRep, id) != null) {
          continue;
        }
        id++;
        builder.addExpressionBuilder()
            .setType(BinExport2.Expression.Type.SYMBOL).setSymbol(opRep);
      }
    }
  }
  
  private void buildOperands(Map<String, Integer> expressionIndices) {
    final var entries = new Vector<Map.Entry<String, Integer>>();
    entries.addAll(expressionIndices.entrySet());
    Collections.sort(entries, (a, b) -> a.getValue().compareTo(b.getValue()));
    for (final var entry : entries) {
      builder.addOperandBuilder().addExpressionIndex(entry.getValue());
    }
  }

  private void buildMnemonics(Map<String, Integer> mnemonicIndices) {
    monitor.setIndeterminate(true);
    monitor.setMessage("Computing mnemonic histogram");
    final var mnemonicHist = new HashMap<String, Integer>();
    for (final Instruction instr : listing.getInstructions(true)) {
      mnemonicHist.merge(mnemonicMapper.getInstructionMnemonic(instr), 1,
          Integer::sum);
    }
    final var mnemonicList = new Vector<Map.Entry<String, Integer>>();
    mnemonicList.addAll(mnemonicHist.entrySet());
    mnemonicList.sort(Comparator
        .comparingInt((ToIntFunction<Entry<String, Integer>>) Entry::getValue)
        .reversed().thenComparing(Entry::getKey));
    int id = 0;
    for (final var entry : mnemonicList) {
      builder.addMnemonicBuilder().setName(entry.getKey());
      mnemonicIndices.put(entry.getKey(), id++);
    }
  }

  private void buildInstructions(Map<String, Integer> mnemonics,
      Map<String, Integer> expressionIndices,
      Map<Long, Integer> instructionIndices) {
    monitor.setIndeterminate(false);
    monitor.setMessage("Exporting instructions");
    monitor.setMaximum(listing.getNumInstructions());
    int progress = 0;
    Instruction prevInstr = null;
    long prevAddress = 0;
    int prevSize = 0;
    int id = 0;
    final var cuf = new CodeUnitFormat(new CodeUnitFormatOptions());
    for (final Instruction instr : listing.getInstructions(true)) {
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
      int mnemonicIndex =
          mnemonics.get(mnemonicMapper.getInstructionMnemonic(instr));
      if (mnemonicIndex != 0) {
        // Only store if different from default value
        instrBuilder.setMnemonicIndex(mnemonicIndex);
      }
      instructionIndices.put(address, id++);

      // TODO(cblichmann): One expression per operand for now
      for (int i = 0; i < instr.getNumOperands(); i++) {
        var lookup =
            expressionIndices.get(cuf.getOperandRepresentationString(instr, i));
        if (lookup == null) {
          continue;
        }
        instrBuilder.addOperandIndex(lookup);
      }

      // Export call targets.
      for (final Reference ref : instr.getReferenceIteratorTo()) {
        final RefType refType = ref.getReferenceType();
        if (!refType.isCall()) {
          continue;
        }
        instrBuilder.addCallTarget(getMappedAddress(ref.getToAddress()));
      }

      prevInstr = instr;
      prevAddress = address;
      monitor.setProgress(progress++);
    }
  }

  private void buildBasicBlocks(Map<Long, Integer> instructionIndices,
      Map<Long, Integer> basicBlockIndices) throws CancelledException {
    int id = 0;
    for (final var bbIter = bbModel.getCodeBlocks(monitor); bbIter.hasNext();) {
      final CodeBlock bb = bbIter.next();

      final var protoBb = builder.addBasicBlockBuilder();

      int instructionIndex = 0;
      int beginIndex = -1;
      int endIndex = -1;
      for (final Instruction instr : listing.getInstructions(bb, true)) {
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
        // Like above, omit end index in the single instruction interval case
        indexRange.setEndIndex(endIndex);
      }
      basicBlockIndices.put(getMappedAddress(bb.getFirstStartAddress()), id++);
    }
  }

  private void buildFlowGraphs(Map<Long, Integer> basicBlockIndices)
      throws CancelledException {
    for (final Function func : program.getFunctionManager()
        .getFunctions(true)) {
      var bbIter = bbModel.getCodeBlocksContaining(func.getBody(), monitor);
      if (!bbIter.hasNext()) {
        continue; // Skip empty flow graphs, they only exist as call graph nodes
      }
      final var flowGraph = builder.addFlowGraphBuilder();
      while (bbIter.hasNext()) {
        final CodeBlock bb = bbIter.next();
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
        for (final var bbRefIter = bb.getDestinations(monitor); bbRefIter
            .hasNext();) {
          final CodeBlockReference bbRef = bbRefIter.next();
          // BinExport2 only stores flow from the very last instruction of a
          // basic block.
          if (getMappedAddress(bbRef.getReferent()) != bbLastInstrAddress) {
            continue;
          }

          final FlowType flow = bbRef.getFlowType();
          final var edge = BinExport2.FlowGraph.Edge.newBuilder();
          final var targetId = basicBlockIndices
              .get(getMappedAddress(bbRef.getDestinationAddress()));
          if (flow.isConditional() || lastFlow.isConditional()) {
            edge.setType(flow.isConditional()
                ? BinExport2.FlowGraph.Edge.Type.CONDITION_TRUE
                : BinExport2.FlowGraph.Edge.Type.CONDITION_FALSE);
            edge.setSourceBasicBlockIndex(id);
            if (targetId != null) {
              edge.setTargetBasicBlockIndex(targetId);
            }
            edges.add(edge.build());
          } else if (flow.isUnConditional() && !flow.isComputed()) {
            edge.setSourceBasicBlockIndex(id);
            if (targetId != null) {
              edge.setTargetBasicBlockIndex(targetId);
            }
            edges.add(edge.build());
          }
          // TODO(cblichmann): Handle switch tables
          lastFlow = flow;
        }
        flowGraph.addAllEdge(edges);
      }
      assert flowGraph.getEntryBasicBlockIndex() > 0;
    }
  }

  private void buildCallGraph() {
    final var callGraph = builder.getCallGraphBuilder();
    final FunctionManager funcManager = program.getFunctionManager();
    monitor.setIndeterminate(false);
    monitor.setMaximum(funcManager.getFunctionCount());
    int i = 0;
    for (final Function func : funcManager.getFunctions(true)) {
      final var vertex = callGraph.addVertexBuilder()
          .setAddress(getMappedAddress(func.getEntryPoint()));
      // TODO(cblichmann): Imported/library
      if (func.isExternal()) {
        vertex.setType(BinExport2.CallGraph.Vertex.Type.IMPORTED);
      } else if (func.isThunk()) {
        // Only store if different from default value (NORMAL)
        vertex.setType(BinExport2.CallGraph.Vertex.Type.THUNK);
      }
      // TODO(cblichmann): Check for artificial names
      // Mangled name always needs to be set.
      vertex.setMangledName(func.getName());
      monitor.setProgress(i++);
    }
  }

  private void buildSections() {
    monitor.setMessage("Exporting sections");
    monitor.setIndeterminate(false);
    final MemoryBlock[] blocks = program.getMemory().getBlocks();
    monitor.setMaximum(blocks.length);
    for (int i = 0; i < blocks.length; i++) {
      final var block = blocks[i];
      builder.addSectionBuilder().setAddress(block.getStart().getOffset())
          .setSize(block.getSize()).setFlagR(block.isRead())
          .setFlagW(block.isWrite()).setFlagX(block.isExecute());
      monitor.setProgress(i);
    }
  }

  /**
   * Parses a Ghidra instruction and outputs its components on stdout.
   * Experimental. 
   * @param instr the instruction to parse.
   */
  @SuppressWarnings("unused")
  private void parseInstruction(Instruction instr) {
    if (instr.getAddress().getOffset() != 0x1420) {
      return;
    }

    final var funMgr = program.getFunctionManager();
    final var refMgr = program.getReferenceManager();

    System.out.printf("%08X: %s (%s) ops:#%d{\n",
        instr.getAddress().getOffset(), instr.getMnemonicString(),
        instr.getLabel(), instr.getNumOperands());

    final var fun = funMgr.getFunctionContaining(instr.getAddress());
    final var params = fun.getParameters(new VariableFilter() {
      @Override
      public boolean matches(Variable variable) {
        final var p = (Parameter) variable;
        final var addr = fun.getEntryPoint().add(p.getFirstUseOffset());
        System.out.printf("P: %s, %d, %08X %s\n", p.getName(),
            p.getFirstUseOffset(), addr.getOffset(), p.getRegister());

        return instr.getAddress().equals(addr);
      }
    });
    final var cuf = new CodeUnitFormat(new CodeUnitFormatOptions());

    for (int i = 0; i < instr.getNumOperands(); i++) {
      final Symbol[] syms = instr.getSymbols();

      System.out.printf("  sym:#%d[\n", syms.length);
      for (final var sym : syms) {
        System.out.printf("    \"%s\"", sym.toString());
      }
      System.out.printf("  ]\n");

      final Reference[] refs = instr.getOperandReferences(i);
      System.out.printf("  ref:#%d[\n", refs.length);
      for (final var ref : refs) {
        final var var = refMgr.getReferencedVariable(ref);
        System.out.printf("    \"%s\" (%s) @%s\n", ref.toString(),
            (var != null ? var.getName() : ""), var.getMinAddress().toString());
      }
      System.out.printf("  ]\n");

      final Object[] objs = instr.getOpObjects(i);
      System.out.printf("  obj:#%d[\n", objs.length);
      for (final var obj : objs) {
        System.out.printf("    %s: \"%s\"", obj.getClass().getName(),
            obj.toString());
        if (obj instanceof Register) {
          final var reg = (Register) obj;
        } else if (obj instanceof Scalar) {
          final var scalar = (Scalar) obj;
        }
        System.out.printf("\n");
      }
      System.out.printf("  ]\n");
      System.out.printf(",\n");
    }
    System.out.printf("}\n");
    throw new RuntimeException();
  }

  public BinExport2 build(TaskMonitor monitor) throws CancelledException {
    this.monitor = monitor != null ? monitor : TaskMonitor.DUMMY;

    buildMetaInformation();

    // TODO(cblichmann): Implement proper expressions. For now, each expression
    // corresponds to exactly one operand. Those consist of Ghidra's string
    // representation and are of type SYMBOL.
    final var expressionIndices = new HashMap<String, Integer>();
    buildExpressions(expressionIndices);
    buildOperands(expressionIndices);
    
    final var mnemonics = new TreeMap<String, Integer>();
    buildMnemonics(mnemonics);
    final var instructionIndices = new TreeMap<Long, Integer>();
    buildInstructions(mnemonics, expressionIndices, instructionIndices);
    monitor.setMessage("Exporting basic block structure");
    final var basicBlockIndices = new HashMap<Long, Integer>();
    buildBasicBlocks(instructionIndices, basicBlockIndices);
    // TODO(cblichmann): Implement these:
    //   buildComments()
    //   buildStrings()
    //   buildDataReferences()
    monitor.setMessage("Exporting flow graphs");
    buildFlowGraphs(basicBlockIndices);
    monitor.setMessage("Exporting call graph");
    buildCallGraph();
    buildSections();

    return builder.build();
  }

  public BinExport2 build() {
    try {
      return build(TaskMonitor.DUMMY);
    } catch (final CancelledException e) {
      assert false : "TaskMonitor.DUMMY should not throw";
      throw new RuntimeException(e);
    }
  }
}
