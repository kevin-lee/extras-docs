import sbt.*
import devoops.DevOopsGitHubPlugin.autoImport.*
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport.*
import just.semver.SemVer
import extras.scala.io.syntax.color._

object ProjectInfo {

  object docsTools {

    object CmdRun {
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

    def writeReleases(websiteDir: File)(implicit logger: Logger): Unit = {
      val (ghVersionExit, ghVersionOut, ghVersionErr) = CmdRun.runAndCapture(Seq("gh", "--version"))
      if (ghVersionExit != 0)
        CmdRun.fail("writeReleases", "gh --version", Seq("gh", "--version"), ghVersionOut, ghVersionErr)(
          logger.error(_)
        )

      val (ghAuthExit, ghAuthOut, ghAuthErr) =
        CmdRun.runAndCapture(Seq("gh", "auth", "status", "-h", "github.com"))
      if (ghAuthExit != 0)
        CmdRun.fail(
          "writeReleases",
          "gh auth status",
          Seq("gh", "auth", "status", "-h", "github.com"),
          ghAuthOut,
          ghAuthErr,
        )(logger.error(_))

      val repo = s"${props.GitHubUser}/${props.CodeRepoName}"

      /* One line for each non-draft release: tag name, TAB, then the release as a compact JSON object. */
      val releaseLineJq =
        """.[] | select(.draft | not) | "\(.tag_name)\t\({tagName: .tag_name, name: .name, publishedAt: .published_at, htmlUrl: .html_url, prerelease: .prerelease, body: (.body // "")} | tojson)""""

      val ghReleasesCmd =
        Seq(
          "gh",
          "api",
          "-H",
          "Accept: application/vnd.github+json",
          s"/repos/$repo/releases",
          "--paginate",
          "-q",
          releaseLineJq,
        )

      val (releasesExit, releasesOut, releasesErr) = CmdRun.runAndCapture(ghReleasesCmd)
      if (releasesExit != 0)
        CmdRun.fail("writeReleases", "gh api releases", ghReleasesCmd, releasesOut, releasesErr)(logger.error(_))

      val releases = releasesOut
        .split("\n")
        .map(_.trim)
        .filter(_.nonEmpty)
        .toList
        .flatMap { line =>
          val (tagName, releaseJson) = line.splitAt(line.indexOf('\t'))
          val semVer                 =
            if (tagName.startsWith("v")) SemVer.parse(tagName.stripPrefix("v")).toOption
            else None
          semVer match {
            case Some(v) =>
              List(v -> releaseJson.drop(1))
            case None =>
              logger.warn(s">> [writeReleases] Skip the release with an invalid tag name: ${tagName.yellow}")
              List.empty
          }
        }
        .sortBy(_._1)(Ordering[SemVer].reverse)
        .map(_._2)

      val releasesFile = websiteDir / "releases.json"
      val releasesJson =
        if (releases.isEmpty) "[]"
        else releases.mkString("[\n", ",\n", "\n]")

      val websiteDirRelativePath =
        s"${releasesFile.getParentFile.getParentFile.getName.cyan}/${releasesFile.getParentFile.getName.yellow}"
      logger.info(
        s""">> Writing ${releases.length.toString.blue} ${"releases".blue} to $websiteDirRelativePath/${releasesFile.getName.green}."""
      )
      IO.write(releasesFile, releasesJson)
    }

    def writeNoReleases(websiteDir: File)(implicit logger: Logger): Unit = {
      val releasesFile         = websiteDir / "releases.json"
      val latestVersionFile    = websiteDir / "latestVersion.json"
      val versionsArchivedFile = websiteDir / "src" / "pages" / "versionsArchived.json"

      logger.info(
        s""">> Writing ${"empty release data".blue} (${"no release".yellow}) to
           |>>   ${releasesFile.getName.green}, ${latestVersionFile.getName.green} and ${versionsArchivedFile.getName.green}.
           |""".stripMargin
      )
      IO.write(releasesFile, "[]")
      IO.write(latestVersionFile, raw"""{"version":null}""")
      IO.write(versionsArchivedFile, "[]")
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

  object props {

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

  object libs {
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

    object tests {
      lazy val scalaNativeCrypto =
        Def.setting("com.github.lolgab" %%% "scala-native-crypto" % props.ScalaNativeCryptoVersion % Test)

      lazy val catsEffect3_7 = Def.setting("org.typelevel" %%% "cats-effect" % props.CatsEffect3_7Version % Test)

    }
  }
}
