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

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.address.AddressYearsFormProvider
import identifiers.register.company.{CompanyAddressId, CompanyAddressYearsId}
import javax.inject.Inject
import models.Mode
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.RegisterCompany
import utils.{Enumerable, Navigator2, UserAnswers}
import views.html.register.company.companyAddressYears

import scala.concurrent.Future

class CompanyAddressYearsController @Inject()(
                                               appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               dataCacheConnector: DataCacheConnector,
                                               @RegisterCompany navigator: Navigator2,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               formProvider: AddressYearsFormProvider
                                             ) extends FrontendController with I18nSupport with Enumerable.Implicits with Retrievals {

  private val form = formProvider("companyAddressYears.error.required")

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      CompanyAddressId.retrieve.right.map { address =>
        val preparedForm = request.userAnswers.get(CompanyAddressYearsId) match {
          case None => form
          case Some(value) => form.fill(value)
        }
        Future.successful(Ok(companyAddressYears(appConfig, address, preparedForm, mode)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      CompanyAddressId.retrieve.right.map { address =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(companyAddressYears(appConfig, address, formWithErrors, mode))),
          value =>
            dataCacheConnector.save(request.externalId, CompanyAddressYearsId, value).map(cacheMap =>
              Redirect(navigator.nextPage(CompanyAddressYearsId, mode, UserAnswers(cacheMap))))
        )
      }
  }
}
