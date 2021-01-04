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

package controllers.register

import controllers.actions._
import controllers.{ControllerSpecBase, UnauthorisedAssistantController}
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.unauthorisedAssistant

class UnauthorisedAssistantControllerSpec extends ControllerSpecBase {

  val view: unauthorisedAssistant = app.injector.instanceOf[unauthorisedAssistant]

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new UnauthorisedAssistantController(
      frontendAppConfig,
      stubMessagesControllerComponents(),
      view
    )

  def viewAsString() = view()(fakeRequest, messages).toString

  "UnauthorisedAssistant Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }
  }
}
