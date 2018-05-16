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

import com.google.inject.{ImplementedBy, Inject}
import config.FrontendAppConfig
import models.TolerantAddress
import play.api.Logger
import play.api.libs.json.Reads
import uk.gov.hmrc.http.{HttpException, _}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import play.api.http.Status._


class AddressLookupConnectorImpl @Inject()(http: HttpClient, config: FrontendAppConfig) extends AddressLookupConnector {

  case class testException(m: String, i: Int) extends HttpException(m, i)

  override def addressLookupByPostCode(postcode: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[TolerantAddress]] = {
    val schemeHc = hc.withExtraHeaders("X-Hmrc-Origin" -> "PODS")

    val addressLookupUrl = s"${config.addressLookUp}/v2/uk/addresses?postcode=$postcode"

    implicit val reads: Reads[Seq[TolerantAddress]] = TolerantAddress.postCodeLookupReads

    http.GET[HttpResponse](addressLookupUrl)(implicitly, schemeHc, implicitly) flatMap {
      case response if response.status equals OK => {
        Future.successful(response.json.as[Seq[TolerantAddress]])
      }

      case response => {
        Future.failed(new HttpException(response.body, response.status))
      }
    } recoverWith logExceptions
  }


  private def logExceptions: PartialFunction[Throwable, Future[Seq[TolerantAddress]]] = {
    case (e: HttpException) => println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" + e.responseCode)
      Logger.error("Exception in AddressLookup", e)
      Future.failed(e)
    case (t: Throwable) => {
      println("nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn" + t)
      Logger.error("Exception in AddressLookup", t)
      Future.failed(t)
    }
  }
}

@ImplementedBy(classOf[AddressLookupConnectorImpl])
trait AddressLookupConnector {
  def addressLookupByPostCode(postcode: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[TolerantAddress]]
}
