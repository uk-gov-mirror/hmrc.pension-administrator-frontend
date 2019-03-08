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

package utils.checkyouranswers

import identifiers.TypedIdentifier
import models._
import models.register.adviser.AdviserDetails
import models.register.company.CompanyDetails
import play.api.libs.json.Reads
import utils.countryOptions.CountryOptions
import utils.{DateHelper, UserAnswers}
import viewmodels.{AnswerRow, Link}

import scala.language.implicitConversions

trait CheckYourAnswers[I <: TypedIdentifier.PathDependent] {
  def row(id: I)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow]
}

object CheckYourAnswers {

  implicit def personDetails[I <: TypedIdentifier[PersonDetails]](implicit r: Reads[PersonDetails]): CheckYourAnswers[I] = PersonDetailsCYA()()

  implicit def businessDetails[I <: TypedIdentifier[BusinessDetails]](implicit r: Reads[BusinessDetails]): CheckYourAnswers[I] = BusinessDetailsCYA()()

  implicit def companyDetails[I <: TypedIdentifier[CompanyDetails]](implicit r: Reads[CompanyDetails]): CheckYourAnswers[I] = CompanyDetailsCYA()()

  implicit def uniqueTaxReference[I <: TypedIdentifier[UniqueTaxReference]](implicit r: Reads[UniqueTaxReference]): CheckYourAnswers[I] = UniqueTaxReferenceCYA()()

  implicit def nino[I <: TypedIdentifier[Nino]](implicit r: Reads[Nino]): CheckYourAnswers[I] = NinoCYA()()

  implicit def tolerantAddress[I <: TypedIdentifier[TolerantAddress]](implicit r: Reads[TolerantAddress], countryOptions: CountryOptions): CheckYourAnswers[I] = TolerantAddressCYA()()

  implicit def address[I <: TypedIdentifier[Address]](implicit rds: Reads[Address], countryOptions: CountryOptions): CheckYourAnswers[I] = AddressCYA()()

  implicit def addressYears[I <: TypedIdentifier[AddressYears]](implicit r: Reads[AddressYears]): CheckYourAnswers[I] = AddressYearsCYA()()

  implicit def string[I <: TypedIdentifier[String]](implicit rds: Reads[String]): CheckYourAnswers[I] = StringCYA()()

  implicit def boolean[I <: TypedIdentifier[Boolean]](implicit rds: Reads[Boolean]): CheckYourAnswers[I] = BooleanCYA()()

  implicit def adviserDetails[I <: TypedIdentifier[AdviserDetails]](implicit r: Reads[AdviserDetails]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(id).map { adviserDetails =>
          Seq(
            AnswerRow("contactDetails.email.checkYourAnswersLabel", Seq(adviserDetails.email), false, changeUrl),
            AnswerRow("contactDetails.phone.checkYourAnswersLabel", Seq(adviserDetails.phone), false, changeUrl)
          )
        } getOrElse Seq.empty[AnswerRow]
      }
    }
  }

  implicit def paye[I <: TypedIdentifier[Paye]](implicit r: Reads[Paye]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          case Paye.Yes(paye) => Seq(
            AnswerRow(
              "commom.paye.label",
              Seq(paye),
              false,
              changeUrl
            )
          )
          case Paye.No => Seq(
            AnswerRow(
              "commom.paye.label",
              Seq("site.no"),
              true,
              changeUrl
            ))
        } getOrElse Seq.empty[AnswerRow]
    }
  }

  implicit def vat[I <: TypedIdentifier[Vat]](implicit r: Reads[Vat]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          case Vat.Yes(vat) => Seq(
            AnswerRow(
              "common.vatRegistrationNumber.checkYourAnswersLabel",
              Seq(vat),
              false,
              changeUrl
            )
          )
          case Vat.No => Seq(
            AnswerRow(
              "common.vatRegistrationNumber.checkYourAnswersLabel",
              Seq("site.no"),
              true,
              changeUrl
            ))
        } getOrElse Seq.empty[AnswerRow]
    }
  }

  implicit def contactDetails[I <: TypedIdentifier[ContactDetails]](implicit r: Reads[ContactDetails]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(id).map { contactDetails =>
          Seq(
            AnswerRow(
              "contactDetails.email",
              Seq(s"${contactDetails.email}"),
              false,
              changeUrl
            ),
            AnswerRow(
              "contactDetails.phone",
              Seq(s"${contactDetails.phone}"),
              false,
              changeUrl
            ))
        } getOrElse Seq.empty[AnswerRow]
      }
    }
  }

}

