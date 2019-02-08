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
import config.{FeatureSwitchManagementService, FrontendAppConfig}
import connectors.{DeRegistrationConnector, SubscriptionConnector, UserAnswersCacheConnector}
import controllers.actions.{AllowAccessActionProvider, AuthAction}
import identifiers.UpdateModeId
import identifiers.register.RegistrationInfoId
import identifiers.register.company.BusinessDetailsId
import identifiers.register.individual.IndividualDetailsId
import identifiers.register.partnership.PartnershipDetailsId
import models.Mode
import models.RegistrationLegalStatus.{Individual, LimitedCompany, Partnership}
import models.requests.AuthenticatedRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Toggles.{isDeregistrationEnabled, isVariationsEnabled}
import utils.countryOptions.CountryOptions
import utils.{PsaDetailsHelper, UserAnswers, ViewPsaDetailsHelper}
import viewmodels.PsaViewDetailsViewModel
import views.html.psa_details

import scala.concurrent.{ExecutionContext, Future}

class PsaDetailsController @Inject()(appConfig: FrontendAppConfig,
                                     override val messagesApi: MessagesApi,
                                     authenticate: AuthAction,
                                     allowAccess: AllowAccessActionProvider,
                                     subscriptionConnector: SubscriptionConnector,
                                     deRegistrationConnector: DeRegistrationConnector,
                                     dataCacheConnector: UserAnswersCacheConnector,
                                     countryOptions: CountryOptions,
                                     fs: FeatureSwitchManagementService
                                    )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode)).async {
    implicit request =>
      val psaId = request.user.alreadyEnrolledPsaId.getOrElse(throw new RuntimeException("PSA ID not found"))
      canStopBeingAPsa(psaId) flatMap { canDeregister =>
        val retrieval = if(fs.get(isVariationsEnabled)) retrievePsaDataFromUserAnswers(psaId, canDeregister) else retrievePsaDataFromModel(psaId, canDeregister)
        retrieval.map { psaDetails =>
            Ok(psa_details(appConfig, psaDetails))
        }
      }
  }

  private def retrievePsaDataFromModel(psaId: String, canDeregister: Boolean)(implicit hc: HeaderCarrier): Future[PsaViewDetailsViewModel] = {
      subscriptionConnector.getSubscriptionModel(psaId).map { response =>
      response.organisationOrPartner match {
        case None =>
          PsaViewDetailsViewModel(
            new PsaDetailsHelper(response, countryOptions).individualSections,
            response.individual.map(_.fullName).getOrElse(""),
            false,
            canDeregister)
        case _ =>
          PsaViewDetailsViewModel(
            new PsaDetailsHelper(response, countryOptions).organisationSections,
            response.organisationOrPartner.map(_.name).getOrElse(""),
            false,
            canDeregister)
      }
    }
  }

  private def retrievePsaDataFromUserAnswers(psaId: String, canDeregister: Boolean)(
    implicit hc: HeaderCarrier, request: AuthenticatedRequest[_]): Future[PsaViewDetailsViewModel] = {
    subscriptionConnector.getSubscriptionDetails(psaId) flatMap { response =>
      val userAnswers = UserAnswers(response).set(UpdateModeId)(true).asOpt.getOrElse(UserAnswers(response))
      dataCacheConnector.upsert(request.externalId, userAnswers.json).flatMap{ _ =>
        val legalStatus = userAnswers.get(RegistrationInfoId) map (_.legalStatus)
        val isUserAnswerUpdated = userAnswers.isUserAnswerUpdated()
        Future.successful(
          legalStatus match {
            case Some(Individual) =>
              PsaViewDetailsViewModel(
                new ViewPsaDetailsHelper(userAnswers, countryOptions).individualSections,
                userAnswers.get(IndividualDetailsId).map(_.fullName).getOrElse(""),
                isUserAnswerUpdated,
                canDeregister)

            case Some(LimitedCompany) =>
              PsaViewDetailsViewModel(
                new ViewPsaDetailsHelper(userAnswers, countryOptions).companySections,
                userAnswers.get(BusinessDetailsId).map(_.companyName).getOrElse(""),
                isUserAnswerUpdated,
                canDeregister)

            case Some(Partnership) =>
              PsaViewDetailsViewModel(
                new ViewPsaDetailsHelper(userAnswers, countryOptions).partnershipSections,
                userAnswers.get(PartnershipDetailsId).map(_.companyName).getOrElse(""),
                isUserAnswerUpdated,
                canDeregister)

            case _ =>
              PsaViewDetailsViewModel(Nil, "", isUserAnswerUpdated, canDeregister)
          })
      }
    }
  }

  private def canStopBeingAPsa(psaId: String)(implicit hc: HeaderCarrier): Future[Boolean] = {
    if (fs.get(isDeregistrationEnabled)) {
      deRegistrationConnector.canDeRegister(psaId)
    } else {
      Future.successful(false)
    }
  }
}
