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

package utils.checkyouranswers

import java.time.LocalDate

import base.SpecBase
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models._
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.{DateHelper, UserAnswers}
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, Link, Message}

class CheckYourAnswersSpec extends SpecBase {

  val onwardUrl = "onwardUrl"

  def testIdentifier[A]: TypedIdentifier[A] = new TypedIdentifier[A] {
    override def toString = "testId"
  }

  def dataRequest(answers: UserAnswers): DataRequest[AnyContent] =
    DataRequest(FakeRequest(), "id", PSAUser(UserType.Organisation, None, isExistingPSA = false, None), answers)

  "CheckYourAnswers" must {

    "produce row of answers for reference value" in {
      val request = dataRequest(UserAnswers().set(testIdentifier[ReferenceValue])(ReferenceValue(value = "test-ref")).asOpt.value)

      testIdentifier[ReferenceValue].row(Some(Link(onwardUrl)))(request, implicitly) must equal(Seq(
        AnswerRow(label = Message("testId.heading"), answer = Seq("test-ref"), answerIsMessageKey = false,
          changeUrl = Some(Link(onwardUrl)))))
    }

    "produce row of answers for personName" in {
      val request = dataRequest(UserAnswers().set(testIdentifier[PersonName])(PersonName("first", "last")).asOpt.value)

      testIdentifier[PersonName].row(Some(Link(onwardUrl)))(request, implicitly) must equal(Seq(
        AnswerRow(label = Message("testId.heading"), answer = Seq("first last"), answerIsMessageKey = false,
          changeUrl = Some(Link(onwardUrl)))))
    }

    "produce row of answers for date" in {
      val request = dataRequest(UserAnswers().set(testIdentifier[LocalDate])(LocalDate.now()).asOpt.value)

      testIdentifier[LocalDate].row(Some(Link(onwardUrl)))(request, implicitly) must equal(Seq(
        AnswerRow(label = Message("testId.heading"), answer = Seq(DateHelper.formatDate(LocalDate.now())), answerIsMessageKey = false,
          changeUrl = Some(Link(onwardUrl)))))
    }
  }
}
