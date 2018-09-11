/*
 * Copyright © 2015 - 2017 Lightbend, Inc. <http://www.lightbend.com>
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

//#github-path-link
object GithubPathLink {
  //#github-neither-path-link
  type Neither[A, B] = Nothing
  //#github-neither-path-link
}
//#github-path-link

//#example
object example extends App {
  println("Hello, World!")
}
//#example

object IndentedExample {
  //#indented-example
  case object Dent
  //#indented-example

  object EventMore {
    //#indented-example
    case object DoubleDent
    //#indented-example
  }
}

//#multi-indented-example
//#some-other-anchor
object AnotherIndentedExample {
  //#multi-indented-example

  def notRendered(): Unit = {
  }

  //#multi-indented-example
  def rendered(): Unit = {
  }
  //#some-other-anchor
  //#multi-indented-example

  def alsoNotRendered(): Unit = {

  }
  //#multi-indented-example
}
//#multi-indented-example

//#multi-indented-example
class AnotherClass
//#multi-indented-example

// check empty line with indented blocks!
// format: OFF
  //#multi-indented-example

  //#multi-indented-example
// format: ON
