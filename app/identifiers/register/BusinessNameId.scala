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

import identifiers.TypedIdentifier
import models.register.BusinessType._
import play.api.i18n.Messages
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, StringCYA}
import viewmodels.{AnswerRow, Link, Message}

case object BusinessNameId extends TypedIdentifier[String] {
  self =>
  override def toString: String = "businessName"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[self.type] =
    new CheckYourAnswers[self.type] {

      override def row(id: self.type)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        StringCYA(Some(label(userAnswers)))().row(id)(changeUrl, userAnswers)
    }

  private def label(userAnswers: UserAnswers)(implicit messages: Messages): String =
    (userAnswers.get(BusinessTypeId), userAnswers.get(NonUKBusinessTypeId)) match {
    case (Some(businessType), _) =>
      Message("businessName.heading", Message(s"businessType.${businessType.toString}.lc"))
    case (_, Some(nonUKBusinessType)) =>
      Message("businessName.heading", Message(s"nonUKBusinessType.${nonUKBusinessType.toString}.lc"))
    case _ =>
      Message("businessName.heading", Message("theCompany"))
  }

}
