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

package controllers.register.individual

import connectors.AddressLookupConnector
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions.{AuthAction, DataRetrievalAction, FakeAuthAction}
import forms.address.PostCodeLookupFormProvider
import models.{NormalMode, TolerantAddress}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.{FakeNavigator, Navigator}
import views.html.address.postcodeLookup

import scala.concurrent.{ExecutionContext, Future}

class IndividualPreviousAddressPostCodeLookupControllerSpec extends ControllerSpecBase {

  import IndividualPreviousAddressPostCodeLookupController._
  import IndividualPreviousAddressPostCodeLookupControllerSpec._

  "IndividualPreviousAddressPostCodeLookupController" must {

    "render the view correctly on a GET request" in {
      val request = addCSRFToken(FakeRequest(routes.IndividualPreviousAddressPostCodeLookupController.onPageLoad(NormalMode)))
      val result = route(application, request).value
      status(result) mustBe OK
      contentAsString(result) mustBe view(form, viewModel(NormalMode), NormalMode)(request, messagesApi.preferred(fakeRequest)).toString()
    }

    "redirect to the next page on a POST request" in {
      val request = FakeRequest(routes.IndividualPreviousAddressPostCodeLookupController.onSubmit(NormalMode))
        .withFormUrlEncodedBody("value" -> validPostcode)
      val result = route(application, request).value
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)

    }

  }
}

object IndividualPreviousAddressPostCodeLookupControllerSpec extends ControllerSpecBase {

  private val formProvider = new PostCodeLookupFormProvider()
  private val form = formProvider()

  private val validPostcode = "ZZ1 1ZZ"

  val view: postcodeLookup = app.injector.instanceOf[postcodeLookup]


  private val address = TolerantAddress(
    Some("test-address-line-1"),
    Some("test-address-line-2"),
    None,
    None,
    Some(validPostcode),
    Some("GB")
  )

  private val fakeAddressLookupConnector = new AddressLookupConnector {
    override def addressLookupByPostCode(postcode: String)(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[Seq[TolerantAddress]] = {
      Future.successful(Seq(address))
    }
  }

  private val onwardRoute = controllers.register.individual.routes.IndividualPreviousAddressListController.onPageLoad(NormalMode)
  private val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)

  def application: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(getEmptyData),
      bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
      bind[Navigator].toInstance(fakeNavigator),
      bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector)
    ).build()

}
