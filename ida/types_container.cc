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

#include "third_party/zynamics/binexport/ida/types_container.h"

#include <boost/iterator/iterator_facade.hpp>

#include "third_party/zynamics/binexport/ida/pro_forward.h"  // NOLINT
#include <enum.hpp>                                          // NOLINT
#include <frame.hpp>                                         // NOLINT
#include <ida.hpp>                                           // NOLINT
#include <idp.hpp>                                           // NOLINT
#include <name.hpp>                                          // NOLINT
#include <struct.hpp>                                        // NOLINT
#include <typeinf.hpp>                                       // NOLINT

#include "base/logging.h"
#include "base/stringprintf.h"
#include "third_party/zynamics/binexport/base_types.h"
#include "third_party/zynamics/binexport/function.h"
#include "third_party/zynamics/binexport/ida/names.h"

namespace {

static const char kVoidPtrName[] = "void *";
static const char kByteName[] = "BYTE";
static const char kWordName[] = "WORD";
static const char kDWordName[] = "DWORD";
static const char kQWordName[] = "QWORD";

// Sometimes IDA creates stack frames that are unreasonably large.
// In that case, we do not iterate further than about 5MB to not waste CPU time.
static const asize_t kMaxMemberOffset = 5 << 20;

enum {
  kByteBitSize = 8,
  kWordBitSize = 16,
  kDWordBitSize = 32,
  kQWordBitSize = 64
};

// The return type of a function prototype corresponds to the member whose
// argument is zero. All regular prototype arguments start at one.
enum { kReturnArgument = 0, kFirstArgument = 1 };

// An iterator that allows enumeration of all structures in IDA.
struct StructIterator
    : public boost::iterator_facade<StructIterator, struc_t,
                                    boost::forward_traversal_tag> {
 public:
  StructIterator() : current_index_(BADADDR) {}
  explicit StructIterator(uval_t struct_index) : current_index_(struct_index) {}

 private:
  friend class boost::iterator_core_access;

  bool equal(const StructIterator& other) const {
    return current_index_ == other.current_index_;
  }

  void increment() {
    if (current_index_ != BADADDR) {
      current_index_ = get_next_struc_idx(current_index_);
    }
  }

  struc_t& dereference() const {
    if (current_index_ == BADADDR) {
      throw std::runtime_error("Out of bounds access in StructIterator");
    }
    return *get_struc(get_struc_by_idx(current_index_));
  }

  uval_t current_index_;
};

// An iterator that allows enumeration of all stack frame structures in IDA.
struct FrameIterator
    : public boost::iterator_facade<FrameIterator, struc_t,
                                    boost::forward_traversal_tag> {
 public:
  FrameIterator() : current_index_(BADADDR), function_count_(0) {}
  explicit FrameIterator(size_t function_count)
      : current_index_(0), function_count_(function_count) {
    current_index_ = first_valid_index(current_index_, function_count_);
  }

 private:
  friend class boost::iterator_core_access;

  static sval_t first_valid_index(size_t start_index, size_t function_count) {
    if (start_index >= function_count) {
      return BADADDR;
    }
    if (!get_frame(getn_func(start_index)->startEA)) {
      return next_valid_index(start_index, function_count);
    }
    return start_index;
  }

  static sval_t next_valid_index(size_t index, size_t function_count) {
    if (index == BADADDR) {
      return BADADDR;
    }
    while (index < function_count - 1) {
      ++index;
      if (get_frame(getn_func(index)->startEA)) {
        return index;
      }
    }
    return BADADDR;
  }

  bool equal(const FrameIterator& other) const {
    return current_index_ == other.current_index_;
  }

  void increment() {
    current_index_ = next_valid_index(current_index_, function_count_);
  }

  struc_t& dereference() const {
    struc_t* frame_struct = get_frame(getn_func(current_index_)->startEA);
    if (!frame_struct) {
      throw std::runtime_error("Out of bounds access in FrameIterator.");
    }
    return *frame_struct;
  }

  sval_t current_index_;
  const size_t function_count_;
};

// Encapsulates iteration over all available structures in IDA.
struct IdaStructures {
  IdaStructures()
      : begin_(StructIterator(get_first_struc_idx())), end_(StructIterator()) {}

