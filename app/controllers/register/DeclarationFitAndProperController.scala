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

package controllers.register

import javax.inject.Inject

import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import controllers.actions._
import config.FrontendAppConfig
import connectors._
import forms.register.DeclarationFormProvider
import identifiers.register.{DeclarationFitAndProperId, ExistingPSAId, PsaSubscriptionResponseId}
import models.requests.DataRequest
import models.{ExistingPSA, NormalMode, UserType}
import play.api.Logger
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.annotations.Register
import utils.{KnownFactsRetrieval, Navigator, UserAnswers}
import views.html.register.declarationFitAndProper

import scala.concurrent.Future

class DeclarationFitAndProperController @Inject()(appConfig: FrontendAppConfig,
                                                  override val messagesApi: MessagesApi,
                                                  authenticate: AuthAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  @Register navigator: Navigator,
                                                  formProvider: DeclarationFormProvider,
                                                  dataCacheConnector: DataCacheConnector,
                                                  pensionsSchemeConnector: PensionsSchemeConnector,
                                                  knownFactsRetrieval: KnownFactsRetrieval,
                                                  enrolments: EnrolmentStoreConnector,
                                                  authenticator: AuthenticationConnector
                                                 ) extends FrontendController with I18nSupport {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      request.user.userType match {
        case UserType.Individual =>
          Future.successful(Ok(
            declarationFitAndProper(appConfig, form, individual.routes.WhatYouWillNeedController.onPageLoad())))

        case UserType.Organisation =>
          Future.successful(Ok(
            declarationFitAndProper(appConfig, form, company.routes.WhatYouWillNeedController.onPageLoad())))
      }
  }

  def onSubmit: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        errors =>
          request.user.userType match {
            case UserType.Individual =>
              Future.successful(BadRequest(
                declarationFitAndProper(appConfig, errors, individual.routes.WhatYouWillNeedController.onPageLoad())))

            case UserType.Organisation =>
              Future.successful(BadRequest(
                declarationFitAndProper(appConfig, errors, company.routes.WhatYouWillNeedController.onPageLoad())))
          },
        success =>
          dataCacheConnector.save(request.externalId, DeclarationFitAndProperId, success).flatMap { cacheMap =>

            val answers = UserAnswers(cacheMap).set(ExistingPSAId)(ExistingPSA(
              request.user.isExistingPSA,
              request.user.existingPSAId
            )).asOpt.getOrElse(UserAnswers(cacheMap))

            (for {
              psaResponse <- pensionsSchemeConnector.registerPsa(answers)
              cacheMap <- dataCacheConnector.save(request.externalId, PsaSubscriptionResponseId, psaResponse)
              _ <- enrol(psaResponse.psaId)
            } yield {
              Redirect(navigator.nextPage(DeclarationFitAndProperId, NormalMode)(UserAnswers(cacheMap)))
            }) recoverWith {
              case _: InvalidBusinessPartnerException =>
                Future.successful(Redirect(controllers.register.routes.DuplicateRegistrationController.onPageLoad()))
              case _ =>
                Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
            }

          }
      )
  }

  private def enrol(psaId: String)(implicit hc: HeaderCarrier, request: DataRequest[AnyContent]): Future[HttpResponse] = {
    knownFactsRetrieval.retrieve map { knownFacts =>
      enrolments.enrol(psaId, knownFacts) flatMap { _ =>
        authenticator.refreshProfile
      }
    } getOrElse Future.failed(KnownFactsRetrievalException())
  }

  case class KnownFactsRetrievalException() extends Exception {
    def apply(): Unit = Logger.error("Could not retrieve Known Facts")
  }

}
