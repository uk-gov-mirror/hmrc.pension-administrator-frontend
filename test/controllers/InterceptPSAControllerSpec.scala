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

package controllers

import play.api.test.Helpers._
import views.html.interceptPSA
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

class InterceptPSAControllerSpec extends ControllerSpecBase {

  val interceptPSAview: interceptPSA = app.injector.instanceOf[interceptPSA]

  def controller() = new InterceptPSAController(frontendAppConfig, stubMessagesControllerComponents(), interceptPSAview)

  def viewAsString(): String = interceptPSAview()(fakeRequest, messages).toString

  "InterceptPSA Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }
  }

}
