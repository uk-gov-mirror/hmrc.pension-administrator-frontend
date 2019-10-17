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
import controllers.{PersonDetailsController, Retrievals}
import identifiers.register.company.directors.DirectorDetailsId
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.CompanyDirector
import viewmodels.{Message, PersonDetailsViewModel}

class DirectorDetailsController @Inject()(
                                           val appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           val cacheConnector: UserAnswersCacheConnector,
                                           @CompanyDirector val navigator: Navigator,
                                           override val allowAccess: AllowAccessActionProvider,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction
                                         ) extends PersonDetailsController with Retrievals{

  private[directors] def viewModel(mode: Mode, index: Index)(implicit request: DataRequest[AnyContent]) =
    PersonDetailsViewModel(
      title = "directorDetails.title",
      heading = Message("directorDetails.title"),
      postCall = routes.DirectorDetailsController.onSubmit(mode, index),
      psaName = psaName()
    )

  private[directors] def id(index: Index): DirectorDetailsId =
    DirectorDetailsId(index)

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData) {
    implicit request =>
      get(id(index), viewModel(mode, index), mode)
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(id(index), viewModel(mode, index), mode)
  }

}
