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

import connectors.{FakeDataCacheConnector, RegistrationConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.company.CompanyAddressFormProvider
import identifiers.register.company.{CompanyAddressId, CompanyDetailsId, CompanyUniqueTaxReferenceId}
import models.register.company.CompanyDetails
import models._
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import utils.FakeNavigator
import views.html.register.company.companyAddress

import scala.concurrent.{ExecutionContext, Future}

class CompanyAddressControllerSpec extends ControllerSpecBase {

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private val testAddress = TolerantAddress(
    Some("Some Building"),
    Some("1 Some Street"),
    Some("Some Village"),
    Some("Some Town"),
    Some("ZZ1 1ZZ"),
    Some("UK")
  )

  val formProvider = new CompanyAddressFormProvider

  val form: Form[Boolean] = formProvider()

  private val validUtr = "1234567890"
  private val invalidUtr = "INVALID"

  val organisation = Organisation("MyCo", OrganisationTypeEnum.CorporateBody)

  val data = Json.obj(
    CompanyDetailsId.toString -> CompanyDetails("MyCo", None, None),
    CompanyUniqueTaxReferenceId.toString -> validUtr
  )
  val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))

  private def fakeRegistrationConnector = new RegistrationConnector {
    override def registerWithIdOrganisation(utr: String, organisation: Organisation)
                                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[OrganizationRegisterWithIdResponse] = {
        if (utr == validUtr) {
          Future.successful(OrganizationRegisterWithIdResponse(testAddress, organisation))
        }
        else {
          Future.failed(new NotFoundException(s"Unnown UTR: $utr"))
        }
    }
    override def registerWithIdIndividual()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[IndividualRegisterWithIdResponse] = ???

  }

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new CompanyAddressController(
      frontendAppConfig,
      messagesApi,
      FakeDataCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      fakeRegistrationConnector,
      formProvider
    )

  private def viewAsString(form: Form[_] = form): String =
    companyAddress(frontendAppConfig, form, testAddress, organisation.organisationName)(fakeRequest, messages).toString

  "CompanyAddress Controller" must {

    "return OK and the correct view for a GET when UTR is valid" in {
      val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller(dataRetrievalAction).onSubmit(NormalMode)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to the next page" when {
      "the UTR is invalid" in {
        val data = Json.obj(
          CompanyDetailsId.toString -> CompanyDetails("MyCo", None, None),
          CompanyUniqueTaxReferenceId.toString -> invalidUtr
        )
        val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
        val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

      "valid data is submitted" when {
        "yes" which {
          "will save the address" in {
            val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

            val result = controller(dataRetrievalAction).onSubmit(NormalMode)(postRequest)

            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(onwardRoute.url)
            FakeDataCacheConnector.verify(CompanyAddressId, testAddress)
          }
        }
        "no" in {
          val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "false"))

          val result = controller(dataRetrievalAction).onSubmit(NormalMode)(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
        }
      }
    }

    "redirect to Session Expired" when {
      "GET" when {
        "no company details data is found" in {
          val data = Json.obj(
            CompanyUniqueTaxReferenceId.toString -> invalidUtr
          )

          val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
          val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
        "no UTR data is found" in {
          val data = Json.obj(
            CompanyDetailsId.toString -> CompanyDetails("MyCo", None, None)
          )

          val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
          val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }

        "no existing data is found" in {
          val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      }
      "POST" when {
        "no company details data is found" in {
          val data = Json.obj(
            CompanyUniqueTaxReferenceId.toString -> invalidUtr
          )

          val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
          val result = controller(dataRetrievalAction).onSubmit(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
        "no UTR data is found" in {
          val data = Json.obj(
            CompanyDetailsId.toString -> CompanyDetails("MyCo", None, None)
          )

          val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
          val result = controller(dataRetrievalAction).onSubmit(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }

        "no existing data is found" in {
          val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
          val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      }
    }

  }

}
