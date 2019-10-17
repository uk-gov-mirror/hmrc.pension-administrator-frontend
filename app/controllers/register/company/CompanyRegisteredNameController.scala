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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.register.NameCleansing
import forms.BusinessNameFormProvider
import identifiers.register.BusinessNameId
import models.{Mode, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.RegisterCompany
import utils.{Navigator, UserAnswers}
import viewmodels.{Message, OrganisationNameViewModel}
import views.html.nonUkBusinessName

import scala.concurrent.{ExecutionContext, Future}

class CompanyRegisteredNameController @Inject()(appConfig: FrontendAppConfig,
                                                override val messagesApi: MessagesApi,
                                                @RegisterCompany navigator: Navigator,
                                                authenticate: AuthAction,
                                                allowAccess: AllowAccessActionProvider,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                cacheConnector: UserAnswersCacheConnector,
                                                formProvider: BusinessNameFormProvider
                                               )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with NameCleansing {

  val form = formProvider()

  private def companyNameViewModel(mode: Mode) =
    OrganisationNameViewModel(
      routes.CompanyRegisteredNameController.onSubmit(mode),
      Message("companyNameNonUk.title"),
      Message("companyNameNonUk.heading")
    )

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      val filledForm =
        request.userAnswers.get(BusinessNameId).map(form.fill).getOrElse(form)

      Future.successful(Ok(nonUkBusinessName(appConfig, filledForm, companyNameViewModel(mode))))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      cleanseAndBindOrRedirect(request.body.asFormUrlEncoded, "value", form) match {
        case Left(futureResult) => futureResult
        case Right(f) => f.fold(
          formWithErrors =>
            Future.successful(BadRequest(nonUkBusinessName(appConfig, formWithErrors, companyNameViewModel(mode)))),
          companyName =>
            cacheConnector.save(request.externalId, BusinessNameId, companyName).map {
              answers =>
                Redirect(navigator.nextPage(BusinessNameId, NormalMode, UserAnswers(answers)))
            }
        )
      }
  }
}
