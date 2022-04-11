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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PagePropertiesSpec extends AnyFlatSpec with Matchers {
  def convertPath = Path.replaceSuffix(".md", ".html") _

  val propOut        = Map("out" -> "newIndex.html")
  val propNoOut      = Map.empty[String, String]
  val propOutInvalid = Map("out" -> "newIndex.foo")

  val outProperties        = new Page.Properties(propOut)
  val noOutProperties      = new Page.Properties(propNoOut)
  val outInvalidProperties = new Page.Properties(propOutInvalid)

  "Page.Properties.convertToTarget(convertPath)(\"index.md\")" should "create target file String according to 'out' field in properties" in {
    outProperties.convertToTarget(convertPath)("index.md") shouldEqual "newIndex.html"
  }

  it should "create default 'index.html' (just by replacing .md by .html) when no 'out' field is specified" in {
    noOutProperties.convertToTarget(convertPath)("index.md") shouldEqual "index.html"
  }

  it should "drop the 'out' field if it is invalid (not finishing by '.html')" in {
    outInvalidProperties.convertToTarget(convertPath)("index.md") shouldEqual "index.html"
  }
}
