import ProjectInfo.*

import just.semver.SemVer
import extras.scala.io.syntax.color._

ThisBuild / scalaVersion := props.ProjectScalaVersion
ThisBuild / organization := props.Org
ThisBuild / organizationName := "Kevin's Code"

ThisBuild / developers := List(
  Developer(
    props.GitHubUser,
    "Kevin Lee",
    "kevin.code@kevinlee.io",
    url(s"https://github.com/${props.GitHubUser}"),
  )
)

ThisBuild / homepage := Some(url(s"https://github.com/${props.GitHubUser}/${props.RepoName}"))
ThisBuild / scmInfo :=
  Some(
    ScmInfo(
      url(s"https://github.com/${props.GitHubUser}/${props.RepoName}"),
      s"git@github.com:${props.GitHubUser}/${props.RepoName}.git",
    )
  )

ThisBuild / licenses := props.licenses

ThisBuild / scalafixConfig := (
  if (scalaVersion.value.startsWith("3")) file(".scalafix-scala3.conf").some
  else file(".scalafix-scala2.conf").some
)

inThisBuild(
  List(
    scalaVersion := scalaVersion.value,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
  )
)

ThisBuild / scalafixDependencies += "com.github.xuwei-k" %% "scalafix-rules" % "0.6.20"

lazy val root = (project in file("."))
  .settings(
    name := props.RepoName
  )
  .settings(noPublish)

lazy val docs = (project in file("docs-gen-tmp/docs"))
  .enablePlugins(MdocPlugin, DocusaurPlugin)
  .settings(
    scalaVersion := props.DocsScalaVersion,
    cleanFiles += ((ThisBuild / baseDirectory).value / "generated-docs" / "docs"),
    name := "docs",
    mdocIn := file("docs/common"),
    mdocOut := file("generated-docs/docs"),
    libraryDependencies := removeScala3Incompatible(scalaVersion.value, libraryDependencies.value),
    mdocVariables := {
      implicit val logger: Logger = sLog.value

      val latestVersion = docsTools.getTheLatestTaggedVersion(logger.error(_))
      docsTools.createMdocVariables(latestVersion)
    },
    mdoc := {
      implicit val logger: Logger = sLog.value

      val latestVersion = docsTools.getTheLatestTaggedVersion(logger.error(_))

      val envVarCi = sys.env.get("CI")
      val ciResult = s"""sys.env.get("CI")=${envVarCi}"""
      envVarCi match {
        case Some("true") =>
          logger.info(
            s">> ${ciResult.yellow} so ${"run".green} `${"writeLatestVersion".blue}` and `${"writeVersionsArchived".blue}`."
          )
          val websiteDir = docusaurDir.value
          docsTools.writeLatestVersion(websiteDir, latestVersion)
          docsTools.writeVersionsArchived(websiteDir, latestVersion)(logger)
        case Some(_) | None =>
          logger.info(
            s">> ${ciResult.yellow} so it will ${"not run".red} `${"writeLatestVersion".cyan}` and `${"writeVersionsArchived".cyan}`."
          )
      }
      mdoc.evaluated
    },
    docusaurDir := (ThisBuild / baseDirectory).value / "website",
    docusaurBuildDir := docusaurDir.value / "build",
  )
  .settings(noPublish)

lazy val docsExtrasCore = docsProject("docs-extras-core", file("docs-gen-tmp/extras-core"))
  .settings(
    scalaVersion := props.DocsScalaVersion,
    mdocIn := file("docs/extras-core"),
    mdocOut := file("generated-docs/docs/extras-core"),
    cleanFiles += ((ThisBuild / baseDirectory).value / "generated-docs" / "docs" / "extras-core"),
    libraryDependencies := removeScala3Incompatible(scalaVersion.value, libraryDependencies.value),
    libraryDependencies ++= {
      val latestVersion = docsTools.getTheLatestTaggedVersion(println(_))
      List(
        "io.kevinlee" %% "extras-core" % latestVersion
      )
    } ++ List(libs.hedgehogCore.value, libs.hedgehogRunner.value),
    mdocVariables := {
      implicit val logger: Logger = sLog.value

      val latestVersion = docsTools.getTheLatestTaggedVersion(logger.error(_))
      docsTools.createMdocVariables(latestVersion)
    },
  )
  .settings(noPublish)

