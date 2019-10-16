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

package forms.mappings

import forms.behaviours.PayeStringBehaviours
import play.api.data.{Form, Mapping}

class PayeMappingStringSpec extends PayeStringBehaviours {

  "Paye mapping" should {
    val fieldName = "value"
    val keyPayeRequired = "enterPAYE.error.required"
    val keyPayeLength = "enterPAYE.error.length"
    val keyPayeInvalid = "enterPAYE.error.invalid"

    val mapping: Mapping[String] = payeMappingString(keyPayeRequired, keyPayeLength, keyPayeInvalid)
    val form: Form[String] = Form(fieldName -> mapping)

    behave like formWithMandatoryPayeField(
      form,
      fieldName,
      keyPayeRequired,
      keyPayeLength,
      keyPayeInvalid
    )
  }

}
