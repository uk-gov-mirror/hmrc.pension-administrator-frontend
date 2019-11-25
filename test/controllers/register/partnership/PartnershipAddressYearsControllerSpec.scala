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

package controllers.register.partnership

import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import controllers.register.partnership.routes.PartnershipAddressYearsController
import forms.address.AddressYearsFormProvider
import models.{AddressYears, NormalMode}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.annotations.Partnership
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

class PartnershipAddressYearsControllerSpec extends ControllerSpecBase {

  import PartnershipAddressYearsControllerSpec._

  "render the view correctly on a GET request" in {
    val request = FakeRequest(PartnershipAddressYearsController.onPageLoad(NormalMode))
    val result = route(application, request).value
        status(result) mustBe OK
        contentAsString(result) mustBe view(form, viewModel, NormalMode)(request, messages).toString
  }

  "redirect to the next page on a POST request" in {
    val request = FakeRequest(PartnershipAddressYearsController.onSubmit(NormalMode))
        .withFormUrlEncodedBody("value" -> AddressYears.OverAYear.toString)
    val result = route(application, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(FakeNavigator.desiredRoute.url)
  }
}

object PartnershipAddressYearsControllerSpec extends ControllerSpecBase {

  val partnershipName = "Test Partnership Name"

  val dataRetrieval: DataRetrievalAction = UserAnswers()
    .businessName(partnershipName)
    .dataRetrievalAction

  val viewModel = AddressYearsViewModel(
    PartnershipAddressYearsController.onSubmit(NormalMode),
    Message("addressYears.heading", Message("thePartnership").resolve),
    Message("addressYears.heading").withArgs(partnershipName),
    Message("addressYears.heading").withArgs(partnershipName)
  )

  val form = new AddressYearsFormProvider()("error.required")

  val view: addressYears = app.injector.instanceOf[addressYears]

  def application: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[AllowAccessActionProvider].to(FakeAllowAccessProvider()),
      bind[DataRetrievalAction].toInstance(dataRetrieval),
      bind[Navigator].qualifiedWith(classOf[Partnership]).toInstance(FakeNavigator),
      bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector)
    ).build()

}