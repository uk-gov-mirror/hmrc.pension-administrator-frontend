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

package forms.register

import forms.mappings.{Constraints, Mappings, Transforms}
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.Messages
import viewmodels.Message

class NINOFormProvider @Inject()() extends Mappings with Constraints with Transforms {
  def apply(personName: String)(implicit messages: Messages): Form[String] =
    Form(
      "value" -> text(Message("enterNINO.error.required", personName).resolve).transform(noSpaceWithUpperCaseTransform, noTransform).
        verifying(validNino(Message("enterNINO.error.invalid", personName)))
    )
}