case class AddressCYA[I <: TypedIdentifier[Address]](label: String = "cya.label.address") {
  def apply()(implicit rds: Reads[Address], countryOptions: CountryOptions): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: Option[Link], userAnswers: UserAnswers) = {
        userAnswers.get(id).map { address =>
          Seq(AnswerRow(
            label,
            address.lines(countryOptions),
            false,
            changeUrl
          ))
        } getOrElse Seq.empty[AnswerRow]
      }
    }
  }
}

case class BusinessDetailsCYA[I <: TypedIdentifier[BusinessDetails]](nameLabel: String = "cya.label.name", utrLabel: String = "businessDetails.utr") {
  def apply()(implicit rds: Reads[BusinessDetails]): CheckYourAnswers[I] = new CheckYourAnswers[I] {
    override def row(id: I)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
      userAnswers.get(id).map { businessDetails =>
        val optionAnswerRow = businessDetails.uniqueTaxReferenceNumber.map{ utr =>
          AnswerRow(
            utrLabel,
            Seq(utr),
            false,
            None
          )
        }.toSeq

        val nameRow = AnswerRow(
          nameLabel,
          Seq(businessDetails.companyName),
          false,
          None
        )

        Seq(nameRow) ++ optionAnswerRow
      } getOrElse Seq.empty[AnswerRow]
  }
}

case class TolerantAddressCYA[I <: TypedIdentifier[TolerantAddress]](label: String = "common.manual.address.checkyouranswers") {
  def apply()(implicit r: Reads[TolerantAddress], countryOptions: CountryOptions): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(id).map { address =>
          Seq(AnswerRow(
            label,
            address.lines(countryOptions),
            false,
            None
          ))
        } getOrElse Seq.empty[AnswerRow]
      }
    }
  }
}

case class AddressYearsCYA[I <: TypedIdentifier[AddressYears]](label: String = "checkyouranswers.partnership.address.years") {
  def apply()(implicit r: Reads[AddressYears]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: Option[Link], userAnswers: UserAnswers) =
        userAnswers.get(id).map(addressYears =>
          Seq(AnswerRow(
            label,
            Seq(s"common.addressYears.$addressYears"),
            true,
            changeUrl
          ))) getOrElse Seq.empty[AnswerRow]
    }
  }
}

case class PersonDetailsCYA[I <: TypedIdentifier[PersonDetails]](label: String = "cya.label.name") {
  def apply()(implicit r: Reads[PersonDetails]): CheckYourAnswers[I] = new CheckYourAnswers[I] {
    override def row(id: I)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
      userAnswers.get(id).map { personDetails =>
        Seq(
          AnswerRow("cya.label.name", Seq(s"${personDetails.firstName} ${personDetails.lastName}"), false, changeUrl),
          AnswerRow("cya.label.dob", Seq(s"${DateHelper.formatDate(personDetails.dateOfBirth)}"), false, changeUrl)
        )
      } getOrElse Seq.empty[AnswerRow]
  }
}

