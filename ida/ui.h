// Copyright 2011-2018 Google LLC. All Rights Reserved.
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

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_IDA_UI_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_IDA_UI_H_

#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <kernwin.hpp>                                          // NOLINT
#include <loader.hpp>                                           // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT

#include "third_party/absl/strings/string_view.h"

// Small RAII class that displays a wait message for long-running actions.
class WaitBox {
 public:
  enum Cancellable { kNoCancel, kCancellable };

  explicit WaitBox(absl::string_view message) : WaitBox(message, kNoCancel) {}
  WaitBox(absl::string_view message, Cancellable cancel_state);
  ~WaitBox();

  static bool IsCancelled();

  void ReplaceText(absl::string_view message) const;

 private:
  bool cancellable_;
};

// Base class using CRTP that makes working with UI actions easier.
// Example usage:
//   class MyAction : public ActionHandler<MyAction> {
//     int idaapi activate(action_activation_ctx*) override {
//       msg("Hello from MyAction!\n");
//       return false;  // Do not refresh
//     }
//   };
// Then in plugin init:
//   register_action(MyAction::MakeActionDesc("plugin:myaction", "Say Hello",
//                                            /*shortcut=*/"", /*tooltip=*/"",
//                                            /*icon=*/-1):
//   attach_action_to_menu("File/QuickStart", "plugin:myaction", SETMENU_APP);
//
template <typename T>
class ActionHandler : public action_handler_t {
 public:
  // Creates a new action description for this action handler class.
  static action_desc_t MakeActionDesc(const char* name, const char* label,
                                      const char* shortcut, const char* tooltip,
                                      int icon) {
    return ACTION_DESC_LITERAL(name, label, T::GetInstance(), shortcut, tooltip,
                               icon);
  }

 private:
  static T* GetInstance() {
    static auto* instance = new T();
    return instance;
  }

  action_state_t idaapi update(action_update_ctx_t* context) override {
    // Return a sensible default
    return AST_ENABLE_FOR_IDB;
  }
};

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_IDA_UI_H_
