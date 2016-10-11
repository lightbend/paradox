/*
 * Copyright © 2015 - 2016 Lightbend, Inc. <http://www.lightbend.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lightbend.paradox.markdown

import org.parboiled.Parboiled
import org.pegdown.ast.RootNode
import org.pegdown.plugins.PegDownPlugins
import org.pegdown.{ Extensions, Parser, ParserWithDirectives }
import scala.concurrent.duration._

/**
 * A configured markdown parser.
 */
class Reader(parser: Parser) {

  def this(options: Int = Extensions.ALL ^ Extensions.HARDWRAPS /* disable hard wraps, see #31 */,
           directiveMarker: Char = ParserWithDirectives.DEFAULT_DIRECTIVE_MARKER,
           maxParsingTime: Duration = 2.seconds,
           parseRunnerProvider: Parser.ParseRunnerProvider = Parser.DefaultParseRunnerProvider,
           plugins: PegDownPlugins = PegDownPlugins.NONE) =
    this(Parboiled.createParser[ParserWithDirectives, AnyRef](
      classOf[ParserWithDirectives],
      directiveMarker: java.lang.Character,
      options: java.lang.Integer,
      maxParsingTime.toMillis: java.lang.Long,
      parseRunnerProvider,
      plugins))

  /**
   * Parse markdown text into a pegdown AST.
   */
  def read(text: String): RootNode = read(text.toArray)

  /**
   * Parse markdown text into a pegdown AST.
   */
  def read(text: Array[Char]): RootNode = parser.parse(prepare(text))

  /**
   * Add two trailing newlines to the text.
   */
  def prepare(text: Array[Char]): Array[Char] = text ++ Array('\n', '\n')

}
