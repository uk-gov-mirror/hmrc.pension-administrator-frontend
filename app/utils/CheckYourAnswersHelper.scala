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

package utils

import identifiers.register.company.{CompanyUniqueTaxReferenceId, ContactDetailsId, DirectorAddressId, DirectorPreviousAddressListId}
import models.register.company.DirectorNino.{No, Yes}
import models.register.company.{DirectorNino, DirectorUniqueTaxReference}
import models.{Address, CheckMode}
import viewmodels.AnswerRow

class CheckYourAnswersHelper(userAnswers: UserAnswers, countryOptions: CountryOptions) {

  def directorContactDetails(index: Int): Seq[AnswerRow] = userAnswers.get(identifiers.register.company.DirectorContactDetailsId(index)) match {
    case Some(x) => Seq(
        AnswerRow("contactDetails.email", s"${x.email}", false,
          controllers.register.company.routes.DirectorContactDetailsController.onPageLoad(CheckMode, index).url),
        AnswerRow("contactDetails.phone", s"${x.phone}", false,
          controllers.register.company.routes.DirectorContactDetailsController.onPageLoad(CheckMode, index).url)
      )

    case _ => Nil
  }

  def companyDirectorAddressList(index: Int): Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyDirectorAddressListId(index)) map {
    x => AnswerRow("companyDirectorAddressList.checkYourAnswersLabel", s"companyDirectorAddressList.$x", true, controllers.register.company.routes.CompanyDirectorAddressListController.onPageLoad(CheckMode, index).url)
  }
  
  def directorPreviousAddressList(index: Int): Option[AnswerRow] = userAnswers.get(DirectorPreviousAddressListId(index)) map {
    x => AnswerRow("directorPreviousAddressList.checkYourAnswersLabel", s"directorPreviousAddressList.$x", true, controllers.register.company.routes.DirectorPreviousAddressListController.onPageLoad(CheckMode, index).url)
  }

  def companyDirectorAddressPostCodeLookup(index: Int): Option[AnswerRow] =
    userAnswers.get(identifiers.register.company.CompanyDirectorAddressPostCodeLookupId(index)) map {
    x => AnswerRow("companyDirectorAddressPostCodeLookup.checkYourAnswersLabel", s"$x", false,
      controllers.register.company.routes.CompanyDirectorAddressPostCodeLookupController.onPageLoad(CheckMode, index).url)
  }

  def directorPreviousAddressPostCodeLookup(index: Int): Option[AnswerRow] =
    userAnswers.get(identifiers.register.company.DirectorPreviousAddressPostCodeLookupId(index)) map {
    x => AnswerRow("directorPreviousAddressPostCodeLookup.checkYourAnswersLabel", s"$x", false,
      controllers.register.company.routes.DirectorPreviousAddressPostCodeLookupController.onPageLoad(CheckMode, index).url)
  }

  def directorAddress(index: Int): Seq[AnswerRow] = userAnswers.get(DirectorAddressId(index)) match {
    case Some(x) => Seq(AnswerRow("cya.label.address", addressAnswer(x), false,
      controllers.register.company.routes.DirectorAddressController.onPageLoad(CheckMode, index).url))
    case _ => Nil
  }

  def directorPreviousAddress(index: Int): Seq[AnswerRow] = userAnswers.get(identifiers.register.company.DirectorPreviousAddressId(index)) match {
    case Some(x) => Seq(AnswerRow("directorPreviousAddress.checkYourAnswersLabel", addressAnswer(x), false,
      controllers.register.company.routes.DirectorPreviousAddressController.onPageLoad(CheckMode, index).url))
    case _ => Nil
  }

  def addressAnswer(address: Address): String = {
    val country = countryOptions.options.find(_.value == address.country).map(_.label).getOrElse(address.country)
    Seq(Some(s"${address.addressLine1},"), Some(s"${address.addressLine2},"), address.addressLine3.map(line3 => s"$line3,"),
      address.addressLine4.map(line4 => s"$line4,"), address.postcode.map(postcode => s"$postcode,"), Some(country)).flatten.mkString
  }

  def directorUniqueTaxReference(index: Int): Seq[AnswerRow] = userAnswers.get(identifiers.register.company.DirectorUniqueTaxReferenceId(index)) match {
    case Some(DirectorUniqueTaxReference.Yes(utr)) => Seq(
      AnswerRow("directorUniqueTaxReference.checkYourAnswersLabel", s"${DirectorUniqueTaxReference.Yes}", true,
        controllers.register.company.routes.DirectorUniqueTaxReferenceController.onPageLoad(CheckMode, index).url),
      AnswerRow("directorUniqueTaxReference.checkYourAnswersLabel.utr", utr, true,
        controllers.register.company.routes.DirectorUniqueTaxReferenceController.onPageLoad(CheckMode, index).url)
    )

    case Some(DirectorUniqueTaxReference.No(reason)) => Seq(
      AnswerRow("directorUniqueTaxReference.checkYourAnswersLabel", s"${DirectorUniqueTaxReference.No}", true,
        controllers.register.company.routes.DirectorUniqueTaxReferenceController.onPageLoad(CheckMode, index).url),
      AnswerRow("directorUniqueTaxReference.checkYourAnswersLabel.reason", reason, true,
        controllers.register.company.routes.DirectorUniqueTaxReferenceController.onPageLoad(CheckMode, index).url)
    )

    case _ => Nil
  }

  def directorAddressYears(index: Int): Seq[AnswerRow] = userAnswers.get(identifiers.register.company.DirectorAddressYearsId(index)) match {
    case Some(x) => Seq(AnswerRow("directorAddressYears.checkYourAnswersLabel", s"directorAddressYears.$x", true,
      controllers.register.company.routes.DirectorAddressYearsController.onPageLoad(CheckMode, index).url))

    case _ => Nil
  }

  def directorDetails(index: Int): Seq[AnswerRow] = userAnswers.get(identifiers.register.company.DirectorDetailsId(index)) match {
    case Some(x) => Seq(AnswerRow("cya.label.name", s"${x.firstName} ${x.lastName}", false,
      controllers.register.company.routes.DirectorDetailsController.onPageLoad(CheckMode, index).url),
      AnswerRow("cya.label.dob", s"${DateHelper.formatDate(x.dateOfBirth)}", false,
        controllers.register.company.routes.DirectorDetailsController.onPageLoad(CheckMode, index).url))
    case _ => Nil
  }

  def directorNino(index: Int): Seq[AnswerRow] = userAnswers.get(identifiers.register.company.DirectorNinoId(index)) match {
    case Some(Yes(nino)) => Seq(
      AnswerRow("directorNino.checkYourAnswersLabel", s"${DirectorNino.Yes}", true,
        controllers.register.company.routes.DirectorNinoController.onPageLoad(CheckMode, index).url),
      AnswerRow("directorNino.checkYourAnswersLabel.nino", nino, true,
        controllers.register.company.routes.DirectorNinoController.onPageLoad(CheckMode, index).url)
    )

    case Some(No(reason)) => Seq(
      AnswerRow("directorNino.checkYourAnswersLabel", s"${DirectorNino.No}", true,
        controllers.register.company.routes.DirectorNinoController.onPageLoad(CheckMode, index).url),
      AnswerRow("directorNino.checkYourAnswersLabel.reason", reason, true,
        controllers.register.company.routes.DirectorNinoController.onPageLoad(CheckMode, index).url)
    )

    case _ => Nil
  }

  def companyPreviousAddress: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyPreviousAddressId) map {
    x => AnswerRow("companyPreviousAddress.checkYourAnswersLabel", s"${x.addressLine1} ${x.addressLine2}", false, controllers.register.company.routes.CompanyPreviousAddressController.onPageLoad(CheckMode).url)
  }

  def companyAddressList: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyAddressListId) map {
    x => AnswerRow("companyAddressList.checkYourAnswersLabel", s"companyAddressList.$x", true, controllers.register.company.routes.CompanyAddressListController.onPageLoad(CheckMode).url)
  }

  def companyPreviousAddressPostCodeLookup: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyPreviousAddressPostCodeLookupId) map {
    x => AnswerRow("companyPreviousAddressPostCodeLookup.checkYourAnswersLabel", s"$x", false, controllers.register.company.routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(CheckMode).url)
  }

  def addCompanyDirectors: Option[AnswerRow] = userAnswers.get(identifiers.register.company.AddCompanyDirectorsId) map {
    x => AnswerRow("addCompanyDirectors.checkYourAnswersLabel", if(x) "site.yes" else "site.no", true, controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(CheckMode).url)
  }

  def moreThanTenDirectors: Option[AnswerRow] = userAnswers.get(identifiers.register.company.MoreThanTenDirectorsId) map {
    x => AnswerRow("moreThanTenDirectors.checkYourAnswersLabel", if(x) "site.yes" else "site.no", true, controllers.register.company.routes.MoreThanTenDirectorsController.onPageLoad(CheckMode).url)
  }

  def contactDetails: Option[AnswerRow] = userAnswers.get(ContactDetailsId) map {
    x => AnswerRow("contactDetails.checkYourAnswersLabel", s"${x.email} ${x.phone}", false, controllers.register.company.routes.ContactDetailsController.onPageLoad(CheckMode).url)
  }
  
  def companyDetails: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyDetailsId) map {
    x => AnswerRow("companyDetails.checkYourAnswersLabel", s"${x.companyName} ${x.vatRegistrationNumber} ${x.payeEmployerReferenceNumber}", false, controllers.register.company.routes.CompanyDetailsController.onPageLoad(CheckMode).url)
  }

  def companyAddressYears: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyAddressYearsId) map {
    x => AnswerRow("companyAddressYears.checkYourAnswersLabel", s"companyAddressYears.$x", true, controllers.register.company.routes.CompanyAddressYearsController.onPageLoad(CheckMode).url)
  }

  def companyUniqueTaxReference: Option[AnswerRow] = userAnswers.get(CompanyUniqueTaxReferenceId) map {
    x => AnswerRow("companyUniqueTaxReference.checkYourAnswersLabel", s"$x", false, controllers.register.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(CheckMode).url)
  }

  def companyRegistrationNumber: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyRegistrationNumberId) map {
    x => AnswerRow("companyRegistrationNumber.checkYourAnswersLabel", s"$x", false, controllers.register.company.routes.CompanyRegistrationNumberController.onPageLoad(CheckMode).url)
  }
}
