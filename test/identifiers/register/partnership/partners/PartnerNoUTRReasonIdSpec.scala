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

package identifiers.register.partnership.partners

import base.SpecBase
import models.requests.DataRequest
import models.{PSAUser, PersonName, UserType}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.UserAnswers
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, Link, Message}

class PartnerNoUTRReasonIdSpec extends SpecBase {

  private val personDetails = PersonName("test first", "test last")
  private val onwardUrl = "onwardUrl"

  "cya" when {
    def answers: UserAnswers =
      UserAnswers()
        .set(PartnerNameId(0))(personDetails).asOpt.value
        .set(PartnerNoUTRReasonId(0))(value = "test-reason").asOpt.value

    "in normal mode" must {
      "return answers rows with change links" in {
        val answerRows =
          Seq(
            AnswerRow(Message("whyNoUTR.heading").withArgs(personDetails.fullName).resolve, Seq("test-reason"), answerIsMessageKey = false,
              Some(Link("site.change", onwardUrl)), Some(Message("whyNoUTR.visuallyHidden.text").withArgs(personDetails.fullName))
            )
          )
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          PSAUser(UserType.Organisation, None, isExistingPSA = false, None), answers)

        PartnerNoUTRReasonId(0).row(Some(Link("site.change", onwardUrl)))(request, implicitly) must equal(answerRows)
      }
    }
  }
}