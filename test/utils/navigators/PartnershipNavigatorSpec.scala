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

package utils.navigators

import base.SpecBase
import connectors.FakeUserAnswersCacheConnector
import controllers.register.partnership.routes
import identifiers._
import identifiers.register.partnership._
import identifiers.register._
import models._
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.countryOptions.CountryOptions
import utils.{FakeCountryOptions, NavigatorBehaviour, UserAnswers}

class PartnershipNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import PartnershipNavigatorSpec._

  def countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  val navigator = new PartnershipNavigator(FakeUserAnswersCacheConnector, countryOptions, frontendAppConfig)

  //scalastyle:off line.size.limit
  private def routes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save(NormalMode)", "Next Page (Check Mode)", "Save(CheckMode"),

    (BusinessUTRId, emptyAnswers, partnershipNamePage, false, None, false),
    (BusinessNameId, uk, partnershipIsRegisteredNamePage, false, None, false),
    (BusinessNameId, nonUk, nonUkAddress, false, None, false),
    (IsRegisteredNameId, isRegisteredNameTrue, confirmPartnershipDetailsPage, false, None, false),
    (IsRegisteredNameId, isRegisteredNameFalse, companyUpdate, false, None, false),

    (ConfirmPartnershipDetailsId, confirmPartnershipDetailsTrue, whatYouWillNeedPage, false, None, false),

    (WhatYouWillNeedId, emptyAnswers, sameContactAddressPage, true, None, true),

    (PartnershipSameContactAddressId, isSameContactAddress, addressYearsPage(NormalMode), true, Some(addressYearsPage(CheckMode)), true),
    (PartnershipSameContactAddressId, notSameContactAddressUk, contactPostcodePage(NormalMode), true, Some(contactPostcodePage(CheckMode)), true),
    (PartnershipSameContactAddressId, notSameContactAddressNonUk, contactAddressPage(NormalMode), true, Some(contactAddressPage(CheckMode)), true),
    (PartnershipSameContactAddressId, emptyAnswers, sessionExpiredPage, false, Some(sessionExpiredPage), false),

    (PartnershipContactAddressPostCodeLookupId, emptyAnswers, contactAddressListPage(NormalMode), true, Some(contactAddressListPage(CheckMode)), true),
    (PartnershipContactAddressListId, emptyAnswers, contactAddressPage(NormalMode), true, Some(contactAddressPage(CheckMode)), true),
    (PartnershipContactAddressId, emptyAnswers, addressYearsPage(NormalMode), true, Some(addressYearsPage(CheckMode)), true),

    (PartnershipAddressYearsId, addressYearsOverAYear, contactDetailsPage, false, Some(checkYourAnswersPage), false),
    (PartnershipAddressYearsId, addressYearsUnderAYear, tradingOverAYearPage(NormalMode), false, Some(tradingOverAYearPage(CheckMode)), false),
    (PartnershipAddressYearsId, emptyAnswers, sessionExpiredPage, false, Some(sessionExpiredPage), false),

    (PartnershipTradingOverAYearId, tradingOverAYearUk, contactPreviousPostCodePage(NormalMode), true, Some(contactPreviousPostCodePage(CheckMode)), true),
    (PartnershipTradingOverAYearId, tradingOverAYearNonUk, contactPreviousAddressPage(NormalMode), true, Some(contactPreviousAddressPage(CheckMode)), true),
    (PartnershipTradingOverAYearId, tradingUnderAYear, contactDetailsPage, true, Some(checkYourAnswersPage), true),

    (PartnershipPreviousAddressPostCodeLookupId, emptyAnswers, contactPreviousAddressListPage(NormalMode), true, Some(contactPreviousAddressListPage(CheckMode)), true),
    (PartnershipPreviousAddressListId, emptyAnswers, contactPreviousAddressPage(NormalMode), true, Some(contactPreviousAddressPage(CheckMode)), true),
    (PartnershipPreviousAddressId, emptyAnswers, contactDetailsPage, true, Some(checkYourAnswersPage), true),

    (PartnershipContactDetailsId, uk, vatPage, true, Some(checkYourAnswersPage), true),
    (PartnershipContactDetailsId, nonUk, checkYourAnswersPage, true, Some(checkYourAnswersPage), true),
    (PartnershipContactDetailsId, emptyAnswers, sessionExpiredPage, false, Some(checkYourAnswersPage), true),

    (PartnershipVatId, emptyAnswers, hasPayePage, true, Some(checkYourAnswersPage), true),

    (HasPAYEId, hasPAYEYes, payePage(NormalMode), true, Some(payePage(CheckMode)), true),
    (HasPAYEId, hasPAYENo, checkYourAnswersPage, true, Some(checkYourAnswersPage), true),
    (EnterPAYEId, emptyAnswers, checkYourAnswersPage, true, Some(checkYourAnswersPage), true),

    (CheckYourAnswersId, emptyAnswers, addPartnersPage, true, None, true),
    (PartnershipReviewId, emptyAnswers, declarationPage, true, None, false),

    (PartnershipRegisteredAddressId, nonUkEuAddress, whatYouWillNeedPage, false, None, false),
    (PartnershipRegisteredAddressId, uKAddress, reconsiderAreYouInUk, false, None, false),
    (PartnershipRegisteredAddressId, nonUkNonEuAddress, outsideEuEea, false, None, false)
  )

  private def updateRoutes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save(NormalMode)", "Next Page (Check Mode)", "Save(CheckMode"),
    (PartnershipContactAddressPostCodeLookupId, emptyAnswers, contactAddressListPage(UpdateMode), true, None, true),
    (PartnershipContactAddressListId, emptyAnswers, contactAddressPage(UpdateMode), true, None, true),
    (PartnershipContactAddressId, emptyAnswers, addressYearsPage(UpdateMode), false, None, false),
    (PartnershipAddressYearsId, addressYearsOverAYear, anyMoreChangesPage, false, None, false),
    (PartnershipAddressYearsId, addressYearsUnderAYear, tradingOverAYearPage(UpdateMode), false, None, false),
    (PartnershipAddressYearsId, emptyAnswers, sessionExpiredPage, false, Some(sessionExpiredPage), false),
    (PartnershipContactDetailsId, uk, anyMoreChangesPage, false, None, false),
    (PartnershipContactDetailsId, nonUk, anyMoreChangesPage, false, None, false),
    (PartnershipContactDetailsId, emptyAnswers, anyMoreChangesPage, false, None, false),
    (PartnershipPreviousAddressPostCodeLookupId, emptyAnswers, contactPreviousAddressListPage(UpdateMode), false, None, false),
    (PartnershipPreviousAddressListId, emptyAnswers, contactPreviousAddressPage(UpdateMode), false, None, false),
    (PartnershipPreviousAddressId, emptyAnswers, anyMoreChangesPage, false, None, false),
    (PartnershipConfirmPreviousAddressId, emptyAnswers, sessionExpiredPage, false, None, false),
    (PartnershipConfirmPreviousAddressId, varianceConfirmPreviousAddressYes, anyMoreChangesPage, false, None, false),
    (PartnershipConfirmPreviousAddressId, varianceConfirmPreviousAddressNo, contactPreviousAddressPage(UpdateMode), false, None, false)
  )

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigator)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(), dataDescriber)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, updateRoutes(), dataDescriber, UpdateMode)
  }
}

