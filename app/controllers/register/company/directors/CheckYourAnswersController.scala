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
import controllers.Retrievals
import controllers.actions._
import identifiers.register.company.directors.{CheckYourAnswersId, IsDirectorCompleteId}
import javax.inject.Inject
import models.{Index, Mode, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.CompanyDirector
import utils.{CheckYourAnswersFactory, Navigator, SectionComplete}
import viewmodels.AnswerSection
import views.html.check_your_answers

import scala.concurrent.Future

class CheckYourAnswersController @Inject()(
                                            appConfig: FrontendAppConfig,
                                            authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            @CompanyDirector navigator: Navigator,
                                            override val messagesApi: MessagesApi,
                                            checkYourAnswersFactory: CheckYourAnswersFactory,
                                            sectionComplete: SectionComplete
                                          ) extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad(index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(index) { directorName =>
        val checkYourAnswerHelper = checkYourAnswersFactory.checkYourAnswersHelper(request.userAnswers)
        val answersSection = Seq(
          AnswerSection(
            Some("directorCheckYourAnswers.directorDetails.heading"),
            checkYourAnswerHelper.directorDetails(index.id) ++
              checkYourAnswerHelper.directorNino(index.id) ++
              checkYourAnswerHelper.directorUniqueTaxReference(index.id)
          ),
          AnswerSection(
            Some("directorCheckYourAnswers.contactDetails.heading"),
            checkYourAnswerHelper.directorAddress(index.id) ++
              checkYourAnswerHelper.directorAddressYears(index.id) ++
              checkYourAnswerHelper.directorPreviousAddress(index.id) ++
              checkYourAnswerHelper.directorContactDetails(index.id)
          ))

        Future.successful(Ok(check_your_answers(
          appConfig,
          answersSection,
          Some(directorName),
          controllers.register.company.directors.routes.CheckYourAnswersController.onSubmit(index)))
        )
      }
  }

  def onSubmit(index: Index, mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      sectionComplete.setComplete(IsDirectorCompleteId(index), request.userAnswers) map { _ =>
        Redirect(navigator.nextPage(CheckYourAnswersId, NormalMode, request.userAnswers))
      }
  }

}
