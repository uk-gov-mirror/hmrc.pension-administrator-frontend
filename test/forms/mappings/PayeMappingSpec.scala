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

import forms.behaviours.PayeBehaviours
import play.api.data.{Form, Mapping}

class PayeMappingSpec extends PayeBehaviours {

  "Paye mapping" should {
    val fieldName = "paye"
    val keyPayeLength = "companyDetails.error.payeEmployerReferenceNumber.length"
    val keyPayeInvalid = "companyDetails.error.payeEmployerReferenceNumber.invalid"

    val mapping: Mapping[String] = payeMapping(keyPayeLength, keyPayeInvalid)
    val form: Form[String] = Form(fieldName -> mapping)

    behave like formWithPayeField(
      form,
      fieldName,
      keyPayeLength,
      keyPayeInvalid
    )
  }

}
