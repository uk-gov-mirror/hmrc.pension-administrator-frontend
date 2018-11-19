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

package controllers.actions

import java.net.URLEncoder

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.routes
import identifiers.register.AreYouInUKId
import identifiers.{PsaId => UserPsaId}
import models.UserType.UserType
import models.requests.AuthenticatedRequest
import models.{PSAUser, UserType}
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.HeaderCarrierConverter
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}

class FullAuthentication @Inject()(override val authConnector: AuthConnector, config: FrontendAppConfig, userAnswersCacheConnector: UserAnswersCacheConnector)
                                  (implicit ec: ExecutionContext) extends AuthAction with AuthorisedFunctions {

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    authorised(User or Admin).retrieve(
      Retrievals.externalId and
        Retrievals.confidenceLevel and
        Retrievals.affinityGroup and
        Retrievals.nino and
        Retrievals.allEnrolments) {
      case Some(id) ~ cl ~ Some(affinityGroup) ~ nino ~ enrolments =>
        redirectToInterceptPages(enrolments, request, cl, affinityGroup).fold(
          result => Future.successful(result),
          _ => {
            val authRequest = AuthenticatedRequest(request, id, psaUser(cl, affinityGroup, nino, enrolments))
            successRedirect(affinityGroup, id, cl, enrolments, authRequest, block)
          }
        )
      case _ =>
        Future.successful(Redirect(routes.UnauthorisedController.onPageLoad()))

    } recover handleFailure
  }

  def successRedirect[A](affinityGroup: AffinityGroup, id: String, cl: ConfidenceLevel,
                              enrolments: Enrolments, authRequest: AuthenticatedRequest[A], block: AuthenticatedRequest[A] => Future[Result])
                                (implicit hc: HeaderCarrier) =
    if (affinityGroup == Individual && config.nonUkJourneys) {
      areYouInUK(id).flatMap {
        case Some(true) if !allowedIndividual(cl) =>
          Future.successful(Redirect(ivUpliftUrl))
        case _ =>
          savePsaIdAndReturnAuthRequest(enrolments, id, authRequest, block)
      }
    } else {
      savePsaIdAndReturnAuthRequest(enrolments, id, authRequest, block)
    }


  protected def savePsaIdAndReturnAuthRequest[A](enrolments: Enrolments, id: String, authRequest: AuthenticatedRequest[A],
                                               block: AuthenticatedRequest[A] => Future[Result])(implicit hc: HeaderCarrier) = {
    if (alreadyEnrolledInPODS(enrolments)) {
      userAnswersCacheConnector.save(id, UserPsaId, getPSAId(enrolments)).flatMap {
        _ => block(authRequest)
      }
    } else {
      block(authRequest)
    }
  }


  private def redirectToInterceptPages[A](enrolments: Enrolments, request: Request[A],
                                          cl: ConfidenceLevel, affinityGroup: AffinityGroup) = {
    if (alreadyEnrolledInPODS(enrolments) && notNewRegPages(request)) {
      Left(Redirect(routes.InterceptPSAController.onPageLoad()))
    } else if (isPSP(enrolments) && !isPSA(enrolments)) {
      Left(Redirect(routes.PensionSchemePractitionerController.onPageLoad()))
    } else if (!config.nonUkJourneys && affinityGroup == Individual && !allowedIndividual(cl)) {
      Left(Redirect(ivUpliftUrl))
    } else {
      Right(())
    }
  }

  private def areYouInUK(id: String)(implicit hc: HeaderCarrier): Future[Option[Boolean]] = {
    userAnswersCacheConnector.fetch(id).map {
      case Some(json) =>
        UserAnswers(json).get(AreYouInUKId)
      case None =>
        None
    }
  }

  private def handleFailure: PartialFunction[Throwable, Result] = {
    case _: NoActiveSession =>
      Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
    case _: InsufficientEnrolments =>
      Redirect(routes.UnauthorisedController.onPageLoad())
    case _: InsufficientConfidenceLevel =>
      Redirect(routes.UnauthorisedController.onPageLoad())
    case _: UnsupportedAuthProvider =>
      Redirect(routes.UnauthorisedController.onPageLoad())
    case _: UnsupportedAffinityGroup =>
      Redirect(routes.UnauthorisedController.onPageLoad())
    case _: UnsupportedCredentialRole =>
      Redirect(routes.UnauthorisedAssistantController.onPageLoad())
    case _: UnauthorizedException =>
      Redirect(routes.UnauthorisedController.onPageLoad())
  }

  private def ivUpliftUrl: String = {
    val continueUrl = if (config.nonUkJourneys) config.ukJourneyContinueUrl else config.loginContinueUrl
    s"${config.ivUpliftUrl}?origin=PODS&" +
      s"completionURL=${URLEncoder.encode(continueUrl, "UTF-8")}&" +
      s"failureURL=${URLEncoder.encode(s"${config.loginContinueUrl}/unauthorised", "UTF-8")}" +
      s"&confidenceLevel=${ConfidenceLevel.L200.level}"
  }

  private def allowedIndividual(confidenceLevel: ConfidenceLevel): Boolean =
    confidenceLevel.compare(ConfidenceLevel.L200) >= 0

  private def existingPSA(enrolments: Enrolments): Option[String] =
    enrolments.getEnrolment("HMRC-PSA-ORG").flatMap(_.getIdentifier("PSAID")).map(_.value)

  private def isPSP(enrolments: Enrolments): Boolean =
    enrolments.getEnrolment(key = "HMRC-PP-ORG").nonEmpty

  private def isPSA(enrolments: Enrolments): Boolean =
    enrolments.getEnrolment(key = "HMRC-PSA-ORG").nonEmpty

  protected def alreadyEnrolledInPODS(enrolments: Enrolments): Boolean =
    enrolments.getEnrolment("HMRC-PODS-ORG").nonEmpty

  private def notNewRegPages[A](request: Request[A]): Boolean = {
    val confirmationSeq = Seq(config.confirmationUri, config.duplicateRegUri, config.registeredPsaDetailsUri)
    !confirmationSeq.contains(request.uri)
  }

  private def userType(affinityGroup: AffinityGroup, cl: ConfidenceLevel): UserType = {
    affinityGroup match {
      case Individual =>
        UserType.Individual
      case Organisation =>
        UserType.Organisation
      case _ =>
        throw new UnauthorizedException("Unable to authorise the user")
    }
  }

  private def psaUser(cl: ConfidenceLevel, affinityGroup: AffinityGroup,
                      nino: Option[String], enrolments: Enrolments): PSAUser = {
    val psa = existingPSA(enrolments)
    PSAUser(userType(affinityGroup, cl), nino, psa.nonEmpty, psa)
  }

  private def getPSAId(enrolments: Enrolments): String =
    enrolments.getEnrolment("HMRC-PODS-ORG").flatMap(_.getIdentifier("PSAID")).map(_.value)
      .getOrElse(throw new RuntimeException("PSA ID missing"))
}

class AuthenticationWithNoConfidence @Inject()(override val authConnector: AuthConnector, config: FrontendAppConfig,
                                               userAnswersCacheConnector: UserAnswersCacheConnector)
                                              (implicit ec: ExecutionContext) extends FullAuthentication(authConnector, config, userAnswersCacheConnector) with AuthorisedFunctions {


  override def successRedirect[A](affinityGroup: AffinityGroup, id: String, cl: ConfidenceLevel,
                                  enrolments: Enrolments, authRequest: AuthenticatedRequest[A], block: AuthenticatedRequest[A] => Future[Result])
                                 (implicit hc: HeaderCarrier) =

    savePsaIdAndReturnAuthRequest(enrolments, id, authRequest, block)
}

trait AuthAction extends ActionBuilder[AuthenticatedRequest] with ActionFunction[Request, AuthenticatedRequest]
