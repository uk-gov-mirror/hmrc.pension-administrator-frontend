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
import connectors.UserAnswersCacheConnector
import controllers.Variations
import controllers.actions._
import forms.register.DeclarationWorkingKnowledgeFormProvider
import identifiers.register.DeclarationWorkingKnowledgeId
import javax.inject.Inject
import models.Mode
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Register
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.declarationWorkingKnowledge

import scala.concurrent.Future

class DeclarationWorkingKnowledgeController @Inject()(
                                                       appConfig: FrontendAppConfig,
                                                       override val messagesApi: MessagesApi,
                                                       override val cacheConnector: UserAnswersCacheConnector,
                                                       @Register navigator: Navigator,
                                                       authenticate: AuthAction,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction,
                                                       formProvider: DeclarationWorkingKnowledgeFormProvider
                                                     ) extends FrontendController with I18nSupport with Enumerable.Implicits with Variations {

  private val form = formProvider()

  def onPageLoad(mode: Mode) = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(DeclarationWorkingKnowledgeId) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(declarationWorkingKnowledge(appConfig, preparedForm, mode))
  }

  def onSubmit(mode: Mode) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(declarationWorkingKnowledge(appConfig, formWithErrors, mode))),
        value => {
          val hasAnswerChanged = request.userAnswers.get(DeclarationWorkingKnowledgeId) match {
            case None => true
            case Some(existing) => existing != value
          }
          if (hasAnswerChanged) {
            cacheConnector.save(request.externalId, DeclarationWorkingKnowledgeId, value).flatMap(cacheMap =>
              saveChangeFlag(mode, DeclarationWorkingKnowledgeId).map(_ =>
                Redirect(navigator.nextPage(DeclarationWorkingKnowledgeId, mode, UserAnswers(cacheMap))))
            )
          } else {
            cacheConnector.save(request.externalId, DeclarationWorkingKnowledgeId, value).map(cacheMap =>
              Redirect(navigator.nextPage(DeclarationWorkingKnowledgeId, mode, UserAnswers(cacheMap))))
          }
        }
      )
  }
}
