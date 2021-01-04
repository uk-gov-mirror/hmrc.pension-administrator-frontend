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

import identifiers._
import play.api.i18n.Messages
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersBusiness, StringCYA}
import viewmodels.{AnswerRow, Link, Message}

case object EnterPAYEId extends TypedIdentifier[String] {
  self =>
  override def toString: String = "paye"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[self.type] =
    new CheckYourAnswersBusiness[self.type] {
      private def label(ua: UserAnswers): String =
        dynamicMessage(ua, messageKey = "enterPAYE.heading")

      private def hiddenLabel(ua: UserAnswers): Message =
        dynamicMessage(ua, messageKey = "enterPAYE.visuallyHidden.text")


      override def row(id: self.type)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        (userAnswers.get(HasPAYEId), userAnswers.get(EnterPAYEId)) match {
          case (Some(true), None) =>
            StringCYA[self.type](Some(label(userAnswers)), Some(hiddenLabel(userAnswers)))().row(id)(changeUrl, userAnswers)
          case _ =>
            StringCYA[self.type](Some(label(userAnswers)), Some(hiddenLabel(userAnswers)), isMandatory = false)().row(id)(changeUrl, userAnswers)
        }
    }
}
