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

package controllers

import javax.inject.Inject

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import identifiers.IndexId
import models.{NormalMode, UserType}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Navigator
import utils.annotations.RegisterCompany

class LoginController @Inject()(appConfig: FrontendAppConfig,
                                override val messagesApi: MessagesApi,
                                dataCacheConnector: DataCacheConnector,
                                authenticate: AuthAction) extends FrontendController with I18nSupport {

  def onPageLoad: Action[AnyContent] = authenticate.async{
    implicit request =>
      dataCacheConnector.save(request.externalId, IndexId, "").map { _ =>
        request.user.userType match {
          case UserType.Individual =>
            Redirect(controllers.register.individual.routes.IndividualDetailsCorrectController.onPageLoad(NormalMode))
          case UserType.Organisation =>
            Redirect(controllers.register.company.routes.BusinessTypeController.onPageLoad(NormalMode))
        }
      }
  }
}
