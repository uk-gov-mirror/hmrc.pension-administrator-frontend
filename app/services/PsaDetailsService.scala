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

package services

import com.google.inject.{ImplementedBy, Inject}
import connectors.SubscriptionConnector
import connectors.cache.UserAnswersCacheConnector
import identifiers.register._
import identifiers.register.company.directors.{DirectorAddressId, IsDirectorCompleteId, ExistingCurrentAddressId => DirectorsExistingCurrentAddressId}
import identifiers.register.company.{CompanyContactAddressChangedId, CompanyContactAddressId, CompanyContactDetailsChangedId, CompanyPreviousAddressChangedId, ExistingCurrentAddressId => CompanyExistingCurrentAddressId}
import identifiers.register.individual._
import identifiers.register.partnership.partners.{IsPartnerCompleteId, PartnerAddressId, ExistingCurrentAddressId => PartnersExistingCurrentAddressId}
import identifiers.register.partnership.{PartnershipContactAddressChangedId, PartnershipContactAddressId, PartnershipContactDetailsChangedId, PartnershipPreviousAddressChangedId, ExistingCurrentAddressId => PartnershipExistingCurrentAddressId}
import identifiers.{IndexId, TypedIdentifier, UpdateModeId}
import models.RegistrationLegalStatus.{Individual, LimitedCompany, Partnership}
import models._
import models.requests.OptionalDataRequest
import play.api.i18n.Messages
import play.api.libs.json._
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.http.HeaderCarrier
import utils.countryOptions.CountryOptions
import utils.{UserAnswers, ViewPsaDetailsHelper}
import viewmodels.{PsaViewDetailsViewModel, SuperSection}

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[PsaDetailServiceImpl])
trait PsaDetailsService {
  def retrievePsaDataAndGenerateViewModel(psaId: String, mode: Mode)
                                         (implicit hc: HeaderCarrier, executionContext: ExecutionContext, request: OptionalDataRequest[_], messages: Messages): Future[PsaViewDetailsViewModel]
}

