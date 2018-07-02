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
import controllers.actions._
import forms.ContactDetailsFormProvider
import identifiers.register.partnership.{PartnershipContactDetailsId, PartnershipDetailsId}
import javax.inject.Inject
import models.Mode
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.Partnership
import viewmodels.{ContactDetailsViewModel, Message}

class PartnershipContactDetailsController @Inject()(
                                                     @Partnership override val navigator: Navigator,
                                                     override val appConfig: FrontendAppConfig,
                                                     override val messagesApi: MessagesApi,
                                                     override val cacheConnector: DataCacheConnector,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     formProvider: ContactDetailsFormProvider
                                                   ) extends controllers.ContactDetailsController {

  private def viewmodel(mode: Mode) = Retrieval {
    implicit request =>
      PartnershipDetailsId.retrieve.right.map { details =>
        ContactDetailsViewModel(
          postCall = routes.PartnershipContactDetailsController.onSubmit(mode),
          title = Message("partnershipContactDetails.title"),
          heading = Message("partnershipContactDetails.heading"),
          body = Message("contactDetails.body")
        )
      }
  }

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode).retrieve.right.map {
        get(PartnershipContactDetailsId, form, _)
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode).retrieve.right.map {
        post(PartnershipContactDetailsId, mode, form, _)
      }
  }
}
