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

package controllers.register.individual

import java.time.LocalDate

import audit.testdoubles.StubSuccessfulAuditService
import connectors.{FakeUserAnswersCacheConnector, RegistrationConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.NonUKAddressFormProvider
import identifiers.register.RegistrationInfoId
import identifiers.register.individual.{IndividualAddressId, IndividualDateOfBirthId, IndividualDetailsId}
import models._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalactic.source.Position
import org.scalatest.BeforeAndAfter
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.countryOptions.CountryOptions
import utils.{FakeCountryOptions, FakeNavigator}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.nonukAddress

import scala.concurrent.{ExecutionContext, Future}

class IndividualRegisteredAddressControllerSpec extends ControllerSpecBase with ScalaFutures with MockitoSugar with BeforeAndAfter{

  before(reset(mockRegistrationConnector))

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  def countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  val formProvider = new NonUKAddressFormProvider(countryOptions)
  val form = formProvider("error.country.invalid")
  val fakeAuditService = new StubSuccessfulAuditService()
  val individualName = "TestFirstName TestLastName"
  val sapNumber = "test-sap-number"
  val registrationInfo = RegistrationInfo(
    RegistrationLegalStatus.Individual,
    sapNumber,
    false,
    RegistrationCustomerType.NonUK,
    None,
    None
  )

  val mockRegistrationConnector = mock[RegistrationConnector]

  val validData: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(
    Json.obj(
      IndividualDetailsId.toString ->
        TolerantIndividual(Some("TestFirstName"), None, Some("TestLastName")),
      IndividualDateOfBirthId.toString -> LocalDate.now()
    )))


  def controller(dataRetrievalAction: DataRetrievalAction = validData) =
    new IndividualRegisteredAddressController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      countryOptions,
      mockRegistrationConnector
    )

  private def viewModel = ManualAddressViewModel(
    routes.IndividualRegisteredAddressController.onSubmit(),
    countryOptions.options,
    Message("individualRegisteredNonUKAddress.title"),
    Message("individualRegisteredNonUKAddress.heading", individualName),
    None,
    Some(Message("individualRegisteredNonUKAddress.hintText"))
  )

  private def viewAsString(form: Form[_] = form) =
    nonukAddress(
      frontendAppConfig,
      form,
      viewModel
    )(fakeRequest, messages).toString()

  "IndividualRegisteredAddress Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad()(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = Json.obj(
        IndividualDetailsId.toString -> TolerantIndividual(Some("fName"), Some("mName"), Some("fName")),
        IndividualAddressId.toString -> Address("value 1", "value 2", None, None, None, "IN").toTolerantAddress)
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad()(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(Address("value 1", "value 2", None, None, None, "IN")))
    }

    "redirect to the next page when valid data with non uk country is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("addressLine1", "value 1"),
        ("addressLine2", "value 2"),
        "country" -> "IN"
      )

      when(mockRegistrationConnector.registerWithNoIdIndividual(any(), any(), any(), any())(any(), any())).thenReturn(Future.successful(registrationInfo))

      val result = controller().onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)

      FakeUserAnswersCacheConnector.verify(RegistrationInfoId, registrationInfo)
      verify(mockRegistrationConnector, atLeastOnce()).registerWithNoIdIndividual(any(), any(), any(), any())(any(), any())
    }

    "redirect to the next page when valid data with uk as country is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("addressLine1", "value 1"),
        ("addressLine2", "value 2"),
        "country" -> "GB"
      )

      when(mockRegistrationConnector.registerWithNoIdIndividual(any(), any(), any(), any())(any(), any())).thenReturn(Future.successful(registrationInfo))

      val result = controller().onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      verify(mockRegistrationConnector, never()).registerWithNoIdIndividual(any(), any(), any(), any())(any(), any())
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit()(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired" when {
      "no existing data is found" when {
        "GET" in {
          val result = controller(dontGetAnyData).onPageLoad()(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
        "POST" in {
          val postRequest = fakeRequest.withFormUrlEncodedBody()
          val result = controller(dontGetAnyData).onSubmit()(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      }
    }

  }
}
