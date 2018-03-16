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

package controllers.register.company.directors

import javax.inject.Inject

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.actions._
import forms.register.company.directors.DirectorDetailsFormProvider
import identifiers.register.company.directors.DirectorDetailsId
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.CompanyDirector
import utils.{Navigator, UserAnswers}
import views.html.register.company.directors.directorDetails

import scala.concurrent.Future

class DirectorDetailsController @Inject()(
                                           appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           dataCacheConnector: DataCacheConnector,
                                           @CompanyDirector navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: DirectorDetailsFormProvider
                                         ) extends FrontendController with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(DirectorDetailsId(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(directorDetails(appConfig, preparedForm, mode, index))
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(directorDetails(appConfig, formWithErrors, mode, index))),
        (value) =>
          dataCacheConnector.save(request.externalId, DirectorDetailsId(index), value).map(cacheMap =>
            Redirect(navigator.nextPage(DirectorDetailsId(index), mode)(UserAnswers(cacheMap))))
      )
  }

}
