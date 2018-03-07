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

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.register.company.DirectorPreviousAddressListFormProvider
import identifiers.register.company.{DirectorPreviousAddressListId, DirectorPreviousAddressPostCodeLookupId}
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.company.directorPreviousAddressList

import scala.concurrent.Future

class DirectorPreviousAddressListController @Inject()(
                                                       appConfig: FrontendAppConfig,
                                                       override val messagesApi: MessagesApi,
                                                       dataCacheConnector: DataCacheConnector,
                                                       navigator: Navigator,
                                                       authenticate: AuthAction,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction,
                                                       formProvider: DirectorPreviousAddressListFormProvider
                                                     ) extends FrontendController with I18nSupport with Enumerable.Implicits with Retrievals {

  def onPageLoad(mode: Mode, index: Index) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(index) { name =>
        request.userAnswers.get(DirectorPreviousAddressPostCodeLookupId(index)) match {
          case None =>
            Future.successful(Redirect(controllers.register.company.routes.DirectorPreviousAddressPostCodeLookupController.onPageLoad(mode, index)))
          case Some(addresses) =>
            Future.successful(Ok(directorPreviousAddressList(appConfig, formProvider(addresses), mode, index, name, addresses)))
        }
      }
  }

  def onSubmit(mode: Mode, index: Index) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(index) { name =>
        request.userAnswers.get(DirectorPreviousAddressPostCodeLookupId(index)) match {
          case None => Future.successful(Redirect(controllers.register.company.routes.DirectorPreviousAddressPostCodeLookupController.onPageLoad(mode, index)))
          case Some(addresses) =>
            formProvider(addresses).bindFromRequest().fold(
              (formWithErrors: Form[_]) =>
                Future.successful(BadRequest(directorPreviousAddressList(appConfig, formWithErrors, mode, index, name, addresses))),
              (value) =>
                dataCacheConnector.save(
                  request.externalId,
                  DirectorPreviousAddressListId(index),
                  addresses(value).copy(country = "GB")
                ).map( cacheMap =>
                  Redirect(navigator.nextPage(DirectorPreviousAddressListId(index), mode)(new UserAnswers(cacheMap)))
                )
            )
        }
      }
  }
}
