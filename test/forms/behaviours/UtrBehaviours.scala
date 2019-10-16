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

package forms.behaviours

import forms.FormSpec
import forms.mappings.{RegexBehaviourSpec, UtrMapping}
import models.UniqueTaxReference
import org.apache.commons.lang3.RandomStringUtils
import play.api.data.{Form, FormError}

class UtrBehaviours extends FormSpec with UtrMapping with RegexBehaviourSpec {

  def formWithUniqueTaxpayerReference(
                   testForm: Form[UniqueTaxReference],
                   keyRequired: String,
                   keyUtrRequired: String,
                   keyReasonRequired: String,
                   keyUtrLength: String,
                   keyReasonLength: String
                 ): Unit = {

    "fail to bind when form is empty" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors shouldBe Seq(FormError("utr.hasUtr", keyRequired))
    }

    "fail to bind when yes is selected but utr is not provided" in {
      val result = testForm.bind(Map("utr.hasUtr" -> "true"))
      result.errors shouldBe Seq(FormError("utr.utr", keyUtrRequired))
    }

    "fail to bind when no is selected but reason is not provided" in {
      val result = testForm.bind(Map("utr.hasUtr" -> "false"))
      result.errors shouldBe Seq(FormError("utr.reason", keyReasonRequired))
    }

    "fail to bind when yes is selected and utr exceeds max length of 10" in {
      val testString = RandomStringUtils.randomAlphabetic(UtrMapping.utrMaxLength + 1)
      val result = testForm.bind(Map("utr.hasUtr" -> "true", "utr.utr" -> testString))
      result.errors shouldBe Seq(FormError("utr.utr", keyUtrLength, Seq(UtrMapping.utrMaxLength)))
    }

    "fail to bind when no is selected and reason exceeds max length of 150" in {
      val testString = RandomStringUtils.randomAlphabetic(UtrMapping.reasonMaxLength + 1)
      val result = testForm.bind(Map("utr.hasUtr" -> "false", "utr.reason" -> testString))
      result.errors shouldBe Seq(FormError("utr.reason", keyReasonLength, Seq(UtrMapping.reasonMaxLength)))
    }

    val valid = Table(
      "data",
      Map("utr.hasUtr" -> "true", "utr.utr" -> " 1234567890 ")
    )

    val invalid = Table(
      "data",
      Map("utr.hasUtr" -> "true", "utr.utr" -> "123456789"),
      Map("utr.hasUtr" -> "true", "utr.utr" -> "12345678901"),
      Map("utr.hasUtr" -> "true", "utr.utr" -> "A234567890"),
      Map("utr.hasUtr" -> "false", "utr.reason" -> "{Not known}")
    )

    "remove spaces" in {
      val actual = testForm.bind(Map(
        "utr.hasUtr" -> "true",
        "utr.utr" -> "  123 456 7890 "))
      actual.errors shouldBe empty
    }

    behave like formWithRegex(testForm, valid, invalid)
  }

  def formWithUtr(
                   testForm: Form[String],
                   keyUtrRequired: String,
                   keyUtrLength: String
                 ): Unit = {

    "fail to bind when form is empty" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors shouldBe Seq(FormError("utr", keyUtrRequired))
    }

    val valid = Table(
      "data",
      Map("utr" -> " 1234567890 ")
    )

    val invalid = Table(
      "data",
      Map("utr" -> "123456789"),
      Map("utr" -> "12345678901"),
      Map("utr" -> "A234567890")
    )

    "remove spaces" in {
      val actual = testForm.bind(Map(
        "utr" -> "  123 456 7890 "))
      actual.errors shouldBe empty
    }

    behave like formWithRegex(testForm, valid, invalid)
  }

}
