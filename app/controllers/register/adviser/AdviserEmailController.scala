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

package controllers.register.adviser

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.register.EmailAddressController
import forms.EmailFormProvider
import identifiers.register.adviser.{AdviserEmailId, AdviserNameId}
import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.Adviser
import viewmodels.{CommonFormWithHintViewModel, Message}

class AdviserEmailController @Inject()(@Adviser val navigator: Navigator,
                                       val appConfig: FrontendAppConfig,
                                       val messagesApi: MessagesApi,
                                       val cacheConnector: UserAnswersCacheConnector,
                                       authenticate: AuthAction,
                                       val allowAccess: AllowAccessActionProvider,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: EmailFormProvider
                               ) extends EmailAddressController {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        get(AdviserEmailId, form, viewModel(mode))
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(AdviserEmailId, mode, form, viewModel(mode))
  }

  private def entityName(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(AdviserNameId).getOrElse(Message("theAdviser"))

  private def viewModel(mode: Mode)(implicit request: DataRequest[AnyContent]) =
    CommonFormWithHintViewModel(
      postCall = routes.AdviserEmailController.onSubmit(mode),
      title = Message("email.title", Message("theAdviser")),
      heading = Message("email.title", entityName),
      mode = mode,
      entityName = entityName
    )
}
