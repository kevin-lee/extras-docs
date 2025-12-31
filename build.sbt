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

lazy val docsTools = new {

  lazy val CmdRun = new {
    import sys.process._

    def runAndCapture(command: Seq[String]): (Int, String, String) = {
      val out      = new StringBuilder
      val err      = new StringBuilder
      val exitCode =
        Process(command).!(
          ProcessLogger(
            (o: String) => out.append(o).append('\n'),
            (e: String) => err.append(e).append('\n'),
          )
        )
      (exitCode, out.result().trim, err.result().trim)
    }

    def fail(prefix: String, step: String, command: Seq[String], out: String, err: String)(
      log: String => Unit
    ): Nothing = {
      val cmdString = command.mkString(" ")
      val details   =
        if (err.nonEmpty) err
        else if (out.nonEmpty) out
        else "(no output)"
      log(s">> [$prefix][$step] Command failed: `$cmdString`\n$details".red)
      throw new MessageOnlyException(s"$step failed: $cmdString\n$details")
    }
  }

  def getTheLatestTaggedVersion(logger: => String => Unit): String = {
    val (ghVersionExit, ghVersionOut, ghVersionErr) = CmdRun.runAndCapture(Seq("gh", "--version"))
    if (ghVersionExit != 0)
      CmdRun.fail(
        "getTheLatestTaggedVersion",
        "gh --version",
        Seq("gh", "--version"),
        ghVersionOut,
        ghVersionErr,
      )(logger)

    val (ghAuthExit, ghAuthOut, ghAuthErr) =
      CmdRun.runAndCapture(Seq("gh", "auth", "status", "-h", "github.com"))
    if (ghAuthExit != 0)
      CmdRun.fail(
        "getTheLatestTaggedVersion",
        "gh auth status",
        Seq("gh", "auth", "status", "-h", "github.com"),
        ghAuthOut,
        ghAuthErr,
      )(logger)

    val repo = s"${props.GitHubUser}/${props.CodeRepoName}"

    val tagNameCmd =
      Seq("gh", "release", "view", "-R", repo, "--json", "tagName", "-q", ".tagName")

    val (tagExit, tagOut, tagErr) = CmdRun.runAndCapture(tagNameCmd)
    if (tagExit != 0)
      CmdRun.fail("getTheLatestTaggedVersion", "gh release view", tagNameCmd, tagOut, tagErr)(logger)

    val tagName = tagOut.trim
    if (tagName.isEmpty)
      CmdRun.fail(
        "getTheLatestTaggedVersion",
        "gh release view (empty tagName)",
        tagNameCmd,
        tagOut,
        tagErr,
      )(logger)

    if (!tagName.startsWith("v")) {
      logger(s">> [getTheLatestTaggedVersion] Expected tagName to start with 'v' but got: $tagName".red)
      throw new MessageOnlyException(s"Expected tagName to start with 'v' but got: $tagName")
    }

    val versionWithoutV = tagName.stripPrefix("v")
    SemVer.parse(versionWithoutV) match {
      case Right(v) => v.render
      case Left(parseError) =>
        logger(s">> [getTheLatestTaggedVersion] Invalid SemVer from tagName ($tagName): ${parseError.toString}".red)
        throw new MessageOnlyException(s"Invalid SemVer from tagName ($tagName): ${parseError.toString}")
    }
  }

  def writeLatestVersion(websiteDir: File, latestVersion: String)(implicit logger: Logger): Unit = {
    val latestVersionFile = websiteDir / "latestVersion.json"
    val latestVersionJson = raw"""{"version":"$latestVersion"}"""

    val websiteDirRelativePath =
      s"${latestVersionFile.getParentFile.getParentFile.getName.cyan}/${latestVersionFile.getParentFile.getName.yellow}"
    logger.info(
      s""">> Writing ${"the latest version".blue} to $websiteDirRelativePath/${latestVersionFile.getName.green}.
         |>> Content: ${latestVersionJson.blue}
         |""".stripMargin
    )
    IO.write(latestVersionFile, latestVersionJson)
  }

  def writeVersionsArchived(websiteDir: File, latestVersion: String)(implicit logger: Logger): Unit = {
    import sys.process._

    val (ghVersionExit, ghVersionOut, ghVersionErr) = CmdRun.runAndCapture(Seq("gh", "--version"))
    if (ghVersionExit != 0)
      CmdRun.fail("writeVersionsArchived", "gh --version", Seq("gh", "--version"), ghVersionOut, ghVersionErr)(
        logger.error(_)
      )

    val (ghAuthExit, ghAuthOut, ghAuthErr) =
      CmdRun.runAndCapture(Seq("gh", "auth", "status", "-h", "github.com"))
    if (ghAuthExit != 0)
      CmdRun.fail(
        "writeVersionsArchived",
        "gh auth status",
        Seq("gh", "auth", "status", "-h", "github.com"),
        ghAuthOut,
        ghAuthErr,
      )(logger.error(_))

    val repo = s"${props.GitHubUser}/${props.CodeRepoName}"

    val ghTagsCmd =
      Seq(
        "gh",
        "api",
        "-H",
        "Accept: application/vnd.github+json",
        s"/repos/$repo/tags",
        "--paginate",
        "-q",
        ".[].name",
      )

    val (tagsExit, tagsOut, tagsErr) = CmdRun.runAndCapture(ghTagsCmd)
    if (tagsExit != 0)
      CmdRun.fail("writeVersionsArchived", "gh api tags", ghTagsCmd, tagsOut, tagsErr)(logger.error(_))

    val tags = tagsOut.trim
    if (tags.isEmpty)
      CmdRun.fail("writeVersionsArchived", "gh api tags (empty)", ghTagsCmd, tagsOut, tagsErr)(logger.error(_))

    val versions = tags
      .split("\n")
      .map(_.trim)
      .filter(t => t.nonEmpty && t.startsWith("v"))
      .map(_.stripPrefix("v"))
      .map(SemVer.parse)
      .collect { case Right(v) => v }
      .sorted(Ordering[SemVer].reverse)
      .map(_.render)
      .filter(_ != latestVersion)

    val versionsArchivedFile = websiteDir / "src" / "pages" / "versionsArchived.json"

    val versionsInJson = versions
      .map { v =>
        raw"""  {
             |    "name": "$v",
             |    "label": "$v"
             |  }""".stripMargin
      }
      .mkString("[\n", ",\n", "\n]")

    IO.write(versionsArchivedFile, versionsInJson)
  }

  def createMdocVariables(version: String): Map[String, String] = {
    val versionForDoc                  = version
    Map(
      "VERSION"                  -> versionForDoc,
      "SUPPORTED_SCALA_VERSIONS" -> {
        val versions = props
          .CrossScalaVersions
          .map(CrossVersion.binaryScalaVersion)
          .map(binVer => s"`$binVer`")
        if (versions.length > 1)
          s"${versions.init.mkString(", ")} and ${versions.last}"
        else
          versions.mkString
      },
    )
  }

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

lazy val props = new {

  private val GitHubRepo = findRepoOrgAndName

  val Org          = "io.kevinlee"
  val GitHubUser   = GitHubRepo.fold("kevin-lee")(_.orgToString)
  val RepoName     = GitHubRepo.fold("extras-docs")(_.nameToString)
  val CodeRepoName = RepoName.stripSuffix("-docs")

  val licenses = List("MIT" -> url("http://opensource.org/licenses/MIT"))

  val Scala2Versions = List(
    "2.13.16",
    "2.12.18",
  )
  val Scala2Version  = Scala2Versions.head

  val Scala3Versions = List("3.3.3")
  val Scala3Version  = Scala3Versions.head

  val ProjectScalaVersion = Scala2Version
//  val ProjectScalaVersion = Scala3Version

  val DocsScalaVersion = "2.13.18"

  val CrossScalaVersions =
    (Scala3Versions ++ Scala2Versions).distinct

  val CatsVersion      = "2.13.0"
  val Cats2_0_0Version = "2.0.0"

  val CatsEffect3Version = "3.6.3"
  val CatsEffectVersion  = "2.5.5"

  val CatsEffect3_7Version = "3.7.0-RC1"

  val DoobieCe2Version = "0.13.4"
  val DoobieCe3Version = "1.0.0-RC10"

  val KittensVersion = "3.5.0"

  val CirceVersion = "0.14.13"
//  val Circe_0_14_3_Version = "0.14.3"

  val Fs2V2Version = "2.5.11"
  val Fs2V3Version = "3.12.2"

  val Http4s_0_22_Version = "0.22.15"
  val Http4s_0_23_Version = "0.23.16"

  val HedgehogVersion = "0.13.0"

  val HedgehogExtraVersion = "0.19.0"

  val NewtypeVersion = "0.4.4"

  val RefinedVersion       = "0.9.27"
  val RefinedLatestVersion = "0.10.1"

  val EmbeddedPostgresVersion = "2.2.0"

  val EffectieVersion = "2.3.0"

  val ScalajsJavaSecurerandomVersion = "1.0.0"

  val ScalaJsMacrotaskExecutorVersion = "1.1.1"

  val ScalacCompatAnnotationVersion = "0.1.4"

  val MunitVersion  = "0.7.29"
  val MunitVersion1 = "1.0.2"

  val ScalaNativeCryptoVersion = "0.2.1"

  val isScala3Incompatible: ModuleID => Boolean =
    m =>
      m.name == "wartremover" ||
        m.name == "ammonite" ||
        m.name == "kind-projector" ||
        m.name == "better-monadic-for"

  val IncludeTest = "compile->compile;test->test"
}

lazy val libs = new {
  lazy val cats    = Def.setting("org.typelevel" %%% "cats-core" % props.CatsVersion)
  lazy val catsOld = Def.setting("org.typelevel" %%% "cats-core" % props.Cats2_0_0Version)

  lazy val catsEffect3 = Def.setting("org.typelevel" %%% "cats-effect" % props.CatsEffect3Version)
  lazy val catsEffect  = Def.setting("org.typelevel" %%% "cats-effect" % props.CatsEffectVersion)

  lazy val doobieCe2Core    = "org.tpolecat" %% "doobie-core"    % props.DoobieCe2Version
  lazy val doobieCe2Refined = "org.tpolecat" %% "doobie-refined" % props.DoobieCe2Version
  lazy val doobieCe3Core    = "org.tpolecat" %% "doobie-core"    % props.DoobieCe3Version

  lazy val libCatsEffectTestKit = Def.setting("org.typelevel" %%% "cats-effect-testkit" % props.CatsEffect3Version)

  lazy val libCatsEffectTestKit3_7 = Def.setting("org.typelevel" %%% "cats-effect-testkit" % props.CatsEffect3_7Version)

  lazy val kittens = Def.setting("org.typelevel" %%% "kittens" % props.KittensVersion)

  lazy val circeCore = Def.setting("io.circe" %%% "circe-core" % props.CirceVersion)
//  lazy val circeCore_0_14_3 = Def.setting("io.circe" %%% "circe-core" % props.Circe_0_14_3_Version)

  lazy val circeJawn = Def.setting("io.circe" %%% "circe-jawn" % props.CirceVersion)
//  lazy val circeJawn_0_14_3 = Def.setting("io.circe" %%% "circe-jawn" % props.Circe_0_14_3_Version)

  lazy val circeParser = Def.setting("io.circe" %%% "circe-parser" % props.CirceVersion)
//  lazy val circeParser_0_14_3 = Def.setting("io.circe" %%% "circe-parser" % props.Circe_0_14_3_Version)

  lazy val circeGeneric = Def.setting("io.circe" %%% "circe-generic" % props.CirceVersion)
//  lazy val circeGeneric_0_14_3 = Def.setting("io.circe" %%% "circe-generic" % props.Circe_0_14_3_Version)

  lazy val circeLiteral = Def.setting("io.circe" %%% "circe-literal" % props.CirceVersion)
//  lazy val circeLiteral_0_14_3 = Def.setting("io.circe" %%% "circe-literal" % props.Circe_0_14_3_Version)

  lazy val fs2V2 = Def.setting("co.fs2" %%% "fs2-core" % props.Fs2V2Version)
  lazy val fs2V3 = Def.setting("co.fs2" %%% "fs2-core" % props.Fs2V3Version)

  lazy val http4sServer_0_22    = "org.http4s" %% "http4s-server" % props.Http4s_0_22_Version
  lazy val http4sServerDsl_0_22 = "org.http4s" %% "http4s-dsl"    % props.Http4s_0_22_Version

  lazy val http4sServer_0_23    = Def.setting("org.http4s" %%% "http4s-server" % props.Http4s_0_23_Version)
  lazy val http4sServerDsl_0_23 = Def.setting("org.http4s" %%% "http4s-dsl" % props.Http4s_0_23_Version)

  lazy val hedgehogCore   = Def.setting("qa.hedgehog" %%% "hedgehog-core" % props.HedgehogVersion)
  lazy val hedgehogRunner = Def.setting("qa.hedgehog" %%% "hedgehog-runner" % props.HedgehogVersion)
  lazy val hedgehogSbt    = Def.setting("qa.hedgehog" %%% "hedgehog-sbt" % props.HedgehogVersion)

  lazy val scalacCompatAnnotation = "org.typelevel" %% "scalac-compat-annotation" % props.ScalacCompatAnnotationVersion

  lazy val hedgehogExtraCore    = Def.setting("io.kevinlee" %%% "hedgehog-extra-core" % props.HedgehogExtraVersion)
  lazy val hedgehogExtraRefined = Def.setting("io.kevinlee" %%% "hedgehog-extra-refined" % props.HedgehogExtraVersion)

  lazy val hedgehog = Def.setting(
    List(
      hedgehogCore.value,
      hedgehogRunner.value,
      hedgehogSbt.value,
    ).map(_ % Test)
  )

  def scalaReflect(scalaVersion: String): ModuleID = "org.scala-lang" % "scala-reflect" % scalaVersion

  lazy val newtype = "io.estatico" %% "newtype" % props.NewtypeVersion
  lazy val refined = Def.setting(("eu.timepit" %%% "refined" % props.RefinedVersion))
//  lazy val refinedLatest = Def.setting("eu.timepit" %%% "refined" % props.RefinedLatestVersion)

  lazy val refinedCats       = Def.setting("eu.timepit" %%% "refined-cats" % props.RefinedVersion)
  lazy val refinedCatsLatest = Def.setting("eu.timepit" %%% "refined-cats" % props.RefinedLatestVersion)

  lazy val embeddedPostgres = "io.zonky.test" % "embedded-postgres" % props.EmbeddedPostgresVersion

  lazy val effectieCore   = Def.setting("io.kevinlee" %% "effectie-core" % props.EffectieVersion)
  lazy val effectieSyntax = Def.setting("io.kevinlee" %% "effectie-syntax" % props.EffectieVersion)
  lazy val effectieCe2    = Def.setting("io.kevinlee" %% "effectie-cats-effect2" % props.EffectieVersion)
  lazy val effectieCe3    = Def.setting("io.kevinlee" %% "effectie-cats-effect3" % props.EffectieVersion)

  lazy val scalajsJavaSecurerandom =
    Def.setting(
      ("org.scala-js" %%% "scalajs-java-securerandom" % props.ScalajsJavaSecurerandomVersion).cross(
        CrossVersion.for3Use2_13
      )
    )

  lazy val scalaJsMacrotaskExecutor =
    Def.setting("org.scala-js" %%% "scala-js-macrotask-executor" % props.ScalaJsMacrotaskExecutorVersion % Test)

  lazy val munit = Def.setting("org.scalameta" %%% "munit" % props.MunitVersion % Test)

  lazy val tests = new {
    lazy val scalaNativeCrypto =
      Def.setting("com.github.lolgab" %%% "scala-native-crypto" % props.ScalaNativeCryptoVersion % Test)

    lazy val catsEffect3_7 = Def.setting("org.typelevel" %%% "cats-effect" % props.CatsEffect3_7Version % Test)

  }
}

def isScala3(scalaVersion: String): Boolean = scalaVersion.startsWith("3.")