lazy val docsExtrasRender = docsProject("docs-extras-render", file("docs-gen-tmp/extras-render"))
  .settings(
    scalaVersion := props.DocsScalaVersion,
    mdocIn := file("docs/extras-render"),
    mdocOut := file("generated-docs/docs/extras-render"),
    cleanFiles += ((ThisBuild / baseDirectory).value / "generated-docs" / "docs" / "extras-render"),
    libraryDependencies := removeScala3Incompatible(scalaVersion.value, libraryDependencies.value),
    libraryDependencies ++= List(
      libs.catsEffect.value
    ) ++ {
      val latestVersion = docsTools.getTheLatestTaggedVersion(println(_))
      List(
        "io.kevinlee" %% "extras-render" % latestVersion
      )
    } ++ List(libs.hedgehogCore.value, libs.hedgehogRunner.value),
    mdocVariables := {
      implicit val logger: Logger = sLog.value

      val latestVersion = docsTools.getTheLatestTaggedVersion(logger.error(_))
      docsTools.createMdocVariables(latestVersion)
    },
  )
  .settings(noPublish)

lazy val docsExtrasCats = docsProject("docs-extras-cats", file("docs-gen-tmp/extras-cats"))
  .enablePlugins(MdocPlugin)
  .settings(
    scalaVersion := props.DocsScalaVersion,
    mdocIn := file("docs/extras-cats"),
    mdocOut := file("generated-docs/docs/extras-cats"),
    cleanFiles += ((ThisBuild / baseDirectory).value / "generated-docs" / "docs" / "extras-cats"),
    libraryDependencies := removeScala3Incompatible(scalaVersion.value, libraryDependencies.value),
    libraryDependencies ++= List(
      libs.catsEffect.value
    ) ++ {
      val latestVersion = docsTools.getTheLatestTaggedVersion(println(_))
      List(
        "io.kevinlee" %% "extras-cats" % latestVersion
      )
    } ++ List(libs.hedgehogCore.value, libs.hedgehogRunner.value),
    mdocVariables := {
      implicit val logger: Logger = sLog.value

      val latestVersion = docsTools.getTheLatestTaggedVersion(logger.error(_))
      docsTools.createMdocVariables(latestVersion)
    },
  )
  .settings(noPublish)

lazy val docsExtrasConcurrent = docsProject("docs-extras-concurrent", file("docs-gen-tmp/extras-concurrent"))
  .enablePlugins(MdocPlugin)
  .settings(
    scalaVersion := props.DocsScalaVersion,
    mdocIn := file("docs/extras-concurrent"),
    mdocOut := file("generated-docs/docs/extras-concurrent"),
    cleanFiles += ((ThisBuild / baseDirectory).value / "generated-docs" / "docs" / "extras-concurrent"),
    libraryDependencies := removeScala3Incompatible(scalaVersion.value, libraryDependencies.value),
    libraryDependencies ++= {
      val latestVersion = docsTools.getTheLatestTaggedVersion(println(_))
      List(
        "io.kevinlee" %% "extras-concurrent" % latestVersion
      )
    } ++ List(libs.hedgehogCore.value, libs.hedgehogRunner.value),
    mdocVariables := {
      implicit val logger: Logger = sLog.value

      val latestVersion = docsTools.getTheLatestTaggedVersion(logger.error(_))
      docsTools.createMdocVariables(latestVersion)
    },
  )
  .settings(noPublish)

lazy val docsExtrasDoobieTools = docsProject("docs-extras-doobie-tools", file("docs-gen-tmp/extras-doobie-tools"))
  .enablePlugins(MdocPlugin)
  .settings(
    scalaVersion := props.DocsScalaVersion,
    mdocIn := file("docs/extras-doobie-tools/common"),
    mdocOut := file("generated-docs/docs/extras-doobie-tools"),
    cleanFiles += ((ThisBuild / baseDirectory).value / "generated-docs" / "docs" / "extras-doobie-tools"),
    libraryDependencies := removeScala3Incompatible(scalaVersion.value, libraryDependencies.value),
    mdocVariables := {
      implicit val logger: Logger = sLog.value

      val latestVersion = docsTools.getTheLatestTaggedVersion(logger.error(_))
      docsTools.createMdocVariables(latestVersion)
    },
  )
  .settings(noPublish)

