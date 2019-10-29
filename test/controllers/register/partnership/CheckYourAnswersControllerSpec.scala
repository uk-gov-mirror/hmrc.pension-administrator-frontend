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

package controllers.register.partnership

import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.partnership._
import models._
import play.api.libs.json.Json
import play.api.test.Helpers._
import utils.{FakeCountryOptions, FakeNavigator}
import viewmodels.{AnswerRow, AnswerSection, Link, Message}
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  import CheckYourAnswersControllerSpec._

  "CheckYourAnswers Controller" when {

    "GET" must {

      "display Partnership Details section " which {

        "renders name and utr" in {
          val rows = Seq(
            answerRow("cya.label.name", Seq("Test Company Name")),
            answerRow("businessDetails.utr", Seq("Test UTR"))
          )

          val sections = answerSections(Some("checkyouranswers.partnership.details"), rows)

          val retrievalAction = dataRetrievalAction(
            PartnershipDetailsId.toString -> BusinessDetails("Test Company Name", Some("Test UTR"))
          )
          testRenderedView(sections :+ partnershipContactDetails :+ contactDetails, retrievalAction)
        }

        "renders paye number" in {
          val rows = Seq(
            answerRow(
              "commom.paye.label",
              Seq("Test Paye"),
              false,
              Some(Link(controllers.register.partnership.routes.PartnershipPayeController.onPageLoad(CheckMode).url))
            ))

          val sections = answerSections(Some("checkyouranswers.partnership.details"), rows)

          val retrievalAction = dataRetrievalAction(
            PartnershipPayeId.toString -> Paye.Yes("Test Paye")
          )
          testRenderedView(sections :+ partnershipContactDetails :+ contactDetails, retrievalAction)
        }

        "renders vat registration number" in {
          val rows = Seq(
            answerRow(
              "common.vatRegistrationNumber.checkYourAnswersLabel",
              Seq("Test Vat"),
              false,
              Some(Link(controllers.register.partnership.routes.PartnershipVatController.onPageLoad(CheckMode).url))
            ))

          val sections = answerSections(Some("checkyouranswers.partnership.details"), rows)

          val retrievalAction = dataRetrievalAction(
            PartnershipVatId.toString -> Vat.Yes("Test Vat")
          )
          testRenderedView(sections :+ partnershipContactDetails :+ contactDetails, retrievalAction)
        }

      }

      "display Partnership Contact Details" which {

        "renders address" in {
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

          val sections = answerSections(Some("checkyouranswers.partnership.contact.details.heading"), rows)

          val retrievalAction = dataRetrievalAction(
            PartnershipRegisteredAddressId.toString -> address
          )
          testRenderedView(partnershipDetails +: sections :+ contactDetails, retrievalAction)
        }

        "renders same contact address" in {
          val rows = Seq(answerRow(
            "cya.label.common.same.contact.address",
            Seq("Yes"),
            true,
            Some(Link(controllers.register.partnership.routes.PartnershipSameContactAddressController.onPageLoad(CheckMode).url))
          ))

          val sections = answerSections(Some("checkyouranswers.partnership.contact.details.heading"), rows)

          val retrievalAction = dataRetrievalAction(
            PartnershipSameContactAddressId.toString -> true
          )
          testRenderedView(partnershipDetails +: sections :+ contactDetails, retrievalAction)
        }

        "renders contact address" in {
          val rows = Seq(answerRow(
            "cya.label.address",
            Seq(
              address.addressLine1,
              address.addressLine2,
              address.postcode.value,
              address.country
            ),
            false,
            Some(Link(routes.PartnershipContactAddressController.onPageLoad(CheckMode).url))
          ))

          val sections = answerSections(Some("checkyouranswers.partnership.contact.details.heading"), rows)

          val retrievalAction = dataRetrievalAction(
            PartnershipContactAddressId.toString -> address
          )
          testRenderedView(partnershipDetails +: sections :+ contactDetails, retrievalAction)
        }

        "renders the address years" in {
          val addressYears = AddressYears.OverAYear
          val rows = Seq(
            answerRow(
            Message("addressYears.heading", Message("thePartnership").resolve),
            Seq(s"common.addressYears.${addressYears.toString}"),
            answerIsMessageKey = true,
            Some(Link(controllers.register.partnership.routes.PartnershipAddressYearsController.onPageLoad(CheckMode).url)),
              visuallyHiddenLabel = Some(Message("addressYears.visuallyHidden.text", Message("thePartnership").resolve))
          ))

          val sections = answerSections(Some("checkyouranswers.partnership.contact.details.heading"), rows)

          val retrievalAction = dataRetrievalAction(
            PartnershipAddressYearsId.toString -> addressYears.toString
          )
          testRenderedView(partnershipDetails +: sections :+ contactDetails, retrievalAction)
        }

        "renders the previous address" in {
          val address = Address(
            "address-line-1",
            "address-line-2",
            None,
            None,
            Some("post-code"),
            "country"
          )
          val rows = Seq(answerRow(
            "common.previousAddress.checkyouranswers",
            Seq(
              address.addressLine1,
              address.addressLine2,
              address.postcode.value,
              address.country
            ),
            false,
            Some(Link(controllers.register.partnership.routes.PartnershipPreviousAddressController.onPageLoad(CheckMode).url))
          ))

          val sections = answerSections(Some("checkyouranswers.partnership.contact.details.heading"), rows)

          val retrievalAction = dataRetrievalAction(
            PartnershipPreviousAddressId.toString -> address
          )
          testRenderedView(partnershipDetails +: sections :+ contactDetails, retrievalAction)
        }
      }

      "display Contact Details section " which {

        "render the view correctly for email and phone" in {

          val partnershipDetails = AnswerSection(
            Some("checkyouranswers.partnership.details"),
            Seq()
          )

          val partnershipContactDetails = AnswerSection(
            Some("checkyouranswers.partnership.contact.details.heading"),
            Seq()
          )

          val rows = Seq(
            answerRow(
              label = messages("email.title", defaultPartnership),
              answer = Seq("test@email"),
              changeUrl = Some(Link(controllers.register.partnership.routes.PartnershipEmailController.onPageLoad(CheckMode).url)),
              visuallyHiddenLabel = Some(Message("email.visuallyHidden.text", defaultPartnership))
            ),
            answerRow(
              label = messages("phone.title", defaultPartnership),
              answer = Seq("1234567890"),
              changeUrl = Some(Link(controllers.register.partnership.routes.PartnershipPhoneController.onPageLoad(CheckMode).url)),
              visuallyHiddenLabel = Some(Message("phone.visuallyHidden.text", defaultPartnership))
            )
          )

          val sections = Seq(partnershipDetails, partnershipContactDetails, AnswerSection(Some("common.checkYourAnswers.contact.details.heading"), rows))

          val retrievalAction = dataRetrievalAction(
            "partnershipContactDetails" -> Json.obj(
              PartnershipPhoneId.toString -> "1234567890",
              PartnershipEmailId.toString -> "test@email"
            )
          )

          testRenderedView(
            sections = sections, dataRetrievalAction = retrievalAction
          )
        }

      }

      "redirect to session expired page" when {
        "no existing data" in {
          val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      }

    }

    "POST" must {
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
  private val defaultPartnership = Message("thePartnership").resolve
  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  def controller(dataRetrievalAction: DataRetrievalAction = getPartnership) =
    new CheckYourAnswersController(
      frontendAppConfig,
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      new FakeNavigator(desiredRoute = onwardRoute),
      messagesApi,
      new FakeCountryOptions(environment, frontendAppConfig)
    )

  private val partnershipContactDetails = AnswerSection(
    Some("checkyouranswers.partnership.contact.details.heading"),
    Seq.empty
  )
  private val partnershipDetails = AnswerSection(
    Some("checkyouranswers.partnership.details"),
    Seq.empty
  )
  private val contactDetails = AnswerSection(
    Some("common.checkYourAnswers.contact.details.heading"),
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

  private def call = routes.CheckYourAnswersController.onSubmit()

  private def answerSections(sectionLabel: Option[String] = None, rows: Seq[AnswerRow]): Seq[AnswerSection] = {
    val section = AnswerSection(sectionLabel, rows)
    Seq(section)
  }

  private def answerRow(label: String, answer: Seq[String], answerIsMessageKey: Boolean = false, changeUrl: Option[Link] = None,
                        visuallyHiddenLabel: Option[Message]= None): AnswerRow = {
    AnswerRow(label, answer, answerIsMessageKey, changeUrl, visuallyHiddenLabel)
  }

  private def dataRetrievalAction(fields: (String, Json.JsValueWrapper)*): DataRetrievalAction = {
    val data = Json.obj(fields: _*)
    new FakeDataRetrievalAction(Some(data))
  }

  private def testRenderedView(sections: Seq[AnswerSection], dataRetrievalAction: DataRetrievalAction): Unit = {

    def writeToDesktop(content:String, fileName:String):Unit = {
      import java.io._
      val pw = new PrintWriter(new File( s"/home/grant/Desktop/$fileName" ))
      pw.write(content)
      pw.close()
    }

    val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)
    writeToDesktop(contentAsString(result), "act.html")
    writeToDesktop(check_your_answers(
      frontendAppConfig,
      sections,
      call,
      None,
      NormalMode
    )(fakeRequest, messages).toString(), "exp.html")
    status(result) mustBe OK
    contentAsString(result) mustBe
      check_your_answers(
        frontendAppConfig,
        sections,
         call,
        None,
        NormalMode
      )(fakeRequest, messages).toString()
  }
}
