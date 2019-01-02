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

package controllers.register.company

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions._
import forms.register.company.AddCompanyDirectorsFormProvider
import identifiers.register.company.AddCompanyDirectorsId
import javax.inject.Inject
import models.Mode
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsResultException
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Navigator
import utils.annotations.CompanyDirector
import viewmodels.Person
import views.html.register.company.addCompanyDirectors

import scala.concurrent.ExecutionContext

class AddCompanyDirectorsController @Inject()(
                                               appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               dataCacheConnector: UserAnswersCacheConnector,
                                               @CompanyDirector navigator: Navigator,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               formProvider: AddCompanyDirectorsFormProvider
                                             )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val directors: Seq[Person] = request.userAnswers.allDirectorsAfterDelete
      Ok(addCompanyDirectors(appConfig, form, mode, directors))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val directors: Seq[Person] = request.userAnswers.allDirectorsAfterDelete

      if (directors.isEmpty || directors.lengthCompare(appConfig.maxDirectors) >= 0) {
        Redirect(navigator.nextPage(AddCompanyDirectorsId, mode, request.userAnswers))
      }
      else {
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            BadRequest(addCompanyDirectors(appConfig, formWithErrors, mode, directors)),
          value => {
            request.userAnswers.set(AddCompanyDirectorsId)(value).fold(
              errors => {
                Logger.error("Unable to set user answer", JsResultException(errors))
                InternalServerError
              },
              userAnswers => Redirect(navigator.nextPage(AddCompanyDirectorsId, mode, userAnswers))
            )
          }
        )
      }
  }

}
