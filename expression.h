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

#ifndef EXPRESSION_H_
#define EXPRESSION_H_

#include <cstdint>
#include <iosfwd>
#include <string>
#include <vector>

#include "third_party/absl/container/node_hash_map.h"
#include "third_party/absl/container/node_hash_set.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/zynamics/binexport/util/types.h"

#pragma pack(push, 1)
class Expression {
 public:
  using ExpressionCache = absl::node_hash_map<std::string, Expression>;
  using StringCache = absl::node_hash_set<std::string>;

  enum Type : uint8_t {
    TYPE_MNEMONIC = 0,
    TYPE_SYMBOL = 1,
    TYPE_IMMEDIATE_INT = 2,
    TYPE_IMMEDIATE_FLOAT = 3,
    TYPE_OPERATOR = 4,
    TYPE_REGISTER = 5,
    TYPE_SIZEPREFIX = 6,
    TYPE_DEREFERENCE = 7,

    // These are only used by the IDA plugin
    TYPE_NEWOPERAND = 8,
    TYPE_STACKVARIABLE = 9,
    TYPE_GLOBALVARIABLE = 10,
    TYPE_JUMPLABEL = 11,
    TYPE_FUNCTION = 12,

    TYPE_INVALID = 255
  };

  ~Expression() = default;

  bool IsSymbol() const;
  bool IsImmediate() const;
  bool IsOperator() const;
  bool IsDereferenceOperator() const;
  bool IsRelocation() const;
  bool IsRegister() const;
  int GetId() const;
  Type GetType() const;
  const std::string& GetSymbol() const;
  uint16_t GetPosition() const;
  int64_t GetImmediate() const;
  const Expression* GetParent() const;
  std::string CreateSignature();
  static Expression* Create(const Expression* parent,
                            const std::string& symbol = "",
                            int64_t immediate = 0,
                            Type type = TYPE_IMMEDIATE_INT,
                            uint16_t position = 0, bool relocatable = false);
  static void EmptyCache();
  static const ExpressionCache& GetExpressions();

  class Builder {
   public:
    explicit Builder(Expression::Type type) : type_(type) {}

    Builder& AtPosition(uint16_t position) {
      position_ = position;
      return *this;
    }

    Builder& SetRelocatable(bool relocatable) {
      relocatable_ = relocatable;
      return *this;
    }

    Builder& WithParent(const Expression* parent) {
      parent_ = parent;
      return *this;
    }

    static Builder Operator(absl::string_view symbol) {
      return Builder(Expression::TYPE_OPERATOR).SetSymbol(symbol);
    }

    static Builder Register(absl::string_view symbol) {
      return Builder(Expression::TYPE_REGISTER).SetSymbol(symbol);
    }

    static Builder ImmediateInt(uint64_t immediate) {
      return Builder(Expression::TYPE_IMMEDIATE_INT).SetImmediate(immediate);
    }

    static Builder SizePrefix(absl::string_view size_prefix) {
      return Builder(Expression::TYPE_SIZEPREFIX).SetSymbol(size_prefix);
    }

    static Builder SizePrefix(int size_in_bits) {
      return SizePrefix(absl::StrCat("b", size_in_bits / 8));
    }

    static Builder Dereference() {
      return Builder(Expression::TYPE_DEREFERENCE).SetSymbol("[");
    }

    Expression* Build() {
      return Expression::Create(parent_, std::move(symbol_), immediate_, type_,
                                position_, relocatable_);
    }

   private:
    Builder& SetSymbol(absl::string_view symbol) {
      symbol_ = std::string(symbol);
      return *this;
    }

    Builder& SetImmediate(uint64_t immediate) {
      immediate_ = immediate;
      return *this;
    }

    std::string symbol_ = "";
    uint64_t immediate_ = 0;
    uint16_t position_ = 0;
    bool relocatable_ = false;
    Expression::Type type_;
    const Expression* parent_ = nullptr;
  };

 private:
  static const std::string* CacheString(const std::string& value);

  // The constructor is private for two reasons:
  // - We want to make sure expressions can only be created on the heap.
  // - We want to avoid creating duplicate expressions so we just
  //   refer to the expression_cache_ if someone tries (that way archiving
  //   compression/de-duping)
  Expression(const Expression* parent, const std::string& symbol,
             int64_t immediate, Type type, uint16_t position, bool relocatable);

  const std::string* symbol_;
  int64_t immediate_;
  const Expression* parent_;
  int id_ = 0;
  uint16_t position_;
  Type type_;
  bool relocatable_;

  static StringCache string_cache_;
  static ExpressionCache expression_cache_;
  static int global_id_;
};
#pragma pack(pop)

using Expressions = std::vector<Expression*>;
std::ostream& operator<<(std::ostream& stream, const Expression& expression);

class Instruction;
std::pair<int, int> GetSourceExpressionId(const Instruction& instruction,
                                          Address target);

#endif  // EXPRESSION_H_
