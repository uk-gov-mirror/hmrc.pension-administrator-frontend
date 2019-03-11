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

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions._
import controllers.register.company.routes.AddCompanyDirectorsController
import controllers.{ConfirmDeleteController, Retrievals}
import forms.ConfirmDeleteFormProvider
import identifiers.register.company.directors.DirectorDetailsId
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode, NormalMode}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import viewmodels.{ConfirmDeleteViewModel, Message}

class ConfirmDeleteDirectorController @Inject()(
                                                 val appConfig: FrontendAppConfig,
                                                 override val messagesApi: MessagesApi,
                                                 val allowAccess: AllowAccessActionProvider,
                                                 authenticate: AuthAction,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 val cacheConnector: UserAnswersCacheConnector,
                                                 formProvider: ConfirmDeleteFormProvider
                                               ) extends ConfirmDeleteController with Retrievals {

  val form = formProvider()

  private def vm(index: Index, name: String, mode:Mode)(implicit request: DataRequest[AnyContent]) = ConfirmDeleteViewModel(
    routes.ConfirmDeleteDirectorController.onSubmit(mode, index),
    controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(NormalMode),
    Message("confirmDeleteDirector.title"),
    "confirmDeleteDirector.heading",
    Some(name),
    None,
    psaName()
  )

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      DirectorDetailsId(index).retrieve.right.map { details =>
        get(vm(index, details.fullName, mode), details.isDeleted, routes.AlreadyDeletedController.onPageLoad(index), mode)
      }
  }

  def onSubmit(mode:Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      DirectorDetailsId(index).retrieve.right.map { details =>
        post(vm(index, details.fullName, mode), DirectorDetailsId(index), AddCompanyDirectorsController.onPageLoad(NormalMode), mode)
      }
  }

}