  StructIterator begin() { return begin_; }

  StructIterator end() { return end_; }

 private:
  StructIterator begin_;
  StructIterator end_;
};

// Encapsulates iteration over all stack frame structures in IDA.
struct IdaFrameStructures {
  IdaFrameStructures()
      : begin_(FrameIterator(get_func_qty())), end_(FrameIterator()) {}

  FrameIterator begin() const { return begin_; }

  FrameIterator end() const { return end_; }

 private:
  FrameIterator begin_;
  FrameIterator end_;
};

std::string GetTypeName(const tinfo_t& tif) {
  qstring type_name;
  return std::string(tif.print(&type_name) ? type_name.c_str() : "");
}

const BaseType* GetKnownType(
    const tinfo_t& tif, const IdaTypesContainer::TypesByString& types_map) {
  const auto cit = types_map.find(GetTypeName(tif));
  return cit != types_map.end() ? cit->second : nullptr;
}

// Returns the size in bits for an IDA type pointer.
int GetTypeSize(const tinfo_t& tif) {
  return tif.get_size() == BADSIZE ? 0 : tif.get_size() * ph.cnbits;
}

// Creates a new type instance of an atomic type (i.e. non-compound) based on
// the given IDA type and saves it to the list/map of known types.
BaseType* CreateAtomicBaseType(const tinfo_t& tif,
                               IdaTypesContainer::TypesByString* types_map,
                               BaseType::BaseTypes* types) {
  BaseType* new_type = new BaseType();
  new_type->SetName(GetTypeName(tif));
  new_type->SetSigned(tif.is_signed());
  if (tif.is_void()) {
    new_type->SetSize(GetArchitectureBitness());
  } else {
    new_type->SetSize(GetTypeSize(tif));
  }
  new_type->SetPointer(nullptr);
  types->push_back(new_type);
  types_map->insert(std::make_pair(new_type->GetName(), new_type));
  return new_type;
}

BaseType* CreatePointerBaseType(const tinfo_t& tif,
                                IdaTypesContainer::TypesByString* types_map,
                                BaseType::BaseTypes* types) {
  BaseType* new_type = CreateAtomicBaseType(tif, types_map, types);
  new_type->SetCategory(BaseType::kPointer);
  return new_type;
}

// Handles the creation of primitive types, i.e. non-compound value types.
// Only creates new base types if the requested types don't already exist.
// If the given type represents a pointer type, this method generates the
// whole hierarchy down to the corresponding value type, e.g.
// int** -> int* -> int. Returns the BaseType instance corresponding to the
// given ida type pointer.
const BaseType* CreateOrGetPrimitiveTypes(
    const tinfo_t& tif, IdaTypesContainer::TypesByString* types_map,
    BaseType::BaseTypes* types) {
  if (tif.is_ptr()) {
    BaseType* previous_type = nullptr;
    tinfo_t type(tif);
    for (; !type.empty(); type = type.get_pointed_object()) {
      // It might happen that parts of the pointer hierarchy have already been
      // created so we have to take care not to create duplicate types. We do
      // a const cast here since this is the only place where base types are
      // taken from the types map and need to be changed after they were
      // added. All other code locations are fine with only having const base
      // types.
      BaseType* known_type =
          const_cast<BaseType*>(GetKnownType(type, *types_map));
      if (known_type) {
        previous_type = known_type;
        continue;
      }
      BaseType* new_type = CreatePointerBaseType(type, types_map, types);
      if (previous_type) {
        previous_type->SetPointer(new_type);
      }
      previous_type = new_type;
    }

    // All pointer types have been created, so create the last link to the
    // actual value type, e.g. int* -> int.
    const BaseType* value_type = GetKnownType(type, *types_map);
    if (!value_type) {
      value_type = CreateAtomicBaseType(type, types_map, types);
    }
    previous_type->SetPointer(value_type);
    return GetKnownType(tif, *types_map);
  }
  const BaseType* known_type = GetKnownType(tif, *types_map);
  return known_type ? known_type : CreateAtomicBaseType(tif, types_map, types);
}

const BaseType* CreateOrGetArrayType(
    const tinfo_t& tif, IdaTypesContainer::TypesByString* types_map,
    BaseType::BaseTypes* types, BaseType::MemberTypes* member_types) {
  if (const BaseType* known_type = GetKnownType(tif, *types_map)) {
    return known_type;
  }

  const BaseType* element_type =
      CreateOrGetPrimitiveTypes(tif.get_array_element(), types_map, types);
  BaseType* array_type = new BaseType();
  array_type->SetSigned(false);
  array_type->SetName(GetTypeName(tif));
  array_type->SetPointer(element_type);
  array_type->SetSize(GetTypeSize(tif));
  array_type->SetCategory(BaseType::kArray);
  MemberType* array_member = new MemberType();
  array_member->num_elements = tif.get_array_nelems();
  array_member->parent_type = array_type;
  array_member->type = element_type;
  array_member->offset = MemberType::DB_NULL_VALUE;
  member_types->push_back(array_member);
  types_map->insert(std::make_pair(array_type->GetName(), array_type));
  types->push_back(array_type);
  return array_type;
}

bool IsNegativeValue(Address value) {
  return Instruction::IsNegativeValue(value);
}

// Creates a sequence of members that uniquely identifies a member that is
// present at the given offset, relative to the beginning of the compound type
// base_type, e.g. base_type.member0.member4.member1.
BaseType::MemberIds GenerateMemberPath(const BaseType* base_type, int offset) {
  BaseType::MemberIds path;
  int remaining_offset = offset * 8;
  for (const MemberType* member =
           BaseType::ResolveMember(base_type, remaining_offset);
       member != nullptr;
       member = BaseType::ResolveMember(base_type, remaining_offset)) {
    path.push_back(member->id);
    remaining_offset -= member->offset;
    base_type = member->type;
  }
  return path;
}

const struc_t* GetIdaStruct(Address address, size_t operand_num,
                            adiff_t* offset) {
  tid_t path[MAXSTRUCPATH];
  adiff_t delta;
  if (get_struct_operand(static_cast<ea_t>(address), operand_num, path, offset,
                         &delta)) {
    // For now we only need to consider the root base type together with an
    // offset in order to uniquely address members in nested structures, so
    // we merely return the first element of that path. This will change once
    // unions are supported.
    return get_struc(path[0]);
  }
  return nullptr;
}

}  // namespace

