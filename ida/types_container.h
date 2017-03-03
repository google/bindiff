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

// Functionality to collect types from IDA and map them to our type system.

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_IDA_TYPES_CONTAINER_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_IDA_TYPES_CONTAINER_H_

#include <iostream>
#include <map>

#include "third_party/zynamics/binexport/types_container.h"

struct MemberType;
class Function;
class member_t;

// Acts as a container for all types and members collected during the export.
// Also contains functionality to map an operand that has an associated type
// to the member it references.
class IdaTypesContainer : public TypesContainer {
 public:
  typedef std::map<std::string, const BaseType*> TypesByString;

  ~IdaTypesContainer();
  // Iterate over IDA's type system in order to create a mapping between
  // IDA types and our type system based on BaseType and MemberType.
  virtual void GatherTypes();
  virtual TypeReference ResolveTypeReference(
      Address address, size_t operand_num) const;
  virtual TypeReference ResolveMemoryTypeReference(Address immediate) const;
  virtual TypeReference ResolveDisplacedTypeReference(
      Address address, Address displacement, size_t operand_num) const;
  virtual const BaseType::BaseTypes& GetBaseTypes() const {
    return base_types_;
  }
  virtual const BaseType::MemberTypes& GetMemberTypes() const {
    return member_types_;
  }
  virtual const BaseType* GetStackFrame(const Function& function) const;
  virtual void CreateFunctionPrototype(const Function& function);
  virtual const BaseType* GetFunctionPrototype(const Function& function) const;

 private:
  typedef std::map<uint64_t, BaseType*> TypeIdMap;
  typedef std::map<Address, const BaseType*> PrototypesMap;

  template<class T> void CollectCompoundTypes(T start_it, T end_it);
  template<class T> void CollectMemberTypes(T start_it, T end_it);

  // The returned BaseType pointer is owned by the types container.
  const BaseType* CreateOrGetBaseTypes(
      const member_t* member, BaseType::MemberTypes* member_types);
  // The returned MemberType pointer is owned by the types container.
  MemberType* CreateMember(
      const BaseType* parent_struct, const member_t* member, int offset,
      const BaseType* member_base_type, BaseType::MemberTypes* member_types);
  void Cleanup();

  const BaseType* CreateVoidPointerType();
  void CreateIdaType(const std::string& name, size_t bit_size);
  void InitializeBuiltinTypes();
  const BaseType* GetVoidPointerType() const;
  const BaseType* GetBuiltinType(size_t type_size) const;
  const BaseType* GetStackFrame(Address function_address) const;
  const BaseType* GetMemberType(const member_t* member) const;
  TypeReference CreateStackReference(Address address, size_t operand_num,
                                     int64_t displacement) const;
  TypeReference CreateNonStackReference(Address address, size_t operand_num,
                                        int64_t displacement) const;

  // Contains and owns all known instances of BaseType.
  BaseType::BaseTypes base_types_;
  // Contains and owns all known instances of MemberType.
  BaseType::MemberTypes member_types_;
  // Associates type names with BaseType instances. This map is used as
  // a lookup mechanism to check whether a given type already exists.
  TypesByString types_map_;
  // Associates IDA type ids with base type instances. This map does not own
  // the base type ids, instead these instances are owned by base_tpyes_.
  TypeIdMap structure_types_by_id_;
  // Associates (function) addresses with function prototypes (i.e. BaseType
  // instances).
  // The base type instances are actually owned by base_types_.
  PrototypesMap prototypes_by_address_;
};

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_IDA_TYPES_CONTAINER_H_
