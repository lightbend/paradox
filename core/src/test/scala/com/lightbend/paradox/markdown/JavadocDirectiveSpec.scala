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

import org.scalatest._

class JavadocDirectiveSpec extends WordSpec with Matchers {
  import JavadocDirective._

  "The JavadocDirective" should {
    "correctly link to an inner JRE class" in {
      url(
        "java.util.concurrent.Flow.Subscriber",
        Url("https://docs.oracle.com/en/java/javase/11/docs/api/java.base/"),
        LinkStyleDirect
      ) should be(Url("https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/Flow.Subscriber.html"))
    }

    "correctly link to an inner Akka class" in {
      url(
        "akka.actor.testkit.typed.Effect.MessageAdapter",
        Url("https://doc.akka.io/japi/akka/current/"),
        LinkStyleDirect
      ) should be(Url("https://doc.akka.io/japi/akka/current/akka/actor/testkit/typed/Effect.MessageAdapter.html"))
    }
  }
}