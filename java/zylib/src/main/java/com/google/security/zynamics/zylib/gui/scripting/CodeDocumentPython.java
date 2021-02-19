// Copyright 2011-2021 Google LLC
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

package com.google.security.zynamics.zylib.gui.scripting;

import java.util.HashSet;

/**
 * Document style for the syntax highlighted Python interpreter.
 */
public class CodeDocumentPython extends SyntaxDocument {
  private static final long serialVersionUID = 3801352080461814771L;

  private static HashSet<String> keywords = new HashSet<String>();

  public CodeDocumentPython(final boolean addBraces) {
    super(addBraces);

    keywords.add("import");
    keywords.add("for");
    keywords.add("from");
    keywords.add("print");
    keywords.add("in");
    keywords.add("while");
    keywords.add("if");
    keywords.add("is");
    keywords.add("and");
    keywords.add("not");
    keywords.add("or");
    keywords.add("else");
    keywords.add("elif");
    keywords.add("lambda");
    keywords.add("continue");
    keywords.add("break");
    keywords.add("return");
    keywords.add("assert");
    keywords.add("class");
    keywords.add("def");
    keywords.add("del");
    keywords.add("except");
    keywords.add("exec");
    keywords.add("finally");
    keywords.add("global");
    keywords.add("pass");
    keywords.add("raise");
    keywords.add("try");
    keywords.add("yield");
  }

  @Override
  protected String getEndDelimiter() {
    return "\"\"\"";
  }

  @Override
  protected String getEscapeString(final String quoteDelimiter) {
    return "\\" + quoteDelimiter;
  }

  @Override
  protected String getSingleLineDelimiter() {
    return "#";
  }

  @Override
  protected String getStartDelimiter() {
    // TODO Auto-generated method stub
    return "\"\"\"";
  }

  @Override
  protected boolean isConstant(final String token) {
    return token.equals("True") || token.equals("False") || token.equals("None");
  }

  @Override
  protected boolean isKeyword(final String token) {
    return keywords.contains(token);
  }

  @Override
  protected boolean isQuoteDelimiter(final String character) {
    final String quoteDelimiters = "\"'";

    if (quoteDelimiters.indexOf(character) < 0) {
      return false;
    } else {
      return true;
    }
  }

  @Override
  protected boolean isType(final String token) {
    return false;
  }

  @Override
  public HashSet<String> getTabCompletionWords() {
    return keywords;
  }

  @Override
  public boolean isDelimiter(final String character) {
    final String operands = ";:{}()[]+-/%<=>!&|^~*,";

    if (Character.isWhitespace(character.charAt(0)) || (operands.indexOf(character) != -1)) {
      return true;
    } else {
      return false;
    }
  }

}
