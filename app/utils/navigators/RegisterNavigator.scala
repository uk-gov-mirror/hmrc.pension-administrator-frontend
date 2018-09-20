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

package utils.navigators

import connectors.UserAnswersCacheConnector
import identifiers.register.{BusinessTypeId, DeclarationFitAndProperId, DeclarationId, DeclarationWorkingKnowledgeId}
import javax.inject.Inject
import models.NormalMode
import models.register.{BusinessType, DeclarationWorkingKnowledge}
import utils.{Navigator, UserAnswers}

class RegisterNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector) extends Navigator {

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case BusinessTypeId =>
      businessTypeRoutes(from.userAnswers)
    case DeclarationId => NavigateTo.save(controllers.register.routes.DeclarationWorkingKnowledgeController.onPageLoad(NormalMode))
    case DeclarationWorkingKnowledgeId =>
      declarationWorkingKnowledgeRoutes(from.userAnswers)
    case DeclarationFitAndProperId => NavigateTo.dontSave(controllers.register.routes.ConfirmationController.onPageLoad())
    case _ => None
  }

  private def businessTypeRoutes(userAnswers: UserAnswers): Option[NavigateTo] = {
    userAnswers.get(BusinessTypeId) match {
      case Some(BusinessType.UnlimitedCompany) =>
        NavigateTo.dontSave(controllers.register.company.routes.CompanyBusinessDetailsController.onPageLoad())
      case Some(BusinessType.LimitedCompany) =>
        NavigateTo.dontSave(controllers.register.company.routes.CompanyBusinessDetailsController.onPageLoad())
      case Some(BusinessType.LimitedLiabilityPartnership) =>
        NavigateTo.dontSave(controllers.register.partnership.routes.PartnershipBusinessDetailsController.onPageLoad())
      case Some(BusinessType.LimitedPartnership) =>
        NavigateTo.dontSave(controllers.register.partnership.routes.PartnershipBusinessDetailsController.onPageLoad())
      case Some(BusinessType.BusinessPartnership) =>
        NavigateTo.dontSave(controllers.register.partnership.routes.PartnershipBusinessDetailsController.onPageLoad())
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def declarationWorkingKnowledgeRoutes(userAnswers: UserAnswers): Option[NavigateTo] = {
    userAnswers.get(DeclarationWorkingKnowledgeId) match {
      case Some(DeclarationWorkingKnowledge.WorkingKnowledge) =>
        NavigateTo.save(controllers.register.routes.DeclarationFitAndProperController.onPageLoad())
      case Some(DeclarationWorkingKnowledge.Adviser) =>
        NavigateTo.save(controllers.register.adviser.routes.AdviserDetailsController.onPageLoad(NormalMode))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case _ => None
  }
}