class PsaDetailServiceImpl @Inject()(subscriptionConnector: SubscriptionConnector,
                                     countryOptions: CountryOptions,
                                     userAnswersCacheConnector: UserAnswersCacheConnector,
                                     controllerComponents: MessagesControllerComponents
                                    ) extends PsaDetailsService {

  override def retrievePsaDataAndGenerateViewModel(psaId: String, mode: Mode)
                                                  (implicit hc: HeaderCarrier, executionContext: ExecutionContext, request: OptionalDataRequest[_], messages: Messages): Future[PsaViewDetailsViewModel] = {

    retrievePsaDataFromUserAnswers(psaId, mode)

  }

  def retrievePsaDataFromUserAnswers(psaId: String, mode: Mode
                                    )(implicit hc: HeaderCarrier, executionContext: ExecutionContext, request: OptionalDataRequest[_], messages: Messages): Future[PsaViewDetailsViewModel] = {
    for {
      userAnswers <- getUserAnswers(psaId, mode)
      _ <- userAnswersCacheConnector.upsert(request.externalId, userAnswers.json)
    } yield {
      getPsaDetailsViewModel(userAnswers)
    }
  }

  private[services] def getUserAnswers(psaId: String, mode: Mode
                                      )(implicit hc: HeaderCarrier, executionContext: ExecutionContext, request: OptionalDataRequest[_]): Future[UserAnswers] =
    userAnswersCacheConnector.fetch(request.externalId).flatMap {
      case None => subscriptionConnector.getSubscriptionDetails(psaId).flatMap {
        getUpdatedUserAnswers(_, mode)
      }
      case Some(data) => {
        (UserAnswers(data).get(IndexId), UserAnswers(data).get(RegistrationInfoId)) match {
          case (Some(_), None) =>
            userAnswersCacheConnector.removeAll(request.externalId).flatMap { _ =>
              subscriptionConnector.getSubscriptionDetails(psaId).flatMap {
                getUpdatedUserAnswers(_, mode)
              }
            }
          case _ =>
            Future(UserAnswers(data))
        }
      }
    }

  private val changeFlagIds = List(
    IndividualAddressChangedId,
    IndividualPreviousAddressChangedId,
    IndividualContactDetailsChangedId,
    CompanyContactAddressChangedId,
    CompanyPreviousAddressChangedId,
    CompanyContactDetailsChangedId,
    PartnershipContactAddressChangedId,
    PartnershipPreviousAddressChangedId,
    PartnershipContactDetailsChangedId,
    DeclarationChangedId,
    MoreThanTenDirectorsOrPartnersChangedId,
    DirectorsOrPartnersChangedId
  )

  private def getUpdatedUserAnswers(response: JsValue, mode: Mode)(implicit executionContext: ExecutionContext): Future[UserAnswers] = {
    val answers = UserAnswers(response)
    val legalStatus = answers.get(RegistrationInfoId) map (_.legalStatus)
    Future.successful(
      setCompleteAndAddressIdsToUserAnswers(answers, legalStatus, mode).flatMap(_.set(UpdateModeId)(true))
        .flatMap(_.setAllFlagsToValue(changeFlagIds, value = false))
        .asOpt.getOrElse(answers)
    )
  }

  private def getPsaDetailsViewModel(userAnswers: UserAnswers)(implicit messages: Messages): PsaViewDetailsViewModel = {
    val isUserAnswerUpdated = userAnswers.isUserAnswerUpdated()
    val legalStatus = userAnswers.get(RegistrationInfoId) map (_.legalStatus)
    val viewPsaDetailsHelper = new ViewPsaDetailsHelper(userAnswers, countryOptions)

    val (superSections, name): (Seq[SuperSection], String) = legalStatus match {
      case Some(Individual) =>
        (viewPsaDetailsHelper.individualSections,
          userAnswers.get(IndividualDetailsId).map(_.fullName).getOrElse(""))

      case Some(LimitedCompany) => (
        viewPsaDetailsHelper.companySections,
        userAnswers.get(BusinessNameId).getOrElse(""))

      case Some(Partnership) => (
        viewPsaDetailsHelper.partnershipSections,
        userAnswers.get(BusinessNameId).getOrElse(""))

      case unknownStatus =>
        throw new IllegalArgumentException(s"Unknown Legal Status : $unknownStatus")
    }

    PsaViewDetailsViewModel(superSections, name, isUserAnswerUpdated)
  }

  private def setCompleteAndAddressIdsToUserAnswers(userAnswers: UserAnswers,
                                                    legalStatus: Option[RegistrationLegalStatus],
                                                    mode: Mode): JsResult[UserAnswers] = {

    val (seqOfCompleteIds, mapOfAddressIds):
      (List[TypedIdentifier[Boolean]], Map[TypedIdentifier[Address], TypedIdentifier[TolerantAddress]]) = legalStatus match {
      case Some(Individual) =>
        (Nil, Map(IndividualContactAddressId -> ExistingCurrentAddressId))

      case Some(LimitedCompany) =>
        val allDirectors = userAnswers.allDirectorsAfterDelete(UpdateMode)
        val allDirectorsCompleteIds = allDirectors.map(director => IsDirectorCompleteId(allDirectors.indexOf(director))).toList
        val allDirectorsAddressIdMap = allDirectors.map { director =>
          val index = allDirectors.indexOf(director)
          (DirectorAddressId(index), DirectorsExistingCurrentAddressId(index))
        }.toMap

        (allDirectorsCompleteIds, Map(CompanyContactAddressId -> CompanyExistingCurrentAddressId) ++ allDirectorsAddressIdMap)

      case Some(Partnership) =>
        val allPartners = userAnswers.allPartnersAfterDelete(UpdateMode)
        val allPartnersCompleteIds = allPartners.map(partner => IsPartnerCompleteId(allPartners.indexOf(partner))).toList
        val allPartnersAddressIds = allPartners.map { partner =>
          val index = allPartners.indexOf(partner)
          (PartnerAddressId(index), PartnersExistingCurrentAddressId(index))
        }.toMap

        (allPartnersCompleteIds, Map(PartnershipContactAddressId -> PartnershipExistingCurrentAddressId) ++ allPartnersAddressIds)

      case _ =>
        (Nil, Map.empty)
    }
    userAnswers.setAllFlagsToValue(seqOfCompleteIds, value = true).flatMap(ua => ua.setAllExistingAddress(mapOfAddressIds))
  }
}
