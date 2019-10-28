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

import forms.PersonDetailsFormProvider
import models.{Mode, NormalMode, PersonDetails, UpdateMode}
import play.api.data.Form
import play.api.mvc.Call
import viewmodels.{Message, PersonDetailsViewModel}
import views.behaviours.QuestionViewBehaviours
import views.html.personDetails

class PersonDetailsViewSpec extends QuestionViewBehaviours[PersonDetails] {

  private val messageKeyPrefix = "directorDetails"

  override val form = new PersonDetailsFormProvider()()

  private lazy val viewModel =
    PersonDetailsViewModel(
      title = "directorDetails.title",
      heading = Message("directorDetails.heading"),
      postCall = Call("POST", "http://www.test.com"),
      psaName = Some("test psa")
    )

  private def createView(mode: Mode = NormalMode) = () =>
    personDetails(frontendAppConfig, form, viewModel, mode)(fakeRequest, messages)

  private def createViewUsingForm = (form: Form[_]) =>
    personDetails(frontendAppConfig, form, viewModel, NormalMode)(fakeRequest, messages)

  "PersonDetails view" must {

    behave like normalPage(createView(), messageKeyPrefix)

    behave like pageWithReturnLink(createView(mode = UpdateMode), controllers.routes.PsaDetailsController.onPageLoad().url)

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      controllers.register.company.directors.routes.DirectorNameController.onSubmit(NormalMode, 0).url,
      "firstName",
      "middleName",
      "lastName"
    )

    behave like pageWithDateField(
      createViewUsingForm,
      "dateOfBirth",
      messages("common.dateOfBirth"),
      Some(messages("common.dateOfBirth.hint"))
    )

  }

}
