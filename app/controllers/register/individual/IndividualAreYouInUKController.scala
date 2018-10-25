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
import connectors.UserAnswersCacheConnector
import controllers.actions._
import controllers.register.AreYouInUKController
import forms.register.AreYouInUKFormProvider
import javax.inject.Inject
import models.{Mode, NormalMode}
import play.api.i18n.MessagesApi
import utils.Navigator
import utils.annotations.Individual
import viewmodels.{AreYouInUKViewModel, Message}

class IndividualAreYouInUKController @Inject()(override val appConfig: FrontendAppConfig,
                                               val messagesApi: MessagesApi,
                                               override val dataCacheConnector: UserAnswersCacheConnector,
                                               @Individual override val navigator: Navigator,
                                               override val authenticate: AuthAction,
                                               override val getData: DataRetrievalAction,
                                               override val requireData: DataRequiredAction,
                                               override val formProvider: AreYouInUKFormProvider
                                              ) extends AreYouInUKController {

  protected override val form = formProvider()

  protected def viewmodel(mode: Mode) =
    AreYouInUKViewModel(mode,
      postCall = controllers.register.individual.routes.IndividualAreYouInUKController.onSubmit(mode),
      title = Message("areYouInUKIndividual.title"),
      heading = Message("areYouInUKIndividual.heading"),
      secondaryLabel= if(mode==NormalMode) Some(Message("areYouInUKIndividual.hint")) else None,
      p1 = Some("areYouInUKIndividual.check.selectedUkAddress"),
      p2 = Some("areYouInUKIndividual.check.provideNonUkAddress")
    )
}
