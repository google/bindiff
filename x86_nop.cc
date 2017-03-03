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

#include "third_party/zynamics/binexport/x86_nop.h"

bool IsNopX86(const char* m, size_t size) {
  // Consume up to six prefix bytes:
  for (int i = 6; i > 0 && size > 0 && m[0] == 0x66; --i, --size, ++m) {
  }

  // Manual trie constructed from the NOP patterns:
  if (size >= 3 && m[0] == 0x0f && m[1] == 0x1f) {
    if (m[2] == 0x00) {
      return true;  // 0f 1f 00 nop [eax]
    }
    if (size >= 4) {
      if (m[2] == 0x40 && m[3] == 0x00) {
        return true;  // 0f 1f 40 00 nop [eax + 0]
      }
      if (size >= 5) {
        if (m[2] == 0x44 && m[3] == 0x00 && m[4] == 0x00) {
          return true;  // 0f 1f 44 00 00 nop [eax + eax * 1 + 0]
        }
        if (size >= 7) {
          if (m[2] == 0x80 && m[3] == 0x00 && m[4] == 0x00 && m[5] == 0x00 &&
              m[6] == 0x00) {
            return true;  // 0f 1f 80 00 00 00 00 nop [eax + 0]
          }
          if (size >= 8) {
            // 0f 1f 84 00 00 00 00 00 nop [eax + eax * 1 + 0]
            return m[2] == 0x84 && m[3] == 0x00 && m[4] == 0x00 &&
                   m[5] == 0x00 && m[6] == 0x00 && m[7] == 0x00;
          }
        }
      }
    }
  } else if (size >= 2 && m[0] == 0x89 && m[1] == 0xf6) {
    return true;  // 89 f6 mov esi, esi
  } else if (size >= 1 && m[0] == 0x8d) {
    if (size >= 3) {
      if (m[1] == 0x74) {
        if (m[2] == 0x00) {
          return true;  // 8d 74 00 lea esi, esi
        } else if (m[2] == 0x26) {
          if (size >= 4 && m[3] == 0x00) {
            return true;  // 8d 74 26 00 lea esi, [esi + eiz * 1 + 0]
          }
        }
      } else if (m[1] == 0x76 && m[2] == 0x00) {
        return true;  // 8d 76 00 lea esi, [esi + 0]
      }
      if (size >= 4) {
        if (m[1] == 0xb4) {
          if (m[2] == 0x00 && m[3] == 0x00) {
            return true;  // 8d b4 00 00 lea
          } else if (size >= 7 && m[2] == 0x26 && m[3] == 0x00 &&
                     m[4] == 0x00 && m[5] == 0x00 && m[6] == 0x00) {
            return true;  // 8d b4 26 00 00 00 00 lea
          }
        } else if (m[1] == 0xbd && m[2] == 0x00 && m[3] == 0x00) {
          return true;  // 8d bd 00 00 lea
        }
        if (size >= 6) {
          if (m[1] == 0xb6 && m[2] == 0x00 && m[3] == 0x00 && m[4] == 0x00 &&
              m[5] == 0x00) {
            return true;  // 8d b6 00 00 00 00 lea
          } else if (m[1] == 0xbf && m[2] == 0x00 && m[3] == 0x00 &&
                     m[4] == 0x00 && m[5] == 0x00) {
            return true;  // 8d bf 00 00 00 00 lea
          }
          if (size >= 7 && m[1] == 0xbc && m[2] == 0x27 && m[3] == 0x00 &&
              m[4] == 0x00 && m[5] == 0x00 && m[6] == 0x00) {
            return true;  // 8d bc 27 00 00 00 00 lea
          }
        }
      }
    }
  } else if (size >= 1 && m[0] == 0x90) {
    return true;  // 90 nop
  } else if (size >= 9 && m[0] == 0x2e && m[1] == 0x0f && m[2] == 0x1f &&
             m[3] == 0x84 && m[4] == 0x00 && m[5] == 0x00 && m[6] == 0x00 &&
             m[7] == 0x00 && m[8] == 0x00) {
    return true;  // 2e 0f 1f 84 00 00 00 00 00 nop [rax + rax * 1 + 0]
  }
  return false;
}

