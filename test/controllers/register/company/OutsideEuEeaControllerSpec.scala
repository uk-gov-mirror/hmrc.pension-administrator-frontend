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

import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction, FakeDataRetrievalAction}
import identifiers.register.company.{CompanyNameId, CompanyRegisteredAddressId}
import models.Address
import play.api.libs.json.Json
import play.api.test.Helpers._
import utils.countryOptions.CountryOptions
import views.html.register.company.outsideEuEea

class OutsideEuEeaControllerSpec extends ControllerSpecBase {

  private val countryOptions: CountryOptions = new CountryOptions(environment, frontendAppConfig)

  def controller(dataRetrievalAction: DataRetrievalAction = validData) =
    new OutsideEuEeaController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      countryOptions
    )


  def validData: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(
    Json.obj(
      CompanyNameId.toString ->
        organisationName,
      CompanyRegisteredAddressId.toString -> Address(
        "value 1",
        "value 2",
        None,
        None,
        Some("NE1 1NE"),
        "AF"
      )
    )))

  val organisationName = "Test company name"
  val country = "Afghanistan"

  "OutsideEuEea Controller" must {
    "return 200 and correct view for a GET" in {
      val result = controller().onPageLoad()(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe outsideEuEea(frontendAppConfig, organisationName, country)(fakeRequest, messages).toString
    }

    "redirect to Session Expired on a GET request if no cached data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
