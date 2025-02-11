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

package identifiers.register

import identifiers._
import identifiers.register.adviser._
import play.api.libs.json.{JsResult, JsSuccess}
import utils.UserAnswers

case object VariationWorkingKnowledgeId extends TypedIdentifier[Boolean] {
  override def toString: String = "declarationWorkingKnowledge"
  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(true) =>  userAnswers.removeAllOf(List(AdviserNameId, AdviserEmailId, AdviserPhoneId, AdviserAddressId,
        AdviserAddressListId, AdviserAddressPostCodeLookupId))
      case _ => JsSuccess(userAnswers)
    }
  }
}
