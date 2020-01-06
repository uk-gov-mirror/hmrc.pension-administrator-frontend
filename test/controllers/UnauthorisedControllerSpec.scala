/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers

import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.unauthorised

class UnauthorisedControllerSpec extends ControllerSpecBase {

  val view: unauthorised = app.injector.instanceOf[unauthorised]

  "Unauthorised Controller" must {
    "return 200 for a GET" in {
      val result = new UnauthorisedController(frontendAppConfig, messagesApi, stubMessagesControllerComponents(), view).onPageLoad()(fakeRequest)
      status(result) mustBe OK
    }

    "return the correct view for a GET" in {
      val result = new UnauthorisedController(frontendAppConfig, messagesApi, stubMessagesControllerComponents(), view).onPageLoad()(fakeRequest)
      contentAsString(result) mustBe view()(fakeRequest, messagesApi.preferred(fakeRequest)).toString
    }
  }

}
