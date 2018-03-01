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

package forms.mappings

import java.time.LocalDate

import models.register.company.{DirectorNino, DirectorUniqueTaxReference}
import play.api.data.Forms.{of, optional, tuple}
import play.api.data.{FieldMapping, Mapping}
import uk.gov.voa.play.form.ConditionalMappings._
import utils.Enumerable

import scala.util.Try

trait Mappings extends Formatters with Constraints {

  protected def text(errorKey: String = "error.required"): FieldMapping[String] =
    of(stringFormatter(errorKey))

  protected def int(requiredKey: String = "error.required",
                    wholeNumberKey: String = "error.wholeNumber",
                    nonNumericKey: String = "error.nonNumeric"): FieldMapping[Int] =
    of(intFormatter(requiredKey, wholeNumberKey, nonNumericKey))

  protected def boolean(requiredKey: String = "error.required",
                        invalidKey: String = "error.boolean"): FieldMapping[Boolean] =
    of(booleanFormatter(requiredKey, invalidKey))

  protected def enumerable[A](requiredKey: String = "error.required",
                              invalidKey: String = "error.invalid")(implicit ev: Enumerable[A]): FieldMapping[A] =
    of(enumerableFormatter[A](requiredKey, invalidKey))

  protected def postCode(requiredKey: String, invalidKey: String): Mapping[Option[String]] = {

    def toPostCode(data: (Option[String], Option[String])): Option[String] = data._2

    def fromPostCode(data: Option[String]): (Option[String], Option[String]) = (data, data)

    tuple(
      "postCode" -> mandatoryIfEqual[String]("country", "GB", text(requiredKey).verifying(postalCode(invalidKey))),
      "postCode" -> optional(text(requiredKey))
    ).transform(toPostCode, fromPostCode)

  }


  protected def directorNinoMapping(requiredKey: String = "directorNino.error.required",
                                    requiredNinoKey: String = "common.error.nino.required",
                                    requiredReasonKey: String = "directorNino.error.reason.required",
                                    reasonLengthKey: String = "directorNino.error.reason.length",
                                    invalidNinoKey: String = "common.error.nino.invalid"):
  Mapping[DirectorNino] = {
    val reasonMaxLength = 150

    def fromDirectorNino(nino: DirectorNino): (Boolean, Option[String], Option[String]) = {
      nino match {
        case DirectorNino.Yes(ninoNo) => (true, Some(ninoNo), None)
        case DirectorNino.No(reason) =>  (false, None, Some(reason))
      }
    }

    def toDirectorNino(ninoTuple: (Boolean, Option[String], Option[String])) = {

      ninoTuple match {
        case (true, Some(nino), None)  => DirectorNino.Yes(nino)
        case (false, None, Some(reason))  => DirectorNino.No(reason)
        case _ => throw new RuntimeException("Invalid selection")
      }
    }

    tuple("hasNino" -> boolean(requiredKey),
      "nino" -> mandatoryIfTrue("directorNino.hasNino", text(requiredNinoKey).verifying(validNino(invalidNinoKey))),
      "reason" -> mandatoryIfFalse("directorNino.hasNino", text(requiredReasonKey).
        verifying(maxLength(reasonMaxLength,reasonLengthKey)))).transform(toDirectorNino, fromDirectorNino)
  }

  protected def date(requiredKey: String, invalidKey: String): Mapping[LocalDate] = {

    def toLocalDate(input: (Int, Int, Int)): LocalDate = {
      LocalDate.of(input._3, input._2, input._1)
    }

    def fromLocalDate(date: LocalDate): (Int, Int, Int) = {
      (date.getDayOfMonth, date.getMonthValue, date.getYear)
    }

    def validDate(input: (Int, Int, Int)): Boolean = {
      Try(toLocalDate(input)).isSuccess
    }

    tuple(
      "day" -> int("error.date.day_blank", "error.date.day_invalid", "error.date.day_invalid"),
      "month" -> int("error.date.month_blank", "error.date.month_invalid", "error.date.month_invalid"),
      "year" -> int("error.date.year_blank", "error.date.year_invalid", "error.date.year_invalid")
    ).verifying(invalidKey, (inputs) => validDate(inputs))
      .transform(toLocalDate, fromLocalDate)

  }

  protected def directorUtrMapping(requiredKey: String = "directorUniqueTaxReference.error.required",
                                                  requiredUtrKey: String  = "directorUniqueTaxReference.error.utr.required",
                                                  utrLengthKey: String = "directorUniqueTaxReference.error.utr.length",
                                                  requiredReasonKey: String = "directorUniqueTaxReference.error.reason.required",
                                                  reasonLengthKey : String = "directorUniqueTaxReference.error.reason.length"):
  Mapping[DirectorUniqueTaxReference] = {
    val utrMaxLength = 10
    val reasonMaxLength = 150

    def toDirectorUtr(utrTuple: (Boolean, Option[String], Option[String])) = {
      utrTuple match {
        case (true, Some(utr), None) => DirectorUniqueTaxReference.Yes(utr)
        case (false, None, Some(reason)) => DirectorUniqueTaxReference.No(reason)
        case _ => throw new RuntimeException("Invalid selection")
      }
    }

    def fromDirectorUtr(utr: DirectorUniqueTaxReference) ={
      utr match {
        case DirectorUniqueTaxReference.Yes(utr) => (true, Some(utr), None)
        case DirectorUniqueTaxReference.No(reason) => (false, None, Some(reason))
      }
    }

    tuple("hasUtr" -> boolean(requiredKey),
    "utr" -> mandatoryIfTrue("directorUtr.hasUtr", text(requiredUtrKey)
      .verifying(maxLength(utrMaxLength, utrLengthKey))),
    "reason" -> mandatoryIfFalse("directorUtr.hasUtr", text(requiredReasonKey)
      .verifying(maxLength(reasonMaxLength, reasonLengthKey)))).transform(toDirectorUtr,fromDirectorUtr)
  }
}