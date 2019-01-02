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

package identifiers.register.partnership

import identifiers.TypedIdentifier
import play.api.libs.json.JsResult
import utils.UserAnswers

case object PartnershipSameContactAddressId extends TypedIdentifier[Boolean] {
  override def toString: String = "partnershipSameContactAddress"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = value match {
    case Some(_) => userAnswers
      .remove(PartnershipContactAddressId)
      .flatMap(_.remove(PartnershipContactAddressPostCodeLookupId))
    case _ => super.cleanup(value, userAnswers)
  }
}
