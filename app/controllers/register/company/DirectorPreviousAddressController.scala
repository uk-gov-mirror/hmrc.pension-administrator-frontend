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

package controllers.register.company

import javax.inject.Inject

import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import connectors.DataCacheConnector
import controllers.actions._
import config.FrontendAppConfig
import forms.register.company.DirectorPreviousAddressFormProvider
import identifiers.register.company.DirectorPreviousAddressId
import models.{Index, Mode}
import models.register.company.DirectorPreviousAddress
import utils.{Navigator, UserAnswers}
import views.html.register.company.directorPreviousAddress

import scala.concurrent.Future

class DirectorPreviousAddressController @Inject() (
                                        appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        dataCacheConnector: DataCacheConnector,
                                        navigator: Navigator,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: DirectorPreviousAddressFormProvider
                                      ) extends FrontendController with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index) = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(DirectorPreviousAddressId(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(directorPreviousAddress(appConfig, preparedForm, mode, index))
  }

  def onSubmit(mode: Mode, index: Index) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(directorPreviousAddress(appConfig, formWithErrors, mode, index))),
        (value) =>
          dataCacheConnector.save(request.externalId, DirectorPreviousAddressId(index), value).map(cacheMap =>
            Redirect(navigator.nextPage(DirectorPreviousAddressId(index), mode)(new UserAnswers(cacheMap))))
    )
  }
}
