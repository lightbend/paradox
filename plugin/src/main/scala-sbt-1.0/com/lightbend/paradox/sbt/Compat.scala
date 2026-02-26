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
import sbt.internal.io.Source
import java.io.File
import java.net.URI

object Compat {
  def sourcesFor(dirs: Seq[File]): Seq[Source] = dirs.map(d => new Source(d, AllPassFilter, NothingFilter))

  def classpathToURLs(classpath: Seq[Attributed[_]], conv: xsbti.FileConverter): Array[java.net.URL] =
    Path.toURLs(classpath.asInstanceOf[Seq[Attributed[File]]].map(_.data)).toArray

  def apiUrlForLinkProperties(opt: Option[_]): Option[URI] =
    opt.asInstanceOf[Option[java.net.URL]].map(_.toURI)

  def browseUrlString(scmInfo: Option[ScmInfo]): Option[String] =
    scmInfo.flatMap(info => { val u = info.browseUrl; if (u.getHost == "github.com") Some(u.toExternalForm) else None })

  def licenseNamesToCommaSeparated(licenses: Seq[_]): String =
    licenses.map(_.asInstanceOf[(String, _)]._1).mkString(",")

  def mappingsToFiles(mappings: Seq[(File, String)], conv: xsbti.FileConverter): Seq[(File, String)] =
    mappings
}