object PartnershipNavigatorSpec extends OptionValues {

  private def sessionExpiredPage: Call = controllers.routes.SessionExpiredController.onPageLoad()

  private def anyMoreChangesPage: Call = controllers.register.routes.AnyMoreChangesController.onPageLoad()

  private def confirmPartnershipDetailsPage: Call = routes.ConfirmPartnershipDetailsController.onPageLoad()

  private def whatYouWillNeedPage: Call = routes.WhatYouWillNeedController.onPageLoad()
  private def partnershipNamePage = routes.PartnershipNameController.onPageLoad()
  private def partnershipIsRegisteredNamePage = routes.PartnershipIsRegisteredNameController.onPageLoad()

  private def sameContactAddressPage: Call = routes.PartnershipSameContactAddressController.onPageLoad(NormalMode)

  private def checkYourAnswersPage: Call = routes.CheckYourAnswersController.onPageLoad()

  private def vatPage: Call = routes.PartnershipVatController.onPageLoad(NormalMode)

  private def tradingOverAYearPage(mode: Mode): Call = routes.PartnershipTradingOverAYearController.onPageLoad(mode)

  private def contactDetailsPage: Call = routes.PartnershipContactDetailsController.onPageLoad(NormalMode)

  private def addPartnersPage: Call = routes.AddPartnerController.onPageLoad(NormalMode)

