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

class FiddleDirectiveSpec extends MarkdownBaseSpec {

  // #fiddle_code
  val sourcePath = new java.io.File(".").getAbsolutePath + "/tests/src/test/scala/"
  // #fiddle_code

  "Fiddle directive" should "generate fiddle iframe" in {
    markdownPages(
      sourcePath + "com/lightbend/paradox/markdown/FiddleDirectiveSpec.scala" -> """
    |@@fiddle [FiddleDirectiveSpec.scala](./FiddleDirectiveSpec.scala) { #fiddle_code extraParams=theme=light&layout=v75 cssStyle=width:100%; }
    """).values.head shouldEqual html("""
      |<iframe class="fiddle" src=
"https://embed.scalafiddle.io/embed?theme=light&amp;layout=v75&amp;source=import%20fiddle.Fiddle%2C%20Fiddle.println%0A%40scalajs.js.annotation.JSExport%0Aobject%20ScalaFiddle%20%7B%0A%20%20%2F%2F%20%24FiddleStart%0Aval%20sourcePath%20%3D%20new%20java.io.File%28%22.%22%29.getAbsolutePath%20%2B%20%22%2Ftests%2Fsrc%2Ftest%2Fscala%2F%22%0A%20%20%2F%2F%20%24FiddleEnd%0A%7D%0A"
 frameborder="0" style="width:100%;"></iframe>
    """.stripMargin)
  }

  it should "properly add width and height" in {
    markdownPages(
      sourcePath + "com/lightbend/paradox/markdown/FiddleDirectiveSpec.scala" -> """
      |@@fiddle [FiddleDirectiveSpec.scala](./FiddleDirectiveSpec.scala) { #fiddle_code width=100px height=100px extraParams=theme=light&layout=v75 cssStyle=width:100%; }
      """).values.head shouldEqual html("""
        |<iframe class="fiddle" width="100px" height="100px" src=
"https://embed.scalafiddle.io/embed?theme=light&amp;layout=v75&amp;source=import%20fiddle.Fiddle%2C%20Fiddle.println%0A%40scalajs.js.annotation.JSExport%0Aobject%20ScalaFiddle%20%7B%0A%20%20%2F%2F%20%24FiddleStart%0Aval%20sourcePath%20%3D%20new%20java.io.File%28%22.%22%29.getAbsolutePath%20%2B%20%22%2Ftests%2Fsrc%2Ftest%2Fscala%2F%22%0A%20%20%2F%2F%20%24FiddleEnd%0A%7D%0A"
 frameborder="0" style="width:100%;"></iframe>
      """.stripMargin)
  }

  it should "change base url" in {
    markdownPages(
      sourcePath + "com/lightbend/paradox/markdown/FiddleDirectiveSpec.scala" -> """
      |@@fiddle [FiddleDirectiveSpec.scala](./FiddleDirectiveSpec.scala) { #fiddle_code baseUrl=http://shadowscalafiddle.io width=100px height=100px extraParams=theme=light&layout=v75 cssStyle=width:100%; }
      """).values.head shouldEqual html("""
        |<iframe class="fiddle" width="100px" height="100px" src=
"http://shadowscalafiddle.io?theme=light&amp;layout=v75&amp;source=import%20fiddle.Fiddle%2C%20Fiddle.println%0A%40scalajs.js.annotation.JSExport%0Aobject%20ScalaFiddle%20%7B%0A%20%20%2F%2F%20%24FiddleStart%0Aval%20sourcePath%20%3D%20new%20java.io.File%28%22.%22%29.getAbsolutePath%20%2B%20%22%2Ftests%2Fsrc%2Ftest%2Fscala%2F%22%0A%20%20%2F%2F%20%24FiddleEnd%0A%7D%0A"
 frameborder="0" style="width:100%;"></iframe>
      """.stripMargin)
  }

}
