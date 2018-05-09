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
import connectors.{AddressLookupConnector, DataCacheConnector}
import controllers.Retrievals
import controllers.actions._
import forms.register.company.directors.DirectorPreviousAddressPostCodeLookupFormProvider
import identifiers.register.company.directors.DirectorPreviousAddressPostCodeLookupId
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.CompanyDirector
import utils.{Navigator, UserAnswers}
import views.html.register.company.directors.directorPreviousAddressPostCodeLookup

import scala.concurrent.Future

class DirectorPreviousAddressPostCodeLookupController @Inject()(
                                                                 appConfig: FrontendAppConfig,
                                                                 override val messagesApi: MessagesApi,
                                                                 dataCacheConnector: DataCacheConnector,
                                                                 addressLookupConnector: AddressLookupConnector,
                                                                 @CompanyDirector navigator: Navigator,
                                                                 authenticate: AuthAction,
                                                                 getData: DataRetrievalAction,
                                                                 requireData: DataRequiredAction,
                                                                 formProvider: DirectorPreviousAddressPostCodeLookupFormProvider
                                                               ) extends FrontendController with Retrievals with I18nSupport {

  private val form = formProvider()

  def formWithError(messageKey: String): Form[String] = {
    form.withError("value", messageKey)
  }

  def onPageLoad(mode: Mode, index: Index) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(index) { directorName =>
        Future.successful(Ok(directorPreviousAddressPostCodeLookup(appConfig, form, mode, index, directorName)))
      }
  }

  def onSubmit(mode: Mode, index: Index) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(index) { directorName =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(directorPreviousAddressPostCodeLookup(appConfig, formWithErrors, mode, index, directorName))),
          (value) =>
            addressLookupConnector.addressLookupByPostCode(value).flatMap {
              case Nil =>
                Future.successful(BadRequest(directorPreviousAddressPostCodeLookup(appConfig, formWithError("directorPreviousAddressPostCodeLookup.error.noResults"), mode, index, directorName)))
              case addresses =>
                dataCacheConnector.save(request.externalId, DirectorPreviousAddressPostCodeLookupId(index), addresses).map(cacheMap =>
                  Redirect(navigator.nextPage(DirectorPreviousAddressPostCodeLookupId(index), mode)(UserAnswers(cacheMap))))
            }.recoverWith {
              case _ =>
                Future.successful(BadRequest(directorPreviousAddressPostCodeLookup(appConfig, formWithError("directorPreviousAddressPostCodeLookup.error.invalid"), mode, index, directorName)))

            }
        )
      }
  }
}
