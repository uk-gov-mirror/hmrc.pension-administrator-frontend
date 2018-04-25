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

import javax.inject.Inject

import identifiers.Identifier
import identifiers.register.{DeclarationFitAndProperId, DeclarationId, DeclarationWorkingKnowledgeId}
import models.NormalMode
import models.register.DeclarationWorkingKnowledge
import play.api.mvc.Call
import utils.{Navigator, UserAnswers}

class RegisterNavigator @Inject() extends Navigator {

  override protected def routeMap: PartialFunction[Identifier, UserAnswers => Call] = {
    case DeclarationId =>
      _ => controllers.register.routes.DeclarationWorkingKnowledgeController.onPageLoad(NormalMode)
    case DeclarationWorkingKnowledgeId =>
      declarationWorkingKnowledgeRoutes()
    case DeclarationFitAndProperId =>
      _ => controllers.register.routes.ConfirmationController.onPageLoad()
  }

  private def declarationWorkingKnowledgeRoutes()(userAnswers: UserAnswers): Call = {
    userAnswers.get(DeclarationWorkingKnowledgeId) match {
      case Some(DeclarationWorkingKnowledge.WorkingKnowledge) =>
        controllers.register.routes.DeclarationFitAndProperController.onPageLoad()
      case Some(DeclarationWorkingKnowledge.Adviser) =>
        controllers.register.advisor.routes.AdvisorDetailsController.onPageLoad(NormalMode)
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }
}
