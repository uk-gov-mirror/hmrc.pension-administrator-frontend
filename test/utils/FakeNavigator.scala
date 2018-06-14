/*
 * Copyright 2018 HM Revenue & Customs
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

import play.api.mvc.Call
import identifiers.Identifier
import models.{Mode, NormalMode}

class FakeNavigator(desiredRoute: Call, mode: Mode = NormalMode) extends Navigator {

  private[this] var userAnswers: Option[UserAnswers] = None

  override def nextPage(controllerId: Identifier, mode: Mode): (UserAnswers) => Call = {
    (ua) =>
      userAnswers = Some(ua)
      desiredRoute
  }

  def lastUserAnswers: Option[UserAnswers] = userAnswers

}

object FakeNavigator extends FakeNavigator(Call("GET", "www.example.com"), NormalMode)
