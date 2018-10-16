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

package controllers.register.company

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.NonUKAddressController
import forms.address.NonUKAddressFormProvider
import identifiers.register.company.{CompanyNameId, CompanyAddressId}
import javax.inject.Inject
import models.{Address, Mode}
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, Request}
import play.twirl.api.HtmlFormat
import utils.Navigator
import utils.annotations.RegisterCompany
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.nonukAddress

class CompanyRegisteredAddressController @Inject()(
                                               override val appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               override val dataCacheConnector: UserAnswersCacheConnector,
                                               @RegisterCompany override val navigator: Navigator,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               formProvider: NonUKAddressFormProvider,
                                               val countryOptions: CountryOptions
                                             ) extends NonUKAddressController with Retrievals{

  protected val form: Form[Address] = formProvider()

  protected override def createView(appConfig: FrontendAppConfig, preparedForm: Form[_], viewModel: ManualAddressViewModel)(
    implicit request: Request[_], messages: Messages): () => HtmlFormat.Appendable = () =>
    nonukAddress(appConfig, preparedForm, viewModel)(request, messages)

  private def addressViewModel(mode: Mode, companyName: String) = ManualAddressViewModel(
    routes.CompanyRegisteredAddressController.onSubmit(mode),
    countryOptions.options,
    Message("companyRegisteredNonUKAddress.title"),
    Message("companyRegisteredNonUKAddress.heading", companyName),
    None,
    Some(Message("companyRegisteredNonUKAddress.hintText"))
  )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      CompanyNameId.retrieve.right.map { companyName =>
        get(CompanyAddressId, addressViewModel(mode, companyName))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      CompanyNameId.retrieve.right.map { companyName =>
        post(CompanyAddressId, addressViewModel(mode, companyName), mode)
      }
  }
}
