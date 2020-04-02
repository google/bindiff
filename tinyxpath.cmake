# Copyright 2011-2020 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# TinyXPath, used for the XmlConfig class
add_library(tinyxpath STATIC ${ThirdParty_DIR}/tinyxpath/action_store.cpp
                             ${ThirdParty_DIR}/tinyxpath/htmlutil.cpp
                             ${ThirdParty_DIR}/tinyxpath/lex_util.cpp
                             ${ThirdParty_DIR}/tinyxpath/main.cpp
                             ${ThirdParty_DIR}/tinyxpath/node_set.cpp
                             ${ThirdParty_DIR}/tinyxpath/tinystr.cpp
                             ${ThirdParty_DIR}/tinyxpath/tinyxml.cpp
                             ${ThirdParty_DIR}/tinyxpath/tinyxmlerror.cpp
                             ${ThirdParty_DIR}/tinyxpath/tinyxmlparser.cpp
                             ${ThirdParty_DIR}/tinyxpath/tokenlist.cpp
                             ${ThirdParty_DIR}/tinyxpath/xml_util.cpp
                             ${ThirdParty_DIR}/tinyxpath/xpath_expression.cpp
                             ${ThirdParty_DIR}/tinyxpath/xpath_processor.cpp
                             ${ThirdParty_DIR}/tinyxpath/xpath_stack.cpp
                             ${ThirdParty_DIR}/tinyxpath/xpath_static.cpp
                             ${ThirdParty_DIR}/tinyxpath/xpath_stream.cpp
                             ${ThirdParty_DIR}/tinyxpath/xpath_syntax.cpp)
if(UNIX)
  target_compile_options(tinyxpath PRIVATE
    -Wno-switch
    -Wno-logical-op-parentheses
  )
elseif(WIN32)
  target_compile_options(tinyxpath PRIVATE
    /wd4267  # conversion from 'size_t' to 'unsigned int'
  )
endif()
