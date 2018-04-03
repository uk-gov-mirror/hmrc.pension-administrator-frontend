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

package controllers.register.individual

import javax.inject.Inject

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.actions._
import forms.address.AddressYearsFormProvider
import identifiers.register.individual.IndividualAddressYearsId
import models.{AddressYears, Mode}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel

class IndividualAddressYearsController @Inject()(
                                        override val appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        override val navigator: Navigator,
                                        override val cacheConnector: DataCacheConnector,
                                       authenticate: AuthAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: AddressYearsFormProvider
                                     ) extends controllers.address.AddressYearsController {

  private def viewmodel(mode: Mode): Retrieval[AddressYearsViewModel] =
    Retrieval(
    implicit request =>
      IndividualAddressYearsId.retrieve.right.map{
        details =>
          val questionText = "individualAddressYears.title"
          val name = "Name from someplace"
          AddressYearsViewModel(
            postCall = routes.IndividualAddressYearsController.onSubmit(mode),
            title = Message(questionText, name),
            heading = Message(questionText, name),
            legend = Message(questionText, name),
            Some(Message("whatYouWillNeed.secondary.heading"))
          )
      }
  )

  private val form: Form[AddressYears] = formProvider(Message("messages__common_error__current_address_years"))

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        viewmodel(mode).retrieve.right.map {
          vm =>
            get(IndividualAddressYearsId, form, vm)
        }
    }

  def onSubmit(mode: Mode) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode).retrieve.right.map {
        vm =>
          post(IndividualAddressYearsId, mode, form, vm)
      }
  }
}
