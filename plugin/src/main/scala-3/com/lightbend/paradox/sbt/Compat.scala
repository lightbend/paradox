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

package com.lightbend.paradox.sbt

import sbt._
import java.io.File
import java.net.URI

object Compat {
  def apiUrlForLinkProperties(opt: Option[URI]): Option[URI] =
    opt

  def browseUrlString(scmInfo: Option[ScmInfo]): Option[String] =
    scmInfo.flatMap { info =>
      val u = info.browseUrl; if (u.getHost == "github.com") Some(u.toString) else None
    }

  def licenseNamesToCommaSeparated(licenses: Seq[sbt.librarymanagement.License]): String =
    licenses
      .map { l =>
        val s     = l.toString
        val start = s.indexOf('(') + 1
        if (start > 0) s.drop(start).takeWhile(_ != ',')
        else s
      }
      .mkString(",")

  def mappingsToFiles(mappings: Seq[(xsbti.HashedVirtualFileRef, String)])(using
      conv: xsbti.FileConverter
  ): Seq[(File, String)] =
    mappings.map { case (ref, path) => (sbtcompat.PluginCompat.toFile(ref), path) }
}
