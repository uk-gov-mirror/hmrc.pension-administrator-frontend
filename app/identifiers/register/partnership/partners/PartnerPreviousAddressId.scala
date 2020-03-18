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

package identifiers.register.partnership.partners

import identifiers._
import models.{Address, AddressYears, Index}
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersPartner}
import utils.countryOptions.CountryOptions
import utils.{UserAnswers, checkyouranswers}
import viewmodels.{AnswerRow, Link, Message}

case class PartnerPreviousAddressId(index: Int) extends TypedIdentifier[Address] {
  override def path: JsPath = JsPath \ "partners" \ index \ PartnerPreviousAddressId.toString
}

object PartnerPreviousAddressId {

  override def toString: String = "partnerPreviousAddress"

  implicit def cya(implicit messages: Messages, countryOptions: CountryOptions): CheckYourAnswers[PartnerPreviousAddressId] =
    new CheckYourAnswersPartner[PartnerPreviousAddressId] {
      private def label(ua: UserAnswers, index: Index): String =
        dynamicMessage(ua, messageKey = "previousAddress.checkYourAnswersLabel", index)

      private def hiddenLabel(ua: UserAnswers, index: Index): Message =
        dynamicMessage(ua, messageKey = "previousAddress.visuallyHidden.text", index)

      override def row(id: PartnerPreviousAddressId)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] = {
        (userAnswers.get(PartnerAddressYearsId(id.index)), userAnswers.get(PartnerPreviousAddressId(id.index))) match {
          case (Some(AddressYears.UnderAYear), None) =>
            checkyouranswers.AddressCYA[PartnerPreviousAddressId](label(userAnswers, id.index),
              Some(hiddenLabel(userAnswers, id.index)))().row(id)(changeUrl, userAnswers)
          case _ =>
            checkyouranswers.AddressCYA[PartnerPreviousAddressId](label(userAnswers, id.index),
              Some(hiddenLabel(userAnswers, id.index)), isMandatory = false)().row(id)(changeUrl, userAnswers)
        }
      }
    }

}