// Creates a new void* and void type and updates the list of types as well as
// the string to type map. Pointers are owned by the base_types container.
const BaseType* IdaTypesContainer::CreateVoidPointerType() {
  BaseType* void_type = new BaseType();
  void_type->SetSigned(false);
  void_type->SetName("void");
  void_type->SetPointer(nullptr);
  void_type->SetSize(GetArchitectureBitness());
  BaseType* void_pointer_type = new BaseType();
  void_pointer_type->SetSigned(false);
  void_pointer_type->SetName(kVoidPtrName);
  void_pointer_type->SetPointer(void_type);
  void_pointer_type->SetSize(GetArchitectureBitness());
  base_types_.push_back(void_type);
  base_types_.push_back(void_pointer_type);
  types_map_.insert(std::make_pair(void_type->GetName(), void_type));
  types_map_.insert(
      std::make_pair(void_pointer_type->GetName(), void_pointer_type));
  return void_pointer_type;
}

// Creates or retrieves a base type instance that has the given size in bytes.
// This is needed since IDA is unable to map simple non-structure types to its
// own type system.
void IdaTypesContainer::InitializeBuiltinTypes() {
  CreateIdaType(kByteName, kByteBitSize);
  CreateIdaType(kWordName, kWordBitSize);
  CreateIdaType(kDWordName, kDWordBitSize);
  CreateIdaType(kQWordName, kQWordBitSize);
  CreateVoidPointerType();
}

