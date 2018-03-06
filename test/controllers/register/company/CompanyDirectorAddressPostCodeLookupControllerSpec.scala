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

import java.time.LocalDate

import play.api.data.{Form, FormError}
import play.api.libs.json.JsString
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{FakeNavigator, UserAnswers}
import connectors.{AddressLookupConnector, FakeDataCacheConnector}
import controllers.actions._
import play.api.test.Helpers._
import forms.register.company.CompanyDirectorAddressPostCodeLookupFormProvider
import identifiers.register.company.{CompanyDirectorAddressPostCodeLookupId, DirectorDetailsId}
import models.{Address, AddressRecord, Index, NormalMode}
import views.html.register.company.companyDirectorAddressPostCodeLookup
import play.api.libs.json._
import controllers.ControllerSpecBase
import models.register.company.DirectorDetails
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.Future

class CompanyDirectorAddressPostCodeLookupControllerSpec  extends ControllerSpecBase with MockitoSugar {

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new CompanyDirectorAddressPostCodeLookupFormProvider()
  private val form = formProvider()
  private val fakeAddressLookupConnector: AddressLookupConnector = mock[AddressLookupConnector]

  val index = Index(0)
  val directorName = "Foo Bar"

  val requiredData = Json.obj(
    "directors" -> Seq(
      Json.obj(
        DirectorDetailsId.toString ->
          DirectorDetails("Foo", None, "Bar", LocalDate.now)
      )
    )
  )

  val getRequiredData = new FakeDataRetrievalAction(Some(requiredData))

  private def controller(dataRetrievalAction: DataRetrievalAction = getRequiredData) =
    new CompanyDirectorAddressPostCodeLookupController(frontendAppConfig, messagesApi, FakeDataCacheConnector,
      fakeAddressLookupConnector, new FakeNavigator(desiredRoute = onwardRoute), FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl, formProvider)

  def viewAsString(form: Form[_] = form) = companyDirectorAddressPostCodeLookup(
    frontendAppConfig, form, NormalMode, index, directorName)(fakeRequest, messages).toString

  private def fakeAddress(postCode: String) = Address(
    "Address Line 1",
    "Address Line 2",
    Some("Address Line 3"),
    Some("Address Line 4"),
    Some(postCode),
    "GB"
  )

  private val testAnswer = "AB12 1AB"

  "CompanyDirectorAddressPostCodeLookup Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, index)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "return redirect to the Session Expired page when the directors name isnt present" in {
      val result = controller(getEmptyData).onPageLoad(NormalMode, index)(fakeRequest)

      status(result) mustBe SEE_OTHER
    }

    "not populate the view on a GET when the question has previously been answered" in {
      val validData = requiredData ++ Json.obj(
        CompanyDirectorAddressPostCodeLookupId.toString -> Seq(fakeAddress(testAnswer))
      )
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode, index)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testAnswer))

      when(fakeAddressLookupConnector.addressLookupByPostCode(Matchers.eq(testAnswer))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(Seq(AddressRecord(fakeAddress(testAnswer))))))

      val result = controller().onSubmit(NormalMode, index)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "save the list of matching addresses when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testAnswer))
      val expected = Seq(fakeAddress(testAnswer))

      when(fakeAddressLookupConnector.addressLookupByPostCode(Matchers.eq(testAnswer))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(Seq(AddressRecord(fakeAddress(testAnswer))))))

      controller().onSubmit(NormalMode, index)(postRequest)

      FakeDataCacheConnector.verify(CompanyDirectorAddressPostCodeLookupId(index), expected)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = controller().onSubmit(NormalMode, index)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, index)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testAnswer))

      val result = controller(dontGetAnyData).onSubmit(NormalMode, index)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "return a Bad Request when post code lookup fails" in {
      val boundForm = form.withError(FormError("value", "companyDirectorAddressPostCodeLookup.error.invalid"))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testAnswer))

      when(fakeAddressLookupConnector.addressLookupByPostCode(Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))

      val result = controller().onSubmit(NormalMode, index)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "return a Bad Request when post code lookup returns zero results" in {
      val boundForm = form.withError(FormError("value", "companyDirectorAddressPostCodeLookup.error.noResults"))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testAnswer))

      when(fakeAddressLookupConnector.addressLookupByPostCode(Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(Nil)))

      val result = controller().onSubmit(NormalMode, index)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }
  }
}
