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
import models.SendEmailRequest
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.crypto.{ApplicationCrypto, PlainText}
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.http.HttpReads.Implicits._

import scala.concurrent.{ExecutionContext, Future}

sealed trait EmailStatus

case object EmailSent extends EmailStatus

case object EmailNotSent extends EmailStatus

@ImplementedBy(classOf[EmailConnectorImpl])
trait EmailConnector {

  def sendEmail(emailAddress: String, templateName: String, templateParams: Map[String, String], psaId: PsaId)
               (implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[EmailStatus]
}

class EmailConnectorImpl @Inject()(
                                    appConfig: FrontendAppConfig,
                                    http: HttpClient,
                                    crypto: ApplicationCrypto
                                  ) extends EmailConnector {

  private def callBackUrl(psaId: PsaId): String = {
    val encryptedPsaId = crypto.QueryParameterCrypto.encrypt(PlainText(psaId.value)).value
    appConfig.psaSubmissionEmailCallback(encryptedPsaId)
  }

  override def sendEmail(
                          emailAddress: String,
                          templateName: String,
                          templateParams: Map[String, String],
                          psaId: PsaId
                        )(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[EmailStatus] = {
    val emailServiceUrl = appConfig.emailUrl

    val sendEmailReq = SendEmailRequest(List(emailAddress), templateName, templateParams, appConfig.emailSendForce, callBackUrl(psaId))

    val jsonData = Json.toJson(sendEmailReq)

    http.POST[JsValue, HttpResponse](emailServiceUrl, jsonData).map { response =>
      response.status match {
        case ACCEPTED =>
          EmailSent
        case status =>
          Logger.warn(s"Sending Email failed with response status $status")
          EmailNotSent
      }
    } recoverWith logExceptions
  }

  private def logExceptions: PartialFunction[Throwable, Future[EmailStatus]] = {
    case t: Throwable =>
      Logger.warn("Unable to connect to Email Service", t)
      Future.successful(EmailNotSent)
  }
}

