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

package controllers

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import identifiers.TypedIdentifier
import models.PersonDetails
import models.requests.DataRequest
import play.api.i18n.I18nSupport
import play.api.libs.json.Format
import play.api.mvc.{AnyContent, Call, Result}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import viewmodels.ConfirmDeleteViewModel
import views.html.confirmDelete

import scala.concurrent.Future

trait ConfirmDeleteController extends FrontendController with I18nSupport with Retrievals {

  implicit val ec = play.api.libs.concurrent.Execution.defaultContext

  protected def appConfig: FrontendAppConfig

  protected def cacheConnector: UserAnswersCacheConnector

  def get(vm: ConfirmDeleteViewModel, isDeleted: Boolean, redirectTo: Call)(implicit request: DataRequest[AnyContent]): Future[Result] =
    if (!isDeleted) {
      Future.successful(Ok(confirmDelete(appConfig, vm)))
    } else {
      Future.successful(Redirect(redirectTo))
    }

  def post(id: TypedIdentifier[PersonDetails], postUrl: Call)
          (implicit request: DataRequest[AnyContent], f: Format[PersonDetails]): Future[Result] =
    id.retrieve.right.map { details =>
      cacheConnector.save(request.externalId, id, details.copy(isDeleted = true)) map { _ =>
        Redirect(postUrl)
      }
    }

}
