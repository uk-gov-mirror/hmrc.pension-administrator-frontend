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

package identifiers.register

import identifiers._
import identifiers.register.company.directors.DirectorId
import identifiers.register.company._
import identifiers.register.partnership.partners.PartnerId
import identifiers.register.partnership._
import models.{PersonDetails, PersonName}
import models.register.BusinessType
import models.register.BusinessType._
import play.api.libs.json.{JsResult, JsSuccess}
import utils.UserAnswers

case object BusinessTypeId extends TypedIdentifier[BusinessType] {
  override def toString: String = "businessType"

  override def cleanup(value: Option[BusinessType], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(LimitedCompany) | Some(UnlimitedCompany) | Some(OverseasCompany) =>
        removeAllPartnership(userAnswers)
      case Some(BusinessPartnership) | Some(LimitedPartnership) | Some(LimitedLiabilityPartnership) =>
        removeAllCompany(userAnswers)
      case _ => JsSuccess(userAnswers)
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

  private def removeDirectorsOrPartners(personNameSeq: Seq[PersonName],
                                        userAnswers: UserAnswers, id: TypedIdentifier[Nothing]): JsResult[UserAnswers] = {
    if (personNameSeq.nonEmpty) {
      userAnswers.remove(id)
    } else {
      JsSuccess(userAnswers)
    }
  }

  private def removeAllCompany(userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.removeAllOf(List(BusinessNameId, BusinessUTRId, IsRegisteredNameId, CompanyAddressId,
      HasCompanyCRNId, CompanyRegistrationNumberId, HasPAYEId, EnterPAYEId, HasVATId, EnterVATId,
      CompanySameContactAddressId, CompanyAddressListId, CompanyContactAddressId, CompanyContactAddressListId,
      CompanyAddressYearsId, CompanyPreviousAddressId, CompanyPreviousAddressPostCodeLookupId,
      CompanyEmailId, CompanyPhoneId, MoreThanTenDirectorsId))
      .flatMap(answers => removeDirectorsOrPartners(answers.allDirectors, answers, DirectorId))
  }

  private def removeAllPartnership(userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.removeAllOf(List(HasCompanyCRNId, BusinessNameId, BusinessUTRId, IsRegisteredNameId, PartnershipRegisteredAddressId,
      HasPAYEId, EnterPAYEId, HasVATId, EnterVATId, PartnershipSameContactAddressId,
      PartnershipContactAddressPostCodeLookupId, PartnershipContactAddressListId, PartnershipContactAddressId,
      PartnershipAddressYearsId, PartnershipPreviousAddressId, PartnershipPreviousAddressPostCodeLookupId,
      PartnershipPreviousAddressListId, PartnershipEmailId, PartnershipPhoneId, MoreThanTenPartnersId))
      .flatMap(answers => removeAllDirectorsOrPartners(answers.allPartners, answers, PartnerId))
  }

}
