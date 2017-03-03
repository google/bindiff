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

#include "third_party/zynamics/binexport/base_types.h"

#include <assert.h>
#include <algorithm>

namespace {

bool SortMemberTypes(const MemberType* lhs, const MemberType* rhs) {
  // If these are function prototype arguments, we sort by position of the
  // argument, otherwise we sort by member offset.
  if (lhs->offset == MemberType::DB_NULL_VALUE &&
      rhs->offset == MemberType::DB_NULL_VALUE) {
    return lhs->argument < rhs->argument;
  }
  return lhs->offset < rhs->offset;
}

}  // namespace

// The database sequences need to start with one.
// Otherwise the initialization in BinNavi can fail.
unsigned int BaseType::NextTypeId() {
  static unsigned int type_id = 1;
  return type_id++;
}

unsigned int BaseType::GetId() const {
  return id_;
}

void BaseType::SetName(const std::string& name) {
  name_ = name;
}

const std::string& BaseType::GetName() const {
  return name_;
}

void BaseType::SetSize(size_t size) {
  size_ = size;
}

size_t BaseType::GetSize() const {
  return size_;
}

void BaseType::SetSigned(bool is_signed) {
  is_signed_ = is_signed;
}

bool BaseType::IsSigned() const {
  return is_signed_;
}

void BaseType::SetPointer(const BaseType* pointer) {
  pointer_ = pointer;
}

const BaseType* BaseType::GetPointer() const {
  return pointer_;
}

void BaseType::AddMember(MemberType* member) {
  members_.insert(std::lower_bound(members_.begin(),
      members_.end(), member, &SortMemberTypes), member);
}

const BaseType::MemberTypes& BaseType::GetMembers() const {
  return members_;
}

void BaseType::SetCategory(TypeCategory category) {
  category_ = category;
}

std::string BaseType::GetCategoryString() const {
  switch (category_) {
    case kAtomic:
      return "atomic";
    case kPointer:
      return "pointer";
    case kStruct:
      return "struct";
    case kArray:
      return "array";
    case kUnion:
      return "union";
    case kFunctionPrototype:
      return "function_pointer";
    default:
      assert(false && "Unknown category value.");
      return "UNKNOWN";
  }
}

bool IsWithinMember(const MemberType* member, int offset) {
  return offset >= member->offset
      && offset < member->offset + member->type->GetSize();
}

// Finds the member x in the struct that occupies space in the range
// [offset, offset + sizeof(x)). Returns NULL if no such member exists.
// The offset needs to be specified in bits.
const MemberType* BaseType::ResolveMember(const BaseType* base_type,
                                          int offset) {
  if (base_type->GetMembers().empty()) {
    return nullptr;
  }

  MemberType search_member;
  search_member.offset = offset;
  MemberTypes::const_iterator cit =
      std::lower_bound(base_type->GetMembers().begin(),
                       base_type->GetMembers().end(),
                       &search_member,
                       &SortMemberTypes);
  // We might have found the subsequent member (since lower bound gives us the
  // first member that is greater or equal regarding our search offset).
  if (cit == base_type->GetMembers().end() || !IsWithinMember(*cit, offset)) {
    if (cit == base_type->GetMembers().begin()) {
      return nullptr;
    }
    --cit;
  }

  return IsWithinMember(*cit, offset) ? *cit : nullptr;
}

unsigned int MemberType::NextTypeId() {
  static unsigned int member_id = 1;
  return member_id++;
}