void IdaTypesContainer::CreateIdaType(const std::string& name,
                                      size_t bit_size) {
  BaseType* new_base_type = new BaseType();
  new_base_type->SetSigned(true);
  new_base_type->SetName(name);
  new_base_type->SetPointer(nullptr);
  new_base_type->SetSize(bit_size);
  base_types_.push_back(new_base_type);
  types_map_.insert(std::make_pair(name, new_base_type));
}

const BaseType* IdaTypesContainer::GetVoidPointerType() const {
  return types_map_.find(kVoidPtrName)->second;
}

// Retrieves the corresponding IDA type for the given size (in bits).
const BaseType* IdaTypesContainer::GetBuiltinType(size_t type_size) const {
  switch (type_size) {
    case kByteBitSize:
      return types_map_.find(kByteName)->second;
    case kWordBitSize:
      return types_map_.find(kWordName)->second;
    case kDWordBitSize:
      return types_map_.find(kDWordName)->second;
    case kQWordBitSize:
      return types_map_.find(kQWordName)->second;
    default:
      return nullptr;
  }
}

// Delegates creation of the corresponding base types for the given member.
const BaseType* IdaTypesContainer::CreateOrGetBaseTypes(
    const member_t* member, BaseType::MemberTypes* member_types) {
  tinfo_t tif;
  if (!get_member_tinfo2(member, &tif) && !guess_tinfo2(member->id, &tif)) {
    return nullptr;
  }
  if (tif.is_array()) {
    return CreateOrGetArrayType(tif, &types_map_, &base_types_, member_types);
  }
  if (!tif.is_funcptr()) {
    return CreateOrGetPrimitiveTypes(tif, &types_map_, &base_types_);
  }
  return nullptr;
}

// Creates a new member at the specified offset (given in bytes) that belongs to
// "parent_struct" and adds it to the list of known members.
MemberType* IdaTypesContainer::CreateMember(
    const BaseType* parent_struct, const member_t* ida_member, int offset,
    const BaseType* member_base_type, BaseType::MemberTypes* member_types) {
  MemberType* new_member = new MemberType();
  new_member->type = member_base_type;
  // If this member is an array type, then the actual number of elements is
  // determined by the (hidden) member in the corresponding array base type.
  new_member->num_elements = MemberType::DB_NULL_VALUE;
  new_member->offset = offset * 8;  // We store offsets in bit granularity.
  new_member->parent_type = parent_struct;

  qstring ida_name;
  if (get_member_name2(&ida_name, ida_member->id)) {
    new_member->name.assign(ida_name.c_str(), ida_name.length());
  } else {
    LOG(INFO) << "Unable to determine name for member with id: "
              << ida_member->id;
  }
  member_types->push_back(new_member);
  return new_member;
}

void IdaTypesContainer::Cleanup() {
  for (auto it = base_types_.begin(), it_end = base_types_.end(); it != it_end;
       ++it) {
    delete *it;
  }

  for (auto it = member_types_.begin(), it_end = member_types_.end();
       it != it_end; ++it) {
    delete *it;
  }

  base_types_.clear();
  member_types_.clear();
  types_map_.clear();
  structure_types_by_id_.clear();
  prototypes_by_address_.clear();
}

