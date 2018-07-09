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

package controllers.register.company.directors

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.UniqueTaxReferenceFormProvider
import identifiers.register.company.directors.DirectorUniqueTaxReferenceId
import javax.inject.Inject
import models.{Index, Mode, UniqueTaxReference}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.CompanyDirector
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.company.directors.directorUniqueTaxReference

import scala.concurrent.Future

class DirectorUniqueTaxReferenceController @Inject()(
                                                      appConfig: FrontendAppConfig,
                                                      override val messagesApi: MessagesApi,
                                                      dataCacheConnector: DataCacheConnector,
                                                      @CompanyDirector navigator: Navigator,
                                                      authenticate: AuthAction,
                                                      getData: DataRetrievalAction,
                                                      requireData: DataRequiredAction,
                                                      formProvider: UniqueTaxReferenceFormProvider
                                                    ) extends FrontendController with I18nSupport with Enumerable.Implicits with Retrievals {

  private val form: Form[UniqueTaxReference] = formProvider.apply(
    requiredKey = "directorUniqueTaxReference.error.required",
    requiredReasonKey = "directorUniqueTaxReference.error.reason.required"
  )

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(index) { directorName =>
        val redirectResult = request.userAnswers.get(DirectorUniqueTaxReferenceId(index)) match {
          case None =>
            Ok(directorUniqueTaxReference(appConfig, form, mode, index, directorName))
          case Some(value) =>
            Ok(directorUniqueTaxReference(appConfig, form.fill(value), mode, index, directorName))
        }
        Future.successful(redirectResult)
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(index) { directorName =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(directorUniqueTaxReference(appConfig, formWithErrors, mode, index, directorName))),
          value =>
            dataCacheConnector.save(request.externalId, DirectorUniqueTaxReferenceId(index), value).map(json =>
              Redirect(navigator.nextPage(DirectorUniqueTaxReferenceId(index), mode, UserAnswers(json))))
        )
      }
  }

}
