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

package forms.register.partnership.partners

import forms.behaviours.UtrBehaviours

class PartnerUniqueTaxReferenceFormProviderSpec extends UtrBehaviours {

  val form = new PartnerUniqueTaxReferenceFormProvider()()

  "PartnerUniqueTaxReference form provider" must {
    behave like formWithUtr(
      form,
      keyUtrRequired = "common.error.utr.required",
      keyReasonRequired = "partnerUniqueTaxReference.error.reason.required",
      keyUtrLength = "common.error.utr.length",
      keyReasonLength = "common.error.utr.reason.length"
    )
  }
}