// First pass: collect all compound types (i.e. structs and unions).
// Note: we need to do this so we can determine that a type which is not
// in our known_types is actually some kind of value type.
template <class T>
void IdaTypesContainer::CollectCompoundTypes(T start_it, T end_it) {
  // IDA has cases where members are believed to be in a stack frame of a
  // function where this is really a disassembly error. Therefore we try to
  // raise awareness to this issue here.
  const int kMaxStructSize = std::numeric_limits<int32_t>::max();
  for (T it = start_it; it != end_it; ++it) {
    const tid_t struct_id = it->id;
    const asize_t struct_size = static_cast<size_t>(get_struc_size(struct_id)) *
                                static_cast<size_t>(ph.cnbits);

    if (struct_size >= kMaxStructSize) {
      LOG(INFO) << "Unable to create BaseType with " << struct_size
                << " >= " << kMaxStructSize
                << " check your disassembly for struct id: " << struct_id
                << " to fix the struct.";
      continue;
    }

    // IDA is unable to give us the reverse mapping of types,
    // i.e. "int **" -> tid_t is impossible, so we represent types as strings.
    qstring ida_name(get_struc_name(struct_id));
    if (ida_name.empty()) {
      LOG(INFO) << "Unable to determine name for structure with id: "
                << struct_id;
      continue;
    }

    BaseType* struct_type = new BaseType();
    struct_type->SetName(ida_name.c_str());
    struct_type->SetSize(struct_size);
    struct_type->SetPointer(nullptr);
    struct_type->SetCategory(::is_union(struct_id) ? BaseType::kUnion
                                                   : BaseType::kStruct);
    types_map_.insert(std::make_pair(struct_type->GetName(), struct_type));
    base_types_.push_back(struct_type);
    structure_types_by_id_.insert(std::make_pair(struct_id, struct_type));
  }
}

// Second pass: Create all members.
template <class T>
void IdaTypesContainer::CollectMemberTypes(T start_it, T end_it) {
  for (T it = start_it; it != end_it; ++it) {
    const struc_t& structure = *it;
    TypeIdMap::const_iterator parent_type_it =
        structure_types_by_id_.find(structure.id);
    if (parent_type_it != structure_types_by_id_.end()) {
      BaseType* parent_type = parent_type_it->second;
      const asize_t max_member_offset =
          std::min(get_struc_size(structure.id), kMaxMemberOffset);
      const bool is_union_type = ::is_union(structure.id);
      for (ea_t offset = get_struc_first_offset(&structure);
           offset < max_member_offset && offset != BADADDR;
           offset = get_struc_next_offset(&structure, offset)) {
        const member_t* ida_member = get_member(&structure, offset);
        // If there is no member, IDA inserts "undefined" bytes. We just skip
        // these and allow "holes" in the structure. This is not an issue since
        // all valid members have a known offset within their structure.
        if (ida_member) {
          const BaseType* member_base_type =
              CreateOrGetBaseTypes(ida_member, &member_types_);
          // If this is a union we set the offset to zero since by definition
          // all elements of a union start at the same offset.
          MemberType* new_member =
              CreateMember(parent_type, ida_member, is_union_type ? 0 : offset,
                           member_base_type, &member_types_);
          if (new_member) {
            parent_type->AddMember(new_member);
          }
        }
      }
    } else {
      LOG(INFO) << "Unable to associate IDA structure id " << structure.id
                << " with internal base type";
    }
  }
}

void IdaTypesContainer::GatherTypes() {
  Cleanup();
  InitializeBuiltinTypes();

  IdaStructures ida_structs;
  IdaFrameStructures frame_structs;
  CollectCompoundTypes(ida_structs.begin(), ida_structs.end());
  CollectCompoundTypes(frame_structs.begin(), frame_structs.end());

  CollectMemberTypes(ida_structs.begin(), ida_structs.end());
  CollectMemberTypes(frame_structs.begin(), frame_structs.end());

  // Third pass: replace all NULL base types for members whose base type could
  // not be resolved or which were function pointers.
  const BaseType* void_pointer_type = GetVoidPointerType();
  for (BaseType::MemberTypes::iterator it = member_types_.begin(),
                                       it_end = member_types_.end();
       it != it_end; ++it) {
    if (!(*it)->type) {
      (*it)->type = void_pointer_type;
    }
  }
}

IdaTypesContainer::~IdaTypesContainer() { Cleanup(); }

