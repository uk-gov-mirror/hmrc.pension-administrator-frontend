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

package utils

import identifiers.TypedIdentifier
import play.api.libs.json._

import scala.language.implicitConversions

case class UserAnswers(private[UserAnswers] val json: JsValue) {

  def get[A](id: TypedIdentifier[A])(implicit rds: Reads[A]): Option[A] = {
    get[A](id.path)
  }

  def get[A](path: JsPath)(implicit rds: Reads[A]): Option[A] = {
    JsLens.fromPath(path).get(json)
      .flatMap(Json.fromJson[A]).asOpt
  }

  def getAll[A](path: JsPath)(implicit rds: Reads[A]): Option[Seq[A]] = {
    JsLens
      .fromPath(path)
      .getAll(json)
      .asOpt
      .flatMap(vs =>
        Some(vs.map(v => v.as[A]))
      )
  }

  def set[I <: TypedIdentifier.PathDependent](id: I)(value: id.Data)(implicit writes: Writes[id.Data], cleanup: Cleanup[I]): JsResult[UserAnswers] = {

    val jsValue = Json.toJson(value)

    JsLens.fromPath(id.path)
      .set(jsValue, json)
      .flatMap(json => cleanup(id)(Some(value), UserAnswers(json)))
  }

  def remove[I <: TypedIdentifier.PathDependent](id: I)(implicit cleanup: Cleanup[I]): JsResult[UserAnswers] = {

    JsLens.fromPath(id.path)
      .remove(json)
      .flatMap(json => cleanup(id)(None, UserAnswers(json)))
  }

}
