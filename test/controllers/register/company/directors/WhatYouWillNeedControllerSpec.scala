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

package controllers.register.company.directors

import controllers.ControllerSpecBase
import controllers.actions.{DataRetrievalAction, FakeAuthAction}
import models.NormalMode
import play.api.test.Helpers._
import views.html.register.company.directors.whatYouWillNeed

class WhatYouWillNeedControllerSpec extends ControllerSpecBase {

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new WhatYouWillNeedController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction
    )

  private def viewAsString() = whatYouWillNeed(frontendAppConfig, controllers.routes.IndexController.onPageLoad())(fakeRequest, messages).toString

  "WhatYouWillNeed Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, 0)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }
  }

}
