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

import controllers.actions.{DataRetrievalAction, FakeDataRetrievalAction}
import identifiers.register.advisor.{AdvisorAddressId, AdvisorAddressListId}
import identifiers.register.company.directors.{CompanyDirectorAddressListId, DirectorAddressId, DirectorPreviousAddressId, DirectorPreviousAddressListId}
import identifiers.register.company.{CompanyAddressListId, CompanyPreviousAddressId}
import identifiers.register.individual.{IndividualPreviousAddressId, IndividualPreviousAddressListId}
import models.{Address, TolerantAddress}
import org.scalatest.OptionValues

package object utils {

  implicit class UserAnswerOps(answers: UserAnswers) extends OptionValues {

    // Individual PSA
    def individualPreviousAddress(address: Address): UserAnswers = {
      answers.set(IndividualPreviousAddressId)(address).asOpt.value
    }

    def individualPreviousAddressList(address: TolerantAddress): UserAnswers = {
      answers.set(IndividualPreviousAddressListId)(address).asOpt.value
    }

    // Company PSA
    def companyPreviousAddress(address: Address): UserAnswers = {
      answers.set(CompanyPreviousAddressId)(address).asOpt.value
    }

    def companyAddressList(address: TolerantAddress): UserAnswers = {
      answers.set(CompanyAddressListId)(address).asOpt.value
    }

    // Company director
    def directorAddress(index: Int, address: Address): UserAnswers = {
      answers.set(DirectorAddressId(index))(address).asOpt.value
    }

    def companyDirectorAddressList(index: Int, address: TolerantAddress): UserAnswers = {
      answers.set(CompanyDirectorAddressListId(index))(address).asOpt.value
    }

    def directorPreviousAddress(index: Int, address: Address): UserAnswers = {
      answers.set(DirectorPreviousAddressId(index))(address).asOpt.value
    }

    def directorPreviousAddressList(index: Int, address: TolerantAddress): UserAnswers = {
      answers.set(DirectorPreviousAddressListId(index))(address).asOpt.value
    }

    // Advisor
    def advisorAddress(address: Address): UserAnswers = {
      answers.set(AdvisorAddressId)(address).asOpt.value
    }

    def advisorAddressList(address: TolerantAddress): UserAnswers = {
      answers.set(AdvisorAddressListId)(address).asOpt.value
    }

    // Converters
    def dataRetrievalAction: DataRetrievalAction = {
      new FakeDataRetrievalAction(Some(answers.json))
    }

  }

}
