/*
 * Copyright © 2015 - 2019 Lightbend, Inc. <http://www.lightbend.com>
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

import java.io.File

import scala.collection.immutable.Seq
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SnippetIndentationTest extends AnyFlatSpec with Matchers {

  "indentation" should "be flattened" in {
    val in =
      """|object IndentedExample {
         |  //#indented-example
         |    case object Dent
         |  //#indented-example
         |
         |  object EventMore {
         |    //#indented-example
         |    case object DoubleDent
         |    //#indented-example
         |  }
         |}
         |""".stripMargin
    extractToString(in, "indented-example") should be(
      """case object Dent
        |case object DoubleDent""".stripMargin
    )
  }

  it should "be removed" in {
    val in =
      """|//#indented
         |  case object WithIndentation
         |//#indented
         |""".stripMargin
    extractToString(in, "indented") should be(
      """case object WithIndentation""".stripMargin
    )
  }

  it should "keep less indented code" in {
    val in =
      """|  //#indented
         |  }
         |}
         |case object NoIndentation
         |//#indented
         |""".stripMargin
    extractToString(in, "indented") should be(
      """  }
        |}
        |case object NoIndentation""".stripMargin
    )
  }

  it should "cope with markers indented more than the text" in {
    val in =
      """|//#multi-indented-example
         |//#some-other-anchor
         |object AnotherIndentedExample {
         |  //#multi-indented-example
         |
         |  def notRendered(): Unit = {
         |  }
         |
         |//#multi-indented-example
         |  def rendered(): Unit = {
         |  }
         |  //#some-other-anchor
         |  //#multi-indented-example
         |
         |  def alsoNotRendered(): Unit = {
         |
         |  }
         |  //#multi-indented-example
         |}
         |//#multi-indented-example
         |
         |//#multi-indented-example
         |class AnotherClass
         |//#multi-indented-example
         |""".stripMargin
    extractToString(in, "multi-indented-example") should be(
      """object AnotherIndentedExample {
        |  def rendered(): Unit = {
        |  }
        |}
        |class AnotherClass""".stripMargin
    )
  }

  "imports" should "align with indentationPerSnippet" in {
    val in =
      """//#indentation
        |import static java.lang.System.out;
        |//#indentation
        |...
        |        //#indentation
        |
        |        for (int i = 0; i < 10; i++) {
        |            out.println(i);
        |        }
        |        //#indentation
      """.stripMargin
    extractToString(in, "indentation") should be(
      """import static java.lang.System.out;
        |
        |for (int i = 0; i < 10; i++) {
        |    out.println(i);
        |}""".stripMargin
    )
  }

  it should "align even without indentationPerSnippet" in {
    val in =
      """//#indentation
        |import static java.lang.System.out;
        |//#indentation
        |...
        |        //#indentation
        |
        |        for (int i = 0; i < 10; i++) {
        |            out.println(i);
        |        }
        |        //#indentation
      """.stripMargin
    extractToString(in, "indentation", indentationPerSnippet = false) should be(
      """import static java.lang.System.out;
        |
        |for (int i = 0; i < 10; i++) {
        |    out.println(i);
        |}""".stripMargin
    )
  }

  def extractToString(inString: String, label: String, indentationPerSnippet: Boolean = true): String = {
    val in = inString.split("\n").toList
    Snippet.extract(new File(""), in, Seq(label), true)
  }
}
