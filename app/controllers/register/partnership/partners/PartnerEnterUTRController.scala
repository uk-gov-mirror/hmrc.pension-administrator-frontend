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

package controllers.register.partnership.partners

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.EnterUTRController
import controllers.actions._
import controllers.register.partnership.partners.routes.PartnerEnterUTRController
import forms.EnterUTRFormProvider
import identifiers.register.partnership.partners.{PartnerEnterUTRId, PartnerNameId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode, ReferenceValue}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.PartnershipPartner
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.enterUTR

import scala.concurrent.ExecutionContext

class PartnerEnterUTRController @Inject()(@PartnershipPartner val navigator: Navigator,
                                          val appConfig: FrontendAppConfig,
                                          val cacheConnector: UserAnswersCacheConnector,
                                          authenticate: AuthAction,
                                          val allowAccess: AllowAccessActionProvider,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          formProvider: EnterUTRFormProvider,
                                          val controllerComponents: MessagesControllerComponents,
                                          val view: enterUTR
                                         )(implicit val executionContext: ExecutionContext) extends EnterUTRController with I18nSupport {

  private def form(partnerName: String)
                  (implicit request: DataRequest[AnyContent]): Form[ReferenceValue] = formProvider(partnerName)

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        val partnerName = entityName(index)
        get(PartnerEnterUTRId(index), form(partnerName), viewModel(mode, index, partnerName))
    }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      val partnerName = entityName(index)
      post(PartnerEnterUTRId(index), mode, form(partnerName), viewModel(mode, index, partnerName))
  }

  private def entityName(index: Index)(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(PartnerNameId(index)).map(_.fullName).getOrElse(Message("thePartner"))

  private def viewModel(mode: Mode, index: Index, partnerName: String)(implicit request: DataRequest[AnyContent]) =
    CommonFormWithHintViewModel(
      postCall = PartnerEnterUTRController.onSubmit(mode, index),
      title = Message("enterUTR.heading", Message("thePartner")),
      heading = Message("enterUTR.heading", partnerName),
      mode = mode,
      entityName = partnerName
    )
}
