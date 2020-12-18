/*
 * Copyright 2020 HM Revenue & Customs
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

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.AllowAccessActionProvider
import controllers.actions.AuthAction
import controllers.actions.DataRetrievalAction
import identifiers.register.DeclarationChangedId
import models._
import play.api.i18n.I18nSupport
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.MessagesControllerComponents
import services.PsaDetailsService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.Navigator
import utils.UserAnswers
import utils.annotations.NoUpdateContactAddress
import views.html.psa_details

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class PsaDetailsController @Inject()(appConfig: FrontendAppConfig,
                                     @utils.annotations.Variations navigator: Navigator,
                                     authenticate: AuthAction,
                                     getData: DataRetrievalAction,
                                     psaDetailsService: PsaDetailsService,
                                     val controllerComponents: MessagesControllerComponents,
                                     view: psa_details
                                    )(implicit val executionContext: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode = UpdateMode): Action[AnyContent] = (authenticate andThen getData).async {
    implicit request =>
      request.user.alreadyEnrolledPsaId.map { psaId =>
        psaDetailsService.retrievePsaDataAndGenerateViewModel(psaId, mode).map { psaDetails =>
          val nextPage = navigator.nextPage(DeclarationChangedId, mode, request.userAnswers.getOrElse(UserAnswers()))
          Ok(view(psaDetails, nextPage))
        }
      }.getOrElse(
        Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
      )
  }
}
