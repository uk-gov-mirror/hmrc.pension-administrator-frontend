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
import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.{PersonNameController, Retrievals}
import identifiers.register.BusinessNameId
import identifiers.register.company.directors.DirectorNameId
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.CompanyDirector
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.personName

import scala.concurrent.{ExecutionContext, Future}

class DirectorNameController @Inject()(val appConfig: FrontendAppConfig,
                                       val cacheConnector: UserAnswersCacheConnector,
                                       @CompanyDirector val navigator: Navigator,
                                       override val allowAccess: AllowAccessActionProvider,
                                       authenticate: AuthAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       val controllerComponents: MessagesControllerComponents,
                                       val view: personName
                                      )(implicit val executionContext: ExecutionContext) extends PersonNameController with Retrievals with I18nSupport {

  private[directors] def viewModel(mode: Mode, index: Index, name: String)
                                  (implicit request: DataRequest[AnyContent]) =
    CommonFormWithHintViewModel(
      postCall = routes.DirectorNameController.onSubmit(mode, index),
      title = "directorName.heading",
      heading = Message("directorName.heading"),
      None,
      None,
      mode,
      entityName = name
    )

  private[directors] def id(index: Index): DirectorNameId =
    DirectorNameId(index)

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      BusinessNameId.retrieve.right.map { name =>
        Future.successful(get(id(index), viewModel(mode, index, name), mode))
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      BusinessNameId.retrieve.right.map { name =>
        post(id(index), viewModel(mode, index, name), mode)
      }
  }

}
