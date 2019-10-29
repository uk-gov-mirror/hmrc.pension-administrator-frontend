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

package views

import controllers.register.company.directors.routes.DirectorEnterUTRController
import forms.EnterUTRFormProvider
import models.{NormalMode, ReferenceValue}
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.behaviours.QuestionViewBehaviours
import views.html.enterUTR

class EnterUTRViewSpec extends QuestionViewBehaviours[ReferenceValue] {

  val messageKeyPrefix = "enterUTR"

  private val name = "Test Name"

  val form = new EnterUTRFormProvider()(name)

  private val postCall: Call = DirectorEnterUTRController.onSubmit(NormalMode, index = 0)

  private def viewModel: CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = postCall,
      title = Message(s"$messageKeyPrefix.title", Message("theCompany").resolve),
      heading = Message(s"$messageKeyPrefix.heading", name),
      mode = NormalMode,
      entityName = name
    )

  def createView: () => HtmlFormat.Appendable = () =>
    enterUTR(
      frontendAppConfig,
      form,
      viewModel
    )(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    enterUTR(
      frontendAppConfig,
      form,
      viewModel
    )(fakeRequest, messages)

  "EnterUTR view" must {
    behave like normalPageWithTitle(
      view = createView,
      messageKeyPrefix = messageKeyPrefix,
      title = Message(s"$messageKeyPrefix.title", Message("theCompany").resolve),
      pageHeader = Message(s"$messageKeyPrefix.heading", name)
    )

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      postCall.url,
      fields = "value"
    )

    behave like pageWithSubmitButton(createView)
  }
}
