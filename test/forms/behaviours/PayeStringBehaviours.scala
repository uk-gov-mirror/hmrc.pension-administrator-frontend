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

package forms.behaviours

import forms.FormSpec
import forms.mappings.{Constraints, PayeMappingString}
import play.api.data.{Form, FormError}
import wolfendale.scalacheck.regexp.RegexpGen

trait PayeStringBehaviours extends FormSpec with StringFieldBehaviours with Constraints with PayeMappingString {

  def formWithPayeField(
                         form: Form[String],
                         fieldName: String,
                         keyPayeRequired: String,
                         keyPayeLength: String,
                         keyPayeInvalid: String): Unit = {

    "behave like a form with a paye field" should {

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, keyPayeRequired)
      )

      behave like fieldThatBindsValidData(
        form,
        fieldName,
        RegexpGen.from(payeRegex)
      )

      behave like fieldWithMaxLength(
        form,
        fieldName,
        maxLength = PayeMappingString.maxPayeLength,
        lengthError = FormError(fieldName, keyPayeLength, Seq(PayeMappingString.maxPayeLength))
      )

      behave like fieldWithRegex(
        form,
        fieldName,
        invalidString = "A1_",
        FormError(fieldName, keyPayeInvalid, Seq(payeRegex))
      )

      behave like formWithTransform(
        form,
        Map(fieldName -> " 123 ab456 "),
        expectedData = "123AB456"
      )
    }
  }

}
