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

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import identifiers.register.company.directors.DirectorDetailsId
import identifiers.register.company.{BusinessDetailsId, CompanyReviewId}
import javax.inject.Inject
import models.NormalMode
import models.register.company.directors.DirectorDetails
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Navigator
import utils.annotations.RegisterCompany
import views.html.register.company.companyReview

import scala.concurrent.Future

class CompanyReviewController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         @RegisterCompany navigator: Navigator,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction) extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      BusinessDetailsId.retrieve.right.map { businessDetails =>
        val directors= request.userAnswers.getAll[DirectorDetails](DirectorDetailsId.collectionPath).getOrElse(Nil).map(_.fullName)
        Future.successful(Ok(companyReview(appConfig, businessDetails.companyName, directors)))
      }
  }

  def onSubmit: Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      Redirect(navigator.nextPage(CompanyReviewId, NormalMode)(request.userAnswers))
  }
}
