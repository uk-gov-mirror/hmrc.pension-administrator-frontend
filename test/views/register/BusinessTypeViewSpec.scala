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

package views.register

import play.api.data.Form
import forms.register.BusinessTypeFormProvider
import models.NormalMode
import models.register.BusinessType
import views.behaviours.ViewBehaviours
import views.html.register.businessType

class BusinessTypeViewSpec extends ViewBehaviours {

  private val messageKeyPrefix = "businessType"

  private val form = new BusinessTypeFormProvider()()

  private def createView = () => businessType(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  private def createViewUsingForm = (form: Form[_]) => businessType(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  "BusinessType view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithSubmitButton(createView)

  }

  "BusinessType view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- BusinessType.options) {
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, isChecked = false)
        }
      }
    }

    for(option <- BusinessType.options) {
      s"rendered with a value of '${option.value}'" must {
        s"have the '${option.value}' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> s"${option.value}"))))
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, isChecked = true)

          for(unselectedOption <- BusinessType.options.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, s"value-${unselectedOption.value}", "value", unselectedOption.value, isChecked = false)
          }
        }
      }
    }
  }

}
