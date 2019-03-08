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

package controllers

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRetrievalAction}
import identifiers.register.DeclarationChangedId
import models._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.PsaDetailsService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import views.html.psa_details

import scala.concurrent.ExecutionContext

class PsaDetailsController @Inject()(appConfig: FrontendAppConfig,
                                     override val messagesApi: MessagesApi,
                                     @utils.annotations.Variations navigator: Navigator,
                                     authenticate: AuthAction,
                                     allowAccess: AllowAccessActionProvider,
                                     getData: DataRetrievalAction,
                                     psaDetailsService: PsaDetailsService
                                    )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport {

  def onPageLoad(mode: Mode = UpdateMode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData).async {
    implicit request =>
      val psaId = request.user.alreadyEnrolledPsaId.getOrElse(throw new RuntimeException("PSA ID not found"))
      psaDetailsService.retrievePsaDataAndGenerateViewModel(psaId, mode).map { psaDetails =>
        val nextPage = navigator.nextPage(DeclarationChangedId, mode, request.userAnswers.getOrElse(UserAnswers()))
        Ok(psa_details(appConfig, psaDetails, nextPage))
      }
  }
}
