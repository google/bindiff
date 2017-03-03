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

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_STRINGS_STRUTIL_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_STRINGS_STRUTIL_H_

#include <google/protobuf/stubs/strutil.h>

// Map names from the Protocol Buffers stubs into the global namespace.
namespace strings = ::google::protobuf::strings;  // NOLINT(build/namespaces)
using ::google::protobuf::Base64Escape;
using ::google::protobuf::Base64Unescape;
using ::google::protobuf::CEscape;
using ::google::protobuf::CEscapeAndAppend;
using ::google::protobuf::CalculateBase64EscapedLen;
using ::google::protobuf::DoubleToBuffer;
using ::google::protobuf::EncodeAsUTF8Char;
using ::google::protobuf::FastHex32ToBuffer;
using ::google::protobuf::FastHex64ToBuffer;
using ::google::protobuf::FastHexToBuffer;
using ::google::protobuf::FastInt32ToBufferLeft;
using ::google::protobuf::FastInt64ToBufferLeft;
using ::google::protobuf::FastIntToBuffer;
using ::google::protobuf::FastUInt32ToBufferLeft;
using ::google::protobuf::FastUInt64ToBufferLeft;
using ::google::protobuf::FloatToBuffer;
using ::google::protobuf::GlobalReplaceSubstring;
using ::google::protobuf::HasPrefixString;
using ::google::protobuf::HasSuffixString;
using ::google::protobuf::IsValidCodePoint;
using ::google::protobuf::Join;
using ::google::protobuf::JoinStrings;
using ::google::protobuf::LowerString;
using ::google::protobuf::ReplaceCharacters;
using ::google::protobuf::SimpleDtoa;
using ::google::protobuf::SimpleItoa;
using ::google::protobuf::Split;
using ::google::protobuf::SplitStringAllowEmpty;
using ::google::protobuf::SplitStringUsing;
using ::google::protobuf::StrAppend;
using ::google::protobuf::StrCat;
using ::google::protobuf::StringReplace;
using ::google::protobuf::StripPrefixString;
using ::google::protobuf::StripString;
using ::google::protobuf::StripSuffixString;
using ::google::protobuf::StripWhitespace;
using ::google::protobuf::ToHex;
using ::google::protobuf::ToUpper;
using ::google::protobuf::UTF8FirstLetterNumBytes;
using ::google::protobuf::UnescapeCEscapeSequences;
using ::google::protobuf::UnescapeCEscapeString;
using ::google::protobuf::UpperString;
using ::google::protobuf::WebSafeBase64Escape;
using ::google::protobuf::WebSafeBase64Unescape;
using ::google::protobuf::ascii_isalnum;
using ::google::protobuf::ascii_isdigit;
using ::google::protobuf::ascii_islower;
using ::google::protobuf::ascii_isspace;
using ::google::protobuf::ascii_isupper;
using ::google::protobuf::ascii_tolower;
using ::google::protobuf::ascii_toupper;
using ::google::protobuf::hex_digit_to_int;
using ::google::protobuf::safe_strto32;
using ::google::protobuf::safe_strto64;
using ::google::protobuf::safe_strtob;
using ::google::protobuf::safe_strtod;
using ::google::protobuf::safe_strtof;
using ::google::protobuf::safe_strtou32;
using ::google::protobuf::safe_strtou64;
using ::google::protobuf::strings::AlphaNum;
using ::google::protobuf::strto32;
using ::google::protobuf::strto32_adaptor;
using ::google::protobuf::strto64;
using ::google::protobuf::strtou32;
using ::google::protobuf::strtou32_adaptor;
using ::google::protobuf::strtou64;

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_STRINGS_STRUTIL_H_
