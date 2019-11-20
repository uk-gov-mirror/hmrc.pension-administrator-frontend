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

package controllers.register.partnership.partners

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressListController
import identifiers.register.partnership.partners.{PartnerAddressId, PartnerAddressListId, PartnerAddressPostCodeLookupId, PartnerNameId}
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import utils.Navigator
import utils.annotations.PartnershipPartner
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import scala.concurrent.{ExecutionContext, Future}

class PartnerAddressListController @Inject()(override val appConfig: FrontendAppConfig,
                                             override val cacheConnector: UserAnswersCacheConnector,
                                             @PartnershipPartner override val navigator: Navigator,
                                             authenticate: AuthAction,
                                             override val allowAccess: AllowAccessActionProvider,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             val controllerComponents: MessagesControllerComponents,
                                             val view: addressList
                                            )(implicit val executionContext: ExecutionContext) extends AddressListController with Retrievals {

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      PartnerNameId(index).retrieve.right.flatMap { pn =>
        viewModel(mode, index, pn.fullName).right.map { vm =>
          get(vm, mode)
        }
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      PartnerNameId(index).retrieve.right.flatMap { pn =>
        viewModel(mode, index, pn.fullName).right.map(vm => post(vm, PartnerAddressListId(index), PartnerAddressId(index), mode))
      }
  }

  private def viewModel(mode: Mode, index: Index, name: String)(implicit request: DataRequest[AnyContent]): Either[Future[Result], AddressListViewModel] = {
    PartnerAddressPostCodeLookupId(index).retrieve.right.map {
      addresses =>
        AddressListViewModel(
          postCall = routes.PartnerAddressListController.onSubmit(mode, index),
          manualInputCall = routes.PartnerAddressController.onPageLoad(mode, index),
          addresses = addresses,
          Message("contactAddressList.heading", Message("thePartner")),
          Message("contactAddressList.heading", name),
          Message("common.selectAddress.text"),
          Message("common.selectAddress.link"),
          psaName = psaName()
        )
    }.left.map(_ => Future.successful(Redirect(routes.PartnerAddressPostCodeLookupController.onPageLoad(mode, index))))
  }

}
