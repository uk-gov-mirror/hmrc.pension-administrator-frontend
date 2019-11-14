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
import forms.register.VariationDeclarationFormProvider
import identifiers.register._
import javax.inject.Inject
import models._
import models.register.DeclarationWorkingKnowledge
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import utils.annotations.{Register, Variations}

import scala.concurrent.{ExecutionContext, Future}

class VariationDeclarationController @Inject()(val appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               authenticate: AuthAction,
                                               allowAccess: AllowAccessActionProvider,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               @Variations navigator: Navigator,
                                               formProvider: VariationDeclarationFormProvider,
                                               dataCacheConnector: UserAnswersCacheConnector,
                                               pensionsSchemeConnector: PensionsSchemeConnector
                                              )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>

      val workingKnowledge = request.userAnswers.get(VariationWorkingKnowledgeId).getOrElse(false)

      Future.successful(Ok(views.html.register.variationDeclaration(
        appConfig, form, psaName(), workingKnowledge)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>

      val workingKnowledge = request.userAnswers.get(VariationWorkingKnowledgeId).getOrElse(false)

        form.bindFromRequest().fold(
          errors => Future.successful(BadRequest(views.html.register.variationDeclaration(
            appConfig, errors, psaName(), workingKnowledge))),

          success =>
            dataCacheConnector.save(request.externalId, DeclarationId, success).flatMap { json =>

              val psaId = request.user.alreadyEnrolledPsaId.getOrElse(throw new RuntimeException("PSA ID not found"))
              val answers = UserAnswers(json).set(ExistingPSAId)(ExistingPSA(
                request.user.isExistingPSA,
                request.user.existingPSAId
              )).asOpt.getOrElse(UserAnswers(json))
                .set(DeclarationWorkingKnowledgeId)(
                  DeclarationWorkingKnowledge.declarationWorkingKnowledge(workingKnowledge))
                .asOpt.getOrElse(UserAnswers(json))

              pensionsSchemeConnector.updatePsa(psaId, answers).map(_ =>
                Redirect(navigator.nextPage(DeclarationId, mode, UserAnswers(json)))
              )
            }
        )
  }
}
