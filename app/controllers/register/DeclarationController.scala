/*
 * Copyright 2021 HM Revenue & Customs
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

import com.google.inject.Singleton
import config.FrontendAppConfig
import connectors._
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import identifiers.register.{DeclarationId, _}
import models.requests.DataRequest
import models.{ExistingPSA, Mode, NormalMode}
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.Register
import utils.{KnownFactsRetrieval, Navigator, UserAnswers}
import views.html.register.declaration

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeclarationController @Inject()(
                                       appConfig: FrontendAppConfig,
                                       authenticate: AuthAction,
                                       allowAccess: AllowAccessActionProvider,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       allowDeclaration: AllowDeclarationActionProvider,
                                       @Register navigator: Navigator,
                                       dataCacheConnector: UserAnswersCacheConnector,
                                       pensionsSchemeConnector: PensionsSchemeConnector,
                                       knownFactsRetrieval: KnownFactsRetrieval,
                                       enrolments: TaxEnrolmentsConnector,
                                       emailConnector: EmailConnector,
                                       val controllerComponents: MessagesControllerComponents,
                                       val view: declaration
                                     )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with Retrievals {

  private val logger = Logger(classOf[DeclarationController])

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen
    getData andThen allowDeclaration(mode) andThen requireData).async {
    implicit request =>
      DeclarationWorkingKnowledgeId.retrieve.right.map {
        workingKnowledge =>
          workingKnowledge.hasWorkingKnowledge
          Future.successful(Ok(view(workingKnowledge.hasWorkingKnowledge)))
      }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen
    getData andThen allowDeclaration(mode) andThen requireData).async {
    implicit request =>
      dataCacheConnector.save(request.externalId, DeclarationId, value = true).flatMap { cacheMap =>
        val answers = UserAnswers(cacheMap).set(ExistingPSAId)(ExistingPSA(
          request.user.isExistingPSA,
          request.user.existingPSAId
        )).asOpt.getOrElse(UserAnswers(cacheMap))

        (for {
          psaResponse <- pensionsSchemeConnector.registerPsa(answers)
          cacheMap <- dataCacheConnector.save(request.externalId, PsaSubscriptionResponseId, psaResponse)
          _ <- enrol(psaResponse.psaId)
          _ <- sendEmail(psaResponse.psaId)
        } yield {
          Redirect(navigator.nextPage(DeclarationId, NormalMode, UserAnswers(cacheMap)))
        }) recoverWith {
          case _: BadRequestException =>
            Future.successful(Redirect(controllers.register.routes.SubmissionInvalidController.onPageLoad()))
          case ex: UpstreamErrorResponse if ex.message.contains("INVALID_BUSINESS_PARTNER") =>
            Future.successful(Redirect(controllers.register.routes.DuplicateRegistrationController.onPageLoad()))
          case _ =>
            Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
        }
      }
  }

  private def sendEmail(psaId: String)
                       (implicit hc: HeaderCarrier, request: DataRequest[AnyContent]): Future[EmailStatus] =
    (psaEmail, psaName) match {
      case (Some(email), Some(name)) =>
        val templateParams = Map("psaName" -> name)
        emailConnector.sendEmail(email, appConfig.emailTemplateId, templateParams, PsaId(psaId))
      case _ => Future.successful(EmailNotSent)
    }

  private def enrol(psaId: String)
                   (implicit hc: HeaderCarrier, request: DataRequest[AnyContent]): Future[HttpResponse] =
    knownFactsRetrieval.retrieve(psaId) map { knownFacts =>
      enrolments.enrol(psaId, knownFacts)
    } getOrElse Future.failed(KnownFactsRetrievalException())

  case class KnownFactsRetrievalException() extends Exception {
    def apply(): Unit = logger.error("Could not retrieve Known Facts")
  }

  case class PSANameNotFoundException() extends Exception("Could not retrieve PSA Name")

}



