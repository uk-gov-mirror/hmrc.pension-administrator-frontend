/*
 * Copyright 2021 HM Revenue & Customs
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

package identifiers.register.company.directors

import base.SpecBase
import models._
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.UserAnswers
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, Link, Message}

class DirectorNoNINOReasonIdSpec extends SpecBase {

  private val onwardUrl = "onwardUrl"

  "cya" when {
    "in normal mode" must {

      "return answers rows with change links when have value" in {
        val request: DataRequest[AnyContent] = DataRequest(
          request = FakeRequest(),
          externalId = "id",
          user = PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
          userAnswers = UserAnswers()
            .directorNoNINOReason(0, "no reason")
            .directorName(0, PersonName("first", "last"))
        )

        DirectorNoNINOReasonId(0).row(Some(Link(onwardUrl)))(request, implicitly) must equal(
          Seq(AnswerRow(
            label = Message("whyNoNINO.heading", "first last"),
            answer = Seq("no reason"),
            answerIsMessageKey = false,
            changeUrl = Some(Link(onwardUrl)),
            visuallyHiddenText = Some(Message("whyNoNINO.visuallyHidden.text", "first last"))
          ))
        )
      }

      "return answers rows with add links when has nino is false but no nino reason" in {
        val request: DataRequest[AnyContent] = DataRequest(
          request = FakeRequest(),
          externalId = "id",
          user = PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
          userAnswers = UserAnswers().directorHasNINO(0, flag = false).
            directorName(0, PersonName("first", "last"))
        )

        DirectorNoNINOReasonId(0).row(Some(Link(onwardUrl)))(request, implicitly) must equal(
          Seq(AnswerRow(
            label = Message("whyNoNINO.heading", "first last"),
            answer = Seq("site.not_entered"),
            answerIsMessageKey = true,
            changeUrl = Some(Link(onwardUrl, "site.add")),
            visuallyHiddenText = Some(Message("whyNoNINO.visuallyHidden.text", "first last"))
          ))
        )
      }

      "return no answers rows when has nino is true" in {
        val request: DataRequest[AnyContent] = DataRequest(
          request = FakeRequest(),
          externalId = "id",
          user = PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
          userAnswers = UserAnswers()
            .directorHasNINO(0, flag = true)
            .directorName(0, PersonName("first", "last"))
        )

        DirectorNoNINOReasonId(0).row(Some(Link(onwardUrl)))(request, implicitly) must equal(Nil)
      }
    }
  }
}
