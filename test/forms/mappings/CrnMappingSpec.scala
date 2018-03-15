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

import forms.behaviours.CrnBehaviours
import play.api.data.{Form, Mapping}

class CrnMappingSpec extends CrnBehaviours {

  "CRN mapping" should {

    val fieldName = "crn"
    val keyCrnRequired = "companyRegistrationNumber.error.required"
    val keyCrnLength = "companyRegistrationNumber.error.length"
    val keyCrnInvalid = "companyRegistrationNumber.error.invalid"

    val mapping: Mapping[String] = crnMapping(keyCrnRequired, keyCrnLength, keyCrnInvalid)
    val form: Form[String] = Form(fieldName -> mapping)

    behave like formWithCrnField(
      form,
      fieldName,
      keyCrnRequired,
      keyCrnLength,
      keyCrnInvalid
    )

  }

}
