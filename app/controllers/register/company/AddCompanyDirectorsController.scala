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

import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import connectors.DataCacheConnector
import controllers.actions._
import config.FrontendAppConfig
import forms.register.company.AddCompanyDirectorsFormProvider
import identifiers.register.company.AddCompanyDirectorsId
import identifiers.register.company.directors.{DirectorDetailsId, IsDirectorCompleteId}
import models.Mode
import models.register.company.directors.DirectorDetails
import play.api.Logger
import play.api.libs.json.JsResultException
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.CompanyDirector
import views.html.register.company.addCompanyDirectors

import scala.annotation.tailrec

class AddCompanyDirectorsController @Inject() (
                                                appConfig: FrontendAppConfig,
                                                override val messagesApi: MessagesApi,
                                                dataCacheConnector: DataCacheConnector,
                                                @CompanyDirector navigator: Navigator,
                                                authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                formProvider: AddCompanyDirectorsFormProvider
                                                   ) extends FrontendController with I18nSupport {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>

      val directors: Seq[DirectorDetails] = request.userAnswers.getAll[DirectorDetails](DirectorDetailsId.collectionPath).getOrElse(Nil)
      val isComplete: Seq[Boolean] = request.userAnswers.getAll[Boolean](IsDirectorCompleteId.collectionPath).getOrElse(Nil)
      val withFlags: Seq[(DirectorDetails, Boolean)] = directorsWithFlag(directors, isComplete)

      Ok(addCompanyDirectors(appConfig, form, mode, withFlags, disableSubmission(withFlags)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>

      val directors = request.userAnswers.getAll[DirectorDetails](DirectorDetailsId.collectionPath).getOrElse(Nil)
      val isComplete: Seq[Boolean] = request.userAnswers.getAll[Boolean](IsDirectorCompleteId.collectionPath).getOrElse(Nil)
      val withFlags: Seq[(DirectorDetails, Boolean)] = directorsWithFlag(directors, isComplete)

      if (directors.isEmpty || directors.lengthCompare(appConfig.maxDirectors) >= 0) {
        Redirect(navigator.nextPage(AddCompanyDirectorsId, mode, request.userAnswers))
      }
      else {
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            BadRequest(addCompanyDirectors(appConfig, formWithErrors, mode, withFlags, disableSubmission(withFlags))),
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

  @tailrec
  private def directorsWithFlag(
                                 directors: Seq[DirectorDetails],
                                 flags: Seq[Boolean],
                                 withFlag: Seq[(DirectorDetails, Boolean)] = Seq.empty
                               ): Seq[(DirectorDetails, Boolean)] = {

    if(directors.isEmpty){
      withFlag
    } else if (flags.isEmpty) {
      val addWithFlag: (DirectorDetails, Boolean) = (directors.head, false)
      directorsWithFlag(directors.tail, Seq.empty, withFlag :+ addWithFlag)
    } else {
      val addWithFlag: (DirectorDetails, Boolean) = (directors.head, flags.head)
      directorsWithFlag(directors.tail, flags.tail, withFlag :+ addWithFlag)
    }

  }

  private def disableSubmission(directorsWithFlag: Seq[(DirectorDetails, Boolean)]): Boolean = directorsWithFlag.foldLeft(true){ case (_, (_, isComplete)) =>
    !isComplete
  }
}
