/*
 * Copyright 2018 HM Revenue & Customs
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
import identifiers.register.company._
import identifiers.register.company.directors.DirectorId
import identifiers.register.individual._
import identifiers.register.partnership._
import identifiers.register.partnership.partners.PartnerId
import models.PersonDetails
import play.api.libs.json.{JsResult, JsSuccess}
import utils.UserAnswers

case object AreYouInUKId extends TypedIdentifier[Boolean] {
  override def toString: String = "areYouInUK"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(false) =>
        removePartnershipData(userAnswers).flatMap( answers =>
          removeCompanyData(answers)).flatMap(answers =>
          removeIndividualData(answers)).flatMap(
            _.removeAllOf(List(BusinessTypeId, CompanyRegistrationNumberId, ConfirmCompanyAddressId, CompanyContactAddressPostCodeLookupId,
              CompanyDetailsId, ConfirmPartnershipDetailsId, PartnershipVatId, PartnershipPayeId, IndividualDetailsCorrectId,
              IndividualContactAddressListId, IndividualPreviousAddressPostCodeLookupId
            ))
          )
      case Some(true) =>
        removePartnershipData(userAnswers).flatMap(
          removeCompanyData).flatMap(
          removeIndividualData).flatMap(
          _.removeAllOf(List(NonUKBusinessTypeId, CompanyAddressId, PartnershipRegisteredAddressId, IndividualDetailsId, IndividualAddressId))
        )
      case _ =>
        super.cleanup(value, userAnswers)
    }
  }

  private def removeAllDirectorsOrPartners(personDetailsSeq: Seq[PersonDetails],
                                           userAnswers: UserAnswers, id: TypedIdentifier[Nothing]): JsResult[UserAnswers] = {
    if (personDetailsSeq.nonEmpty) {
      userAnswers.remove(id)
    } else {
      JsSuccess(userAnswers)
    }
  }

  private def removeIndividualData(userAnswers: UserAnswers) = {
    userAnswers.removeAllOf(List(
      IndividualAddressYearsId,
      IndividualPreviousAddressListId, IndividualPreviousAddressId, IndividualContactDetailsId, IndividualDateOfBirthId,
      IndividualSameContactAddressId
    ))
  }

  private def removePartnershipData(userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.removeAllOf(List(PartnershipDetailsId, PartnershipSameContactAddressId,
      PartnershipContactAddressPostCodeLookupId, PartnershipContactAddressListId, PartnershipContactAddressId,
      PartnershipAddressYearsId, PartnershipPreviousAddressId, PartnershipPreviousAddressPostCodeLookupId,
      PartnershipPreviousAddressListId, PartnershipContactDetailsId, MoreThanTenPartnersId))
      .flatMap(answers => removeAllDirectorsOrPartners(answers.allPartners, answers, PartnerId))
  }

  private def removeCompanyData(userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.removeAllOf(List(BusinessDetailsId, CompanySameContactAddressId,
      CompanyAddressListId, CompanyContactAddressId, CompanyContactAddressListId, CompanyAddressYearsId, CompanyPreviousAddressId,
      CompanyPreviousAddressPostCodeLookupId, ContactDetailsId, MoreThanTenDirectorsId))
      .flatMap(answers => removeAllDirectorsOrPartners(answers.allDirectors, answers, DirectorId))
  }
}