TypesContainer::TypeReference IdaTypesContainer::CreateStackReference(
    Address address, size_t operand_num, int64_t displacement) const {
  func_t* function = get_func(address);
  if (!function) {
    return TypeReference::CreateEmptyReference();
  }
  const int structure_offset =
      calc_stkvar_struc_offset(function, address, operand_num);
  // final_offset describes the offset that we need to store for the
  // substitution so BinNavi can determine the referenced member as:
  // member_offset = displacement + final_offset
  // where member_offset is the offset of the referenced member relative to the
  // beginning of the stack frame.
  const int final_offset = structure_offset - displacement;
  if (final_offset < 0) {
    // We got ambiguous data from IDA (the structure is actually smaller than
    // the reported structure offset) so we don't create a substitution at all.
    return TypeReference::CreateEmptyReference();
  }
  const BaseType* base_type = GetStackFrame(function->startEA);
  if (!base_type) {
    LOG(INFO) << StringPrintf("Stack frame of function: %08llx corrupted.",
                              function->startEA);
    return TypeReference::CreateEmptyReference();
  }

  return TypeReference::CreateMemberTypeReference(
      base_type, GenerateMemberPath(base_type, structure_offset),
      final_offset * 8 /* offset in bits */);
}

TypesContainer::TypeReference IdaTypesContainer::CreateNonStackReference(
    Address address, size_t operand_num, int64_t displacement) const {
  // For negative displacements we give up and return an empty reference, since
  // there is no way to obtain a unique base structure in the general case:
  // the obtained base type could be nested in arbitrarily many other
  // structures.
  // For positive displacements, we simply let the type substitution point at
  // the beginning of the structure since Navi will resolve the actual member
  // by taking the immediate displacement itself as the offset into the
  // structure anyway.
  if (displacement >= 0) {
    // Note: IDA associates a type reference (e.g. structure) with a whole
    // operand whereas Navi associates a type with a register, i.e. Navi uses
    // a more precise representation since a register can point into the
    // middle of a structure. This is always the case for instances of
    // structures that are contained in a stack frame.
    // IDA:  [ebp-70h] -> Foo.x
    // Navi: [ebp-70h] -> Stackframe.Foo.x
    // Therefore, we have to find the "parent" structure for references
    // with negative immediate offsets.
    adiff_t offset;
    const struc_t* ida_struct = GetIdaStruct(address, operand_num, &offset);
    if (!ida_struct) {
      return TypeReference::CreateEmptyReference();
    }
    TypeIdMap::const_iterator cit = structure_types_by_id_.find(ida_struct->id);
    if (cit != structure_types_by_id_.end()) {
      return TypeReference::CreateMemberTypeReference(
          cit->second, GenerateMemberPath(cit->second, displacement),
          0 /* offset */);
    }
  }
  return TypeReference::CreateEmptyReference();
}

TypesContainer::TypeReference IdaTypesContainer::ResolveDisplacedTypeReference(
    Address address, Address displacement, size_t operand_num) const {
  int64_t signed_displacement = Instruction::IsNegativeValue(displacement)
                                    ? static_cast<int32_t>(displacement)
                                    : static_cast<int64_t>(displacement);
  if (isStkvar(get_flags_novalue(static_cast<ea_t>(address)), operand_num)) {
    return CreateStackReference(address, operand_num, signed_displacement);
  }
  return CreateNonStackReference(address, operand_num, signed_displacement);
}

// Determines whether the given operand of the instruction at the given address
// references a base type instance.
TypesContainer::TypeReference IdaTypesContainer::ResolveTypeReference(
    Address address, size_t operand_num) const {
  adiff_t offset;
  const struc_t* ida_struct = GetIdaStruct(address, operand_num, &offset);
  if (ida_struct) {
    TypeIdMap::const_iterator cit = structure_types_by_id_.find(ida_struct->id);
    if (cit != structure_types_by_id_.end()) {
      return TypeReference::CreateMemberTypeReference(
          cit->second, GenerateMemberPath(cit->second, offset),
          offset * 8 /* offset in bits */);
    }
  }
  return TypeReference::CreateBaseTypeReference(GetVoidPointerType());
}

