import com.typesafe.sbt.digest.Import._
import com.typesafe.sbt.web.Import._
import net.ground5hark.sbt.concat.Import._
import play.sbt.PlayImport.PlayKeys
import play.sbt.routes.RoutesKeys
import sbt.Keys._
import sbt._
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

lazy val appName: String = "pension-administrator-frontend"


  lazy val root = Project(appName, file("."))
    .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)
    .settings(DefaultBuildSettings.scalaSettings: _*)
    .settings(DefaultBuildSettings.defaultSettings(): _*)
    .settings(SbtDistributablesPlugin.publishingSettings: _*)
    .settings(inConfig(Test)(testSettings): _*)
    .settings(majorVersion := 0)
    .settings(RoutesKeys.routesImport ++= Seq("models.Mode", "models.CheckMode", "models.NormalMode", "models.UpdateMode", "models.Index"),
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "play.twirl.api.HtmlFormat._",
      "uk.gov.hmrc.play.views.html.helpers._",
      "uk.gov.hmrc.play.views.html.layouts._"
    ))
    .settings(
      ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*filters.*;.*handlers.*;.*components.*;.*models.*;.*repositories.*;" +
        ".*BuildInfo.*;.*javascript.*;.*FrontendAuditConnector.*;.*Routes.*;.*GuiceInjector;.*UserAnswersCacheConnector;" +
        ".*ControllerConfiguration;.*LanguageSwitchController",
      ScoverageKeys.coverageMinimum := 80,
      ScoverageKeys.coverageFailOnMinimum := true,
      ScoverageKeys.coverageHighlighting := true
    )
    .settings(scalaSettings: _*)
    .settings(publishingSettings: _*)
    .settings(defaultSettings(): _*)
    .settings(
      scalacOptions ++= Seq("-Xfatal-warnings", "-feature"),
      libraryDependencies ++= AppDependencies(),
      retrieveManaged := true,
      evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
    )
    .configs(IntegrationTest)
    .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
    .settings(
      fork in Test := true,
      javaOptions in Test += "-Dconfig.resource=test.application.conf"
    )
    .settings(resolvers ++= Seq(
      Resolver.bintrayRepo("hmrc", "releases"),
      Resolver.jcenterRepo,
      Resolver.bintrayRepo("emueller", "maven"),
      Resolver.bintrayRepo("wolfendale", "maven")
    )
    )
    .settings(
      // concatenate js
      Concat.groups := Seq(
        "javascripts/pensionadministratorfrontend-app.js" -> group(Seq(
          "javascripts/show-hide-content.js",
          "javascripts/pensionadministratorfrontend.js"

        ))),
      // prevent removal of unused code which generates warning errors due to use of third-party libs
      uglifyCompressOptions := Seq("unused=false", "dead_code=false"),
      pipelineStages := Seq(digest),
      // below line required to force asset pipeline to operate in dev rather than only prod
      pipelineStages in Assets := Seq(concat, uglify),
      // only compress files generated by concat
      includeFilter in uglify := GlobFilter("pensionadministratorfrontend-*.js")
    )
    .settings(
      PlayKeys.devSettings ++= Seq(
        "metrics.enabled" -> "false",
        "auditing.enabled" -> "false",
        "play.server.http.port" -> "8201",
        "urls.loginContinue" -> "http://localhost:8201/register-as-pension-scheme-administrator"
      )
    )

lazy val testSettings: Seq[Def.Setting[_]] = Seq(
  fork        := true,
  javaOptions ++= Seq(
    "-Dconfig.resource=test.application.conf"
  )
)