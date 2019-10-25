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

import java.time.LocalDate

import base.SpecBase
import controllers.actions._
import identifiers.register.company.directors.DirectorDetailsId
import identifiers.register.individual.IndividualDetailsId
import identifiers.register.partnership.partners.PartnerDetailsId
import identifiers.register.{BusinessNameId, RegistrationInfoId}
import models._
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.libs.json.Json
import utils.UserAnswers

trait ControllerSpecBase extends SpecBase {

  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  val cacheMapId = "id"

  val firstIndex = Index(0)

  def getEmptyData: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj()))

  def dontGetAnyData: FakeDataRetrievalAction = new FakeDataRetrievalAction(None)

  def getCompany: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(
    Json.obj(
      RegistrationInfoId.toString -> RegistrationInfo(
        RegistrationLegalStatus.LimitedCompany, "", false, RegistrationCustomerType.UK, None, None),
      BusinessNameId.toString -> "Test Company Name")
    ))

  def getDirector: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(
    Json.obj(
      BusinessNameId.toString -> "Test Company Name",
      "directors" -> Json.arr(
        Json.obj(
          DirectorDetailsId.toString -> PersonDetails("test first name", Some("test middle name"), "test last name", LocalDate.now())
        )
      )
    )))

  def getIndividual: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(
    Json.obj(
      RegistrationInfoId.toString -> RegistrationInfo(
        RegistrationLegalStatus.Individual, "", false, RegistrationCustomerType.UK, None, None),
      IndividualDetailsId.toString ->
        TolerantIndividual(Some("TestFirstName"), None, Some("TestLastName"))
    )))

  def getPartnership: DataRetrievalAction =
    UserAnswers().partnershipDetails(details = models.BusinessDetails("Test Partnership Name", Some("1234567890"))).dataRetrievalAction

  def getPartner: FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(
      Json.obj(
        BusinessNameId.toString -> BusinessDetails("Test Partnership Name", Some("1234567890")),
        "partners" -> Json.arr(
          Json.obj(
            PartnerDetailsId.toString ->
              PersonDetails("test first name", Some("test middle name"), "test last name", LocalDate.now())
          )
        )
      )
    )
  )

  def modules(dataRetrievalAction: DataRetrievalAction): Seq[GuiceableModule] = Seq(
    bind[AuthAction].toInstance(FakeAuthAction),
    bind[AllowAccessActionProvider].toInstance(FakeAllowAccessProvider()),
    bind[DataRetrievalAction].toInstance(dataRetrievalAction)
  )
}
