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

package controllers.register.company

import audit.testdoubles.StubSuccessfulAuditService
import connectors.{FakeUserAnswersCacheConnector, RegistrationConnector, UserAnswersCacheConnector}
import controllers.actions._
import controllers.address.NonUKAddressControllerDataMocks
import controllers.register.DeclarationFitAndProperControllerSpec.{contactDetails, mockEmailConnector}
import forms.address.NonUKAddressFormProvider
import identifiers.register.RegistrationInfoId
import identifiers.register.company.{BusinessDetailsId, CompanyAddressId}
import identifiers.register.partnership.PartnershipContactAddressId
import models._
import org.mockito.Matchers.any
import org.mockito.Mockito.{atLeastOnce, never, verify}
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.domain.PsaId
import utils.FakeNavigator
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.nonukAddress
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class CompanyRegisteredAddressControllerSpec extends NonUKAddressControllerDataMocks with ScalaFutures {

  val formProvider = new NonUKAddressFormProvider(countryOptions)
  val form = formProvider("error.country.invalid")
  val fakeAuditService = new StubSuccessfulAuditService()

  def controller(dataRetrievalAction: DataRetrievalAction = getCompany, registrationConnector: RegistrationConnector = fakeRegistrationConnector,
                 userAnswersCacheConnector : UserAnswersCacheConnector = FakeUserAnswersCacheConnector) =
    new CompanyRegisteredAddressController(
      frontendAppConfig,
      messagesApi,
      userAnswersCacheConnector,
      registrationConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      countryOptions
    )

  private def viewModel = ManualAddressViewModel(
    routes.CompanyRegisteredAddressController.onSubmit(),
    countryOptions.options,
    Message("companyRegisteredNonUKAddress.title"),
    Message("companyRegisteredNonUKAddress.heading", companyName),
    None,
    Some(Message("companyRegisteredNonUKAddress.hintText"))
  )

  private def viewAsString(form: Form[_] = form) =
    nonukAddress(
      frontendAppConfig,
      form,
      viewModel
    )(fakeRequest, messages).toString()

  "CompanyRegisteredAddress Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = Json.obj(
        BusinessDetailsId.toString -> BusinessDetails("Test Company Name", None),
        CompanyAddressId.toString -> Address("value 1", "value 2", None, None, None, "IN").toTolerantAddress)
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(Address("value 1", "value 2", None, None, None, "IN")))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("addressLine1", "value 1"),
        ("addressLine2", "value 2"),
        "country" -> "IN"
      )

      val result = controller().onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "for non EEA country not call the register without id method" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("addressLine1", "value 1"),
        ("addressLine2", "value 2"),
        "country" -> "IN"
      )

      val mockRegistrationConnector = mock[RegistrationConnector]

      when(mockRegistrationConnector.registerWithNoIdOrganisation(any(),any(),any())(any(),any()))
        .thenReturn(Future.successful(registrationInfo))

      val result = controller(registrationConnector = mockRegistrationConnector).onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      verify(mockRegistrationConnector, never()).registerWithNoIdOrganisation(any(), any(), any())(any(), any())
    }


    "delete the registrationInfo data" when {
      "we have changed the contact address to NON EEA" in {

        val userAnswersCacheConnector = mock[UserAnswersCacheConnector]

        val postRequest = fakeRequest.withFormUrlEncodedBody(
          ("addressLine1", "value 1"),
          ("addressLine2", "value 2"),
          "country" -> "IN"
        )

        val validConnectorCallResult = Json.obj("test" -> "test")
        val mockRegistrationConnector = mock[RegistrationConnector]

        when(mockRegistrationConnector.registerWithNoIdOrganisation(any(),any(),any())(any(),any()))
          .thenReturn(Future.successful(registrationInfo))
        when(userAnswersCacheConnector.remove(any(),any())(any(),any())).thenReturn(Future.successful(validConnectorCallResult))
        when(userAnswersCacheConnector.save(any(),any(), any())(any(),any(), any())).thenReturn(Future.successful(validConnectorCallResult))

        val result = controller(registrationConnector = mockRegistrationConnector,userAnswersCacheConnector = userAnswersCacheConnector)
          .onSubmit()(postRequest)

        whenReady(result) { _ =>
          verify(userAnswersCacheConnector, atLeastOnce()).remove(any(),Matchers.eq(RegistrationInfoId))(any(),any())
        }
      }
    }

    "for EEA country call the register without id method" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("addressLine1", "value 1"),
        ("addressLine2", "value 2"),
        "country" -> "ES"
      )

      val mockRegistrationConnector = mock[RegistrationConnector]

      when(mockRegistrationConnector.registerWithNoIdOrganisation(any(),any(),any())(any(),any()))
        .thenReturn(Future.successful(registrationInfo))

      val result = controller(registrationConnector = mockRegistrationConnector).onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      verify(mockRegistrationConnector, atLeastOnce()).registerWithNoIdOrganisation(any(), any(), any())(any(), any())
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
          val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

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
