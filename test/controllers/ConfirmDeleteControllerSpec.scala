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

package controllers

import config.FrontendAppConfig
import connectors.{DataCacheConnector, FakeDataCacheConnector}
import identifiers.TypedIdentifier
import models._
import models.requests.DataRequest
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.Helpers._
import utils.{FakeNavigator, UserAnswers}
import viewmodels.ConfirmDeleteViewModel
import views.html.confirmDelete

class ConfirmDeleteControllerSpec extends ControllerSpecBase with MockitoSugar {

  private val firstIndex = Index(0)

  val testIdentifier = new TypedIdentifier[String] {
    override def toString: String = "test"
  }

  implicit val request: DataRequest[AnyContent] = DataRequest(
    fakeRequest, "cacheId", PSAUser(UserType.Individual, None, false, None), UserAnswers(Json.obj(testIdentifier.toString -> "answer"))
  )

  val viewModel = ConfirmDeleteViewModel(
    FakeNavigator.desiredRoute,
    FakeNavigator.desiredRoute,
    "title", "heading"
  )

  private def controller() =
    new ConfirmDeleteController {
      override protected def cacheConnector: DataCacheConnector = FakeDataCacheConnector

      override protected def appConfig: FrontendAppConfig = frontendAppConfig

      override def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]
    }

  private def viewAsString() = confirmDelete(frontendAppConfig, firstIndex, viewModel)(fakeRequest, messages).toString

  "ConfirmDeleteDirector Controller" must {

    "return OK and the correct view for a GET" in {

      val result = controller().get(viewModel, firstIndex)(request)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to directors list on removal of director" in {

      val result = controller().post[String](testIdentifier, FakeNavigator.desiredRoute, _ => "")

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(FakeNavigator.desiredRoute.url)
    }

    "set the isDelete flag to true for the selected director on submission of POST request" in {

      val result = controller().post[String](testIdentifier, FakeNavigator.desiredRoute, _ => "newAnswer")

      status(result) mustBe SEE_OTHER
      FakeDataCacheConnector.verify(testIdentifier, "newAnswer")
    }

  }

}
