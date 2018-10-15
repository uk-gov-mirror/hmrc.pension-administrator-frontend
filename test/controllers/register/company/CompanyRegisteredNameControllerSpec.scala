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

package controllers.register.company

import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.CompanyNameFormProvider
import identifiers.register.company.CompanyNameId
import models.NormalMode
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers.{contentAsString, _}
import utils.FakeNavigator
import viewmodels.CompanyNameViewModel
import views.html.companyName

class CompanyRegisteredNameControllerSpec extends ControllerSpecBase {

  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new CompanyNameFormProvider()
  val form = formProvider()
  val testCompanyName = "test company name"

  def viewmodel = CompanyNameViewModel(
    postCall = controllers.register.company.routes.CompanyRegisteredNameController.onSubmit(NormalMode),
    title = "companyName.title",
    heading = "companyName.heading"
  )

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new controllers.register.company.CompanyRegisteredNameController(
      frontendAppConfig,
      messagesApi,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      FakeUserAnswersCacheConnector
    )

  def viewAsString(form: Form[_] = form): String = companyName(frontendAppConfig, form, viewmodel)(fakeRequest, messages).toString

  "CompanyRegisteredName Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = Json.obj(CompanyNameId.toString -> testCompanyName)
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(testCompanyName))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testCompanyName))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "[invalid value]"))
      val boundForm = form.bind(Map("value" -> "[invalid value]"))

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
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testCompanyName))
      val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
