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

package controllers.register.partnership

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressYearsController
import forms.address.AddressYearsFormProvider
import identifiers.register.partnership.PartnershipAddressYearsId
import javax.inject.Inject
import models.{Index, Mode}
import play.api.i18n.MessagesApi
import utils.Navigator
import utils.annotations.Partnership
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel

class PartnershipAddressYearsController @Inject()(
                                                   val appConfig: FrontendAppConfig,
                                                   val cacheConnector: DataCacheConnector,
                                                   @Partnership val navigator: Navigator,
                                                   val messagesApi: MessagesApi,
                                                   authenticate: AuthAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   formProvider: AddressYearsFormProvider
                                                 ) extends AddressYearsController {

  private def viewModel(mode: Mode, index: Index) = AddressYearsViewModel(
    routes.PartnershipAddressYearsController.onSubmit(mode, index),
    "",
    Message("partnership.addressYears.heading").withArgs(""),
    Message("partnership.addressYears.heading").withArgs(""),
    Some("site.secondaryHeader")
  )

  val form = formProvider("error.addressYears.required")

  def onPageLoad(mode: Mode, index: Index) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      get(PartnershipAddressYearsId(index), form, viewModel(mode, index))
  }

  def onSubmit(mode: Mode, index: Index) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      post(PartnershipAddressYearsId(index), mode, form, viewModel(mode, index))
  }

}