lazy val docsExtrasDoobieToolsCe2 =
  docsProject("docs-extras-doobie-tools-ce2", file("docs-gen-tmp/extras-doobie-tools-ce2"))
    .enablePlugins(MdocPlugin)
    .settings(
      scalaVersion := props.DocsScalaVersion,
      mdocIn := file("docs/extras-doobie-tools/ce2"),
      mdocOut := file("generated-docs/docs/extras-doobie-tools/ce2"),
      cleanFiles += ((ThisBuild / baseDirectory).value / "generated-docs" / "docs" / "extras-doobie-tools" / "ce2"),
      libraryDependencies := removeScala3Incompatible(scalaVersion.value, libraryDependencies.value),
      libraryDependencies ++= {
        val latestVersion = docsTools.getTheLatestTaggedVersion(println(_))
        List(
          "io.kevinlee" %% "extras-doobie-tools-ce2" % latestVersion,
          libs.doobieCe2Core,
          libs.embeddedPostgres,
          libs.effectieCe2.value,
        )
      } ++ List(libs.hedgehogCore.value, libs.hedgehogRunner.value),
      mdocVariables := {
        implicit val logger: Logger = sLog.value

        val latestVersion = docsTools.getTheLatestTaggedVersion(logger.error(_))
        docsTools.createMdocVariables(latestVersion)
      },
    )
    .settings(noPublish)

lazy val docsExtrasDoobieToolsCe3 =
  docsProject("docs-extras-doobie-tools-ce3", file("docs-gen-tmp/extras-doobie-tools-ce3"))
    .enablePlugins(MdocPlugin)
    .settings(
      scalaVersion := props.DocsScalaVersion,
      mdocIn := file("docs/extras-doobie-tools/ce3"),
      mdocOut := file("generated-docs/docs/extras-doobie-tools/ce3"),
      cleanFiles += ((ThisBuild / baseDirectory).value / "generated-docs" / "docs" / "extras-doobie-tools" / "ce3"),
      libraryDependencies := removeScala3Incompatible(scalaVersion.value, libraryDependencies.value),
      libraryDependencies ++= {
        val latestVersion = docsTools.getTheLatestTaggedVersion(println(_))
        List(
          "io.kevinlee" %% "extras-doobie-tools-ce3" % latestVersion,
          libs.doobieCe3Core,
          libs.embeddedPostgres,
          libs.effectieCe3.value,
        )
      } ++ List(libs.hedgehogCore.value, libs.hedgehogRunner.value),
      dependencyOverrides ++= List(libs.doobieCe3Core),
      mdocVariables := {
        implicit val logger: Logger = sLog.value

        val latestVersion = docsTools.getTheLatestTaggedVersion(logger.error(_))
        docsTools.createMdocVariables(latestVersion)
      },
    )
    .settings(noPublish)

lazy val docsExtrasHedgehog = docsProject("docs-extras-hedgehog", file("docs-gen-tmp/extras-hedgehog"))
  .enablePlugins(MdocPlugin)
  .settings(
    scalaVersion := props.DocsScalaVersion,
    mdocIn := file("docs/extras-hedgehog"),
    mdocOut := file("generated-docs/docs/extras-hedgehog"),
    cleanFiles += ((ThisBuild / baseDirectory).value / "generated-docs" / "docs" / "extras-hedgehog"),
    libraryDependencies := removeScala3Incompatible(scalaVersion.value, libraryDependencies.value),
    libraryDependencies ++= {
      val latestVersion   = docsTools.getTheLatestTaggedVersion(println(_))
      List(
        "io.kevinlee" %% "extras-hedgehog-ce3"   % latestVersion,
        "io.kevinlee" %% "extras-hedgehog-circe" % latestVersion,
      )
    } ++ List(
      libs.hedgehogCore.value,
      libs.hedgehogRunner.value,
      libs.circeGeneric.value,
    ),
    mdocVariables := {
      implicit val logger: Logger = sLog.value

      val latestVersion = docsTools.getTheLatestTaggedVersion(logger.error(_))
      docsTools.createMdocVariables(latestVersion)
    },
  )
  .settings(noPublish)

