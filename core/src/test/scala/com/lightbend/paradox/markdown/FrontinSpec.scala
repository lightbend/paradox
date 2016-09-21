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

import java.io._

class FrontinSpec extends MarkdownBaseSpec {
  val first = "first line"
  val second = "second line"

  val f1 = new File("f1.md")
  writeInFile(f1,
    """
  	|%s
  	|%s
  	""" format (first, second))

  val f2 = new File("f2.md")
  writeInFile(f2,
    """
  	|---
  	|---
    |%s
    |%s
  	""" format (first, second))

  val f3 = new File("f3.md")
  writeInFile(f3,
    """
  	|---
  	|out: index.html
  	|---
  	|%s
  	|%s
  	""" format (first, second))

  val f4 = new File("f4.md")
  writeInFile(f4,
    """
  	|---
  	|out: index.html
  	|---
  	""")

  "Frontin.apply()" should "return an empty header when no property are specified at the page level" in {
    Frontin(f1).header shouldEqual Map.empty[String, String]
    Frontin(f1).body shouldEqual
      prepare("""
        |%s
        |%s
        """ format (first, second))
    f1.delete()
  }

  it should "return an empty header when '---' are used, but without new properties inside" in {
    Frontin(f2).header shouldEqual Map.empty[String, String]
    Frontin(f2).body shouldEqual
      prepare("""
      	|%s
      	|%s
        """ format (first, second))
    f2.delete()
  }

  it should "return the corresponding properties at 'out' field instantiation but shouldn't change the content of the generated file" in {
    Frontin(f3).header shouldEqual Map("out" -> "index.html")
    Frontin(f3).body shouldEqual
      prepare("""
  	  	|%s
  	  	|%s
  	  	""" format (first, second))
    f3.delete()
  }

  it should "return the corresponding properties at 'out' filed instantiation, but can return an empty String as the body" in {
    Frontin(f4).header shouldEqual Map("out" -> "index.html")
    Frontin(f4).body shouldEqual
      prepare("""
  	  	""")
    f4.delete()
  }

  def writeInFile(file: java.io.File, content: String) = {
    val pw = new PrintWriter(file)
    pw.write(prepare(content))
    pw.close
  }
}