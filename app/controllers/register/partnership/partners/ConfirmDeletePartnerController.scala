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
import controllers.actions._
import controllers.{ConfirmDeleteController, Retrievals}
import forms.ConfirmDeleteFormProvider
import identifiers.register.partnership.partners.PartnerNameId
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode, NormalMode}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import viewmodels.{ConfirmDeleteViewModel, Message}
import views.html.confirmDelete

import scala.concurrent.ExecutionContext

class ConfirmDeletePartnerController @Inject()(val appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               authenticate: AuthAction,
                                               allowAccess: AllowAccessActionProvider,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               val cacheConnector: UserAnswersCacheConnector,
                                               formProvider: ConfirmDeleteFormProvider,
                                               val controllerComponents: MessagesControllerComponents,
                                               val view: confirmDelete
                                              )(implicit val executionContext: ExecutionContext) extends ConfirmDeleteController with Retrievals {

  val form: Form[Boolean] = formProvider()

  private def viewModel(index: Index, name: String, mode: Mode)(implicit request: DataRequest[AnyContent]) = ConfirmDeleteViewModel(
    routes.ConfirmDeletePartnerController.onSubmit(index, mode),
    controllers.register.partnership.routes.AddPartnerController.onPageLoad(NormalMode),
    Message("confirmDelete.partner.title"),
    "confirmDelete.partner.heading",
    Some(name),
    None,
    psaName = psaName()
  )

  def onPageLoad(index: Index, mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>

      PartnerNameId(index).retrieve.right.map { details =>
        get(viewModel(index, details.fullName, mode), details.isDeleted, routes.AlreadyDeletedController.onPageLoad(index), mode)

      }
  }

  def onSubmit(index: Index, mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      PartnerNameId(index).retrieve.right.map { details =>
        post(viewModel(index, details.fullName, mode), PartnerNameId(index),
          controllers.register.partnership.routes.AddPartnerController.onPageLoad(mode), mode)
      }
  }

}
