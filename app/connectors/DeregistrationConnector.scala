/*
 * Copyright 2021 HM Revenue & Customs
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

package connectors

import com.google.inject.{ImplementedBy, Inject}
import config.FrontendAppConfig
import models.Deregistration
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{JsError, JsResultException, JsSuccess}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import utils.HttpResponseHelper

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

@ImplementedBy(classOf[DeregistrationConnectorImpl])
trait DeregistrationConnector {
  def stopBeingPSA(psaId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse]

  def canDeRegister(psaId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Deregistration]
}

class DeregistrationConnectorImpl @Inject()(http: HttpClient, config: FrontendAppConfig)
  extends DeregistrationConnector
    with HttpResponseHelper {

  private val logger = Logger(classOf[DeregistrationConnectorImpl])

  override def stopBeingPSA(psaId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val deregisterUrl = config.deregisterPsaUrl.format(psaId)
    http.DELETE[HttpResponse](deregisterUrl) map {
      response =>
        response.status match {
          case NO_CONTENT => response
          case _ => handleErrorResponse("DELETE", deregisterUrl)(response)
        }
    } andThen {
      case Failure(t: Throwable) => logger.warn("Unable to deregister PSA", t)
    }
  }

  override def canDeRegister(psaId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Deregistration] = {

    val url = config.canDeRegisterPsaUrl(psaId)

    http.GET[HttpResponse](url).map { response =>
      response.status match {
        case OK => response.json.validate[Deregistration] match {
          case JsSuccess(value, _) => value
          case JsError(errors) => throw JsResultException(errors)
        }
        case _ => handleErrorResponse("GET", url)(response)
      }
    } andThen {
      case Failure(t: Throwable) => logger.warn("Unable to get the response from can de register api", t)
    }
  }
}
