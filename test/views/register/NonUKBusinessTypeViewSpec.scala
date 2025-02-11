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

package views.register

import forms.register.NonUKBusinessTypeFormProvider
import models.register.NonUKBusinessType
import play.api.data.Form
import play.twirl.api.Html
import views.behaviours.ViewBehaviours
import views.html.register.nonUKBusinessType

class NonUKBusinessTypeViewSpec extends ViewBehaviours {

  private val messageKeyPrefix = "nonUKBusinessType"
  private val form = new NonUKBusinessTypeFormProvider()()
  private val nonUKBusinessTypeOptions = NonUKBusinessType.options

  val view: nonUKBusinessType = app.injector.instanceOf[nonUKBusinessType]

  private def createView: () => Html = () => view(form)(fakeRequest, messages)

  private def createViewUsingForm: Form[_] => Html = (form: Form[_]) => view(form)(fakeRequest, messages)

  "NonUKBusinessType view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithSubmitButton(createView)

  }

  "NonUKBusinessType view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- nonUKBusinessTypeOptions) {
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, isChecked = false)
        }
      }
    }

    for (option <- nonUKBusinessTypeOptions) {
      s"rendered with a value of '${option.value}'" must {
        s"have the '${option.value}' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> s"${option.value}"))))
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, isChecked = true)

          for (unselectedOption <- nonUKBusinessTypeOptions.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, s"value-${unselectedOption.value}", "value", unselectedOption.value, isChecked = false)
          }
        }
      }
    }
  }

}