lazy val docsExtrasRefinement = docsProject("docs-extras-refinement", file("docs-gen-tmp/extras-refinement"))
  .enablePlugins(MdocPlugin)
  .settings(
    scalaVersion := props.DocsScalaVersion,
    mdocIn := file("docs/extras-refinement"),
    mdocOut := file("generated-docs/docs/extras-refinement"),
    cleanFiles += ((ThisBuild / baseDirectory).value / "generated-docs" / "docs" / "extras-refinement"),
    libraryDependencies := removeScala3Incompatible(scalaVersion.value, libraryDependencies.value),
    libraryDependencies ++= {
      val latestVersion = docsTools.getTheLatestTaggedVersion(println(_))
      List(
        "io.kevinlee" %% "extras-refinement" % latestVersion,
        libs.newtype,
      ) ++ List(libs.cats.value, libs.refined.value.excludeAll("org.scala-lang.modules" %% "scala-xml"))
    } ++ List(libs.hedgehogCore.value, libs.hedgehogRunner.value),
    libraryDependencies := removeScala3Incompatible(scalaVersion.value, libraryDependencies.value),
    mdocVariables := {
      implicit val logger: Logger = sLog.value

      val latestVersion = docsTools.getTheLatestTaggedVersion(logger.error(_))
      docsTools.createMdocVariables(latestVersion)
    },
  )
  .settings(noPublish)

lazy val docsExtrasReflects = docsProject("docs-extras-reflects", file("docs-gen-tmp/extras-reflects"))
  .enablePlugins(MdocPlugin)
  .settings(
    scalaVersion := props.DocsScalaVersion,
    mdocIn := file("docs/extras-reflects"),
    mdocOut := file("generated-docs/docs/extras-reflects"),
    cleanFiles += ((ThisBuild / baseDirectory).value / "generated-docs" / "docs" / "extras-reflects"),
    libraryDependencies := removeScala3Incompatible(scalaVersion.value, libraryDependencies.value),
    libraryDependencies ++= {
      val latestVersion = docsTools.getTheLatestTaggedVersion(println(_))
      List(
        libs.newtype,
        "io.kevinlee" %% "extras-reflects" % latestVersion,
      )
    } ++ List(libs.hedgehogCore.value, libs.hedgehogRunner.value),
    libraryDependencies := (if (isScala3(scalaVersion.value)) List.empty[ModuleID] else libraryDependencies.value),
    mdocVariables := {
      implicit val logger: Logger = sLog.value

      val latestVersion = docsTools.getTheLatestTaggedVersion(logger.error(_))
      docsTools.createMdocVariables(latestVersion)
    },
  )
  .settings(noPublish)

lazy val docsExtrasScalaIo = docsProject("docs-extras-scala-io", file("docs-gen-tmp/extras-scala-io"))
  .enablePlugins(MdocPlugin)
  .settings(
    scalaVersion := props.DocsScalaVersion,
    mdocIn := file("docs/extras-scala-io"),
    mdocOut := file("generated-docs/docs/extras-scala-io"),
    cleanFiles += ((ThisBuild / baseDirectory).value / "generated-docs" / "docs" / "extras-scala-io"),
    libraryDependencies := removeScala3Incompatible(scalaVersion.value, libraryDependencies.value),
    libraryDependencies ++= {
      val latestVersion = docsTools.getTheLatestTaggedVersion(println(_))
      List(
        "io.kevinlee" %% "extras-scala-io" % latestVersion
      )
    } ++ List(libs.hedgehogCore.value, libs.hedgehogRunner.value),
    mdocVariables := {
      implicit val logger: Logger = sLog.value

      val latestVersion = docsTools.getTheLatestTaggedVersion(logger.error(_))
      docsTools.createMdocVariables(latestVersion)
    },
  )
  .settings(noPublish)

