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

import identifiers.register.company.directors.{DirectorAddressId, DirectorPreviousAddressListId}
import identifiers.register.company.{CompanyUniqueTaxReferenceId, ContactDetailsId}
import models.{Address, CheckMode, UniqueTaxReference, Nino}
import viewmodels.AnswerRow

class CheckYourAnswersHelper(userAnswers: UserAnswers, countryOptions: CountryOptions) {

  def directorContactDetails(index: Int): Seq[AnswerRow] = userAnswers.get(identifiers.register.company.directors.DirectorContactDetailsId(index)) match {
    case Some(x) => Seq(
      AnswerRow("contactDetails.email", Seq(s"${x.email}"), false,
        controllers.register.company.directors.routes.DirectorContactDetailsController.onPageLoad(CheckMode, index).url),
      AnswerRow("contactDetails.phone", Seq(s"${x.phone}"), false,
        controllers.register.company.directors.routes.DirectorContactDetailsController.onPageLoad(CheckMode, index).url)
    )

    case _ => Nil
  }

  def companyDirectorAddressList(index: Int): Option[AnswerRow] = userAnswers.get(identifiers.register.company.directors.CompanyDirectorAddressListId(index)) map {
    x => AnswerRow("companyDirectorAddressList.checkYourAnswersLabel", Seq(s"companyDirectorAddressList.$x"), true, controllers.register.company.directors.routes.CompanyDirectorAddressListController.onPageLoad(CheckMode, index).url)
  }
  
  def directorPreviousAddressList(index: Int): Option[AnswerRow] = userAnswers.get(DirectorPreviousAddressListId(index)) map {
    x => AnswerRow("directorPreviousAddressList.checkYourAnswersLabel", Seq(s"directorPreviousAddressList.$x"), true, controllers.register.company.directors.routes.DirectorPreviousAddressListController.onPageLoad(CheckMode, index).url)
  }

  def companyDirectorAddressPostCodeLookup(index: Int): Option[AnswerRow] =
    userAnswers.get(identifiers.register.company.directors.CompanyDirectorAddressPostCodeLookupId(index)) map {
    x => AnswerRow("companyDirectorAddressPostCodeLookup.checkYourAnswersLabel", Seq(s"$x"), false,
      controllers.register.company.directors.routes.CompanyDirectorAddressPostCodeLookupController.onPageLoad(CheckMode, index).url)
  }

  def directorPreviousAddressPostCodeLookup(index: Int): Option[AnswerRow] =
    userAnswers.get(identifiers.register.company.directors.DirectorPreviousAddressPostCodeLookupId(index)) map {
    x => AnswerRow("directorPreviousAddressPostCodeLookup.checkYourAnswersLabel", Seq(s"$x"), false,
      controllers.register.company.directors.routes.DirectorPreviousAddressPostCodeLookupController.onPageLoad(CheckMode, index).url)
  }

  def directorAddress(index: Int): Seq[AnswerRow] = userAnswers.get(DirectorAddressId(index)) match {
    case Some(x) => Seq(AnswerRow("cya.label.address", addressAnswer(x), false,
      controllers.register.company.directors.routes.DirectorAddressController.onPageLoad(CheckMode, index).url))
    case _ => Nil
  }

  def directorPreviousAddress(index: Int): Seq[AnswerRow] = userAnswers.get(identifiers.register.company.directors.DirectorPreviousAddressId(index)) match {
    case Some(x) => Seq(AnswerRow("directorPreviousAddress.checkYourAnswersLabel", addressAnswer(x), false,
      controllers.register.company.directors.routes.DirectorPreviousAddressController.onPageLoad(CheckMode, index).url))
    case _ => Nil
  }

