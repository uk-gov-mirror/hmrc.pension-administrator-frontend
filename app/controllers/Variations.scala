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

import connectors.UserAnswersCacheConnector
import identifiers.TypedIdentifier
import identifiers.register._
import identifiers.register.adviser.{AdviserAddressId, AdviserDetailsId, ConfirmDeleteAdviserId}
import identifiers.register.company._
import identifiers.register.company.directors._
import identifiers.register.individual._
import identifiers.register.partnership._
import identifiers.register.partnership.partners._
import models._
import models.requests.DataRequest
import play.api.libs.json._
import play.api.mvc.AnyContent
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

trait Variations extends FrontendController {

  protected def cacheConnector: UserAnswersCacheConnector

  protected implicit val ec: ExecutionContext = play.api.libs.concurrent.Execution.defaultContext

  private val changeIds: Map[TypedIdentifier[_], TypedIdentifier[Boolean]] = Map(
    IndividualContactAddressId -> IndividualAddressChangedId,
    IndividualPreviousAddressId -> IndividualPreviousAddressChangedId,
    IndividualContactDetailsId -> IndividualContactDetailsChangedId,
    CompanyContactAddressId -> CompanyContactAddressChangedId,
    CompanyPreviousAddressId -> CompanyPreviousAddressChangedId,
    ContactDetailsId -> CompanyContactDetailsChangedId,
    PartnershipContactAddressId -> PartnershipContactAddressChangedId,
    PartnershipPreviousAddressId -> PartnershipPreviousAddressChangedId,
    PartnershipContactDetailsId -> PartnershipContactDetailsChangedId,
    DeclarationWorkingKnowledgeId -> DeclarationChangedId,
    VariationWorkingKnowledgeId -> DeclarationChangedId,
    AdviserAddressId -> DeclarationChangedId,
    AdviserDetailsId -> DeclarationChangedId,
    ConfirmDeleteAdviserId -> DeclarationChangedId,
    MoreThanTenDirectorsId -> MoreThanTenDirectorsOrPartnersChangedId,
    MoreThanTenPartnersId -> MoreThanTenDirectorsOrPartnersChangedId,
    identifiers.register.company.directors.CheckYourAnswersId -> DirectorsOrPartnersChangedId,
    identifiers.register.partnership.partners.CheckYourAnswersId -> DirectorsOrPartnersChangedId
  )

  protected def findChangeIdNonIndexed[A](id: TypedIdentifier[A]): Option[TypedIdentifier[Boolean]] = {
    changeIds.find(_._1 == id) match {
      case Some(item) => Some(item._2)
      case None => None
    }
  }

  protected def findChangeIdIndexed[A](id: TypedIdentifier[A]): Option[TypedIdentifier[Boolean]] = {
    id match {
      case DirectorAddressId(_) | DirectorAddressYearsId(_) | DirectorContactDetailsId(_) |
           DirectorNinoId(_) | DirectorPreviousAddressId(_) | DirectorUniqueTaxReferenceId(_) | DirectorDetailsId(_)
              => Some(DirectorsOrPartnersChangedId)
      case PartnerAddressId(_) | PartnerAddressYearsId(_) | PartnerContactDetailsId(_) |
           PartnerNinoId(_) | PartnerPreviousAddressId(_) | PartnerUniqueTaxReferenceId(_) | PartnerDetailsId(_)
      => Some(DirectorsOrPartnersChangedId)
      case _ => None
    }
  }

  def saveChangeFlag[A](mode: Mode, id: TypedIdentifier[A])(implicit request: DataRequest[AnyContent]): Future[JsValue] = {
    val applicableMode = if(mode == UpdateMode) Some(mode) else None

    val result = applicableMode.flatMap { _ =>
      findChangeIdNonIndexed(id).fold(findChangeIdIndexed(id))(Some(_))
        .map(cacheConnector.save(request.externalId, _, value = true))
    }
    result.fold(Future.successful(request.userAnswers.json))(identity)
  }
}
