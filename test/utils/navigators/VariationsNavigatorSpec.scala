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

package utils.navigators

import base.SpecBase
import identifiers.{UpdateContactAddressId, Identifier}
import identifiers.register._
import identifiers.register.adviser.{ConfirmDeleteAdviserId, AdviserNameId}
import identifiers.register.company.CompanyContactAddressId
import identifiers.register.individual.{IndividualContactAddressId, IndividualDetailsId}
import identifiers.register.partnership.PartnershipContactAddressId
import models._
import models.requests.IdentifiedRequest
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor3
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{NavigatorBehaviour, Navigator, UserAnswers}

class VariationsNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import VariationsNavigatorSpec._

  val navigator: Navigator = injector.instanceOf[VariationsNavigator]

  "VariationsNavigator in UpdateMode" must {
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),
      (ConfirmDeleteAdviserId, confirmDeleteYes, variationWorkingKnowledgePage(UpdateMode)),
      (ConfirmDeleteAdviserId, confirmDeleteNo, checkYourAnswersPage),
      (ConfirmDeleteAdviserId, emptyAnswers, sessionExpiredPage),

      (AnyMoreChangesId, haveMoreChanges, checkYourAnswersPage),
      (AnyMoreChangesId, noMoreChangesAdviserUnchanged, variationWorkingKnowledgePage(CheckUpdateMode)),
      (AnyMoreChangesId, noMoreChangesAdviserChanged, variationDeclarationFitAndProperPage),
      (AnyMoreChangesId, emptyAnswers, sessionExpiredPage),

      (VariationWorkingKnowledgeId, haveWorkingKnowledge, anyMoreChangesPage),
      (VariationWorkingKnowledgeId, noWorkingKnowledge, adviserNamePage),
      (VariationWorkingKnowledgeId, emptyAnswers, sessionExpiredPage),

      (UpdateContactAddressCYAId, declarationChangedWithIncompleteIndividual, incompleteChangesPage),
      (UpdateContactAddressCYAId, declarationChangedWithCompleteIndividual, variationDeclarationFitAndProperPage),
      (UpdateContactAddressCYAId, declarationNotChangedWithAdviser, variationStillWorkingKnowledgePage),
      (UpdateContactAddressCYAId, completeIndividualDetails, variationWorkingKnowledgePage(CheckUpdateMode)),

      (VariationStillDeclarationWorkingKnowledgeId, emptyAnswers, sessionExpiredPage),
      (VariationStillDeclarationWorkingKnowledgeId, stillHaveWorkingKnowledge, variationDeclarationFitAndProperPage),
      (VariationStillDeclarationWorkingKnowledgeId, stillNotHaveWorkingKnowledge, variationWorkingKnowledgePage(CheckUpdateMode)),

      (DeclarationFitAndProperId, haveFitAndProper, variationDeclarationPage),
      (DeclarationFitAndProperId, noFitAndProper, variationNoLongerFitAndProperPage),
      (DeclarationFitAndProperId, emptyAnswers, sessionExpiredPage),

      (DeclarationChangedId, declarationChangedWithIncompleteIndividual, incompleteChangesPage),
      (DeclarationChangedId, declarationChangedWithCompleteIndividual, variationDeclarationFitAndProperPage),
      (DeclarationChangedId, declarationNotChangedWithAdviser, variationStillWorkingKnowledgePage),
      (DeclarationChangedId, completeIndividualDetails, variationWorkingKnowledgePage(CheckUpdateMode)),

      (UpdateContactAddressId, individualWithUpdateContactAddress, individualContactAddressPostCodeLookupPage),
      (UpdateContactAddressId, companyWithUpdateContactAddress, companyContactAddressPostCodeLookupPage),
      (UpdateContactAddressId, partnershipWithUpdateContactAddress, partnershipContactAddressPostCodeLookupPage),

      (DeclarationId, emptyAnswers, variationSuccessPage)
    )

    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, UpdateMode)
  }

  "VariationsNavigator in CheckUpdateMode" must {
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),

      (VariationWorkingKnowledgeId, haveWorkingKnowledge, variationDeclarationFitAndProperPage),
      (VariationWorkingKnowledgeId, noWorkingKnowledge, adviserNamePage)
    )

    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, CheckUpdateMode)
  }

}

object VariationsNavigatorSpec extends OptionValues {

  import utils.testhelpers.DataCompletionBuilder.DataCompletionUserAnswerOps

  private val address = Address("value 1", "value 2", None, None, Some("AB1 1AB"), "GB")

  private val individualContactAddressPostCodeLookupPage =
    controllers.register.individual.routes.IndividualContactAddressPostCodeLookupController.onPageLoad(UpdateMode)

  private val companyContactAddressPostCodeLookupPage =
    controllers.register.company.routes.CompanyContactAddressPostCodeLookupController.onPageLoad(UpdateMode)

  private val partnershipContactAddressPostCodeLookupPage =
    controllers.register.partnership.routes.PartnershipContactAddressPostCodeLookupController.onPageLoad(UpdateMode)