case class NinoCYA[I <: TypedIdentifier[Nino]](
                                                questionLabel: String = "common.nino",
                                                ninoLabel: String = "common.nino",
                                                reasonLabel: String = "partnerNino.checkYourAnswersLabel.reason"
                                              ) {
  def apply()(implicit r: Reads[Nino]): CheckYourAnswers[I] = new CheckYourAnswers[I] {
    override def row(id: I)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
      userAnswers.get(id).map {
        case Nino.Yes(nino) => Seq(
          AnswerRow(questionLabel, Seq(s"${Nino.Yes}"), true, changeUrl),
          AnswerRow(ninoLabel, Seq(nino), true, changeUrl)
        )
        case Nino.No(reason) => Seq(
          AnswerRow(questionLabel, Seq(s"${Nino.No}"), true, changeUrl),
          AnswerRow(reasonLabel, Seq(reason), true, changeUrl)
        )
        case _ => Seq.empty[AnswerRow]
      } getOrElse Seq.empty[AnswerRow]
  }
}

case class UniqueTaxReferenceCYA[I <: TypedIdentifier[UniqueTaxReference]](
                                                                            questionLabel: String = "partnerUniqueTaxReference.checkYourAnswersLabel",
                                                                            utrLabel: String = "common.utr.text",
                                                                            reasonLabel: String = "partnerUniqueTaxReference.checkYourAnswersLabel.reason"
                                                                          ) {
  def apply()(implicit r: Reads[UniqueTaxReference]): CheckYourAnswers[I] = new CheckYourAnswers[I] {
    override def row(id: I)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
      userAnswers.get(id).map {
        case UniqueTaxReference.Yes(utr) => Seq(
          AnswerRow(questionLabel, Seq(s"${UniqueTaxReference.Yes}"), true, changeUrl),
          AnswerRow(utrLabel, Seq(utr), true, changeUrl)
        )
        case UniqueTaxReference.No(reason) => Seq(
          AnswerRow(questionLabel, Seq(s"${UniqueTaxReference.No}"), true, changeUrl),
          AnswerRow(reasonLabel, Seq(reason), true, changeUrl)
        )
        case _ => Seq.empty[AnswerRow]
      } getOrElse Seq.empty[AnswerRow]
  }
}

case class StringCYA[I <: TypedIdentifier[String]](label: Option[String] = None) {
  def apply()(implicit rds: Reads[String]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          string =>
            Seq(AnswerRow(
              label getOrElse s"${id.toString}.checkYourAnswersLabel",
              Seq(string),
              answerIsMessageKey = false,
              changeUrl
            ))
        } getOrElse Seq.empty[AnswerRow]
    }
}

case class CompanyDetailsCYA[I <: TypedIdentifier[CompanyDetails]](
                                                                    vatLabel: String = "companyDetails.vatRegistrationNumber.checkYourAnswersLabel",
                                                                    payeLabel: String = "companyDetails.payeEmployerReferenceNumber.checkYourAnswersLabel"
                                                                  ) {
  def apply()(implicit r: Reads[CompanyDetails]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map { companyDetails =>

          val vat = companyDetails.vatRegistrationNumber.map { vatRegNo =>
            Seq(AnswerRow(
              vatLabel,
              Seq(vatRegNo),
              false,
              Link(controllers.register.company.routes.CompanyDetailsController.onPageLoad(CheckMode).url)
            ))
          } getOrElse Seq.empty[AnswerRow]

          val paye = companyDetails.payeEmployerReferenceNumber.map { payeRefNo =>
            Seq(AnswerRow(
              payeLabel,
              Seq(payeRefNo),
              false,
              Link(controllers.register.company.routes.CompanyDetailsController.onPageLoad(CheckMode).url)
            ))
          } getOrElse Seq.empty[AnswerRow]

          vat ++ paye

        } getOrElse Seq.empty[AnswerRow]
    }
}

case class BooleanCYA[I <: TypedIdentifier[Boolean]](label: Option[String] = None) {
  implicit def apply()(implicit rds: Reads[Boolean]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          flag =>
            Seq(AnswerRow(
              label getOrElse s"${id.toString}.checkYourAnswersLabel",
              Seq(if (flag) "site.yes" else "site.no"),
              answerIsMessageKey = true,
              changeUrl
            ))
        } getOrElse Seq.empty[AnswerRow]
    }
  }
}