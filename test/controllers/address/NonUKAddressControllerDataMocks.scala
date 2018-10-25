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

package controllers.address

import connectors.RegistrationConnector
import controllers.ControllerSpecBase
import identifiers.TypedIdentifier
import models._
import play.api.mvc.Call
import uk.gov.hmrc.http.HeaderCarrier
import utils.FakeCountryOptions
import utils.countryOptions.CountryOptions

import scala.concurrent.{ExecutionContext, Future}

trait NonUKAddressControllerDataMocks extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  def countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  val fakeAddressId: TypedIdentifier[TolerantAddress] = new TypedIdentifier[TolerantAddress] {
    override def toString = "fakeAddressId"
  }

  val externalId: String = "test-external-id"
  val companyName = "Test Company Name"
  val sapNumber = "test-sap-number"
  val psaUser = PSAUser(UserType.Individual, None, isExistingPSA = false, None)

  val registrationInfo = RegistrationInfo(
    RegistrationLegalStatus.LimitedCompany,
    sapNumber,
    false,
    RegistrationCustomerType.NonUK,
    None,
    None
  )

  def fakeRegistrationConnector = new RegistrationConnector {

    override def registerWithIdOrganisation(utr: String, organisation: Organisation, legalStatus: RegistrationLegalStatus)(
      implicit hc: HeaderCarrier, ec: ExecutionContext): Future[OrganizationRegistration] = ???

    override def registerWithNoIdOrganisation(name: String, address: Address)(
      implicit hc: HeaderCarrier, ec: ExecutionContext): Future[RegistrationInfo] = Future.successful(registrationInfo)

    override def registerWithIdIndividual(nino: String)(
      implicit hc: HeaderCarrier, ec: ExecutionContext): Future[IndividualRegistration] = ???
  }
}
