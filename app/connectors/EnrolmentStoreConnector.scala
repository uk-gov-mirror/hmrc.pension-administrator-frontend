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

package connectors

import javax.inject.Inject

import com.google.inject.{ImplementedBy, Singleton}
import config.FrontendAppConfig
import models.register.{Enrol, KnownFacts}
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

@ImplementedBy(classOf[EnrolmentStoreConnectorImpl])
trait EnrolmentStoreConnector {

  def enrol(enrolmentKey: String, knownFacts: KnownFacts)
           (implicit w: Writes[KnownFacts], hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse]

}

@Singleton
class EnrolmentStoreConnectorImpl @Inject()(val http: HttpClient, config: FrontendAppConfig) extends EnrolmentStoreConnector {

  def url(enrolmentKey: String) = config.enrolmentStoreUrl(Enrol(enrolmentKey).key)

  def enrol(enrolmentKey: String, knownFacts: KnownFacts)
           (implicit w: Writes[KnownFacts], hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    println(">>>>>>>>>>>>>>" + knownFacts)
    http.PUT(url(enrolmentKey), knownFacts) flatMap {
      case response if response.status equals NO_CONTENT => Future.successful(response)
      case response => Future.failed(new HttpException(response.body, response.status))
    } andThen {
      logExceptions(knownFacts)
    }
  }

  private def logExceptions(knownFacts: KnownFacts): PartialFunction[Try[HttpResponse], Unit] = {
    case Failure(t: Throwable) => {
      println("<<<<<<<<<<<<<<<<<<<<<<<< " + Json.toJson(knownFacts))
      Logger.error("Unable to connect to Tax Enrolments", t)
    }
  }

}