lazy val docsExtrasString = docsProject("docs-extras-string", file("docs-gen-tmp/extras-string"))
  .enablePlugins(MdocPlugin)
  .settings(
    scalaVersion := props.DocsScalaVersion,
    mdocIn := file("docs/extras-string"),
    mdocOut := file("generated-docs/docs/extras-string"),
    cleanFiles += ((ThisBuild / baseDirectory).value / "generated-docs" / "docs" / "extras-string"),
    libraryDependencies := removeScala3Incompatible(scalaVersion.value, libraryDependencies.value),
    libraryDependencies ++= {
      val latestVersion = docsTools.getTheLatestTaggedVersion(println(_))
      List(
        "io.kevinlee" %% "extras-string" % latestVersion
      )
    } ++ List(libs.hedgehogCore.value, libs.hedgehogRunner.value),
    mdocVariables := {
      implicit val logger: Logger = sLog.value

      val latestVersion = docsTools.getTheLatestTaggedVersion(logger.error(_))
      docsTools.createMdocVariables(latestVersion)
    },
  )
  .settings(noPublish)

lazy val docsExtrasTypeInfo = docsProject("docs-extras-type-info", file("docs-gen-tmp/extras-type-info"))
  .enablePlugins(MdocPlugin)
  .settings(
    scalaVersion := props.DocsScalaVersion,
    mdocIn := file("docs/extras-type-info"),
    mdocOut := file("generated-docs/docs/extras-type-info"),
    cleanFiles += ((ThisBuild / baseDirectory).value / "generated-docs" / "docs" / "extras-type-info"),
    libraryDependencies := removeScala3Incompatible(scalaVersion.value, libraryDependencies.value),
    libraryDependencies ++= {
      val latestVersion = docsTools.getTheLatestTaggedVersion(println(_))
      List(
        "io.kevinlee" %% "extras-type-info" % latestVersion
      )
    } ++ List(libs.hedgehogCore.value, libs.hedgehogRunner.value),
    mdocVariables := {
      implicit val logger: Logger = sLog.value

      val latestVersion = docsTools.getTheLatestTaggedVersion(logger.error(_))
      docsTools.createMdocVariables(latestVersion)
    },
  )
  .settings(noPublish)

lazy val docsExtrasTypeInfoScala2 =
  docsProject("docs-extras-type-info-scala2", file("docs-gen-tmp/extras-type-info-scala2"))
    .enablePlugins(MdocPlugin)
    .settings(
      scalaVersion := props.DocsScalaVersion,
      mdocIn := file("docs/extras-type-info-scala2"),
      mdocOut := file("generated-docs/docs/extras-type-info/scala2"),
      cleanFiles += ((ThisBuild / baseDirectory).value / "generated-docs" / "docs" / "extras-type-info/scala2"),
      libraryDependencies := removeScala3Incompatible(scalaVersion.value, libraryDependencies.value),
      libraryDependencies ++= {
        val latestVersion = docsTools.getTheLatestTaggedVersion(println(_))
        List(
          "io.kevinlee" %% "extras-type-info" % latestVersion
        )
      } ++ List(
        libs.newtype,
        libs.refined.value.excludeAll("org.scala-lang.modules" %% "scala-xml"),
        libs.hedgehogCore.value,
        libs.hedgehogRunner.value,
      ),
      mdocVariables := {
        implicit val logger: Logger = sLog.value

        val latestVersion = docsTools.getTheLatestTaggedVersion(logger.error(_))
        docsTools.createMdocVariables(latestVersion)
      },
    )
    .settings(noPublish)

lazy val docsExtrasTypeInfoScala3 =
  docsProject("docs-extras-type-info-scala3", file("docs-gen-tmp/extras-type-info-scala3"))
    .enablePlugins(MdocPlugin)
    .settings(
      scalaVersion := props.DocsScalaVersion,
      scalaVersion := "3.3.6",
      mdocIn := file("docs/extras-type-info-scala3"),
      mdocOut := file("generated-docs/docs/extras-type-info/scala3"),
      cleanFiles += ((ThisBuild / baseDirectory).value / "generated-docs" / "docs" / "extras-type-info/scala3"),
      libraryDependencies := removeScala3Incompatible(scalaVersion.value, libraryDependencies.value),
      libraryDependencies ++= {
        val latestVersion = docsTools.getTheLatestTaggedVersion(println(_))
        List(
          "io.kevinlee" %% "extras-type-info" % latestVersion
        )
      } ++ List(libs.hedgehogCore.value, libs.hedgehogRunner.value),
      mdocVariables := {
        implicit val logger: Logger = sLog.value

        val latestVersion = docsTools.getTheLatestTaggedVersion(logger.error(_))
        docsTools.createMdocVariables(latestVersion)
      },
    )
    .settings(noPublish)

