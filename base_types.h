// Copyright 2011-2020 Google LLC
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

// Defines the structures corresponding to the database tables ex_?_base_types
// and ex_?_types, respectively, to represent types and members of compound
// types according to the C type system.

#ifndef BASE_TYPES_H_
#define BASE_TYPES_H_

#include <cstdint>
#include <string>
#include <vector>

#include "third_party/zynamics/binexport/types.h"

struct MemberType;

// Represents a base type (e.g. int, struct_A, etc).
// Instantiation of base_type is not thread safe.
class BaseType {
 public:
  // Note: when changing this enum, the GetCategoryString method needs to be
  // updated as well.
  enum TypeCategory {
    kAtomic,
    kPointer,
    kStruct,
    kArray,
    kUnion,
    kFunctionPrototype
  };

  BaseType()
      : id_(NextTypeId()),
        pointer_(nullptr),
        category_(kAtomic) {}

  using BaseTypes = std::vector<const BaseType*>;
  using MemberTypes = std::vector<MemberType*>;
  using MemberIds = std::vector<int>;

  // Returns the database id of this base type.
  uint32_t GetId() const;

  void SetName(const std::string& name);
  const std::string& GetName() const;

  void SetSize(size_t size);
  size_t GetSize() const;

  void SetSigned(bool is_signed);
  bool IsSigned() const;

  void SetPointer(const BaseType* pointer);
  const BaseType* GetPointer() const;

  void AddMember(MemberType* member);
  // Returns a list of members sorted by offset in ascending order.
  const MemberTypes& GetMembers() const;

  void SetCategory(TypeCategory category);
  std::string GetCategoryString() const;

  static const MemberType* ResolveMember(const BaseType* base_type, int offset);

 private:
  static uint32_t NextTypeId();

  uint32_t id_;
  // The name of  this type.
  std::string name_;
  // The size of this type in bits.
  size_t size_ = 0;
  // Is this type able to represent signed values?
  bool is_signed_ = false;
  // If this is a pointer type we also have a parent relation
  // (e.g. int** -> int* -> int).
  const BaseType* pointer_;
  // The list of members that belong to this base type. The base type does not
  // own the MemberType instances. Instead, the types container owns them.
  MemberTypes members_;
  TypeCategory category_;
};

// Represents an element of a compound type.
// Instances of MemberType are not thread safe.
struct MemberType {
  // Represents a null value in the database schema.
  enum { DB_NULL_VALUE = -1 };

  MemberType() : id(NextTypeId()), type(nullptr), parent_type(nullptr) {}

  // The corresponding id in the database.
  uint32_t id;
  // The name of the member.
  std::string name;
  // The type of this member.
  const BaseType* type;
  // The parent id where this type is contained in.
  const BaseType* parent_type;
  // The offset of this member within a structure type.
  // DB_NULL_VALUE if this should be NULL in the database (for arrays).
  int offset = DB_NULL_VALUE;
  // The ith argument if this is a function pointer.
  // DB_NULL_VALUE if this should be NULL in the database.
  // (anything but function pointers).
  int argument = DB_NULL_VALUE;
  // Number of elements if this is an array.
  // DB_NULL_VALUE if this should be NULL in the database
  // (anything but arrays).
  int num_elements = DB_NULL_VALUE;

 private:
  static uint32_t NextTypeId();
};

#endif  // BASE_TYPES_H_