  def directorUniqueTaxReference(index: Int): Seq[AnswerRow] = userAnswers.get(identifiers.register.company.directors.DirectorUniqueTaxReferenceId(index)) match {
    case Some(UniqueTaxReference.Yes(utr)) => Seq(
      AnswerRow("directorUniqueTaxReference.checkYourAnswersLabel", Seq(s"${UniqueTaxReference.Yes}"), true,
        controllers.register.company.directors.routes.DirectorUniqueTaxReferenceController.onPageLoad(CheckMode, index).url),
      AnswerRow("directorUniqueTaxReference.checkYourAnswersLabel.utr", Seq(utr), true,
        controllers.register.company.directors.routes.DirectorUniqueTaxReferenceController.onPageLoad(CheckMode, index).url)
    )

    case Some(UniqueTaxReference.No(reason)) => Seq(
      AnswerRow("directorUniqueTaxReference.checkYourAnswersLabel", Seq(s"${UniqueTaxReference.No}"), true,
        controllers.register.company.directors.routes.DirectorUniqueTaxReferenceController.onPageLoad(CheckMode, index).url),
      AnswerRow("directorUniqueTaxReference.checkYourAnswersLabel.reason", Seq(reason), true,
        controllers.register.company.directors.routes.DirectorUniqueTaxReferenceController.onPageLoad(CheckMode, index).url)
    )

    case _ => Nil
  }

  def directorAddressYears(index: Int): Seq[AnswerRow] = userAnswers.get(identifiers.register.company.directors.DirectorAddressYearsId(index)) match {
    case Some(x) => Seq(AnswerRow("directorAddressYears.checkYourAnswersLabel", Seq(s"common.addressYears.$x"), true,
      controllers.register.company.directors.routes.DirectorAddressYearsController.onPageLoad(CheckMode, index).url))

    case _ => Nil
  }

  def directorDetails(index: Int): Seq[AnswerRow] = userAnswers.get(identifiers.register.company.directors.DirectorDetailsId(index)) match {
    case Some(x) => Seq(AnswerRow("cya.label.name", Seq(s"${x.firstName} ${x.lastName}"), false,
      controllers.register.company.directors.routes.DirectorDetailsController.onPageLoad(CheckMode, index).url),
      AnswerRow("cya.label.dob", Seq(s"${DateHelper.formatDate(x.dateOfBirth)}"), false,
        controllers.register.company.directors.routes.DirectorDetailsController.onPageLoad(CheckMode, index).url))
    case _ => Nil
  }

  def directorNino(index: Int): Seq[AnswerRow] = userAnswers.get(identifiers.register.company.directors.DirectorNinoId(index)) match {
    case Some(Nino.Yes(nino)) => Seq(
      AnswerRow("directorNino.checkYourAnswersLabel", Seq(s"${Nino.Yes}"), true,
        controllers.register.company.directors.routes.DirectorNinoController.onPageLoad(CheckMode, index).url),
      AnswerRow("directorNino.checkYourAnswersLabel.nino", Seq(nino), true,
        controllers.register.company.directors.routes.DirectorNinoController.onPageLoad(CheckMode, index).url)
    )

    case Some(Nino.No(reason)) => Seq(
      AnswerRow("directorNino.checkYourAnswersLabel", Seq(s"${Nino.No}"), true,
        controllers.register.company.directors.routes.DirectorNinoController.onPageLoad(CheckMode, index).url),
      AnswerRow("directorNino.checkYourAnswersLabel.reason", Seq(reason), true,
        controllers.register.company.directors.routes.DirectorNinoController.onPageLoad(CheckMode, index).url)
    )

    case _ => Nil
  }

