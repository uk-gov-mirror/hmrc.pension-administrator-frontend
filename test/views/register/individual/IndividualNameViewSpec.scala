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

package views.register.individual

import forms.register.individual.IndividualNameFormProvider
import models.{NormalMode, TolerantIndividual}
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.Html
import viewmodels.{Message, PersonDetailsViewModel}
import views.behaviours.QuestionViewBehaviours
import views.html.register.individual.individualName

class IndividualNameViewSpec extends QuestionViewBehaviours[TolerantIndividual] {

  private val messageKeyPrefix = "individualName"

  override val form = new IndividualNameFormProvider()()

  private lazy val viewModel =
    PersonDetailsViewModel(
      title = "individualName.title",
      heading = Message("individualName.heading"),
      postCall = Call("POST", "http://www.test.com")
    )

  val view: individualName = app.injector.instanceOf[individualName]

  private def createView: () => Html = () =>
    view(form, viewModel)(fakeRequest, messages)

  private def createViewUsingForm: Form[_] => Html = (form: Form[_]) =>
    view(form, viewModel)(fakeRequest, messages)

  "IndividualName view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      controllers.register.individual.routes.IndividualNameController.onSubmit(NormalMode).url,
      "firstName",
      "lastName"
    )
  }

}
