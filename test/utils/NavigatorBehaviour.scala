/*
 * Copyright 2020 HM Revenue & Customs
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
import models.requests.IdentifiedRequest
import models.{Mode, NormalMode}
import org.scalatest.prop.TableFor3
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import play.api.mvc.Call
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

trait NavigatorBehaviour extends ScalaCheckPropertyChecks with OptionValues {
  this: WordSpec with MustMatchers =>

  protected val emptyAnswers: UserAnswers = UserAnswers(Json.obj())

  protected def dataDescriber(answers: UserAnswers): String = answers.toString

  protected implicit val request: IdentifiedRequest = new IdentifiedRequest {
    override def externalId: String = "test-external-id"
  }

  protected implicit val hc: HeaderCarrier = HeaderCarrier()

  def navigatorWithRoutesWithMode[A <: Identifier](  navigator: Navigator,
                                                     routes: TableFor3[A, UserAnswers, Call],
                                                     describer: UserAnswers => String,
                                                     mode: Mode = NormalMode
                                                  ): Unit = {

    forAll(routes) {
      (id: Identifier, userAnswers: UserAnswers, call: Call) =>
        s"move from $id to $call in ${Mode.jsLiteral.to(mode)} with data: ${describer(userAnswers)}" in {
          val result = navigator.nextPage(id, mode, userAnswers)
          result mustBe call
        }
    }


  }

}
