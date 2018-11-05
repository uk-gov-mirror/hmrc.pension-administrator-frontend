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
import config.FrontendAppConfig
import connectors.FakeUserAnswersCacheConnector
import controllers.register.company.routes
import identifiers.Identifier
import identifiers.LastPageId
import identifiers.register.company._
import identifiers.register.partnership.ConfirmPartnershipDetailsId
import models._
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.FakeCountryOptions
import utils.NavigatorBehaviour
import utils.UserAnswers
import utils.countryOptions.CountryOptions

class RegisterCompanyNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import RegisterCompanyNavigatorSpec._
  def countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)
  val navigatorUK = new RegisterCompanyNavigator(FakeUserAnswersCacheConnector, countryOptions, appConfig(false))
  val navigatorNonUK = new RegisterCompanyNavigator(FakeUserAnswersCacheConnector, countryOptions, appConfig(true))

  //scalastyle:off line.size.limit
  private def routes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (BusinessDetailsId, emptyAnswers, confirmCompanyDetailsPage, false, None, false),
    (ConfirmCompanyAddressId, confirmPartnershipDetailsTrue, whatYouWillNeedPage, false, None, false),
    (WhatYouWillNeedId, emptyAnswers, sameContactAddress(NormalMode), true, None, false),
    (CompanySameContactAddressId, isSameContactAddress, companyAddressYearsPage(NormalMode), true, Some(companyAddressYearsPage(CheckMode)), true),
    (CompanySameContactAddressId, notSameContactAddressUk, contactAddressPostCode(NormalMode), true, Some(contactAddressPostCode(CheckMode)), true),
    (CompanySameContactAddressId, emptyAnswers, sessionExpiredPage, false, Some(sessionExpiredPage), false),
    (CompanyContactAddressPostCodeLookupId, emptyAnswers, contactAddressList(NormalMode), true, Some(contactAddressList(CheckMode)), true),
    (CompanyContactAddressListId, emptyAnswers, contactAddress(NormalMode), true, Some(contactAddress(CheckMode)), true),
    (CompanyContactAddressId, emptyAnswers, companyAddressYearsPage(NormalMode), true, Some(companyAddressYearsPage(CheckMode)), true),
    (CompanyAddressYearsId, addressYearsOverAYear, contactDetailsPage, true, Some(checkYourAnswersPage), true),
    (CompanyAddressYearsId, addressYearsUnderAYearUk, paPostCodePage(NormalMode), true, Some(paPostCodePage(CheckMode)), true),
    (CompanyAddressYearsId, emptyAnswers, sessionExpiredPage, false, Some(sessionExpiredPage), false),
    (CompanyPreviousAddressPostCodeLookupId, emptyAnswers, paAddressListPage(NormalMode), true, Some(paAddressListPage(CheckMode)), true),
    (CompanyAddressListId, emptyAnswers, previousAddressPage(NormalMode), true, Some(previousAddressPage(CheckMode)), true),
    (CompanyPreviousAddressId, emptyAnswers, contactDetailsPage, true, Some(checkYourAnswersPage), true),
    (ContactDetailsId, uk, companyDetailsPage, true, Some(checkYourAnswersPage), true),
    (CompanyDetailsId, emptyAnswers, companyRegistrationNumberPage, true, Some(checkYourAnswersPage), true),
    (CompanyRegistrationNumberId, emptyAnswers, checkYourAnswersPage, true, Some(checkYourAnswersPage), true),
    (CheckYourAnswersId, emptyAnswers, addCompanyDirectors(NormalMode), true, None, false),
    (CompanyReviewId, emptyAnswers, declarationPage, true, None, false)
  )

  private def nonUKRoutes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (BusinessDetailsId, uk, confirmCompanyDetailsPage, false, None, false),
    (BusinessDetailsId, nonUk, nonUkAddress, false, None, false),

    (CompanySameContactAddressId, isSameContactAddress, companyAddressYearsPage(NormalMode), true, Some(companyAddressYearsPage(CheckMode)), true),
    (CompanySameContactAddressId, notSameContactAddressUk, contactAddressPostCode(NormalMode), true, Some(contactAddressPostCode(CheckMode)), true),
    (CompanySameContactAddressId, notSameContactAddressNonUk, contactAddress(NormalMode), true, Some(contactAddress(CheckMode)), true),
    (CompanySameContactAddressId, emptyAnswers, sessionExpiredPage, false, Some(sessionExpiredPage), false),

    (CompanyAddressYearsId, addressYearsOverAYear, contactDetailsPage, true, Some(checkYourAnswersPage), true),
    (CompanyAddressYearsId, addressYearsUnderAYearUk, paPostCodePage(NormalMode), true, Some(paPostCodePage(CheckMode)), true),
    (CompanyAddressYearsId, addressYearsUnderAYearNonUk, previousAddressPage(NormalMode), true, Some(previousAddressPage(CheckMode)), true),
    (CompanyAddressYearsId, emptyAnswers, sessionExpiredPage, false, Some(sessionExpiredPage), false),

    (ContactDetailsId, uk, companyDetailsPage, true, Some(checkYourAnswersPage), true),
    (ContactDetailsId, nonUk, checkYourAnswersPage, true, Some(checkYourAnswersPage), true),
    (CompanyDetailsId, emptyAnswers, companyRegistrationNumberPage, true, Some(checkYourAnswersPage), true),
    (CompanyRegistrationNumberId, emptyAnswers, checkYourAnswersPage, true, Some(checkYourAnswersPage), true),

    (CompanyAddressId, nonUkEuAddress, whatYouWillNeedPage, false, None, false),
    (CompanyAddressId, nonUkButUKAddress, reconsiderAreYouInUk, false, None, false),
    (CompanyAddressId, nonUkNonEuAddress, outsideEuEea, false, None, false)
  )

  //scalastyle:on line.size.limit

  navigatorUK.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigatorUK)
    behave like navigatorWithRoutes(navigatorUK, FakeUserAnswersCacheConnector, routes(), dataDescriber)
  }

  s"NonUK ${navigatorNonUK.getClass.getSimpleName}" must {
    appRunning()
    behave like navigatorWithRoutes(navigatorNonUK, FakeUserAnswersCacheConnector, nonUKRoutes(), dataDescriber)
  }

}

