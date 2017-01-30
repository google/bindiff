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

#include "third_party/zynamics/binexport/type_system.h"

#include "third_party/zynamics/binexport/base_types.h"
#include "third_party/zynamics/binexport/types_container.h"

// Possibly creates a new type substitution iff the (platform specific)
// container is able to determine a type reference for the given location.
void TypeSystem::AddTypeSubstitution(Address address, /*Address immediate,*/
                                     int operand_num, int expression_id) {
  TypesContainer::TypeReference reference =
      types_container_.ResolveTypeReference(address, operand_num);
  if (reference.IsValid()) {
    type_substitutions_.emplace_back(
        address, operand_num, expression_id, reference.offset,
        reference.root_type->GetId(), reference.member_path);
  }
}

void TypeSystem::AddDisplacedTypeSubstitution(Address address,
                                              Address displacement,
                                              int operand_num,
                                              int expression_id) {
  TypesContainer::TypeReference reference =
      types_container_.ResolveDisplacedTypeReference(address, displacement,
                                                     operand_num);
  if (reference.IsValid()) {
    type_substitutions_.emplace_back(
        address, operand_num, expression_id, reference.offset,
        reference.root_type->GetId(), reference.member_path);
  }
}

// Possibly creates a new type instance as well as a data cross reference to
// that type instance iff the container is able to determine a type reference
// at from_address referencing a type instance at to_address.
void TypeSystem::CreateTypeInstance(Address from_address, int operand_num,
                                    int expression_id, Address to_address,
                                    GetNameForInstance get_name) {
  CreateInstance(
      types_container_.ResolveTypeReference(from_address, operand_num),
      from_address, operand_num, expression_id, to_address, get_name);
}

// Does the same as CreateTypeInstance but is specialized for memory operands.
void TypeSystem::CreateMemoryTypeInstance(Address from_address, int operand_num,
                                          int expression_id, Address to_address,
                                          GetNameForInstance get_name) {
  CreateInstance(types_container_.ResolveMemoryTypeReference(to_address),
                 from_address, operand_num, expression_id, to_address,
                 get_name);
}

void TypeSystem::CreateInstance(const TypesContainer::TypeReference& reference,
                                Address from_address, int operand_num,
                                int expression_id, Address to_address,
                                GetNameForInstance get_name) {
  if (reference.IsValid()) {
    // The actual memory address where the referenced type instance is
    // located in memory.
    Address start_address = to_address - reference.offset;
    if (!address_space_.IsValidAddress(start_address)) {
      return;
    }
    Address memory_block_addres =
        address_space_.GetMemoryBlock(start_address)->first;
    Address section_offset = start_address - memory_block_addres;
    auto cit = type_instances_.insert(
        TypeInstance(section_offset, reference.root_type, memory_block_addres,
                     get_name(start_address)));
    const TypeInstance* type_instance = &*cit.first;
    data_xrefs_.insert(
        DataXRef(from_address, operand_num, expression_id, type_instance));
  }
}

bool operator<(const TypeSystem::TypeInstance& lhs,
               const TypeSystem::TypeInstance& rhs) {
  if (lhs.section_offset != rhs.section_offset) {
    return lhs.section_offset < rhs.section_offset;
  }
  if (lhs.base_type != rhs.base_type) {
    return lhs.base_type < rhs.base_type;
  }
  return lhs.name.compare(rhs.name) < 0;
}

bool operator<(const TypeSystem::DataXRef& lhs,
               const TypeSystem::DataXRef& rhs) {
  if (lhs.address != rhs.address) {
    return lhs.address < rhs.address;
  }
  if (lhs.operand_num != rhs.operand_num) {
    return lhs.operand_num < rhs.operand_num;
  }
  if (lhs.expression_id != rhs.expression_id) {
    return lhs.expression_id < rhs.expression_id;
  }
  if (lhs.type_instance != rhs.type_instance) {
    return lhs.type_instance < rhs.type_instance;
  }
  return false;
}
