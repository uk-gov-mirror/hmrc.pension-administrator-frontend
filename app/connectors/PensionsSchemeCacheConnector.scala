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

package connectors

import com.google.inject.Inject
import config.FrontendAppConfig
import identifiers.TypedIdentifier
import play.api.http.Status._
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.Result
import play.api.mvc.Results.Ok
import uk.gov.hmrc.crypto.{ApplicationCrypto, PlainText}
import uk.gov.hmrc.http._
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}

class PensionsSchemeCacheConnector @Inject()(
                                            config: FrontendAppConfig,
                                            http: WSClient
                                          ) extends ICacheConnector(config, http) {

  override protected def url(id: String) = s"${config.pensionsSchemeUrl}/pensions-scheme/journey-cache/psa/$id"
}
