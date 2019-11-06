/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils

import identifiers.Identifier
import models.Mode.checkMode
import models.requests.IdentifiedRequest
import models.{CheckMode, Mode, NormalMode}
import org.scalatest.exceptions.TableDrivenPropertyCheckFailedException
import org.scalatest.prop.{PropertyChecks, TableFor4}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import play.api.mvc.Call
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

trait NavigatorBehaviour extends PropertyChecks with OptionValues {
  this: WordSpec with MustMatchers =>

  protected val emptyAnswers: UserAnswers = UserAnswers(Json.obj())

  protected def dataDescriber(answers: UserAnswers): String = answers.toString

  protected implicit val request: IdentifiedRequest = new IdentifiedRequest {
    override def externalId: String = "test-external-id"
  }

  protected implicit val hc: HeaderCarrier = HeaderCarrier()

  //scalastyle:off method.length
  //scalastyle:off regex
  def navigatorWithRoutes[A <: Identifier, B <: Option[Call]](
                                                               navigator: Navigator,
                                                               routes: TableFor4[A, UserAnswers, Call, B],
                                                               describer: UserAnswers => String,
                                                               mode: Mode = NormalMode
                                                             ): Unit = {

    s"behave like a navigator in ${Mode.jsLiteral.to(mode)} journey" when {

      s"navigating in ${mode.getClass.getName}" must {

        try {
          forAll(routes) {
            (id: Identifier, userAnswers: UserAnswers, call: Call, _: Option[Call]) =>
              s"move from $id to $call with data: ${describer(userAnswers)}" in {
                val result = navigator.nextPage(id, mode, userAnswers)
                result mustBe call
              }
          }
        }
        catch {
          case e: TableDrivenPropertyCheckFailedException =>
            println(s"Invalid routes: ${e.toString}")
            throw e
        }

      }

      "navigating in CheckMode" must {

        try {
          if (routes.nonEmpty) {
            forAll(routes) { (id: Identifier, userAnswers: UserAnswers, _: Call, editCall: Option[Call]) =>
              if (editCall.isDefined) {
                s"move from $id to ${editCall.value} with data: ${describer(userAnswers)}" in {
                  val result = navigator.nextPage(id, checkMode(mode), userAnswers)
                  result mustBe editCall.value
                }
              }
            }
          }
        }
        catch {
          case e: TableDrivenPropertyCheckFailedException =>
            println(s"Invalid routes: ${e.toString}")
            throw e
        }

      }

    }

  }

  def nonMatchingNavigator(navigator: Navigator): Unit = {

    val testId: Identifier = new Identifier {}

    "behaviour like a navigator without routes" when {
      "navigating in NormalMode" must {
        "return a call given a non-configured Id" in {
          navigator.nextPage(testId, NormalMode, UserAnswers()) mustBe a[Call]
        }
      }

      "navigating in CheckMode" must {
        "return a call given a non-configured Id" in {
          navigator.nextPage(testId, CheckMode, UserAnswers()) mustBe a[Call]
        }
      }
    }

  }

}
