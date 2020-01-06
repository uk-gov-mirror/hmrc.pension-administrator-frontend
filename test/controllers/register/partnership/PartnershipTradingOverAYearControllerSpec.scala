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

package controllers.register.partnership

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import controllers.behaviours.ControllerWithCommonBehaviour
import controllers.register.partnership.routes._
import forms.HasReferenceNumberFormProvider
import models.{Mode, NormalMode}
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

class PartnershipTradingOverAYearControllerSpec extends ControllerWithCommonBehaviour {

  override def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private val name = "Test Partnership Name"
  private val formProvider = new HasReferenceNumberFormProvider()

  val view: hasReferenceNumber = app.injector.instanceOf[hasReferenceNumber]

  private val partnershipName = "Test Partnership Name"

  private val hasReferenceNumberForm = formProvider("error.required", partnershipName)

  private def hasReferenceNumberView(form: Form[_] = hasReferenceNumberForm): String =
    view(form, viewModel(NormalMode))(fakeRequest, messages).toString

  private def viewModel(mode: Mode): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = PartnershipTradingOverAYearController.onSubmit(NormalMode),
      title = Message("trading.title", Message("thePartnership").resolve),
      heading = Message("trading.title", name),
      mode = NormalMode,
      hint = None,
      entityName = name
    )

  private val postRequest = FakeRequest().withFormUrlEncodedBody(("value", "true"))

  private def controller(dataRetrievalAction: DataRetrievalAction = getPartnership) =
    new PartnershipTradingOverAYearController(frontendAppConfig,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      stubMessagesControllerComponents(),
      view
    )

  "PartnershipTradingOverAYearController" must {
    behave like controllerWithCommonFunctions(
      onPageLoadAction = data => controller(data).onPageLoad(NormalMode),
      onSubmitAction = data => controller(data).onSubmit(NormalMode),
      validData = getPartnership,
      viewAsString = hasReferenceNumberView,
      form = hasReferenceNumberForm,
      request = postRequest
    )
  }
}
