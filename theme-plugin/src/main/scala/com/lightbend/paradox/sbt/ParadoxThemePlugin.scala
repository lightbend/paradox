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
import sbt.Keys._
import com.typesafe.sbt.web.Import.{Assets, WebKeys}
import com.typesafe.sbt.web.SbtWeb
import org.webjars.WebJarAssetLocator.WEBJARS_PATH_PREFIX

/**
 * Common settings for themes.
 */
object ParadoxThemePlugin extends AutoPlugin {

  object ParadoxThemeKeys {
    val includeMinimalWebjars  = settingKey[Boolean]("Enable bundling of referenced webjar assets.")
    val referencedWebjarAssets = taskKey[Set[String]]("Paths for webjar assets referenced in the theme.")
  }

  val autoImport = ParadoxThemeKeys

  import autoImport._

  override def requires = SbtWeb

  override def projectSettings = minimalWebjarSettings ++ Seq(
    autoScalaLibrary := false,
    crossPaths       := false
  )

  /**
   * Directly include all webjar dependency assets referenced in the theme. Requires webjar dependencies to be marked
   * `provided` or similar.
   */
  def minimalWebjarSettings = inConfig(Assets)(
    Seq(
      includeMinimalWebjars  := true,
      referencedWebjarAssets := {
        // extract all webjar asset references in string template files
        val libReference = """\$page\.base\$(lib/.*)\"""".r
        val templates    = sources.value.filter(_.getName.endsWith(".st"))
        (templates flatMap { template =>
          libReference.findAllIn(IO.read(template)).matchData.flatMap(_.subgroups).toSeq
        }).toSet
      },
      WebKeys.exportedMappings ++= Def.taskDyn {
        if (includeMinimalWebjars.value) {
          val prefix  = SbtWeb.path(s"${WEBJARS_PATH_PREFIX}/${moduleName.value}/${version.value}/")
          val include = referencedWebjarAssets.value
          Def.task {
            (WebKeys.webModules / mappings).value flatMap {
              case (file, path) if include(path) => Some(file -> (prefix + path))
              case _                             => None
            }
          }
        } else Def.task(Seq.empty[(java.io.File, String)])
      }.value
    )
  )

}
