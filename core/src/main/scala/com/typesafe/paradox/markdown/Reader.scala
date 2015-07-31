/*
 * Copyright © 2015 Typesafe, Inc. <http://www.typesafe.com>
 */

package com.typesafe.paradox.markdown

import org.parboiled.Parboiled
import org.pegdown.ast.RootNode
import org.pegdown.plugins.PegDownPlugins
import org.pegdown.{ Extensions, Parser, ParserWithDirectives }
import scala.concurrent.duration._

/**
 * A configured markdown parser.
 */
class Reader(parser: Parser) {

  def this(options: Int = Extensions.ALL,
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