  private def addressYearsPage(mode: Mode): Call = routes.PartnershipAddressYearsController.onPageLoad(mode)

  private def contactPostcodePage(mode: Mode): Call = routes.PartnershipContactAddressPostCodeLookupController.onPageLoad(mode)

  private def contactAddressListPage(mode: Mode): Call = routes.PartnershipContactAddressListController.onPageLoad(mode)

  private def contactAddressPage(mode: Mode): Call = routes.PartnershipContactAddressController.onPageLoad(mode)

  private def contactPreviousPostCodePage(mode: Mode): Call = routes.PartnershipPreviousAddressPostCodeLookupController.onPageLoad(mode)

  private def contactPreviousAddressListPage(mode: Mode): Call = routes.PartnershipPreviousAddressListController.onPageLoad(mode)

  private def confirmPreviousAddressPage: Call = routes.PartnershipConfirmPreviousAddressController.onPageLoad()

  private def contactPreviousAddressPage(mode: Mode): Call = routes.PartnershipPreviousAddressController.onPageLoad(mode)

  private def declarationPage: Call = controllers.register.routes.DeclarationController.onPageLoad()

  private def nonUkAddress: Call = routes.PartnershipRegisteredAddressController.onPageLoad()

  private def hasPayePage: Call = routes.HasPartnershipPAYEController.onPageLoad(NormalMode)
  private def payePage(mode: Mode): Call = routes.PartnershipEnterPAYEController.onPageLoad(mode)

  private def reconsiderAreYouInUk: Call = controllers.register.routes.BusinessTypeAreYouInUKController.onPageLoad(CheckMode)

  private def outsideEuEea: Call = routes.OutsideEuEeaController.onPageLoad()

  private def companyUpdate = controllers.register.company.routes.CompanyUpdateDetailsController.onPageLoad()

  protected val uk: UserAnswers = UserAnswers().areYouInUk(true)
  protected val nonUk: UserAnswers = UserAnswers().areYouInUk(false)
  private val hasPAYEYes = UserAnswers().set(HasPAYEId)(value = true).asOpt.value
  private val hasPAYENo = UserAnswers().set(HasPAYEId)(value = false).asOpt.value

  protected val isRegisteredNameTrue: UserAnswers = UserAnswers().isRegisteredName(true)
  protected val isRegisteredNameFalse: UserAnswers = UserAnswers().isRegisteredName(false)

  private val varianceConfirmPreviousAddressYes = UserAnswers().set(PartnershipConfirmPreviousAddressId)(true).asOpt.get
  private val varianceConfirmPreviousAddressNo = UserAnswers().set(PartnershipConfirmPreviousAddressId)(false).asOpt.get

  private val nonUkEuAddress = UserAnswers().nonUkPartnershipAddress(address("AT"))
  private val uKAddress = UserAnswers().nonUkPartnershipAddress(address("GB"))
  private val nonUkNonEuAddress = UserAnswers().nonUkPartnershipAddress(address("AF"))

  private val notSameContactAddressUk = UserAnswers().areYouInUk(true).partnershipSameContactAddress(false)
  private val notSameContactAddressNonUk = UserAnswers().areYouInUk(false).partnershipSameContactAddress(false)
  private val isSameContactAddress = UserAnswers().partnershipSameContactAddress(true)

  private val addressYearsUnderAYear = UserAnswers().partnershipAddressYears(AddressYears.UnderAYear)
  private val tradingUnderAYear = UserAnswers().set(PartnershipTradingOverAYearId)(false).asOpt.value
  private val tradingOverAYearUk = UserAnswers(Json.obj()).areYouInUk(true).set(PartnershipTradingOverAYearId)(true).asOpt.value
  private val tradingOverAYearNonUk = UserAnswers(Json.obj()).areYouInUk(false).set(PartnershipTradingOverAYearId)(true).asOpt.value
  private val addressYearsOverAYear = UserAnswers().partnershipAddressYears(AddressYears.OverAYear)

  private def address(countryCode: String) = Address("addressLine1", "addressLine2", Some("addressLine3"), Some("addressLine4"), Some("NE11AA"), countryCode)

  private val confirmPartnershipDetailsTrue = UserAnswers(Json.obj()).set(ConfirmPartnershipDetailsId)(true).asOpt.value
}
