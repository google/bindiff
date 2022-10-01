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

#include "third_party/zynamics/binexport/expression.h"

#include <sstream>

#include "third_party/absl/log/check.h"
#include "third_party/zynamics/binexport/instruction.h"
#include "third_party/zynamics/binexport/util/hash.h"

Expression::StringCache Expression::string_cache_;
Expression::ExpressionCache Expression::expression_cache_;
int Expression::global_id_ = 0;

Expression::Expression(const Expression* parent, const std::string& symbol,
                       int64_t immediate, Type type, uint16_t position,
                       bool relocatable)
    : symbol_(CacheString(symbol)),
      immediate_(immediate),
      parent_(parent),
      position_(position),
      type_(type),
      relocatable_(relocatable) {
  DCHECK(!symbol.empty() || IsImmediate());
}

Expression* Expression::Create(const Expression* parent,
                               const std::string& symbol, int64_t immediate,
                               Type type, uint16_t position, bool relocatable) {
  Expression expression(parent, symbol, immediate, type, position, relocatable);
  const std::string signature = expression.CreateSignature();
  ExpressionCache::iterator i = expression_cache_.find(signature);
  if (i != expression_cache_.end()) {
    return &i->second;
  }
  // Id should simply be a count of how many objects are already in the cache.
  expression.id_ = ++global_id_;
  return &expression_cache_.insert(std::make_pair(signature, expression))
              .first->second;
}

void Expression::EmptyCache() {
  ExpressionCache().swap(expression_cache_);
  StringCache().swap(string_cache_);
  global_id_ = 0;
}

const std::string* Expression::CacheString(const std::string& value) {
  return &*string_cache_.insert(value).first;
}

const Expression::ExpressionCache& Expression::GetExpressions() {
  return expression_cache_;
}

bool Expression::IsSymbol() const { return type_ == TYPE_SYMBOL; }

bool Expression::IsRegister() const { return type_ == TYPE_REGISTER; }

bool Expression::IsImmediate() const {
  return type_ == TYPE_IMMEDIATE_INT || type_ == TYPE_IMMEDIATE_FLOAT ||
         type_ > TYPE_DEREFERENCE;
  // The additional types are only used for syntax highlighting and
  // differentiating the various types of immediate labels (location, function,
  // local/global variable).
}

bool Expression::IsOperator() const { return type_ == TYPE_OPERATOR; }

bool Expression::IsDereferenceOperator() const {
  return type_ == TYPE_DEREFERENCE;
}

bool Expression::IsRelocation() const { return relocatable_; }

int Expression::GetId() const { return id_; }

Expression::Type Expression::GetType() const { return type_; }

const std::string& Expression::GetSymbol() const { return *symbol_; }

uint16_t Expression::GetPosition() const { return position_; }

int64_t Expression::GetImmediate() const { return immediate_; }

const Expression* Expression::GetParent() const { return parent_; }

std::string Expression::CreateSignature() {
  std::string signature(19 /* length of the signature */, '0');
  signature[0] = static_cast<char>(type_);
  *reinterpret_cast<uint16_t*>(&signature[1]) = position_;
  *reinterpret_cast<Address*>(&signature[3]) = immediate_;
  *reinterpret_cast<uint32_t*>(&signature[11]) = GetSdbmHash(*symbol_);
  *reinterpret_cast<uint32_t*>(&signature[15]) = parent_ ? parent_->GetId() : 0;
  return signature;
}

std::ostream& operator<<(std::ostream& stream, const Expression& expression) {
  if (expression.IsDereferenceOperator()) {
    stream << "[";
  } else if (!expression.GetSymbol().empty()) {
    stream << expression.GetSymbol();
  } else if (expression.GetImmediate() >= 0) {
    stream << std::hex << expression.GetImmediate();
  } else {
    stream << "-" << std::hex << -expression.GetImmediate();
  }

  // TODO(cblichmann): Reimplement using RenderExpression in instruction.cc
  // for (auto it =
  //      expression.children.begin(); it != expression.children.end(); ++it)
  //   stream << "(" << **it << ")";

  if (expression.IsDereferenceOperator()) {
    stream << "]";
  }
  return stream;
}

// Since IDA's xref structure only contains the address but neither the operand
// nor expression for an address reference we have to figure it out ourselves.
// First: Try to match an immediate to the exact target address, if that fails,
// use an address dereference operator, the only operand or the first immediate
// we come across - in that order.
std::pair<int, int> GetSourceExpressionId(const Instruction& instruction,
                                          Address target) {
  // Try an exact immediate match in any operand's expression first.
  int operand_index = 0;
  for (const auto* operand : instruction) {
    int expression_index = 0;
    for (const auto* expression : *operand) {
      if (expression->IsImmediate() &&
          (Address)expression->GetImmediate() == target) {
        return {operand_index, expression_index};
      }
      ++expression_index;
    }
    ++operand_index;
  }

  // Try a memory dereference in any operand's expression second.
  operand_index = 0;
  for (const auto* operand : instruction) {
    int expression_index = 0;
    for (const auto* expression : *operand) {
      if (expression->GetParent() &&
          expression->GetParent()->IsDereferenceOperator()) {
        // Use the parent expression index if the expression is not an
        // immediate.
        return {operand_index, expression->IsImmediate()
                                   ? expression_index
                                   : expression_index - 1};
      }
      ++expression_index;
    }
    ++operand_index;
  }

  // If we only have a single operand return that.
  if (instruction.GetOperandCount() == 1) {
    return {0, 0};
  }

  // Return any immediate expression we can find.
  operand_index = 0;
  for (const auto* operand : instruction) {
    int expression_index = 0;
    for (const auto* expression : *operand) {
      if (expression->IsImmediate()) {
        return {operand_index, expression_index};
      }
      ++expression_index;
    }
    ++operand_index;
  }

  // Give up.
  return {-1, -1};
}
