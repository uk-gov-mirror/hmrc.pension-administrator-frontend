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

package controllers

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.actions.FakeAuthAction
import play.api.test.Helpers._


class LoginControllerSpec extends ControllerSpecBase {

  private def loginController = new LoginController(frontendAppConfig, FakeUserAnswersCacheConnector, FakeAuthAction, controllerComponents)

  "Login Controller" must {

    "redirect to register as business page" in {
      val result = loginController.onPageLoad(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(register.routes.RegisterAsBusinessController.onPageLoad().url)
    }
  }
}
