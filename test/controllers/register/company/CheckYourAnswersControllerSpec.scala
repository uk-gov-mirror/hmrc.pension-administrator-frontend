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

package controllers.register.company

import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.{BusinessNameId, BusinessTypeId, BusinessUTRId}
import identifiers.register.company.{PhoneId, _}
import identifiers.register.{EnterPAYEId, EnterVATId, HasPAYEId, HasVATId}
import models._
import models.register.BusinessType
import play.api.libs.json.Json
import play.api.test.Helpers._
import utils.countryOptions.CountryOptions
import utils.{FakeCountryOptions, FakeNavigator}
import viewmodels.{AnswerRow, AnswerSection, Link, Message}
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  import CheckYourAnswersControllerSpec._


  "CheckYourAnswers Controller" when {

    "on a GET request for Company Details section " must {

      "render the view correctly for the company name and utr" in {
        val rows = Seq(answerRow("cya.label.companyName", Seq("Test Company Name")),
          answerRow("utr.checkYourAnswersLabel", Seq("Test UTR")))

        val sections = answerSections(Some("company.checkYourAnswers.company.details.heading"), rows)

        val retrievalAction = dataRetrievalAction(
          BusinessTypeId.toString -> BusinessType.LimitedCompany.toString,
          BusinessNameId.toString -> "Test Company Name",
          BusinessUTRId.toString -> "Test UTR")

        testRenderedView(sections :+ companyContactDetails :+ contactDetails, retrievalAction)
      }

      "render the view correctly for vat registration number" in {
        val rows = Seq(answerRow("cya.label.companyName", Seq("Test Company Name")),
          answerRow(label = Message("hasVAT.heading", companyName), Seq("site.yes"), answerIsMessageKey = true,
          Some(Link(controllers.register.company.routes.HasCompanyVATController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("hasVAT.visuallyHidden.text", companyName))),
          answerRow(label = Message("enterVAT.heading", companyName), Seq("Test Vat"), answerIsMessageKey = false,
            Some(Link(controllers.register.company.routes.CompanyEnterVATController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("enterVAT.visuallyHidden.text", companyName))))

        val sections = answerSections(Some("company.checkYourAnswers.company.details.heading"), rows)

        val retrievalAction = dataRetrievalAction(
          BusinessNameId.toString -> companyName,
          HasVATId.toString -> true,
          EnterVATId.toString -> "Test Vat"
        )
        testRenderedView(sections :+ companyContactDetails :+ contactDetails, retrievalAction)
      }

      "render the view correctly for paye number" in {
        val rows = Seq(answerRow("cya.label.companyName", Seq("Test Company Name")),
          answerRow(Message("hasPAYE.heading", companyName), Seq("site.yes"), answerIsMessageKey = true,
          Some(Link(controllers.register.company.routes.HasCompanyPAYEController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("hasPAYE.visuallyHidden.text", companyName))),
          answerRow(Message("enterPAYE.heading", companyName), Seq("Test Paye"), answerIsMessageKey = false,
            Some(Link(controllers.register.company.routes.CompanyEnterPAYEController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("enterPAYE.visuallyHidden.text", companyName))))

        val sections = answerSections(Some("company.checkYourAnswers.company.details.heading"), rows)

        val retrievalAction = dataRetrievalAction(
          BusinessNameId.toString -> companyName,
          HasPAYEId.toString -> true,
          EnterPAYEId.toString -> "Test Paye"
        )
        testRenderedView(sections :+ companyContactDetails :+ contactDetails, retrievalAction)
      }

      "render the view correctly for company registration number" in {
        val rows = Seq(
          answerRow("cya.label.companyName", Seq(companyName)),
          answerRow(
            messages("companyRegistrationNumber.heading", companyName), Seq("test reg no"), false,
            Some(Link(controllers.register.company.routes.CompanyRegistrationNumberController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("companyRegistrationNumber.visuallyHidden.text", companyName))
          )
        )

        val sections = answerSections(Some("company.checkYourAnswers.company.details.heading"), rows)

        val retrievalAction = dataRetrievalAction(
          BusinessNameId.toString -> companyName,
          CompanyRegistrationNumberId.toString -> "test reg no"
        )
        testRenderedView(sections :+ companyContactDetails :+ contactDetails, retrievalAction)
      }

      "render the view correctly for has company number" in {
        val rows = Seq(
          answerRow("cya.label.companyName", Seq(companyName)),
          answerRow(
            messages("hasCompanyNumber.heading", companyName), Seq("site.yes"), true,
            Some(Link(controllers.register.company.routes.HasCompanyCRNController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("hasCompanyNumber.visuallyHidden.text", companyName))
          )
        )

        val sections = answerSections(Some("company.checkYourAnswers.company.details.heading"), rows)

        val retrievalAction = dataRetrievalAction(
          BusinessNameId.toString -> companyName,
          HasCompanyCRNId.toString -> true
        )
        testRenderedView(sections :+ companyContactDetails :+ contactDetails, retrievalAction)
      }
    }

    "on a GET request for Company Contact Details section " must {

      "render the view correctly for the company address" in {
        val address = TolerantAddress(
          Some("address-line-1"),
          Some("address-line-2"),
          None,
          None,
          Some("post-code"),
          Some("country")
        )
        val rows = Seq(answerRow("companyAddress.checkYourAnswersLabel",
          Seq(
            address.addressLine1.value,
            address.addressLine2.value,
            address.postcode.value,
            address.country.value
          )))

        val sections = answerSections(Some("company.checkYourAnswers.company.contact.details.heading"), rows)

        val retrievalAction = dataRetrievalAction(
          CompanyAddressId.toString -> address
        )
        testRenderedView(companyDetails() +: sections :+ contactDetails, retrievalAction)
      }

      "render the view correctly for company same contact address" in {
        val rows = Seq(answerRow("cya.label.company.same.contact.address", Seq("Yes"), true,
          Some(Link(controllers.register.company.routes.CompanySameContactAddressController.onPageLoad(CheckMode).url))))

        val sections = answerSections(Some("company.checkYourAnswers.company.contact.details.heading"), rows)

        val retrievalAction = dataRetrievalAction(
          CompanySameContactAddressId.toString -> true
        )
        testRenderedView(companyDetails() +: sections :+ contactDetails, retrievalAction)
      }

      "render the view correctly for the company contact address" in {
        val rows = Seq(answerRow("cya.label.company.contact.address",
          Seq(
            address.addressLine1,
            address.addressLine2,
            address.postcode.value,
            address.country
          )))

        val sections = answerSections(Some("company.checkYourAnswers.company.contact.details.heading"), rows)

        val retrievalAction = dataRetrievalAction(
          CompanyContactAddressId.toString -> address
        )
        testRenderedView(companyDetails() +: sections :+ contactDetails, retrievalAction)
      }

      "render the view correctly for the company address years" in {
        val addressYears = AddressYears.OverAYear
        val rows = Seq(answerRow("companyAddressYears.checkYourAnswersLabel",
          Seq(s"common.addressYears.${addressYears.toString}"), true,
          Some(Link(controllers.register.company.routes.CompanyAddressYearsController.onPageLoad(CheckMode).url))))

        val sections = answerSections(Some("company.checkYourAnswers.company.contact.details.heading"), rows)

        val retrievalAction = dataRetrievalAction(
          CompanyAddressYearsId.toString -> addressYears.toString
        )
        testRenderedView(companyDetails() +: sections :+ contactDetails, retrievalAction)
      }

      "render the view correctly for the company previous address" in {
        val address = Address(
          "address-line-1",
          "address-line-2",
          None,
          None,
          Some("post-code"),
          "country"
        )
        val rows = Seq(answerRow("companyPreviousAddress.checkYourAnswersLabel",
          Seq(
            address.addressLine1,
            address.addressLine2,
            address.postcode.value,
            address.country
          ), false, Some(Link(controllers.register.company.routes.CompanyPreviousAddressController.onPageLoad(CheckMode).url))))

        val sections = answerSections(Some("company.checkYourAnswers.company.contact.details.heading"), rows)

        val retrievalAction = dataRetrievalAction(
          CompanyPreviousAddressId.toString -> address
        )
        testRenderedView(companyDetails() +: sections :+ contactDetails, retrievalAction)
      }
    }

    "on a GET request for Contact Details section " must {

      "render the view correctly for email and phone" in {
        val rows = Seq(
          answerRow(
            label = messages("email.title", companyName),
            answer = Seq("test@email"),
            changeUrl = Some(Link(controllers.register.company.routes.EmailController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("email.visuallyHidden.text", companyName))
          ),
          answerRow(
            label = messages("phone.title", companyName),
            answer = Seq("1234567890"),
            changeUrl = Some(Link(controllers.register.company.routes.PhoneController.onPageLoad(CheckMode).url)),
            visuallyHiddenLabel = Some(Message("phone.visuallyHidden.text", companyName))
          )
        )

        val sections = answerSections(Some(contactDetailsHeading), rows)

        val retrievalAction = dataRetrievalAction(
          BusinessNameId.toString -> "Test Company Name",
          "contactDetails" -> Json.obj(
            PhoneId.toString -> "1234567890",
            EmailId.toString -> "test@email"
          )
        )

        testRenderedView(
          sections = Seq(
            companyDetails(
              row = Seq(answerRow(label = "companyDetails.companyName", answer = Seq("Test Company Name")))
            ),
            companyContactDetails
          ) ++ sections, dataRetrievalAction = retrievalAction
        )
      }
    }

    "on a GET request with no existing data" must {
      "redirect to session expired page" in {
        val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }

    "on a POST request" must {
      "redirect to the next page" in {
        val result = controller().onSubmit(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

      "redirect to Session expired if there is no cached data" in {
        val result = controller(dontGetAnyData).onSubmit(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }
  }
}

object CheckYourAnswersControllerSpec extends ControllerSpecBase {

  private val companyName = "Test Company Name"
  private val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  val contactDetailsHeading = "common.checkYourAnswers.contact.details.heading"

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  def controller(dataRetrievalAction: DataRetrievalAction = getCompany) =
    new CheckYourAnswersController(
      frontendAppConfig,
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      new FakeNavigator(desiredRoute = onwardRoute),
      messagesApi,
      countryOptions
    )

  private val companyContactDetails = AnswerSection(
    Some("company.checkYourAnswers.company.contact.details.heading"),
    Seq.empty
  )

  private def companyDetails(row: Seq[AnswerRow] = Seq.empty) = AnswerSection(
    Some("company.checkYourAnswers.company.details.heading"),
    row
  )

  private val contactDetails = AnswerSection(
    Some(contactDetailsHeading),
    Seq.empty
  )
  private val address = Address(
    "address-line-1",
    "address-line-2",
    None,
    None,
    Some("post-code"),
    "country"
  )

  private def call = controllers.register.company.routes.CheckYourAnswersController.onSubmit()

  private def answerSections(sectionLabel: Option[String] = None, rows: Seq[AnswerRow]): Seq[AnswerSection] = {
    val section = AnswerSection(sectionLabel, rows)
    Seq(section)
  }

  private def answerRow(label: String, answer: Seq[String], answerIsMessageKey: Boolean = false,
                        changeUrl: Option[Link] = None, visuallyHiddenLabel: Option[Message]= None): AnswerRow = {
    AnswerRow(label, answer, answerIsMessageKey, changeUrl, visuallyHiddenLabel)
  }

  private def dataRetrievalAction(fields: (String, Json.JsValueWrapper)*): DataRetrievalAction = {
    val data = Json.obj(fields: _*)
    new FakeDataRetrievalAction(Some(data))
  }


  private def testRenderedView(sections: Seq[AnswerSection], dataRetrievalAction: DataRetrievalAction): Unit = {

    val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

    val expectedResult = check_your_answers(
      frontendAppConfig,
      sections,
      call,
      None,
      NormalMode
    )(fakeRequest, messages).toString()

    status(result) mustBe OK

    contentAsString(result) mustBe expectedResult
  }
}