TypesContainer::TypeReference IdaTypesContainer::ResolveMemoryTypeReference(
    Address immediate) const {
  const flags_t flags = getFlags(immediate);
  if (isOff(flags, 0 /* operand number*/)) {
    // We treat every datum pointing anywhere else as void* since we can not
    // currently handle function pointers or intra-section offsets.
    return TypeReference::CreateBaseTypeReference(GetVoidPointerType());
  } else if (isASCII(flags)) {
    // TODO(jannewger): actually emit a char array of the corresponding size.
    return TypeReference::CreateBaseTypeReference(
        GetBuiltinType(get_item_size(immediate) * 8 /* bits per byte*/));
  } else {
    // Check if this instance is simply a byte, word, dword or qword.
    // Note that IDA can't give us a "real type" for this instance but only
    // the size of the datum.
    return TypeReference::CreateBaseTypeReference(
        GetBuiltinType(get_item_size(immediate) * 8 /* bits per byte*/));
  }
}

const BaseType* IdaTypesContainer::GetStackFrame(
    Address function_address) const {
  struc_t* stack_frame = get_frame(function_address);
  if (!stack_frame) {
    return nullptr;
  }
  TypeIdMap::const_iterator cit = structure_types_by_id_.find(stack_frame->id);
  return (cit != structure_types_by_id_.end()) ? cit->second : nullptr;
}

const BaseType* IdaTypesContainer::GetStackFrame(
    const Function& function) const {
  return GetStackFrame(function.GetEntryPoint());
}

// Creates a single function prototype for the given function and stores the
// corresponding type in the internal containers.
void IdaTypesContainer::CreateFunctionPrototype(const Function& function) {
  const Address address = function.GetEntryPoint();

  tinfo_t tif;
  if (!get_tinfo2(address, &tif) && !guess_tinfo2(address, &tif)) {
    return;
  }

  if (!tif.is_func()) {
    return;
  }

  const int num_arguments = tif.get_nargs();
  if (num_arguments == -1) {
    LOG(INFO) << StringPrintf(
        "Error: unable to determine function prototype for function at %08llx");
    return;
  }

  BaseType* prototype = new BaseType();
  prototype->SetCategory(BaseType::kFunctionPrototype);
  // Note that prototypes have empty names, therefore we don't put them in the
  // names map.
  base_types_.push_back(prototype);

  // 1) Determine and instantiate the return type of the prototype.
  // If the type could not be determined, it is set to void*.
  MemberType* return_member = new MemberType();
  member_types_.push_back(return_member);
  prototype->AddMember(return_member);
  return_member->argument = kReturnArgument;
  return_member->parent_type = prototype;
  return_member->type =
      CreateOrGetPrimitiveTypes(tif.get_rettype(), &types_map_, &base_types_);
  if (!return_member->type) {
    LOG(INFO) << StringPrintf(
        "Warning: unable to determine return type for prototype of "
        " function at %08llx");
    return_member->type = GetVoidPointerType();
  }

  // 2) Determine and instantiate all argument types.
  for (int i = 0; i < num_arguments; ++i) {
    const BaseType* arg_type = CreateOrGetPrimitiveTypes(
        tif.get_nth_arg(i), &types_map_, &base_types_);
    if (!arg_type) {
      LOG(INFO) << StringPrintf(
          "Warning: unable to determine type of function argument %d for "
          "prototype of function at %08llx",
          kFirstArgument + i, address);
      arg_type = GetVoidPointerType();
    }
    MemberType* argument = new MemberType();
    argument->type = arg_type;
    argument->parent_type = prototype;
    argument->argument = kFirstArgument + i;
    member_types_.push_back(argument);
    prototype->AddMember(argument);
  }
  prototypes_by_address_.insert(
      std::make_pair(function.GetEntryPoint(), prototype));
}

const BaseType* IdaTypesContainer::GetFunctionPrototype(
    const Function& function) const {
  auto cit = prototypes_by_address_.find(function.GetEntryPoint());
  return (cit != prototypes_by_address_.end()) ? cit->second : nullptr;
}
