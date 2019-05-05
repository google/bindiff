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

import java.util.HashMap;
import java.util.Map;
import ghidra.program.model.lang.Language;
import ghidra.program.model.listing.Instruction;

// IDA uses lowercase instruction mnemonics for some architecture (notably X86).
public class IdaProMnemonicMapper implements MnemonicMapper {

  private enum IdaProArchitecture {
    ARM, DALVIK, METAPC, MIPS, PPC, GENERIC
  }

  private final IdaProArchitecture idaArch;
  
  private final Map<String,String> mapCache = new HashMap<>();

  public IdaProMnemonicMapper(Language language) {
    switch (language.getProcessor().toString().toLowerCase()) {
      case "x86":
        idaArch = IdaProArchitecture.METAPC;
        mapCache.put("RET", "retn");
        break;
      default:
        idaArch = IdaProArchitecture.GENERIC;
    }
  }

  @Override
  public String getInstructionMnemonic(Instruction instr) {
    // TODO(cblichmann): Implement a more sophisticated scheme that tries
    // harder to do what IDA does.
    final String mnemnonic = instr.getMnemonicString();
    if (idaArch != IdaProArchitecture.METAPC) {
      return mnemnonic;
    }
    return mapCache.computeIfAbsent(mnemnonic, String::toLowerCase);
  }
}