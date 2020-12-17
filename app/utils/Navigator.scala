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
import models._
import models.requests.IdentifiedRequest
import play.api.Logger
import play.api.mvc.Call
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

abstract class Navigator {

  protected def routeMap(ua: UserAnswers): PartialFunction[Identifier, Call]

  protected def editRouteMap(ua: UserAnswers, mode: Mode = CheckMode): PartialFunction[Identifier, Call]

  protected def updateRouteMap(ua: UserAnswers): PartialFunction[Identifier, Call]

  def nextPage(id: Identifier, mode: Mode, userAnswers: UserAnswers)
              (implicit ex: IdentifiedRequest, executionContext: ExecutionContext, hc: HeaderCarrier): Call = {
    val navigateTo = {
      println("\nMBMBMB:" + id + " --- " + this + " --- " + mode)
      mode match {
        case NormalMode => routeMap(userAnswers).lift
        case CheckMode => editRouteMap(userAnswers).lift
        case UpdateMode => updateRouteMap(userAnswers).lift
        case CheckUpdateMode =>
        println( "\n>>>>OOOP")
          editRouteMap(userAnswers, CheckUpdateMode).lift
      }
    }

    navigateTo(id).getOrElse(defaultPage(id, mode))
  }

  private[this] def defaultPage(id: Identifier, mode: Mode): Call = {
    Logger.warn(s"No navigation defined for id $id in mode $mode")
    controllers.routes.IndexController.onPageLoad()
  }
}
