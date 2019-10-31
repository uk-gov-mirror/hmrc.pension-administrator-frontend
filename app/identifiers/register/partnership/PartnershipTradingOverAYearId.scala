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

package identifiers.register.partnership

import identifiers.TypedIdentifier
import play.api.i18n.Messages
import play.api.libs.json.JsResult
import utils.UserAnswers
import utils.checkyouranswers.{BooleanCYA, CheckYourAnswers, CheckYourAnswersBusiness}
import viewmodels.{AnswerRow, Link, Message}

case object PartnershipTradingOverAYearId extends TypedIdentifier[Boolean] {
  self =>
  override def toString: String = "partnershipTradingOverAYear"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[self.type] =
    new CheckYourAnswersBusiness[self.type] {
      private def label(ua: UserAnswers): String =
        dynamicMessage(ua, "trading.title")

      private def hiddenLabel(ua: UserAnswers): Message =
        dynamicMessage(ua, "trading.visuallyHidden.text")

      override def row(id: self.type)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        BooleanCYA[self.type](Some(label(userAnswers)), Some(hiddenLabel(userAnswers)))().row(id)(changeUrl, userAnswers)
    }

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(true) =>
        userAnswers
          .remove(PartnershipPreviousAddressPostCodeLookupId)
          .flatMap(_.remove(PartnershipPreviousAddressId))
          .flatMap(_.remove(PartnershipPreviousAddressListId))
      case _ =>
        super.cleanup(value, userAnswers)
    }
  }
}
