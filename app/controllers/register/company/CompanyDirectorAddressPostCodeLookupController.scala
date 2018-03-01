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
import connectors.{AddressLookupConnector, DataCacheConnector}
import controllers.actions._
import config.FrontendAppConfig
import forms.register.company.CompanyDirectorAddressPostCodeLookupFormProvider
import identifiers.register.company.CompanyDirectorAddressPostCodeLookupId
import models.Mode
import play.api.mvc.{Action, AnyContent}
import utils.{Navigator, UserAnswers}
import views.html.register.company.companyDirectorAddressPostCodeLookup

import scala.concurrent.Future

class CompanyDirectorAddressPostCodeLookupController @Inject() (
                                                                 appConfig: FrontendAppConfig,
                                                                 override val messagesApi: MessagesApi,
                                                                 dataCacheConnector: DataCacheConnector,
                                                                 addressLookupConnector: AddressLookupConnector,
                                                                 navigator: Navigator,
                                                                 authenticate: AuthAction,
                                                                 getData: DataRetrievalAction,
                                                                 requireData: DataRequiredAction,
                                                                 formProvider: CompanyDirectorAddressPostCodeLookupFormProvider
                                                               ) extends FrontendController with I18nSupport {

  private val form = formProvider()

  def formWithError(messageKey: String): Form[String] = {
    form.withError("value", messageKey)
  }
  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      Ok(companyDirectorAddressPostCodeLookup(appConfig, form, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(companyDirectorAddressPostCodeLookup(appConfig, formWithErrors, mode))),
        (value) =>
          addressLookupConnector.addressLookupByPostCode(value).flatMap {
            case None =>
              Future.successful(
                BadRequest(
                  companyDirectorAddressPostCodeLookup(
                    appConfig,
                    formWithError("companyDirectorAddressPostCodeLookup.error.invalid"),
                    mode
                  )
                )
              )
            case Some(Nil) =>
              Future.successful(
                BadRequest(
                  companyDirectorAddressPostCodeLookup(
                    appConfig,
                    formWithError("companyDirectorAddressPostCodeLookup.error.noResults"),
                    mode
                  )
                )
              )
            case Some(addressRecords) =>
              val addresses = addressRecords.map(_.address)

              dataCacheConnector
                .save(
                  request.externalId,
                  CompanyDirectorAddressPostCodeLookupId,
                  addresses
                )
                .map(cacheMap =>
                  Redirect(
                    navigator.nextPage(CompanyDirectorAddressPostCodeLookupId, mode)(UserAnswers(cacheMap))
                  )
                )
          }
      )
  }
}
