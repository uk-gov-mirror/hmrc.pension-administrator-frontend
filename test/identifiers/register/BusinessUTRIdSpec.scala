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

package identifiers.register

import base.SpecBase
import models.register.BusinessType
import models.requests.DataRequest
import models.{PSAUser, UserType}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.UserAnswers
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, Link, Message}

class BusinessUTRIdSpec extends SpecBase {
  private val onwardUrl = "onwardUrl"

  "cya" when {
    "in normal mode" must {
      "return no rows when non uk" in {
        val request: DataRequest[AnyContent] = DataRequest(
          request = FakeRequest(),
          externalId = "id",
          user = PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
          userAnswers = UserAnswers().areYouInUk(false)
        )

        BusinessUTRId.row(Some(Link("site.change", onwardUrl)))(request, implicitly) must equal(Nil)
      }

      "return answers rows with change links when uk and have value" in {
        val request: DataRequest[AnyContent] = DataRequest(
          request = FakeRequest(),
          externalId = "id",
          user = PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
          userAnswers = UserAnswers()
            .areYouInUk(true)
            .businessName()
            .businessUtr()
        )

        BusinessUTRId.row(Some(Link(onwardUrl)))(request, implicitly) must equal(
          Seq(AnswerRow(
            label = Message("utr.heading", "the company"),
            answer = Seq("1111111111"),
            answerIsMessageKey = false,
            changeUrl = Some(Link(onwardUrl)),
            visuallyHiddenText = None
          ))
        )
      }

      "return answers rows with add links when uk and have no value" in {
        val request: DataRequest[AnyContent] = DataRequest(
          request = FakeRequest(),
          externalId = "id",
          user = PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
          userAnswers = UserAnswers()
            .areYouInUk(true)
            .businessType(BusinessType.UnlimitedCompany)
            .businessName()
        )

        BusinessUTRId.row(Some(Link(onwardUrl)))(request, implicitly) must equal(
          Seq(AnswerRow(
            label = Message("utr.heading", "unlimited company"),
            answer = Seq("site.not_entered"),
            answerIsMessageKey = true,
            changeUrl = Some(Link(onwardUrl, "site.add")),
            visuallyHiddenText = None
          ))
        )
      }
    }
  }
}


