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

#include "third_party/zynamics/binexport/ida/digest.h"

#include <openssl/md5.h>
#include <openssl/sha.h>

#include "base/integral_types.h"

string Md5(const string& data) {
  string digest(MD5_DIGEST_LENGTH, '\0');
  MD5(reinterpret_cast<const uint8*>(data.data()), data.size(),
      reinterpret_cast<uint8*>(&digest[0]));
  return digest;
}

string Sha1(const string& data) {
  string digest(SHA_DIGEST_LENGTH, '\0');
  SHA1(reinterpret_cast<const uint8*>(data.data()), data.size(),
       reinterpret_cast<uint8*>(&digest[0]));
  return digest;
}
