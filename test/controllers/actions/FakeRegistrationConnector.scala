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

package controllers.actions

import connectors.RegistrationConnector
import models._
import models.RegistrationLegalStatus
import org.joda.time.LocalDate
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

abstract class FakeRegistrationConnector extends RegistrationConnector {

  override def registerWithIdOrganisation
  (utr: String, organisation: Organisation, legalStatus: RegistrationLegalStatus)
  (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[OrganizationRegistration] = ???

  override def registerWithNoIdOrganisation
  (name: String, address: Address, legalStatus: RegistrationLegalStatus)
  (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[RegistrationInfo] =  ???

  override def registerWithIdIndividual
  (nino: String)
  (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[IndividualRegistration] = ???

  override def registerWithNoIdIndividual
  (firstName: String, lastName: String, address: Address, dateOfBirth: LocalDate)
  (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[RegistrationInfo] = ???
}
