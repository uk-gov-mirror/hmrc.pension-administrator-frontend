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

import base.SpecBase
import connectors.FakeUserAnswersCacheConnector
import controllers.register.routes
import identifiers.Identifier
import identifiers.register._
import models.NormalMode
import models.register.{BusinessType, DeclarationWorkingKnowledge, NonUKBusinessType}
import models.requests.IdentifiedRequest
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{NavigatorBehaviour, UserAnswers}

class RegisterNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import RegisterNavigatorSpec._

  val navigator = new RegisterNavigator(FakeUserAnswersCacheConnector)

  //scalastyle:off line.size.limit
  def routes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save(NormalMode)", "Next Page (Check Mode)", "Save(CheckMode"),
    (BusinessTypeId, unlimitedCompany, businessDetailsPage, false, None, false),
    (BusinessTypeId, limitedCompany, businessDetailsPage, false, None, false),
    (BusinessTypeId, businessPartnership, partnershipBusinessDetails, false, None, false),
    (BusinessTypeId, limitedPartnership, partnershipBusinessDetails, false, None, false),
    (BusinessTypeId, limitedLiabilityPartnership, partnershipBusinessDetails, false, None, false),
    (DeclarationId, emptyAnswers, declarationWorkingKnowledgePage, true, None, false),
    (DeclarationWorkingKnowledgeId, haveDeclarationWorkingKnowledge, declarationFitAndProperPage, true, None, false),
    (DeclarationWorkingKnowledgeId, haveAnAdviser, adviserDetailsPage, true, None, false),
    (DeclarationWorkingKnowledgeId, emptyAnswers, sessionExpiredPage, false, None, false),
    (DeclarationFitAndProperId, emptyAnswers, confirmationPage, false, None, false),
    (AreYouInUKId, inUk,   ukBusinessTypePage, false, None, false),
    (AreYouInUKId, notInUk,   nonUkBusinessTypePage, false, None, false),
    (NonUKBusinessTypeId, nonUkCompany, nonUkCompanyName, false, None, false),
    (NonUKBusinessTypeId, nonUkPartnership, nonUkPartnershipName, false, None, false)
  )

  //scalastyle:on line.size.limit

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigator)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(), dataDescriber)
  }

}

object RegisterNavigatorSpec extends OptionValues {
  lazy val emptyAnswers = UserAnswers(Json.obj())
  lazy val sessionExpiredPage: Call = controllers.routes.SessionExpiredController.onPageLoad()
  lazy val businessDetailsPage = controllers.register.company.routes.CompanyBusinessDetailsController.onPageLoad()
  lazy val partnershipBusinessDetails = controllers.register.partnership.routes.PartnershipBusinessDetailsController.onPageLoad()
  lazy val declarationWorkingKnowledgePage: Call = routes.DeclarationWorkingKnowledgeController.onPageLoad(NormalMode)
  lazy val declarationFitAndProperPage: Call = routes.DeclarationFitAndProperController.onPageLoad()
  lazy val adviserDetailsPage: Call = controllers.register.adviser.routes.AdviserDetailsController.onPageLoad(NormalMode)
  lazy val confirmationPage: Call = routes.ConfirmationController.onPageLoad()
  lazy val surveyPage: Call = controllers.routes.LogoutController.onPageLoad()
  lazy val ukBusinessTypePage: Call = controllers.register.routes.BusinessTypeController.onPageLoad(NormalMode)
  lazy val nonUkBusinessTypePage: Call = controllers.register.routes.NonUKBusinessTypeController.onPageLoad()
  lazy val nonUkCompanyName: Call = controllers.register.company.routes.CompanyRegisteredNameController.onPageLoad()
  lazy val nonUkPartnershipName: Call = controllers.register.partnership.routes.PartnershipRegisteredNameController.onPageLoad()

  val haveDeclarationWorkingKnowledge: UserAnswers = UserAnswers(Json.obj())
    .set(DeclarationWorkingKnowledgeId)(DeclarationWorkingKnowledge.WorkingKnowledge).asOpt.value
  val haveAnAdviser: UserAnswers = UserAnswers(Json.obj())
    .set(DeclarationWorkingKnowledgeId)(DeclarationWorkingKnowledge.Adviser).asOpt.value
  val unlimitedCompany: UserAnswers = UserAnswers(Json.obj())
    .set(BusinessTypeId)(BusinessType.UnlimitedCompany).asOpt.value
  val limitedCompany: UserAnswers = UserAnswers(Json.obj())
    .set(BusinessTypeId)(BusinessType.LimitedCompany).asOpt.value
  val businessPartnership: UserAnswers = UserAnswers(Json.obj())
    .set(BusinessTypeId)(BusinessType.BusinessPartnership).asOpt.value
  val limitedPartnership: UserAnswers = UserAnswers(Json.obj())
    .set(BusinessTypeId)(BusinessType.LimitedPartnership).asOpt.value
  val limitedLiabilityPartnership: UserAnswers = UserAnswers(Json.obj())
    .set(BusinessTypeId)(BusinessType.LimitedLiabilityPartnership).asOpt.value
  val inUk: UserAnswers = UserAnswers(Json.obj())
    .set(AreYouInUKId)(true).asOpt.value
  val notInUk: UserAnswers = UserAnswers(Json.obj())
    .set(AreYouInUKId)(false).asOpt.value
  val nonUkCompany: UserAnswers = UserAnswers(Json.obj())
    .set(NonUKBusinessTypeId)(NonUKBusinessType.Company).asOpt.value
  val nonUkPartnership: UserAnswers = UserAnswers(Json.obj())
    .set(NonUKBusinessTypeId)(NonUKBusinessType.BusinessPartnership).asOpt.value

  implicit val ex: IdentifiedRequest = new IdentifiedRequest() {
    val externalId: String = "test-external-id"
  }

}
