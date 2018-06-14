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

package forms.register.company

import forms.behaviours.{EmailBehaviours, PhoneNumberBehaviours, StringFieldBehaviours}
import forms.mappings.Constraints
import models.ContactDetails

class ContactDetailsFormProviderSpec extends StringFieldBehaviours with EmailBehaviours with PhoneNumberBehaviours with Constraints {

  val form = new ContactDetailsFormProvider()()

  ".emailAddress" must {

    val fieldName = "emailAddress"
    val keyEmailRequired = "contactDetails.error.email.required"
    val keyEmailLength = "contactDetails.error.email.length"
    val keyEmailInvalid = "contactDetails.error.email.invalid"

    behave like formWithEmailField(
      form,
      fieldName,
      keyEmailRequired,
      keyEmailLength,
      keyEmailInvalid
    )

  }

  ".phoneNumber" must {
    val fieldName = "phoneNumber"
    val keyPhoneNumberRequired = "contactDetails.error.phone.required"
    val keyPhoneNumberLength = "contactDetails.error.phone.length"
    val keyPhoneNumberInvalid = "contactDetails.error.phone.invalid"

    behave like formWithPhoneNumberField(
      form,
      fieldName,
      keyPhoneNumberRequired,
      keyPhoneNumberLength,
      keyPhoneNumberInvalid
    )

  }

  "form" must {
    val rawData = Map("emailAddress" -> "test@test.com", "phoneNumber" -> " 123456789012345678901234 ")
    val expectedData = ContactDetails("test@test.com", "123456789012345678901234")

    behave like formWithTransform(
      form,
      rawData,
      expectedData
    )
  }

}
