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

// Holds all elements that constitute the type system. These are:
// a) the types container which holds all instances of base types and members.
// b) the set of type instances, i.e. memory backed objects of a specific type.
// c) all segments of the executable, i.e. contiguous blocks of memory that may
// contain type instances.

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_TYPE_SYSTEM_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_TYPE_SYSTEM_H_

#include <set>
#include <vector>

#include "third_party/zynamics/binexport/types_container.h"
#include "third_party/zynamics/binexport/virtual_memory.h"

class BaseType;

// This class is thread-hostile as it uses static state.
class TypeSystem {
 public:
  TypeSystem(const TypesContainer& types_container,
             const AddressSpace& address_space)
      : types_container_(types_container), address_space_(address_space) {}
  TypeSystem(const TypeSystem&) = delete;
  TypeSystem& operator =(const TypeSystem&) = delete;

  typedef std::string (*GetNameForInstance)(Address address);
  // Creates a type substitution at the given location (if possible).
  void AddTypeSubstitution(Address address, int operand_num, int expression_id);
  // Creates a type substitution at the given location but also takes an
  // additional immediate offset that is part of the operand into account.
  void AddDisplacedTypeSubstitution(Address address, Address displacement,
                                    int operand_num, int expression_id);
  // Creates a type instance for the given address
  void CreateTypeInstance(Address from_address, int operand_num,
                          int expression_id, Address to_address,
                          GetNameForInstance get_name);
  void CreateMemoryTypeInstance(Address from_addres, int operand_num,
                                int expression_id, Address to_address,
                                GetNameForInstance get_name);

  // A type instance associates a base type with a specified address
  // (i.e. some datum in the data section).
  struct TypeInstance {
    TypeInstance(Address section_offset, const BaseType* base_type,
                 Address segment_address, const std::string& name)
        : section_offset(section_offset),
          base_type(base_type),
          segment_address(segment_address),
          database_id(NextId()),
          name(name) {}

    Address section_offset;
    const BaseType* base_type;
    int segment_address;
    // We need an explicit database a priori since the expression substitution
    // table has a foreign key to the type instances table.
    int database_id;
    std::string name;

   private:
    static int NextId() {
      static int id = 0;
      return id++;
    }
  };
  typedef std::set<TypeInstance> TypeInstances;

  // Represents an association of an operand expression with a type reference.
  struct TypeSubstitution {
    TypeSubstitution(Address address, int operand_num, int expression_id,
                     int offset, int base_type_id,
                     const BaseType::MemberIds& member_path)
        : address(address),
          operand_num(operand_num),
          expression_id(expression_id),
          offset(offset),
          base_type_id(base_type_id),
          member_path(member_path) {}

    Address address;
    int operand_num;
    int expression_id;
    int offset;
    int base_type_id;
    BaseType::MemberIds member_path;
  };
  typedef std::vector<TypeSubstitution> TypeSubstitutions;

  // Represents a data cross reference (to a type instance).
  struct DataXRef {
    DataXRef(Address address, int operand_num, int expression_id,
             const TypeInstance* type_instance)
        : address(address),
          operand_num(operand_num),
          expression_id(expression_id),
          type_instance(type_instance) {}

    Address address;
    int operand_num;
    int expression_id;
    const TypeInstance* type_instance;
  };
  typedef std::set<DataXRef> DataXRefs;

  const TypeSubstitutions& GetTypeSubstitutions() const {
    return type_substitutions_;
  }

  const TypeInstances& GetTypeInstances() const { return type_instances_; }

  const TypesContainer& GetTypes() const { return types_container_; }

  const DataXRefs& GetDataXRefs() const { return data_xrefs_; }

  const BaseType* GetStackFrame(const Function& function) const {
    return types_container_.GetStackFrame(function);
  }

  const BaseType* GetFunctionPrototype(const Function& function) const {
    return types_container_.GetFunctionPrototype(function);
  }

 private:
  void CreateInstance(const TypesContainer::TypeReference& reference,
                      Address from_address, int operand_num, int expression_id,
                      Address to_address, GetNameForInstance get_name);

  const TypesContainer& types_container_;
  const AddressSpace& address_space_;
  TypeInstances type_instances_;
  TypeSubstitutions type_substitutions_;
  DataXRefs data_xrefs_;
};

bool operator<(const TypeSystem::DataXRef& lhs,
               const TypeSystem::DataXRef& rhs);

bool operator<(const TypeSystem::TypeInstance& lhs,
               const TypeSystem::TypeInstance& rhs);

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_TYPE_SYSTEM_H_
