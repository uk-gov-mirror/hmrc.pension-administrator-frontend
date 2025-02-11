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


import identifiers.register.company._
import identifiers.register.company.directors.DirectorNameId
import identifiers.register.partnership._
import identifiers.register.partnership.partners.PartnerNameId
import models._
import models.register.BusinessType._
import models.register.NonUKBusinessType.Company
import models.register.{BusinessType, NonUKBusinessType}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class BusinessTypeIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {
  import BusinessTypeIdSpec._
  "BusinessTypeId" must {
    "remove the has company CRN id when the business type is changed to LTD company" in {
      val ua = UserAnswers(Json.obj(HasCompanyCRNId.toString -> true))
      val result = BusinessTypeId.cleanup(Some(BusinessType.LimitedCompany), ua).asOpt.value
      result.get(HasCompanyCRNId) mustBe None
    }
  }

  "Cleanup for Company" when {

    "business type was Company and we change to Partnership" must {
      val result: UserAnswers =
        answersCompany.set(BusinessTypeId)(LimitedPartnership)
          .asOpt.value

      "remove the data for business details " in {
        result.get(BusinessNameId) mustNot be(defined)
        result.get(BusinessUTRId) mustNot be(defined)
      }

      "remove the data for company address " in {
        result.get(CompanyAddressId) mustNot be(defined)
        result.get(CompanyAddressListId) mustNot be(defined)
      }

      "remove the data for company contact address " in {
        result.get(CompanySameContactAddressId) mustNot be(defined)
        result.get(CompanyContactAddressId) mustNot be(defined)
        result.get(CompanyContactAddressListId) mustNot be(defined)
        result.get(CompanyContactAddressPostCodeLookupId) mustNot be(defined)
      }

      "remove the data for company previous address " in {
        result.get(CompanyAddressYearsId) mustNot be(defined)
        result.get(CompanyPreviousAddressId) mustNot be(defined)
        result.get(CompanyPreviousAddressPostCodeLookupId) mustNot be(defined)
      }

      "remove the data for contact details " in {
        result.get(CompanyEmailId) mustNot be(defined)
        result.get(CompanyPhoneId) mustNot be(defined)
      }

      "remove the data for directors " in {
        result.get(DirectorNameId(0)) mustNot be(defined)
        result.get(DirectorNameId(1)) mustNot be(defined)
      }

      "remove the data for more than 10 directors " in {
        result.get(MoreThanTenDirectorsId) mustNot be(defined)
      }
    }
  }

  "Cleanup for Partnership" when {

    "business type was Partnership and we change to Company" must {
      val result: UserAnswers =
        answersPartnership.set(NonUKBusinessTypeId)(Company)
          .asOpt.value

      "remove the data for partnership details " in {
        result.get(BusinessNameId) mustNot be(defined)
      }

      "remove the data for partnership address " in {
        result.get(PartnershipRegisteredAddressId) mustNot be(defined)
      }

      "remove the data for partnership contact address " in {
        result.get(PartnershipSameContactAddressId) mustNot be(defined)
        result.get(PartnershipContactAddressId) mustNot be(defined)
        result.get(PartnershipContactAddressListId) mustNot be(defined)
        result.get(PartnershipContactAddressPostCodeLookupId) mustNot be(defined)
      }

      "remove the data for partnership previous address " in {
        result.get(PartnershipAddressYearsId) mustNot be(defined)
        result.get(PartnershipPreviousAddressId) mustNot be(defined)
        result.get(PartnershipPreviousAddressListId) mustNot be(defined)
        result.get(PartnershipPreviousAddressPostCodeLookupId) mustNot be(defined)
      }

      "remove the data for email details " in {
        result.get(PartnershipEmailId) mustNot be(defined)
      }

      "remove the data for phone details " in {
        result.get(PartnershipPhoneId) mustNot be(defined)
      }


      "remove the data for partners " in {
        result.get(PartnerNameId(0)) mustNot be(defined)
        result.get(PartnerNameId(1)) mustNot be(defined)
      }

      "remove the data for more than 10 partners" in {
        result.get(MoreThanTenPartnersId) mustNot be(defined)
      }
    }
  }
}

