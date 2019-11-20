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

package controllers.register

import config.FrontendAppConfig
import connectors._
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.register.VariationDeclarationFitAndProperFormProvider
import identifiers.register._
import javax.inject.Inject
import models._
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.{Navigator, UserAnswers}
import utils.annotations.Variations

import scala.concurrent.{ExecutionContext, Future}

class VariationDeclarationFitAndProperController @Inject()(val appConfig: FrontendAppConfig,
                                                           override val messagesApi: MessagesApi,
                                                           authenticate: AuthAction,
                                                           allowAccess: AllowAccessActionProvider,
                                                           getData: DataRetrievalAction,
                                                           requireData: DataRequiredAction,
                                                           @Variations navigator: Navigator,
                                                           formProvider: VariationDeclarationFitAndProperFormProvider,
                                                           dataCacheConnector: UserAnswersCacheConnector
                                                 )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      val preparedForm = request.userAnswers.get(DeclarationFitAndProperId) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Future.successful(Ok(views.html.register.variationDeclarationFitAndProper(appConfig, preparedForm, psaName())))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        errors => Future.successful(BadRequest(views.html.register.variationDeclarationFitAndProper(appConfig, errors, psaName()))),
        success => {
          dataCacheConnector.save(request.externalId, DeclarationFitAndProperId, success).map { json =>
            Redirect(navigator.nextPage(DeclarationFitAndProperId, mode, UserAnswers(json)))
          }
        }
      )
  }
}