object RegisterCompanyNavigatorSpec extends OptionValues {

  private def appConfig(nonUk: Boolean = false) : FrontendAppConfig = new GuiceApplicationBuilder().configure(
    conf = "features.non-uk-journeys" -> nonUk
  ).build().injector.instanceOf[FrontendAppConfig]

  private def sessionExpiredPage = controllers.routes.SessionExpiredController.onPageLoad()

  private def checkYourAnswersPage = routes.CheckYourAnswersController.onPageLoad()

  private def companyBusinessDetailsPage = routes.CompanyBusinessDetailsController.onPageLoad()

  private def confirmCompanyDetailsPage = routes.ConfirmCompanyDetailsController.onPageLoad()

  private def whatYouWillNeedPage = routes.WhatYouWillNeedController.onPageLoad()

  private def companyDetailsPage = routes.CompanyDetailsController.onPageLoad(NormalMode)

  private def companyRegistrationNumberPage = routes.CompanyRegistrationNumberController.onPageLoad(NormalMode)

  private def companyAddressYearsPage(mode: Mode) = routes.CompanyAddressYearsController.onPageLoad(mode)

  private def contactDetailsPage = routes.ContactDetailsController.onPageLoad(NormalMode)

  private def paPostCodePage(mode: Mode) = routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(mode)

  private def paAddressListPage(mode: Mode) = routes.CompanyAddressListController.onPageLoad(mode)

  private def previousAddressPage(mode: Mode) = routes.CompanyPreviousAddressController.onPageLoad(mode)

  private def declarationPage = controllers.register.routes.DeclarationController.onPageLoad()

  private def sameContactAddress(mode: Mode) = routes.CompanySameContactAddressController.onPageLoad(mode)

  private def contactAddressPostCode(mode: Mode) = routes.CompanyContactAddressPostCodeLookupController.onPageLoad(mode)

  private def contactAddressList(mode: Mode) = routes.CompanyContactAddressListController.onPageLoad(mode)

  private def contactAddress(mode: Mode) = routes.CompanyContactAddressController.onPageLoad(mode)

  private def addCompanyDirectors(mode: Mode) = routes.AddCompanyDirectorsController.onPageLoad(mode)
  private def nonUkAddress = routes.CompanyRegisteredAddressController.onPageLoad()
  private def reconsiderAreYouInUk = controllers.register.routes.BusinessTypeAreYouInUKController.onPageLoad(CheckMode)
  private def outsideEuEea = routes.OutsideEuEeaController.onPageLoad()

  private val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId)(AddressYears.OverAYear).asOpt.value
  private val addressYearsUnderAYearUk = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId)(AddressYears.UnderAYear).asOpt.value.areYouInUk(true)
  private val addressYearsUnderAYearNonUk = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId)(AddressYears.UnderAYear).asOpt.value.areYouInUk(false)
  private val isSameContactAddress = UserAnswers().companySameContactAddress(true)
  private val notSameContactAddressUk = UserAnswers().companySameContactAddress(false).areYouInUk(true)
  private val notSameContactAddressNonUk = UserAnswers().companySameContactAddress(false).areYouInUk(false)
  private lazy val lastPage = UserAnswers(Json.obj())
    .set(LastPageId)(LastPage("GET", "www.test.com")).asOpt.value

  private val nonUkEuAddress = UserAnswers().nonUkCompanyAddress(address("AT"))
  private val nonUkButUKAddress = UserAnswers().nonUkCompanyAddress(address("GB"))
  private val nonUkNonEuAddress = UserAnswers().nonUkCompanyAddress(address("AF"))

  protected val uk: UserAnswers = UserAnswers().areYouInUk(true)
  protected val nonUk: UserAnswers = UserAnswers().areYouInUk(false)

  private def address(countryCode: String) =Address("addressLine1","addressLine2", Some("addressLine3"), Some("addressLine4"), Some("NE11AA"), countryCode)

  private val confirmPartnershipDetailsTrue = UserAnswers(Json.obj()).set(ConfirmPartnershipDetailsId)(true).asOpt.value
}
