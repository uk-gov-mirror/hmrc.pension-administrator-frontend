/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.register.partnership.partners

import config.FrontendAppConfig
import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import controllers.{Retrievals, Variations}
import identifiers.register.partnership.partners._
import javax.inject.Inject
import models.Mode.checkMode
import models._
import models.requests.DataRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.Navigator
import utils.annotations.PartnershipPartner
import utils.checkyouranswers.Ops._
import utils.countryOptions.CountryOptions
import viewmodels.{AnswerSection, Link}
import views.html.check_your_answers

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(appConfig: FrontendAppConfig,
                                           authenticate: AuthAction,
                                           allowAccess: AllowAccessActionProvider,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           @PartnershipPartner navigator: Navigator,
                                           implicit val countryOptions: CountryOptions,
                                           override val cacheConnector: UserAnswersCacheConnector,
                                           val controllerComponents: MessagesControllerComponents,
                                           val view: check_your_answers
                                          )(implicit val executionContext: ExecutionContext) extends FrontendBaseController with Retrievals with Variations with I18nSupport {

  def onPageLoad(index: Index, mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      val userAnswers = request.userAnswers
      userAnswers.get(PartnerNameId(index)) match {
        case Some(_) =>
          loadCyaPage(index, mode)
        case _ =>
          Future.successful(Redirect(routes.PartnerNameController.onPageLoad(mode, index)))
      }
  }

  def onSubmit(index: Index, mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      val isDataComplete = request.userAnswers.isPartnerComplete(index)
      if (isDataComplete) {
        saveChangeFlag(mode, CheckYourAnswersId).map { _ =>
          Redirect(navigator.nextPage(CheckYourAnswersId, mode, request.userAnswers))
        }
      } else {
        loadCyaPage(index, mode)
      }
  }

  private def loadCyaPage(index: Int, mode: Mode)(implicit request: DataRequest[AnyContent]): Future[Result] = {
    val answerSection = Seq(
      AnswerSection(
        None,
        PartnerNameId(index).row(Some(Link(routes.PartnerNameController.onPageLoad(checkMode(mode), index).url))) ++
          PartnerDOBId(index).row(Some(Link(routes.PartnerDOBController.onPageLoad(checkMode(mode), index).url))) ++
          HasPartnerNINOId(index).row(Some(Link(routes.HasPartnerNINOController.onPageLoad(checkMode(mode), index).url))) ++
          PartnerEnterNINOId(index).row(Some(Link(routes.PartnerEnterNINOController.onPageLoad(checkMode(mode), index).url))) ++
          PartnerNoNINOReasonId(index).row(Some(Link(routes.PartnerNoNINOReasonController.onPageLoad(checkMode(mode), index).url))) ++
          HasPartnerUTRId(index).row(Some(Link(routes.HasPartnerUTRController.onPageLoad(checkMode(mode), index).url))) ++
          PartnerEnterUTRId(index).row(Some(Link(routes.PartnerEnterUTRController.onPageLoad(checkMode(mode), index).url))) ++
          PartnerNoUTRReasonId(index).row(Some(Link(routes.PartnerNoUTRReasonController.onPageLoad(checkMode(mode), index).url))) ++
          PartnerAddressId(index).row(Some(Link(routes.PartnerAddressPostCodeLookupController.onPageLoad(checkMode(mode), index).url))) ++
          PartnerAddressYearsId(index).row(Some(Link(routes.PartnerAddressYearsController.onPageLoad(checkMode(mode), index).url))) ++
          PartnerPreviousAddressId(index).row(Some(Link(routes.PartnerPreviousAddressPostCodeLookupController.onPageLoad(checkMode(mode), index).url))) ++
          PartnerEmailId(index).row(Some(Link(routes.PartnerEmailController.onPageLoad(checkMode(mode), index).url))) ++
          PartnerPhoneId(index).row(Some(Link(routes.PartnerPhoneController.onPageLoad(checkMode(mode), index).url)))
      ))
    Future.successful(Ok(view(
      answerSection,
      routes.CheckYourAnswersController.onSubmit(index, mode),
      psaName(),
      mode,
      request.userAnswers.isPartnerComplete(index)))
    )
  }
}