lazy val docsExtrasCirce = docsProject("docs-extras-circe", file("docs-gen-tmp/extras-circe"))
  .enablePlugins(MdocPlugin)
  .settings(
    scalaVersion := props.DocsScalaVersion,
    mdocIn := file("docs/extras-circe"),
    mdocOut := file("generated-docs/docs/extras-circe"),
    cleanFiles += ((ThisBuild / baseDirectory).value / "generated-docs" / "docs" / "extras-circe"),
    libraryDependencies := removeScala3Incompatible(scalaVersion.value, libraryDependencies.value),
    libraryDependencies ++= {
      val latestVersion = docsTools.getTheLatestTaggedVersion(println(_))
      List(
        "io.kevinlee" %% "extras-circe" % latestVersion,
        libs.circeCore.value,
        libs.circeParser.value,
        libs.circeGeneric.value,
        libs.circeLiteral.value,
      )
    } ++ List(libs.hedgehogCore.value, libs.hedgehogRunner.value),
    mdocVariables := {
      implicit val logger: Logger = sLog.value

      val latestVersion = docsTools.getTheLatestTaggedVersion(logger.error(_))
      docsTools.createMdocVariables(latestVersion)
    },
  )
  .settings(noPublish)

lazy val docsExtrasFs2 = docsProject("docs-extras-fs2", file("docs-gen-tmp/extras-fs2"))
  .enablePlugins(MdocPlugin)
  .settings(
    scalaVersion := props.DocsScalaVersion,
    mdocIn := file("docs/extras-fs2/common"),
    mdocOut := file("generated-docs/docs/extras-fs2"),
    cleanFiles += ((ThisBuild / baseDirectory).value / "generated-docs" / "docs" / "extras-fs2"),
    libraryDependencies := removeScala3Incompatible(scalaVersion.value, libraryDependencies.value),
    libraryDependencies ++= List(libs.hedgehogCore.value, libs.hedgehogRunner.value),
    mdocVariables := {
      implicit val logger: Logger = sLog.value

      val latestVersion = docsTools.getTheLatestTaggedVersion(logger.error(_))
      docsTools.createMdocVariables(latestVersion)
    },
  )
  .settings(noPublish)

lazy val docsExtrasFs2V2 = docsProject("docs-extras-fs2-v2", file("docs-gen-tmp/extras-fs2/v2"))
  .enablePlugins(MdocPlugin)
  .settings(
    scalaVersion := props.DocsScalaVersion,
    mdocIn := file("docs/extras-fs2/v2"),
    mdocOut := file("generated-docs/docs/extras-fs2/v2"),
    cleanFiles += ((ThisBuild / baseDirectory).value / "generated-docs" / "docs" / "extras-fs2" / "v2"),
    libraryDependencies := removeScala3Incompatible(scalaVersion.value, libraryDependencies.value),
    libraryDependencies ++= {
      val latestVersion = docsTools.getTheLatestTaggedVersion(println(_))
      List(
        "io.kevinlee" %% "extras-fs2-v2-text" % latestVersion,
        libs.http4sServerDsl_0_22,
      )
    },
    mdocVariables := {
      implicit val logger: Logger = sLog.value

      val latestVersion = docsTools.getTheLatestTaggedVersion(logger.error(_))
      docsTools.createMdocVariables(latestVersion)
    },
  )
  .settings(noPublish)

