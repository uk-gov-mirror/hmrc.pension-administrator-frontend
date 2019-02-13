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
import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import models.NormalMode
import play.api.test.Helpers._
import utils.FakeNavigator
import views.html.register.company.whatYouWillNeed

class WhatYouWillNeedControllerSpec extends ControllerSpecBase {

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private val auditService = new StubSuccessfulAuditService()

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new WhatYouWillNeedController(
      frontendAppConfig,
      messagesApi,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      auditService
    )

  private def viewAsString() = whatYouWillNeed(frontendAppConfig)(fakeRequest, messages).toString

  "WhatYouWillNeed Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to next page on submit" in {
      val result = controller().onSubmit(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "send a PSAStart audit event" in {

      auditService.reset()

      val result = controller().onSubmit(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      auditService.verifyNothingSent() mustBe false

    }

  }

}
