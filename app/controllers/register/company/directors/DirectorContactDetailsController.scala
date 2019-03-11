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
import forms.ContactDetailsFormProvider
import identifiers.register.company.directors.DirectorContactDetailsId
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.CompanyDirector
import viewmodels.{ContactDetailsViewModel, Message}

class DirectorContactDetailsController @Inject()(
                                                  @CompanyDirector override val navigator: Navigator,
                                                  override val appConfig: FrontendAppConfig,
                                                  override val messagesApi: MessagesApi,
                                                  override val cacheConnector: UserAnswersCacheConnector,
                                                  override val allowAccess: AllowAccessActionProvider,
                                                  authenticate: AuthAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  formProvider: ContactDetailsFormProvider
                                                ) extends controllers.ContactDetailsController {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      get(DirectorContactDetailsId(index), form, viewmodel(mode, index), mode)
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(index) { directorName =>
        post(DirectorContactDetailsId(index), mode, form, viewmodel(mode, index))
      }
  }

  private def viewmodel(mode: Mode, index: Index)(implicit request: DataRequest[AnyContent]) = ContactDetailsViewModel(
    postCall = routes.DirectorContactDetailsController.onSubmit(mode, index),
    title = Message("directorContactDetails.title"),
    heading = Message("directorContactDetails.heading"),
    body = Some(Message("contactDetails.body")),
    psaName = psaName()
  )
}
