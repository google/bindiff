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

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_TYPES_CONTAINER_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_TYPES_CONTAINER_H_

#include <stddef.h>
#include <vector>

#include "third_party/zynamics/binexport/base_types.h"
#include "third_party/zynamics/binexport/types.h"

class Function;

// Abstract base class to map a platform specific type system to a type system
// being described by BaseType and TypeMember instances.
// The (derived) TypesContainer provides access to this mapping by implementing
// the GetBaseTypes, GetMemberTypes and ResolveStructureVariable methods.
// The actual types must be initialized by a call to GatherTypes.
// GatherTypes should not be called multiple times, but the implementation must
// be aware of such a scenario.
class TypesContainer {
 public:
  TypesContainer() = default;
  virtual ~TypesContainer() = default;

  // Describes a reference to either a non-compound type, e.g. "int" or "char*"
  // or a reference to an element within a compound type, e.g. a struct member.
  // In case of (nested) unions, a root type together with an offset is not
  // enough to uniquely address a specific (union) member, so we need a sequence
  // of members starting at a root type (the least nested compound type).
  // Note that offset needs to be given in bits, not bytes!
  struct TypeReference {
    TypeReference(const BaseType* root_type,
                  const BaseType::MemberIds& member_path,
                  int offset)
      : root_type(root_type), member_path(member_path), offset(offset) {}

    bool IsValid() const {
      return root_type != nullptr;
    }

    // Creates a type reference which signals that it doesn't contain a valid
    // reference to a type or member.
    static TypeReference CreateEmptyReference() {
      return TypeReference(nullptr /* root_type */,
                           BaseType::MemberIds(),
                           0 /* offset */);
    }

    // Creates a TypeReference instance that describes a reference to a simple,
    // non-compound base type, i.e. a value or pointer type.
    static TypeReference CreateBaseTypeReference(const BaseType* base_type) {
      return TypeReference(base_type,
                           BaseType::MemberIds(),
                           0 /* offset */);
    }

    // Creates a TypeReference instance that describes a reference to a
    // compound type, i.e. a structure or union.
    static TypeReference CreateMemberTypeReference(const BaseType* root_type,
        const BaseType::MemberIds& member_path, int offset) {
      return TypeReference(root_type, member_path, offset);
    }

    const BaseType* root_type;
    const BaseType::MemberIds member_path;
    const int offset;
  };

  // Iterates of the platform/domain specific type system and creates
  // corresponding types and members in our type system.
  virtual void GatherTypes() = 0;
  // Determines the type reference that might be associated
  // with the operand at the given address. If no type is found for this
  // location, the base type field of the returned type reference is NULL.
  virtual TypeReference ResolveTypeReference(Address address,
                                             size_t operand_num) const = 0;
  // Determines the type reference that is associated with the operand at the
  // given address, but also takes the immediate value into account that is
  // added as an offset to the operand. If no such type reference is found, the
  // base type field of the returned type reference is NULL.
  virtual TypeReference ResolveDisplacedTypeReference(
      Address address, Address displacement, size_t operand_num) const = 0;
  // Determines the type reference that might be associated with the given
  // immediate value of an address de-referencing operand. If no type is found
  // for this immediate, the base type field of the returned type reference
  // is NULL.
  virtual TypeReference ResolveMemoryTypeReference(Address immediate) const = 0;
  // Returns a list of all known base types.
  // GatherTypes needs to be called first.
  virtual const BaseType::BaseTypes& GetBaseTypes() const = 0;
  // Returns a list of all known members.
  // GatherTypes needs to be called first.
  virtual const BaseType::MemberTypes& GetMemberTypes() const = 0;
  // Determines the base type that represents the stack frame for the given
  // function. Can be NULL if the function doesn't have a stack frame.
  virtual const BaseType* GetStackFrame(const Function& function) const = 0;
  // Creates a new function prototype declaration for the given function and
  // stores it in the internal storage.
  virtual void CreateFunctionPrototype(const Function& function) = 0;
  // Returns the prototype for the given function or null if no prototype is
  // associated with the function.
  virtual const BaseType* GetFunctionPrototype(
      const Function& function) const = 0;

 private:
  TypesContainer(const TypesContainer&) = delete;
  TypesContainer& operator=(const TypesContainer&) = delete;
};

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_TYPES_CONTAINER_H_
