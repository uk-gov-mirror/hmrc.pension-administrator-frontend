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

package controllers.register.company

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.SameContactAddressController
import controllers.register.company.routes.CompanySameContactAddressController
import forms.address.SameContactAddressFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.company._
import javax.inject.{Inject, Singleton}
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Navigator
import utils.annotations.RegisterCompany
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.SameContactAddressViewModel
import views.html.address.sameContactAddress

import scala.concurrent.ExecutionContext

@Singleton()
class CompanySameContactAddressController @Inject()(@RegisterCompany val navigator: Navigator,
                                                    val appConfig: FrontendAppConfig,
                                                    override val messagesApi: MessagesApi,
                                                    val dataCacheConnector: UserAnswersCacheConnector,
                                                    authenticate: AuthAction,
                                                    allowAccess: AllowAccessActionProvider,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    formProvider: SameContactAddressFormProvider,
                                                    val countryOptions: CountryOptions,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    val view: sameContactAddress
                                                   )(implicit val executionContext: ExecutionContext) extends SameContactAddressController {

  def form(name: String)(implicit request: DataRequest[AnyContent]): Form[Boolean] = formProvider(Message("same.contact.address.error").withArgs(name))

  private[controllers] val postCall = CompanySameContactAddressController.onSubmit _
  private[controllers] val title: Message = "company.same.contact.address.title"
  private[controllers] val heading: Message = "company.same.contact.address.heading"

  private def viewmodel(mode: Mode): Retrieval[SameContactAddressViewModel] =
    Retrieval(
      implicit request =>
        (CompanyAddressId and BusinessNameId).retrieve.right.map {
          case address ~ name =>
            SameContactAddressViewModel(
              postCall(mode),
              title = Message(title),
              heading = Message(heading).withArgs(name),
              hint = None,
              address = address,
              psaName = name,
              mode = mode
            )
        }
    )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode).retrieve.right.map { vm =>
        get(CompanySameContactAddressId, vm, form(vm.psaName))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode).retrieve.right.map { vm =>
        post(CompanySameContactAddressId, CompanyAddressListId, CompanyContactAddressId, vm, mode, form(vm.psaName))
      }
  }

}
