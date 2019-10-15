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

package views.register

import controllers.register.company.routes
import forms.register.company.VATNumberFormProvider
import models.NormalMode
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.behaviours.QuestionViewBehaviours
import views.html.register.vatNumber

class VatNumberViewSpec extends QuestionViewBehaviours[String] {

  val messageKeyPrefix = "VATNumber"

  private val name = "Test Name"

  val form = new VATNumberFormProvider()()

  private val postCall: Call = routes.CompanyVATNumberController.onSubmit(NormalMode)

  private def viewModel: CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = postCall,
      title = Message("VATNumber.title", Message("theCompany").resolve),
      heading = Message("VATNumber.heading", name),
      mode = NormalMode,
      entityName = name
    )

  def createView: () => HtmlFormat.Appendable = () =>
    vatNumber(
      frontendAppConfig,
      form,
      viewModel
    )(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    vatNumber(
      frontendAppConfig,
      form,
      viewModel
    )(fakeRequest, messages)

  "VatNumber view" must {
    behave like normalPageWithDynamicTitle(
      view = createView,
      messageKeyPrefix = messageKeyPrefix,
      dynamicContent = Message("theCompany").resolve
    )

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      postCall.url,
      "value"
    )
  }
}