  def companyPreviousAddress: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyPreviousAddressId) map {
    x => AnswerRow("companyPreviousAddress.checkYourAnswersLabel", addressAnswer(x), false, controllers.register.company.routes.CompanyPreviousAddressController.onPageLoad(CheckMode).url)
  }

  def companyAddressList: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyAddressListId) map {
    x => AnswerRow("companyAddressList.checkYourAnswersLabel", Seq(s"companyAddressList.$x"), true, controllers.register.company.routes.CompanyAddressListController.onPageLoad(CheckMode).url)
  }

  def companyPreviousAddressPostCodeLookup: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyPreviousAddressPostCodeLookupId) map {
    x => AnswerRow("companyPreviousAddressPostCodeLookup.checkYourAnswersLabel", Seq(s"$x"), false, controllers.register.company.routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(CheckMode).url)
  }

  def addCompanyDirectors: Option[AnswerRow] = userAnswers.get(identifiers.register.company.AddCompanyDirectorsId) map {
    x => AnswerRow("addCompanyDirectors.checkYourAnswersLabel", Seq(if(x) "site.yes" else "site.no"), true, controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(CheckMode).url)
  }

  def moreThanTenDirectors: Option[AnswerRow] = userAnswers.get(identifiers.register.company.MoreThanTenDirectorsId) map {
    x => AnswerRow("moreThanTenDirectors.checkYourAnswersLabel", Seq(if(x) "site.yes" else "site.no"), true, controllers.register.company.routes.MoreThanTenDirectorsController.onPageLoad(CheckMode).url)
  }

  def email: Option[AnswerRow] = userAnswers.get(ContactDetailsId) map { x =>
    AnswerRow(
      "contactDetails.email.checkYourAnswersLabel",
      Seq(x.email),
      false,
      controllers.register.company.routes.ContactDetailsController.onPageLoad(CheckMode).url
    )
  }

  def phone: Option[AnswerRow] = userAnswers.get(ContactDetailsId) map { x =>
    AnswerRow(
      "contactDetails.phone.checkYourAnswersLabel",
      Seq(x.phone),
      false,
      controllers.register.company.routes.ContactDetailsController.onPageLoad(CheckMode).url
    )
  }

  def companyDetails: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyDetailsId) map { x =>
    AnswerRow(
      "companyDetails.checkYourAnswersLabel",
      Seq(x.companyName),
      false,
      controllers.register.company.routes.CompanyDetailsController.onPageLoad(CheckMode).url
    )
  }

  def vatRegistrationNumber: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyDetailsId) flatMap { x =>
    x.vatRegistrationNumber map { vatRegNo =>
      AnswerRow(
        "companyDetails.vatRegistrationNumber.checkYourAnswersLabel",
        Seq(vatRegNo),
        false,
        controllers.register.company.routes.CompanyDetailsController.onPageLoad(CheckMode).url
      )
    }
  }

  def payeEmployerReferenceNumber: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyDetailsId) flatMap { x =>
    x.payeEmployerReferenceNumber map { payeRefNo =>
      AnswerRow(
        "companyDetails.payeEmployerReferenceNumber.checkYourAnswersLabel",
        Seq(payeRefNo),
        false,
        controllers.register.company.routes.CompanyDetailsController.onPageLoad(CheckMode).url
      )
    }
  }

  def companyAddressYears: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyAddressYearsId) map {
    x => AnswerRow("companyAddressYears.checkYourAnswersLabel", Seq(s"common.addressYears.$x"), true, controllers.register.company.routes.CompanyAddressYearsController.onPageLoad(CheckMode).url)
  }

  def companyAddress: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyAddressId) flatMap { x =>
    x.toAddress map { address =>
      AnswerRow("companyAddress.checkYourAnswersLabel", addressAnswer(address), false, controllers.register.company.routes.CompanyAddressController.onPageLoad().url)
    }
  }

  def companyUniqueTaxReference: Option[AnswerRow] = userAnswers.get(CompanyUniqueTaxReferenceId) map {
    x => AnswerRow("companyUniqueTaxReference.checkYourAnswersLabel", Seq(s"$x"), false, controllers.register.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(CheckMode).url)
  }

  def companyRegistrationNumber: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyRegistrationNumberId) map {
    x => AnswerRow("companyRegistrationNumber.checkYourAnswersLabel", Seq(s"$x"), false, controllers.register.company.routes.CompanyRegistrationNumberController.onPageLoad(CheckMode).url)
  }

  private def addressAnswer(address: Address): Seq[String] = {
    val country = countryOptions.options
      .find(_.value == address.country)
      .map(_.label)
      .getOrElse(address.country)

    Seq(
      Some(s"${address.addressLine1},"),
      Some(s"${address.addressLine2},"),
      address.addressLine3.map(line3 => s"$line3,"),
      address.addressLine4.map(line4 => s"$line4,"),
      address.postcode.map(postcode => s"$postcode,"),
      Some(country)
    ).flatten
  }

}
