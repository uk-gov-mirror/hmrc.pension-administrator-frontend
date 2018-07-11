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
import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import identifiers.register.company.{BusinessDetailsId, CheckYourAnswersId, CompanyAddressId, CompanySameContactAddressId}
import models.{CheckMode, Mode, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{CheckYourAnswersFactory, Navigator}
import utils.annotations.RegisterCompany
import viewmodels.AnswerSection
import views.html.check_your_answers
import utils.checkyouranswers.Ops._

class CheckYourAnswersController @Inject()(
                                            appConfig: FrontendAppConfig,
                                            authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            @RegisterCompany navigator: Navigator,
                                            override val messagesApi: MessagesApi,
                                            checkYourAnswersFactory: CheckYourAnswersFactory
                                          ) extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>

      val checkYourAnswerHelper = checkYourAnswersFactory.checkYourAnswersHelper(request.userAnswers)

      val companyDetails = AnswerSection(
        Some("company.checkYourAnswers.company.details.heading"),
        BusinessDetailsId.row(None)
        ++ Seq(
          checkYourAnswerHelper.vatRegistrationNumber,
          checkYourAnswerHelper.payeEmployerReferenceNumber,
          checkYourAnswerHelper.companyRegistrationNumber
        ).flatten
      )

      val companyContactDetails = AnswerSection(
        Some("company.checkYourAnswers.company.contact.details.heading"),
        Seq(
          checkYourAnswerHelper.companyAddress,
          checkYourAnswerHelper.companySameContactAddress,
          checkYourAnswerHelper.companyContactAddress,
          checkYourAnswerHelper.companyAddressYears,
          checkYourAnswerHelper.companyPreviousAddress
        ).flatten
      )

      val contactDetails = AnswerSection(
        Some("common.checkYourAnswers.contact.details.heading"),
        Seq(
          checkYourAnswerHelper.email,
          checkYourAnswerHelper.phone
        ).flatten
      )

      Ok(check_your_answers(
        appConfig,
        Seq(companyDetails, companyContactDetails, contactDetails),
        Some(messagesApi("site.secondaryHeader")),
        controllers.register.company.routes.CheckYourAnswersController.onSubmit()
      ))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      Redirect(navigator.nextPage(CheckYourAnswersId, NormalMode, request.userAnswers))

  }
}
