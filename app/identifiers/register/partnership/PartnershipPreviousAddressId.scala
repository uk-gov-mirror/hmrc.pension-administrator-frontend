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

package identifiers.register.partnership

import identifiers.TypedIdentifier
import models.Address
import play.api.i18n.Messages
import utils.{UserAnswers, checkyouranswers}
import utils.checkyouranswers.{AddressCYA, CheckYourAnswers, CheckYourAnswersBusiness}
import utils.countryOptions.CountryOptions
import viewmodels.{AnswerRow, Link, Message}

case object PartnershipPreviousAddressId extends TypedIdentifier[Address] {
  self =>
  override def toString: String = "partnershipPreviousAddress"

  implicit def cya(implicit messages: Messages, countryOptions: CountryOptions): CheckYourAnswers[self.type] =
    new CheckYourAnswersBusiness[self.type] {
      private def label(ua: UserAnswers): String =
        dynamicMessage(ua, "previousAddress.checkYourAnswersLabel")

      private def hiddenLabel(ua: UserAnswers): Message =
        dynamicMessage(ua, "previousAddress.visuallyHidden.text")

      override def row(id: self.type)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] = {
        checkyouranswers.AddressCYA[self.type](label(userAnswers), Some(hiddenLabel(userAnswers)))().row(id)(changeUrl, userAnswers)
      }
    }
}