  private val individualWithUpdateContactAddress = UserAnswers(Json.obj()).registrationInfo(RegistrationInfo(
    RegistrationLegalStatus.Individual, "", noIdentifier = false, RegistrationCustomerType.UK, None, None))
    .set(IndividualDetailsId)(TolerantIndividual(Some("Mark"), None, Some("Wright"))).asOpt.value
    .setOrException(IndividualContactAddressId)(address)
    .setOrException(UpdateContactAddressId)(true)

  private val companyWithUpdateContactAddress = UserAnswers(Json.obj()).registrationInfo(RegistrationInfo(
    RegistrationLegalStatus.LimitedCompany, "", noIdentifier = false, RegistrationCustomerType.UK, None, None))
    .setOrException(BusinessNameId)("Big company")
    .setOrException(CompanyContactAddressId)(address)
    .setOrException(UpdateContactAddressId)(true)

  private val partnershipWithUpdateContactAddress = UserAnswers(Json.obj()).registrationInfo(RegistrationInfo(
    RegistrationLegalStatus.Partnership, "", noIdentifier = false, RegistrationCustomerType.UK, None, None))
    .setOrException(BusinessNameId)("Big company")
    .setOrException(PartnershipContactAddressId)(address)
    .setOrException(UpdateContactAddressId)(true)

  private val declarationChangedWithIncompleteIndividual = UserAnswers(Json.obj()).registrationInfo(
    RegistrationInfo(
      RegistrationLegalStatus.Individual, "", noIdentifier = false, RegistrationCustomerType.UK, None, None)
  ).individualAddressYears(AddressYears.OverAYear)

  private val completeIndividualDetails = UserAnswers().completeIndividualVariations.variationWorkingKnowledge(true)

  private val declarationChangedWithCompleteIndividual: UserAnswers = completeIndividualDetails.set(DeclarationChangedId)(true).asOpt.value

  private val haveMoreChanges: UserAnswers = completeIndividualDetails.set(AnyMoreChangesId)(true).asOpt.value
  private val confirmDeleteYes: UserAnswers = UserAnswers(Json.obj()).set(ConfirmDeleteAdviserId)(true).asOpt.value
  private val confirmDeleteNo: UserAnswers = UserAnswers(Json.obj()).set(ConfirmDeleteAdviserId)(false).asOpt.value
  private val noMoreChangesAdviserUnchanged: UserAnswers = completeIndividualDetails.set(AnyMoreChangesId)(false).asOpt.value
  private val noMoreChangesAdviserChanged: UserAnswers = completeIndividualDetails
    .set(AnyMoreChangesId)(false).asOpt.value
    .set(DeclarationChangedId)(true).asOpt.value

  private val haveWorkingKnowledge: UserAnswers = UserAnswers(Json.obj()).set(VariationWorkingKnowledgeId)(true).asOpt.value
  private val noWorkingKnowledge: UserAnswers = UserAnswers(Json.obj()).set(VariationWorkingKnowledgeId)(false).asOpt.value

  private val stillHaveWorkingKnowledge: UserAnswers = UserAnswers(Json.obj()).set(VariationStillDeclarationWorkingKnowledgeId)(true).asOpt.value
  private val stillNotHaveWorkingKnowledge: UserAnswers = UserAnswers(Json.obj()).set(VariationStillDeclarationWorkingKnowledgeId)(false).asOpt.value

  private val declarationNotChangedWithAdviser: UserAnswers = completeIndividualDetails
    .set(AdviserNameId)("adviser-Name").asOpt.value

  private val haveFitAndProper: UserAnswers = UserAnswers(Json.obj()).set(DeclarationFitAndProperId)(true).asOpt.value
  private val noFitAndProper: UserAnswers = UserAnswers(Json.obj()).set(DeclarationFitAndProperId)(false).asOpt.value

  private val checkYourAnswersPage: Call = controllers.routes.PsaDetailsController.onPageLoad()
  private val incompleteChangesPage: Call = controllers.register.routes.IncompleteChangesController.onPageLoad()

  private def variationWorkingKnowledgePage(mode: Mode): Call = controllers.register.routes.VariationWorkingKnowledgeController.onPageLoad(mode)

  private val variationStillWorkingKnowledgePage: Call = controllers.register.routes.StillUseAdviserController.onPageLoad()

  private val variationDeclarationFitAndProperPage: Call = controllers.register.routes.VariationDeclarationFitAndProperController.onPageLoad()
  private val adviserNamePage: Call = controllers.register.adviser.routes.AdviserNameController.onPageLoad(UpdateMode)

  private val variationDeclarationPage: Call = controllers.register.routes.VariationDeclarationController.onPageLoad()
  private val variationNoLongerFitAndProperPage: Call = controllers.register.routes.VariationNoLongerFitAndProperController.onPageLoad()

  private val variationSuccessPage: Call = controllers.register.routes.PSAVarianceSuccessController.onPageLoad()
  private val anyMoreChangesPage: Call = controllers.register.routes.AnyMoreChangesController.onPageLoad()

  private val sessionExpiredPage: Call = controllers.routes.SessionExpiredController.onPageLoad()

  implicit val ex: IdentifiedRequest = new IdentifiedRequest() {
    val externalId: String = "test-external-id"
  }

}


