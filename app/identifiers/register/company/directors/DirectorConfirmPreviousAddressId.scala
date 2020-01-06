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

package identifiers.register.company.directors

import identifiers.TypedIdentifier
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers

case class DirectorConfirmPreviousAddressId(index: Int) extends TypedIdentifier[Boolean] {
  override def path: JsPath = JsPath \ "directors" \ index \ DirectorConfirmPreviousAddressId.toString

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(false) =>
        userAnswers.set(IsDirectorCompleteId(index))(value = false).flatMap(_.remove(DirectorPreviousAddressId(index)))
      case _ => super.cleanup(value, userAnswers)
    }
  }
}

object DirectorConfirmPreviousAddressId {
  override def toString: String = "directorConfirmPreviousAddress"
}

