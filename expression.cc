// Copyright 2011-2016 Google Inc. All Rights Reserved.
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

#include "third_party/zynamics/binexport/expression.h"

#include <sstream>

#include "third_party/zynamics/binexport/hash.h"
#include "third_party/zynamics/binexport/instruction.h"

Expression::StringCache Expression::string_cache_;
Expression::ExpressionCache Expression::expression_cache_;
int Expression::global_id_ = 0;

Expression::Expression(Expression* parent, const std::string& symbol,
                       int64_t immediate, Type type, uint16_t position)
    : symbol_(CacheString(symbol)),
      immediate_(immediate),
      parent_(parent),
      id_(0),
      position_(position),
      type_(type) {
  assert(!symbol.empty() || IsImmediate());
}

Expression* Expression::Create(Expression* parent, const std::string& symbol,
                               int64_t immediate, Type type,
                               uint16_t position) {
  Expression expression(parent, symbol, immediate, type, position);
  const std::string signature = expression.CreateSignature();
  ExpressionCache::iterator i = expression_cache_.find(signature);
  if (i != expression_cache_.end()) {
    return &i->second;
  } else {
    // Id should simply be a count of how many objects are already in the cache.
    expression.id_ = ++global_id_;
    return &expression_cache_.insert(std::make_pair(signature, expression))
                .first->second;
  }
}

void Expression::EmptyCache() {
  ExpressionCache().swap(expression_cache_);
  StringCache().swap(string_cache_);
  global_id_ = 0;
}

const std::string* Expression::CacheString(const std::string& string) {
  return &*string_cache_.insert(string).first;
}

const Expression::ExpressionCache& Expression::GetExpressions() {
  return expression_cache_;
}

bool Expression::IsSymbol() const { return type_ == TYPE_SYMBOL; }

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
  if (expression.IsDereferenceOperator())
    stream << "[";
  else if (!expression.GetSymbol().empty())
    stream << expression.GetSymbol();
  else if (expression.GetImmediate() >= 0)
    stream << std::hex << expression.GetImmediate();
  else
    stream << "-" << std::hex << -expression.GetImmediate();

  // TODO(user) Re-implement using renderExpression in instruction.cpp
  // for (std::list< CExpression * >::const_iterator i =
  //      expression.m_Children.begin(); i != expression.m_Children.end(); ++i )
  //   stream << "(" << **i << ")";

  if (expression.IsDereferenceOperator()) stream << "]";

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
  int operand_num = 0;
  for (Operands::const_iterator j = instruction.GetFirstOperand(),
                                jend = instruction.GetLastOperand();
       j != jend; ++j, ++operand_num) {
    const Operand& operand = **j;
    for (Expressions::const_iterator i = operand.GetFirstExpression(),
                                     end = operand.GetLastExpression();
         i != end; ++i) {
      const Expression* expression = *i;
      if (expression->IsImmediate() &&
          (Address)expression->GetImmediate() == target) {
        return std::make_pair(operand_num, expression->GetId());
      }
    }
  }

  // Try a memory dereference in any operand's expression second.
  operand_num = 0;
  for (Operands::const_iterator j = instruction.GetFirstOperand(),
                                jend = instruction.GetLastOperand();
       j != jend; ++j, ++operand_num) {
    const Operand& operand = **j;
    for (Expressions::const_iterator i = operand.GetFirstExpression(),
                                     end = operand.GetLastExpression();
         i != end; ++i) {
      const Expression* expression = *i;
      if (expression->GetParent() &&
          expression->GetParent()->IsDereferenceOperator()) {
        return std::make_pair(operand_num,
                              expression->IsImmediate()
                                  ? expression->GetId()
                                  : expression->GetParent()->GetId());
      }
    }
  }

  // If we only have a single operand return that.
  if (instruction.GetOperandCount() == 1) {
    const Operand& operand = **instruction.GetFirstOperand();
    return std::make_pair(0, (*operand.GetFirstExpression())->GetId());
  }

  // Return any immediate expression we can find.
  operand_num = 0;
  for (Operands::const_iterator j = instruction.GetFirstOperand(),
                                jend = instruction.GetLastOperand();
       j != jend; ++j, ++operand_num) {
    const Operand& operand = **j;
    for (Expressions::const_iterator i = operand.GetFirstExpression(),
                                     end = operand.GetLastExpression();
         i != end; ++i) {
      const Expression* expression = *i;
      if (expression->IsImmediate())
        return std::make_pair(operand_num, expression->GetId());
    }
  }

  // Give up.
  return std::make_pair(-1, -1);
}
