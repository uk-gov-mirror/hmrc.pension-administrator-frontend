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

package controllers.register.individual

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.register.individual.IndividualAddressId
import javax.inject.Inject
import models.Address
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.countryOptions.CountryOptions
import views.html.register.individual.outsideEuEea

import scala.concurrent.Future

class OutsideEuEeaController @Inject()(appConfig: FrontendAppConfig,
                                       override val messagesApi: MessagesApi,
                                       authenticate: AuthAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       countryOptions: CountryOptions
                                      ) extends FrontendController with I18nSupport with Retrievals {

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>

      (IndividualAddressId).retrieve.right.map { address =>
          Future.successful(Ok(outsideEuEea(appConfig, getCountryNameFromCode(address.toAddress))))
        }.left.map(_ => Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad())))

  }

  def getCountryNameFromCode(address: Address) = countryOptions.options
    .find(_.value == address.country)
    .map(_.label)
    .getOrElse(address.country)

}