object BusinessTypeIdSpec extends OptionValues {

  val tolerantAddress = TolerantAddress(Some("line 1"),Some("line 2"), Some("line 3"), Some("line 4"), None, Some("DE"))
  val tolerantIndividual = TolerantIndividual(Some("firstName"), Some("middleName"), Some("lastName"))
  val address = Address("line 1", "line 2", None, None, None, "GB")
  val email = "s@s.com"
  val phone = "999"
  val personName = PersonName("test first", "test last")

  val answersCompany: UserAnswers = UserAnswers(Json.obj())
    .set(BusinessTypeId)(UnlimitedCompany)
    .flatMap(_.set(BusinessNameId)("company name")
      .flatMap(_.set(BusinessUTRId)("test-utr"))
      .flatMap(_.set(IsRegisteredNameId)(true))
      .flatMap(_.set(HasCompanyCRNId)(true))
      .flatMap(_.set(CompanyRegistrationNumberId)("test-crn"))
      .flatMap(_.set(HasPAYEId)(true))
      .flatMap(_.set(EnterPAYEId)("test-paye"))
      .flatMap(_.set(HasVATId)(true))
      .flatMap(_.set(EnterVATId)("test-vat"))
      .flatMap(_.set(CompanyAddressId)(tolerantAddress))
      .flatMap(_.set(CompanySameContactAddressId)(false))
      .flatMap(_.set(CompanyContactAddressPostCodeLookupId)(Seq(tolerantAddress)))
      .flatMap(_.set(CompanyAddressListId)(tolerantAddress))
      .flatMap(_.set(CompanyContactAddressId)(address))
      .flatMap(_.set(CompanyContactAddressListId)(tolerantAddress))
      .flatMap(_.set(CompanyAddressYearsId)(AddressYears.OverAYear))
      .flatMap(_.set(CompanyPreviousAddressId)(address))
      .flatMap(_.set(CompanyPreviousAddressPostCodeLookupId)(Seq(tolerantAddress)))
      .flatMap(_.set(CompanyEmailId)(email))
      .flatMap(_.set(CompanyPhoneId)(phone))
      .flatMap(_.set(DirectorNameId(0))(personName))
      .flatMap(_.set(DirectorNameId(1))(personName))
      .flatMap(_.set(MoreThanTenDirectorsId)(true))
    )
    .asOpt.value

  val answersPartnership: UserAnswers = UserAnswers(Json.obj())
    .set(NonUKBusinessTypeId)(NonUKBusinessType.BusinessPartnership)
    .flatMap(_.set(BusinessNameId)("company name"))
      .flatMap(_.set(BusinessUTRId)("test-utr"))
      .flatMap(_.set(IsRegisteredNameId)(true))
      .flatMap(_.set(HasPAYEId)(true))
      .flatMap(_.set(EnterPAYEId)("test-paye"))
      .flatMap(_.set(HasVATId)(true))
      .flatMap(_.set(EnterVATId)("test-vat"))
      .flatMap(_.set(PartnershipRegisteredAddressId)(tolerantAddress))
      .flatMap(_.set(PartnershipSameContactAddressId)(false))
      .flatMap(_.set(PartnershipContactAddressListId)(tolerantAddress))
      .flatMap(_.set(PartnershipContactAddressPostCodeLookupId)(Seq(tolerantAddress)))
      .flatMap(_.set(PartnershipContactAddressId)(address))
      .flatMap(_.set(PartnershipAddressYearsId)(AddressYears.OverAYear))
      .flatMap(_.set(PartnershipPreviousAddressId)(address))
      .flatMap(_.set(PartnershipPreviousAddressPostCodeLookupId)(Seq(tolerantAddress)))
      .flatMap(_.set(PartnershipEmailId)(email))
      .flatMap(_.set(PartnershipPhoneId)(phone))
      .flatMap(_.set(PartnerNameId(0))(personName))
      .flatMap(_.set(PartnerNameId(1))(personName))
      .flatMap(_.set(MoreThanTenPartnersId)(true))
      .flatMap(_.set(BusinessNameId)("company name"))
      .flatMap(_.set(BusinessUTRId)("test-utr"))
    .asOpt.value

}
