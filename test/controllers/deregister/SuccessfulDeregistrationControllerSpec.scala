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

package controllers.deregister

import controllers.ControllerSpecBase
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import views.html.deregister.successful_deregistration
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents


class SuccessfulDeregistrationControllerSpec extends ControllerSpecBase {

  import SuccessfulDeregistrationControllerSpec._

  "Successful Deregistration Controller" must {
    "return OK and the correct view for a GET" in {
      val result = controller.onPageLoad()(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }
  }
}

object SuccessfulDeregistrationControllerSpec extends ControllerSpecBase with MockitoSugar {

  val view: successful_deregistration = app.injector.instanceOf[successful_deregistration]

  private def controller =
    new SuccessfulDeregistrationController(
      frontendAppConfig,
      stubMessagesControllerComponents(),
      view
    )

  private def viewAsString() =
    view()(fakeRequest, messages).toString
}
