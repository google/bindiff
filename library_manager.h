// Copyright 2011-2017 Google Inc. All Rights Reserved.
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

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_LIBRARY_MANAGER_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_LIBRARY_MANAGER_H_

#include <map>
#include <set>
#include <string>
#include <vector>

#include "third_party/zynamics/binexport/types.h"

// Theory of operations: this class keeps a list of currently known libraries,
// identified by name and linkage, along with functions and references,
// and maintains structures sufficient to maintain following API:
//  - add a library, obtain unique integer library id (if already added, reuse)
//  - remember that an address is associated with library id (used)
//  - remember that library is being used
//  - obtain vector of all libraries, for which such association exist
//  - bind address to function without explicitly using it
//  - record reference between two addresses and retrieve target by source
// Library manager differentiates known vs. used functions and libraries.
// Known library/function is just registered in the manager, but we don't claim
// they are used by currently analyzed item.
// Used library/function is explicitly referenced by an analyzed item.
class LibraryManager {
 public:
  enum class Linkage { kDynamic, kStatic };

  struct LibraryRecord {
    LibraryRecord(const std::string& name, Linkage linkage, int library_index)
        : name(name), linkage(linkage), library_index(library_index) {}

    bool IsStatic() const { return linkage == Linkage::kStatic; }

    bool Equals(const LibraryRecord& other) const {
      // We compare name and linkage to check equality, library index is not
      // relevant.
      return (name == other.name) && (linkage == other.linkage);
    }

    std::string name;
    Linkage linkage;
    int library_index;
  };

  struct FunctionInfo {
    FunctionInfo() = default;

    FunctionInfo(const std::string& module_name,
                 const std::string& function_name, int library_index)
        : module_name(module_name),
          function_name(function_name),
          library_index(library_index) {}

    // It is not the same as library, as modules may have finer granularity,
    // for example DEX uses class name as module_name, while library is a DEX
    // or JAR file containing multiple classes.
    std::string module_name;
    std::string function_name;
    int library_index = -1;
  };

  LibraryManager() = default;

  // Assigns given library name an integer index. If already present - reuses
  // an existing index. Returns library index.
  int AddKnownLibrary(const std::string& library_name, Linkage linkage);

  // Assigns function name to address.
  void AddKnownFunction(const std::string& function_name, Address address) {
    AddKnownFunction("", function_name, -1, address);
  }

  void AddKnownFunction(const std::string& module_name,
                        const std::string& function_name, int library_index,
                        Address address);

  // Adds function imported from library (-1 if unknown), address is made up.
  Address AddImportedFunction(const std::string& module_name,
                              const std::string& function_name,
                              int library_index);

  // Returns library by library index.
  const LibraryRecord& GetKnownLibrary(int library_index) const {
    return library_list_[library_index];
  }

  // Returns known function by address.
  const FunctionInfo* GetKnownFunction(Address address) const;

  // Enumerates all used libraries.
  void GetUsedLibraries(std::vector<const LibraryRecord*>* used) const;

  // Finds library index for a given address, -1 if none.
  int GetLibraryIndex(Address address) const;

  // Informs manager that 'address' is associated with the library with
  // provided library index.
  void UseFunction(Address address, int library_index);

  // Marks provided library as used.
  int UseLibrary(const std::string& library, Linkage linkage);

  // Returns true if the address was earlier marked with UseFunction().
  bool IsKnownFunction(Address address) const {
    return used_functions_.count(address) > 0;
  }

  // Returns how many library functions are discovered so far.
  int CountUsedFunctions() const { return used_functions_.size(); }

  // Returns how many functions are known.
  int CountKnownFunctions() const { return known_functions_.size(); }

  // Record reference between two addresses.
  void AddReference(Address source, Address target) {
    references_.emplace(source, target);
  }

  // Retrieve reference by source address.
  bool GetReference(Address source, Address* target) const;

  // Initialize value used to make up imported function address, according to
  // provided bitness.
  void InitializeImportsBase(int bitness);

 private:
  // First address for imported function. Subsequent ones are obtained by
  // decrementing this value. Values are selected to minimize potential for
  // conflicts with addresses of real functions.
  // Uncanonical x86-64-bit address range starting at FFFF800000000000 - 1.
  // See https://en.wikipedia.org/wiki/X86-64#Canonical_form_addresses.
  // It's impossible to encounter such an address in real x86-64.
  static const Address kFirstImportedAddress64 = 0xFFFF800000000000ULL - 1;
  // For 32-bit case it's harder to come up with unused address range, so
  // we just use end of address space.
  static const Address kFirstImportedAddress32 = 0xFFFFFFFF;

  // Used libraries.
  std::set<int> used_libraries_;

  // Maps address of known function to its integer library index.
  std::map<Address, int> used_functions_;

  // List of all known libraries, indexed by library index.
  std::vector<LibraryRecord> library_list_;

  // Map of all known functions, indexed by address. Separate from
  // used_functions_ as known and used functions are not the same.
  std::map<Address, FunctionInfo> known_functions_;

  // References between addresses.
  std::map<Address, Address> references_;

  // Current made up address of imported function.
  Address current_imported_address_ = kFirstImportedAddress32;

  // Current bitness.
  int bitness_ = 32;
};

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_LIBRARY_MANAGER_H_
