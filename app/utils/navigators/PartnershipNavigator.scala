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

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.register.partnership.routes
import controllers.register.partnership.routes._
import controllers.register.routes._
import controllers.routes._
import identifiers.register._
import identifiers.register.partnership._
import models.InternationalRegion.{EuEea, RestOfTheWorld, UK}
import models._
import utils.countryOptions.CountryOptions
import utils.{Navigator, UserAnswers}
import controllers.register.partnership.partners.routes.WhatYouWillNeedController
import controllers.register.partnership.routes.AddPartnerController

@Singleton
class PartnershipNavigator @Inject()(
                                      val dataCacheConnector: UserAnswersCacheConnector,
                                      countryOptions: CountryOptions,
                                      appConfig: FrontendAppConfig) extends Navigator {

  //scalastyle:off cyclomatic.complexity
  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case BusinessUTRId =>
      NavigateTo.dontSave(PartnershipNameController.onPageLoad())
    case BusinessNameId =>
      regionBasedNameNavigation(from.userAnswers)
    case IsRegisteredNameId =>
      registeredNameRoutes(from.userAnswers)
    case ConfirmPartnershipDetailsId =>
      NavigateTo.dontSave(controllers.register.partnership.routes.WhatYouWillNeedController.onPageLoad())
    case WhatYouWillNeedId =>
      NavigateTo.save(PartnershipSameContactAddressController.onPageLoad(NormalMode))
    case PartnershipSameContactAddressId =>
      sameContactAddress(NormalMode, from.userAnswers)
    case PartnershipContactAddressPostCodeLookupId =>
      NavigateTo.save(PartnershipContactAddressListController.onPageLoad(NormalMode))
    case PartnershipContactAddressListId =>
      NavigateTo.save(PartnershipContactAddressController.onPageLoad(NormalMode))
    case PartnershipContactAddressId =>
      NavigateTo.save(PartnershipAddressYearsController.onPageLoad(NormalMode))
    case PartnershipAddressYearsId =>
      addressYearsIdRoutes(from.userAnswers, NormalMode)
    case PartnershipTradingOverAYearId =>
      tradingOverAYearRoutes(from.userAnswers, NormalMode)
    case PartnershipPreviousAddressPostCodeLookupId =>
      NavigateTo.save(PartnershipPreviousAddressListController.onPageLoad(NormalMode))
    case PartnershipPreviousAddressListId =>
      NavigateTo.save(PartnershipPreviousAddressController.onPageLoad(NormalMode))
    case PartnershipPreviousAddressId =>
      NavigateTo.save(PartnershipEmailController.onPageLoad(NormalMode))
    case PartnershipEmailId =>
      NavigateTo.save(PartnershipPhoneController.onPageLoad(NormalMode))
    case PartnershipPhoneId =>
      regionBasedContactDetailsRoutes(from.userAnswers)
    case HasVATId =>
      vatNavigation(from.userAnswers, NormalMode)
    case EnterVATId =>
      NavigateTo.save(HasPartnershipPAYEController.onPageLoad(NormalMode))
    case HasPAYEId if hasPaye(from.userAnswers) =>
      NavigateTo.save(routes.PartnershipEnterPAYEController.onPageLoad(NormalMode))
    case HasPAYEId =>
      NavigateTo.save(routes.CheckYourAnswersController.onPageLoad())
    case EnterPAYEId =>
      NavigateTo.save(routes.CheckYourAnswersController.onPageLoad())
    case CheckYourAnswersId =>
      partnerRoutes(from.userAnswers)
    case PartnershipReviewId =>
      NavigateTo.save(DeclarationController.onPageLoad())
    case PartnershipRegisteredAddressId =>
      regionBasedNavigation(from.userAnswers)
    case _ =>
      NavigateTo.dontSave(SessionExpiredController.onPageLoad())
  }

  override protected def editRouteMap(from: NavigateFrom, mode: Mode): Option[NavigateTo] = {
    from.id match {
      case PartnershipSameContactAddressId =>
        sameContactAddress(CheckMode, from.userAnswers)
      case PartnershipContactAddressPostCodeLookupId =>
        NavigateTo.save(PartnershipContactAddressListController.onPageLoad(CheckMode))
      case PartnershipContactAddressListId =>
        NavigateTo.save(PartnershipContactAddressController.onPageLoad(CheckMode))
      case PartnershipContactAddressId =>
        NavigateTo.save(PartnershipAddressYearsController.onPageLoad(CheckMode))
      case PartnershipAddressYearsId =>
        addressYearsCheckIdRoutes(from.userAnswers, CheckMode)
      case PartnershipTradingOverAYearId =>
        tradingOverAYearRoutes(from.userAnswers, CheckMode)
      case PartnershipPreviousAddressPostCodeLookupId =>
        NavigateTo.save(PartnershipPreviousAddressListController.onPageLoad(CheckMode))
      case PartnershipPreviousAddressListId =>
        NavigateTo.save(PartnershipPreviousAddressController.onPageLoad(CheckMode))
      case PartnershipPreviousAddressId =>
        NavigateTo.save(CheckYourAnswersController.onPageLoad())
      case PartnershipEmailId =>
        NavigateTo.save(CheckYourAnswersController.onPageLoad())
      case PartnershipPhoneId =>
        NavigateTo.save(CheckYourAnswersController.onPageLoad())
      case HasVATId =>
        vatNavigation(from.userAnswers, mode)
      case EnterVATId =>
        NavigateTo.save(CheckYourAnswersController.onPageLoad())
      case HasPAYEId if hasPaye(from.userAnswers) =>
        NavigateTo.save(routes.PartnershipEnterPAYEController.onPageLoad(CheckMode))
      case HasPAYEId =>
        NavigateTo.save(routes.CheckYourAnswersController.onPageLoad())
      case EnterPAYEId =>
        NavigateTo.save(routes.CheckYourAnswersController.onPageLoad())
      case _ =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }

  override protected def updateRouteMap(from: NavigateFrom): Option[NavigateTo] = {
    from.id match {
      case PartnershipContactAddressPostCodeLookupId =>
        NavigateTo.save(PartnershipContactAddressListController.onPageLoad(UpdateMode))
      case PartnershipContactAddressListId =>
        NavigateTo.save(PartnershipContactAddressController.onPageLoad(UpdateMode))
      case PartnershipContactAddressId =>
        NavigateTo.dontSave(PartnershipAddressYearsController.onPageLoad(UpdateMode))
      case PartnershipAddressYearsId =>
        addressYearsCheckIdRoutes(from.userAnswers, UpdateMode)
      case PartnershipTradingOverAYearId =>
        tradingOverAYearRoutes(from.userAnswers, UpdateMode)
      case PartnershipPhoneId =>
        NavigateTo.dontSave(controllers.register.routes.AnyMoreChangesController.onPageLoad())
      case PartnershipEmailId =>
        NavigateTo.dontSave(controllers.register.routes.AnyMoreChangesController.onPageLoad())
      case PartnershipPreviousAddressPostCodeLookupId =>
        NavigateTo.dontSave(PartnershipPreviousAddressListController.onPageLoad(UpdateMode))
      case PartnershipPreviousAddressListId =>
        NavigateTo.dontSave(PartnershipPreviousAddressController.onPageLoad(UpdateMode))
      case PartnershipPreviousAddressId =>
        NavigateTo.dontSave(AnyMoreChangesController.onPageLoad())
      case PartnershipConfirmPreviousAddressId =>
        variationManualPreviousAddressRoutes(from.userAnswers, UpdateMode)
      case _ =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }

  private def addressYearsIdRoutes(answers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    answers.get(PartnershipAddressYearsId) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(PartnershipTradingOverAYearController.onPageLoad(NormalMode))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.save(PartnershipEmailController.onPageLoad(NormalMode))
      case _ =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }

  private def addressYearsCheckIdRoutes(answers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    answers.get(PartnershipAddressYearsId) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(PartnershipTradingOverAYearController.onPageLoad(mode))
      case Some(AddressYears.OverAYear) =>
        mode match {
          case CheckMode =>
            NavigateTo.save(CheckYourAnswersController.onPageLoad())
          case UpdateMode =>
            NavigateTo.save(AnyMoreChangesController.onPageLoad())
          case _ =>
            NavigateTo.dontSave(SessionExpiredController.onPageLoad())
        }
      case _ =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }
  private def hasPaye(ua: UserAnswers): Boolean = ua.get(HasPAYEId).getOrElse(false)

  private def tradingOverAYearRoutes(answers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    (answers.get(PartnershipTradingOverAYearId), answers.get(AreYouInUKId)) match {
      case (Some(true), Some(false)) =>
        mode match {
          case NormalMode | CheckMode =>
            NavigateTo.dontSave(PartnershipPreviousAddressController.onPageLoad(mode))
          case _ =>
            NavigateTo.dontSave(PartnershipConfirmPreviousAddressController.onPageLoad())
        }
      case (Some(true), Some(true)) =>
        mode match {
          case NormalMode | CheckMode =>
            NavigateTo.dontSave(PartnershipPreviousAddressPostCodeLookupController.onPageLoad(mode))
          case _ =>
            NavigateTo.dontSave(PartnershipConfirmPreviousAddressController.onPageLoad())
        }
      case (Some(false), _) =>
        mode match {
          case NormalMode =>
            NavigateTo.dontSave(PartnershipEmailController.onPageLoad(NormalMode))
          case CheckMode =>
            NavigateTo.dontSave(CheckYourAnswersController.onPageLoad())
          case _ =>
            NavigateTo.dontSave(AnyMoreChangesController.onPageLoad())
        }
      case _ =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }

  private def sameContactAddress(mode: Mode, answers: UserAnswers): Option[NavigateTo] = {
    (answers.get(PartnershipSameContactAddressId), answers.get(AreYouInUKId)) match {
      case (Some(true), _) => NavigateTo.save(PartnershipAddressYearsController.onPageLoad(mode))
      case (Some(false), Some(false)) => NavigateTo.save(PartnershipContactAddressController.onPageLoad(mode))
      case (Some(false), Some(true)) => NavigateTo.save(PartnershipContactAddressPostCodeLookupController.onPageLoad(mode))
      case _ => NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }


  private def regionBasedNameNavigation(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(AreYouInUKId) match {
      case Some(false) => NavigateTo.dontSave(PartnershipRegisteredAddressController.onPageLoad())
      case Some(true) => NavigateTo.dontSave(PartnershipIsRegisteredNameController.onPageLoad())
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def regionBasedNavigation(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(PartnershipRegisteredAddressId) flatMap { address =>
      countryOptions.regions(address.country.getOrElse("")) match {
        case UK => NavigateTo.dontSave(BusinessTypeAreYouInUKController.onPageLoad(CheckMode))
        case EuEea => NavigateTo.dontSave(controllers.register.partnership.routes.WhatYouWillNeedController.onPageLoad())
        case RestOfTheWorld => NavigateTo.dontSave(OutsideEuEeaController.onPageLoad())
        case _ => NavigateTo.dontSave(SessionExpiredController.onPageLoad())
      }
    }
  }

  private def regionBasedContactDetailsRoutes(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(AreYouInUKId) match {
      case Some(false) => NavigateTo.save(CheckYourAnswersController.onPageLoad())
      case Some(true) => NavigateTo.save(HasPartnershipVATController.onPageLoad(NormalMode))
      case _ => NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }

  private def variationManualPreviousAddressRoutes(answers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    answers.get(PartnershipConfirmPreviousAddressId) match {
      case Some(false) => NavigateTo.dontSave(PartnershipPreviousAddressController.onPageLoad(mode))
      case Some(true) => NavigateTo.dontSave(AnyMoreChangesController.onPageLoad())
      case _ => NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }

  private def registeredNameRoutes(answers: UserAnswers): Option[NavigateTo] =
    answers.get(IsRegisteredNameId) match {
      case Some(true) => NavigateTo.dontSave(ConfirmPartnershipDetailsController.onPageLoad())
      case _ => NavigateTo.dontSave(controllers.register.company.routes.CompanyUpdateDetailsController.onPageLoad())
    }

  def vatNavigation(userAnswers: UserAnswers, mode: Mode): Option[NavigateTo] = userAnswers.get(HasVATId) match {
    case Some(true) => NavigateTo.save(PartnershipEnterVATController.onPageLoad(mode))
    case Some(false) if mode == NormalMode => NavigateTo.save(HasPartnershipPAYEController.onPageLoad(mode))
    case Some(false) if mode == CheckMode => NavigateTo.save(CheckYourAnswersController.onPageLoad())
    case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
  }

  private def partnerRoutes(answers: UserAnswers): Option[NavigateTo] =
    if (answers.allPartnersAfterDelete(NormalMode).isEmpty) {
      NavigateTo.dontSave(WhatYouWillNeedController.onPageLoad())
    }
    else {
      NavigateTo.save(AddPartnerController.onPageLoad(NormalMode))
    }
}
