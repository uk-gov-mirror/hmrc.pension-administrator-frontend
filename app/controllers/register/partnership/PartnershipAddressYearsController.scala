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

package controllers.register.partnership

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressYearsController
import forms.address.AddressYearsFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.partnership.PartnershipAddressYearsId
import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.Partnership
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel

class PartnershipAddressYearsController @Inject()(
                                                   val appConfig: FrontendAppConfig,
                                                   val cacheConnector: UserAnswersCacheConnector,
                                                   @Partnership val navigator: Navigator,
                                                   val messagesApi: MessagesApi,
                                                   override val allowAccess: AllowAccessActionProvider,
                                                   authenticate: AuthAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   formProvider: AddressYearsFormProvider
                                                 ) extends AddressYearsController with Retrievals {


  private def viewModel(mode: Mode, partnershipName: String)(implicit request: DataRequest[AnyContent]) =
    AddressYearsViewModel(
      routes.PartnershipAddressYearsController.onSubmit(mode),
      Message("addressYears.heading", Message("thePartnership").resolve),
      Message("addressYears.heading").withArgs(partnershipName),
      Message("addressYears.heading").withArgs(partnershipName),
      psaName = psaName()
    )

  def form(partnershipName: String) = formProvider(partnershipName)

  private def entityName(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers.get(BusinessNameId).getOrElse(Message("theCompany").resolve)

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      get(PartnershipAddressYearsId, form(entityName), viewModel(mode, entityName), mode)
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(PartnershipAddressYearsId, mode, form(entityName), viewModel(mode, entityName))
  }
}