lazy val docsExtrasFs2V3 = docsProject("docs-extras-fs2-v3", file("docs-gen-tmp/extras-fs2/v3"))
  .enablePlugins(MdocPlugin)
  .settings(
    scalaVersion := props.DocsScalaVersion,
    mdocIn := file("docs/extras-fs2/v3"),
    mdocOut := file("generated-docs/docs/extras-fs2/v3"),
    cleanFiles += ((ThisBuild / baseDirectory).value / "generated-docs" / "docs" / "extras-fs2" / "v3"),
    libraryDependencies := removeScala3Incompatible(scalaVersion.value, libraryDependencies.value),
    libraryDependencies ++= {
      val latestVersion = docsTools.getTheLatestTaggedVersion(println(_))
      List(
        "io.kevinlee" %% "extras-fs2-v3-text" % latestVersion,
        libs.http4sServerDsl_0_23.value,
      )
    } ++ List(libs.hedgehogCore.value, libs.hedgehogRunner.value),
    mdocVariables := {
      implicit val logger: Logger = sLog.value

      val latestVersion = docsTools.getTheLatestTaggedVersion(logger.error(_))
      docsTools.createMdocVariables(latestVersion)
    },
  )
  .settings(noPublish)

lazy val docsExtrasTestingTools = docsProject("docs-extras-testing-tools", file("docs-gen-tmp/extras-testing-tools"))
  .enablePlugins(MdocPlugin)
  .settings(
    scalaVersion := props.DocsScalaVersion,
    mdocIn := file("docs/extras-testing-tools/common"),
    mdocOut := file("generated-docs/docs/extras-testing-tools"),
    cleanFiles += ((ThisBuild / baseDirectory).value / "generated-docs" / "docs" / "extras-testing-tools"),
    libraryDependencies := removeScala3Incompatible(scalaVersion.value, libraryDependencies.value),
    libraryDependencies ++= {
      val latestVersion = docsTools.getTheLatestTaggedVersion(println(_))
      List(
        "io.kevinlee" %% "extras-testing-tools" % latestVersion,
        libs.newtype,
        libs.cats.value,
        libs.refined.value.excludeAll("org.scala-lang.modules" %% "scala-xml"),
        libs.refinedCats.value,
      )
    } ++ List(libs.hedgehogCore.value, libs.hedgehogRunner.value),
    mdocVariables := {
      implicit val logger: Logger = sLog.value

      val latestVersion = docsTools.getTheLatestTaggedVersion(logger.error(_))
      docsTools.createMdocVariables(latestVersion)
    },
  )
  .settings(noPublish)

lazy val docsExtrasTestingToolsCats =
  docsProject("docs-extras-testing-tools-cats", file("docs-gen-tmp/extras-testing-tools-cats"))
    .enablePlugins(MdocPlugin)
    .settings(
      scalaVersion := props.DocsScalaVersion,
      mdocIn := file("docs/extras-testing-tools/cats"),
      mdocOut := file("generated-docs/docs/extras-testing-tools/cats"),
      cleanFiles += ((ThisBuild / baseDirectory).value / "generated-docs" / "docs" / "extras-testing-tools" / "cats"),
      libraryDependencies := removeScala3Incompatible(scalaVersion.value, libraryDependencies.value),
      libraryDependencies ++= {
        val latestVersion = docsTools.getTheLatestTaggedVersion(println(_))
        List(
          "io.kevinlee" %% "extras-testing-tools-cats" % latestVersion,
          libs.newtype,
          libs.cats.value,
          libs.catsEffect.value,
          libs.refined.value.excludeAll("org.scala-lang.modules" %% "scala-xml"),
          libs.refinedCats.value,
        )
      } ++ List(libs.hedgehogCore.value, libs.hedgehogRunner.value),
      mdocVariables := {
        implicit val logger: Logger = sLog.value

        val latestVersion = docsTools.getTheLatestTaggedVersion(logger.error(_))
        docsTools.createMdocVariables(latestVersion)
      },
    )
    .settings(noPublish)

