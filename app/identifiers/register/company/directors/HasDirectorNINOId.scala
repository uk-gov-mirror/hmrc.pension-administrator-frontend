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

package identifiers.register.company.directors

import identifiers.TypedIdentifier
import play.api.i18n.Messages
import play.api.libs.json.{JsResult, JsSuccess}
import utils.UserAnswers
import utils.checkyouranswers.{BooleanCYA, CheckYourAnswers, CheckYourAnswersCompany}
import viewmodels.{AnswerRow, Link, Message}

case class HasDirectorNINOId(index: Int) extends TypedIdentifier[Boolean] {
  self =>
  override def toString: String = "hasNino"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(false) =>
        userAnswers.remove(DirectorEnterNINOId(index))
      case _ => JsSuccess(userAnswers)
    }
  }
}

object HasDirectorNINOId {
  implicit def cya(implicit messages: Messages): CheckYourAnswers[HasDirectorNINOId] =
    new CheckYourAnswersCompany[HasDirectorNINOId] {
      private def label(ua: UserAnswers): String =
        dynamicMessage(ua, messageKey = "hasNINO.heading")

      private def hiddenLabel(ua: UserAnswers): Message =
        dynamicMessage(ua, messageKey = "hasNINO.visuallyHidden.text")


      override def row(id: HasDirectorNINOId)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        BooleanCYA[HasDirectorNINOId](Some(label(userAnswers)), Some(hiddenLabel(userAnswers)))().row(id)(changeUrl, userAnswers)
    }
}
