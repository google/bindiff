// Copyright 2011-2022 Google LLC
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

#include "third_party/zynamics/binexport/library_manager.h"

#include "third_party/absl/log/check.h"

int LibraryManager::AddKnownLibrary(const std::string& library_name,
                                    Linkage linkage) {
  LibraryRecord library(library_name, linkage, library_list_.size());
  for (const auto& element : library_list_) {
    if (element.Equals(library)) {
      return element.library_index;
    }
  }
  library_list_.emplace_back(library);
  return library.library_index;
}

void LibraryManager::AddKnownFunction(const std::string& module_name,
                                      const std::string& function_name,
                                      int library_index, Address address) {
  // To keep behavior compatible with what Detego did earlier, we have to use
  // operator [] and not emplace() to add new element.
  known_functions_[address] =
      FunctionInfo(module_name, function_name, library_index);
}

Address LibraryManager::AddImportedFunction(const std::string& module_name,
                                            const std::string& function_name,
                                            int library_index) {
  Address address = --current_imported_address_;
  AddKnownFunction(module_name, function_name, library_index, address);
  if (library_index != -1) {
    UseFunction(address, library_index);
  }
  return address;
}

const LibraryManager::FunctionInfo* LibraryManager::GetKnownFunction(
    Address address) const {
  auto it = known_functions_.find(address);
  if (it != known_functions_.end()) {
    return &it->second;
  }
  return nullptr;
}

void LibraryManager::GetUsedLibraries(
    std::vector<const LibraryRecord*>* used) const {
  for (int library_index : used_libraries_) {
    used->push_back(&library_list_[library_index]);
  }
}

void LibraryManager::UpdateUsedLibraries() {
  for (const auto& used_function : used_functions_) {
    used_libraries_.insert(used_function.second);
  }
}

// Finds library index for a given address, -1 if none.
int LibraryManager::GetLibraryIndex(Address address) const {
  auto it = used_functions_.find(address);
  if (it != used_functions_.end()) {
    return it->second;
  } else {
    return -1;
  }
}

void LibraryManager::UseFunction(Address address, int library_index,
                                 UpdateKind update_kind) {
  used_functions_[address] = library_index;
  if (update_kind == UpdateKind::kUpdateUsedLibraries) {
    used_libraries_.insert(library_index);
  }
}

int LibraryManager::UseLibrary(const std::string& library, Linkage linkage) {
  int library_index = AddKnownLibrary(library, linkage);
  used_libraries_.insert(library_index);
  return library_index;
}

void LibraryManager::InitializeImportsBase(int bitness) {
  CHECK(bitness == 32 || bitness == 64);
  bitness_ = bitness;
  current_imported_address_ = bitness_ == 64 ?
      kFirstImportedAddress64 : kFirstImportedAddress32;
}

bool LibraryManager::GetReference(Address source, Address* target) const {
  auto it = references_.find(source);
  if (it == references_.end()) {
    return false;
  } else {
    *target = it->second;
    return true;
  }
}