lazy val docsExtrasTestingToolsEffectie =
  docsProject("docs-extras-testing-tools-effectie", file("docs-gen-tmp/extras-testing-tools-effectie"))
    .enablePlugins(MdocPlugin)
    .settings(
      scalaVersion := props.DocsScalaVersion,
      mdocIn := file("docs/extras-testing-tools/effectie"),
      mdocOut := file("generated-docs/docs/extras-testing-tools/effectie"),
      cleanFiles += ((ThisBuild / baseDirectory).value / "generated-docs" / "docs" / "extras-testing-tools" / "effectie"),
      libraryDependencies := removeScala3Incompatible(scalaVersion.value, libraryDependencies.value),
      libraryDependencies ++= {
        val latestVersion = docsTools.getTheLatestTaggedVersion(println(_))
        List(
          "io.kevinlee" %% "extras-testing-tools-effectie" % latestVersion,
          libs.newtype,
          libs.cats.value,
          libs.catsEffect.value,
          libs.refined.value.excludeAll("org.scala-lang.modules" %% "scala-xml"),
          libs.refinedCats.value,
          libs.effectieCe2.value,
        )
      } ++ List(libs.hedgehogCore.value, libs.hedgehogRunner.value),
      mdocVariables := {
        implicit val logger: Logger = sLog.value

        val latestVersion = docsTools.getTheLatestTaggedVersion(logger.error(_))
        docsTools.createMdocVariables(latestVersion)
      },
    )
    .settings(noPublish)

// scalafmt: off

def prefixedProjectName(name: String) = s"${props.RepoName}${if (name.isEmpty) "" else s"-$name"}"
// scalafmt: on

def subProject(projectName: String): Project = {
  val prefixedName = prefixedProjectName(projectName)
  Project(projectName, file(s"modules/$prefixedName"))
    .settings(
      name := prefixedName,
      Test / fork := true,
      scalacOptions += "-explain",
      libraryDependencies ++= libs.hedgehog.value,
      testFrameworks ~=
        (frameworks => (TestFramework("hedgehog.sbt.Framework") +: frameworks).distinct),
      scalafixConfig := (
        if (scalaVersion.value.startsWith("3"))
          ((ThisBuild / baseDirectory).value / ".scalafix-scala3.conf").some
        else
          ((ThisBuild / baseDirectory).value / ".scalafix-scala2.conf").some
      ),
    )
}

def removeScala3Incompatible(scalaVersion: String, libraryDependencies: Seq[ModuleID]): Seq[ModuleID] =
  if (isScala3(scalaVersion)) {
    libraryDependencies.filterNot(props.isScala3Incompatible)
  } else {
    libraryDependencies
  }

addCommandAlias(
  "docsCleanAll",
  "docs/clean",
)
addCommandAlias(
  "docsMdocAll",
  "; docsExtrasCore/mdoc; docsExtrasRender/mdoc; docsExtrasCats/mdoc; docsExtrasCirce/mdoc; docsExtrasHedgehog/mdoc; docsExtrasDoobieTools/mdoc; docsExtrasDoobieToolsCe2/mdoc; docsExtrasDoobieToolsCe3/mdoc; docsExtrasRefinement/mdoc; docsExtrasTypeInfo/mdoc; docsExtrasTypeInfoScala2/mdoc; docsExtrasTypeInfoScala3/mdoc; docsExtrasScalaIo/mdoc; docsExtrasString/mdoc; docsExtrasFs2/mdoc; docsExtrasFs2V2/mdoc; docsExtrasFs2V3/mdoc; docsExtrasTestingTools/mdoc; docsExtrasTestingToolsCats/mdoc; docsExtrasTestingToolsEffectie/mdoc; docsExtrasConcurrent/mdoc; docsExtrasReflects/mdoc; docs/mdoc",
)

def easeScalacOptionsForDocs(scalacOptions: Seq[String]): Seq[String] =
  scalacOptions.filterNot(_ == "-Xfatal-warnings")

def kebabCaseToCamelCase(s: String): String               = s.split("-+").toList match {
  case head :: tail => (head :: tail.map(_.capitalize)).mkString
  case Nil => ""
}
def docsProject(projectName: String, path: File): Project =
  Project(kebabCaseToCamelCase(projectName), path)
    .enablePlugins(MdocPlugin)
    .settings(
      name := projectName,
      scalacOptions ~= (ops => "-Ymacro-annotations" +: easeScalacOptionsForDocs(ops)),
    )

def isScala3(scalaVersion: String): Boolean = scalaVersion.startsWith("3.")
