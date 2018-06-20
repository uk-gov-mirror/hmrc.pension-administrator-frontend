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

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.actions._
import controllers.address.SameContactAddressController
import controllers.register.individual.routes._
import forms.address.SameContactAddressFormProvider
import identifiers.register.individual._
import javax.inject.Inject
import models.Mode
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import utils.Navigator2
import utils.annotations.Individual
import viewmodels.Message
import viewmodels.address.SameContactAddressViewModel

class IndividualSameContactAddressController @Inject()(val appConfig: FrontendAppConfig,
                                                       val messagesApi: MessagesApi,
                                                       val dataCacheConnector: DataCacheConnector,
                                                       @Individual val navigator: Navigator2,
                                                       authenticate: AuthAction,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction,
                                                       formProvider: SameContactAddressFormProvider
                                                      ) extends SameContactAddressController with I18nSupport {

  private[controllers] val postCall = IndividualSameContactAddressController.onSubmit _
  private[controllers] val title: Message = "individual.same.contact.address.title"
  private[controllers] val heading: Message = "individual.same.contact.address.heading"
  private[controllers] val hint: Message = "individual.same.contact.address.hint"
  private[controllers] val secondaryHeader: Message = "site.secondaryHeader"

  protected val form: Form[Boolean] = formProvider()

  private def viewmodel(mode: Mode) =
    Retrieval(
      implicit request =>
        IndividualAddressId.retrieve.right.map {
          address =>
            SameContactAddressViewModel(
              postCall(mode),
              title = Message(title),
              heading = Message(heading),
              hint = Some(Message(hint)),
              secondaryHeader = Some(secondaryHeader),
              address = address
            )
        }
    )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode).retrieve.right.map { vm =>
        get(IndividualSameContactAddressId, vm)
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode).retrieve.right.map { vm =>
        post(IndividualSameContactAddressId, IndividualContactAddressListId, IndividualContactAddressId, vm, mode)
      }
  }

}
