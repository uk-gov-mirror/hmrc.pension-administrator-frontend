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

package controllers.register.individual

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.individual.IndividualNameFormProvider
import identifiers.register.individual.IndividualDetailsId
import models.{NormalMode, TolerantIndividual}
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc.Call
import play.api.test.Helpers._

import utils.FakeNavigator
import viewmodels.{Message, PersonDetailsViewModel}
import views.html.register.individual.individualName

class IndividualNameControllerSpec extends ControllerSpecBase {
  private val formProvider = new IndividualNameFormProvider()
  private val form = formProvider()

  private val testAnswer = TolerantIndividual(Some("John"), None, Some("Doe"))
  val view: individualName = app.injector.instanceOf[individualName]


  "IndividualNameController" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = Json.obj(IndividualDetailsId.toString -> testAnswer)
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(testAnswer))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("firstName", "fName"),
        ("lastName", "lName"))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("firstName", ""),("lastName", ""))
      val boundForm = form.bind(Map("firstName" -> "", "lastName" -> ""))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("firstName", ""),("lastName", ""))
      val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  def viewModel = PersonDetailsViewModel(title = "individualName.title",
                                         heading = Message("individualName.title"),
                                         postCall = controllers.register.individual.routes.IndividualNameController.onSubmit(NormalMode)
                                        )

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new IndividualNameController(frontendAppConfig,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      controllerComponents,
      view
    )


  def viewAsString(form: Form[_] = form): String = view(form, viewModel)(fakeRequest, messages).toString

}


