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

package controllers.register.company.directors

import java.time.LocalDate

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import controllers.register.company.directors.routes._
import identifiers.register.DirectorsOrPartnersChangedId
import models.Mode.{checkMode, _}
import models._
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils._
import utils.countryOptions.CountryOptions
import utils.dataCompletion.DataCompletion
import viewmodels.{AnswerRow, AnswerSection, Link, Message}
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    FakeUserAnswersCacheConnector.reset()
    when(mockDataCompletion.isDirectorComplete(any(), any())).thenReturn(true)
  }

  "CheckYourAnswers Controller" when {

    "on a GET" must {

      Seq(NormalMode, UpdateMode).foreach { mode =>

        s"render the view correctly for all the rows of answer section in ${jsLiteral.to(mode)}" in {
          val retrievalAction = completeUserAnswers.dataRetrievalAction
          val rows = completeRows(mode)
          val sections = Seq(AnswerSection(None, rows))

          testRenderedView(
            sections, retrievalAction, mode = mode)
        }

        s"redirect to director name page when director name is not entered for ${jsLiteral.to(mode)}" in {
          val result = controller(getEmptyData).onPageLoad(mode, index)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.DirectorNameController.onPageLoad(mode, index).url
        }
      }
    }

    "on a POST" must {
      "not save the change flag but redirect to the next page on Normal mode when data is complete" in {
        val result = controller().onSubmit(NormalMode, index)(fakeRequest)

        status(result) mustBe SEE_OTHER
        FakeUserAnswersCacheConnector.verifyNot(DirectorsOrPartnersChangedId)
      }

      "not save the change flag and load the cya page on submit when data is not complete" in {
        when(mockDataCompletion.isDirectorComplete(any(), any())).thenReturn(false)
        val retrievalAction = completeUserAnswers.dataRetrievalAction
        val rows = completeRows(UpdateMode)
        val sections = Seq(AnswerSection(None, rows))

        testRenderedView(
          sections, retrievalAction, mode = UpdateMode, isComplete = false)

        FakeUserAnswersCacheConnector.verifyNot(DirectorsOrPartnersChangedId)
      }

      "save the change flag and redirect to the next page for UpdateMode on submit when data is complete" in {
        val result = controller().onSubmit(UpdateMode, index)(fakeRequest)

        status(result) mustBe SEE_OTHER
        FakeUserAnswersCacheConnector.verify(DirectorsOrPartnersChangedId, value = true)
      }
    }
  }

  private val mockDataCompletion = mock[DataCompletion]

  private def answerRow(label: String, answer: Seq[String], answerIsMessageKey: Boolean = false,
                        changeUrl: Option[Link], visuallyHiddenLabel: Option[Message]): AnswerRow = {
    AnswerRow(label, answer, answerIsMessageKey, changeUrl, visuallyHiddenLabel)
  }

  def controller(dataRetrievalAction: DataRetrievalAction = getDirector) =
    new CheckYourAnswersController(
      frontendAppConfig,
      FakeAllowAccessProvider(config = frontendAppConfig),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      mockDataCompletion,
      FakeNavigator,
      FakeUserAnswersCacheConnector,
      countryOptions,
      stubMessagesControllerComponents(),
      view
    )

  private def testRenderedView(sections: Seq[AnswerSection], dataRetrievalAction: DataRetrievalAction,
                               mode: Mode, isComplete: Boolean = true): Unit = {
    val result = controller(dataRetrievalAction).onPageLoad(mode, index)(fakeRequest)
    val expectedResult = view(
      sections,
      call(mode),
      None,
      mode,
      isComplete
    )(fakeRequest, messages).toString()

    status(result) mustBe OK
    contentAsString(result) mustBe expectedResult
  }

  private val email = "test@test.com"
  private val phone = "1234"
  private val index = Index(0)
  private val directorName = PersonName("Test", "Name")
  private val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)
  private val defaultDirectorName = Message("theDirector")

  private def call(mode: Mode): Call = CheckYourAnswersController.onSubmit(mode, index)

  private val address = Address("line1", "line2", None, None, Some("zz11zz"), "country")
  private val dob = LocalDate.now().minusYears(20)
  private val nino = ReferenceValue("AB100100A")
  private val reason = "test reason"
  private val utr = ReferenceValue("1111111111")
  private val addressYears = AddressYears.UnderAYear

  private val completeUserAnswers = UserAnswers().directorName(index = 0, directorName).
    directorDob(index = 0, dob).
    directorHasNINO(index = 0, flag = true).directorEnterNINO(index = 0, nino).directorNoNINOReason(index = 0, reason).
    directorHasUTR(index = 0, flag = true).directorEnterUTR(index = 0, utr).directorNoUTRReason(index = 0, reason = reason).
    directorAddress(index, address).directorAddressYears(index, addressYears).directorPreviousAddress(index, address).
    directorEmail(index = 0, email).directorPhone(index = 0, phone)


  private val view: check_your_answers = app.injector.instanceOf[check_your_answers]

  private def completeRows(mode: Mode) = Seq(
    answerRow(
      label = Message("directorName.cya.label"),
      answer = Seq("Test Name"),
      changeUrl = Some(Link(routes.DirectorNameController.onPageLoad(checkMode(mode), index).url)),
      visuallyHiddenLabel = Some(Message("directorName.visuallyHidden.text"))
    ),
    answerRow(
      label = Message("dob.heading").withArgs(directorName.fullName),
      answer = Seq(DateHelper.formatDate(dob)),
      changeUrl = Some(Link(routes.DirectorDOBController.onPageLoad(checkMode(mode), index).url)),
      visuallyHiddenLabel = Some(Message("dob.visuallyHidden.text").withArgs(directorName.fullName))
    ),
    answerRow(
      label = messages("hasNINO.heading", defaultDirectorName),
      answer = Seq("site.yes"),
      answerIsMessageKey = true,
      changeUrl = Some(Link(HasDirectorNINOController.onPageLoad(checkMode(mode), index).url)),
      visuallyHiddenLabel = Some(Message("hasNINO.visuallyHidden.text", defaultDirectorName))
    ),
    answerRow(
      label = messages("enterNINO.heading", defaultDirectorName),
      answer = Seq(nino.value),
      changeUrl = Some(Link(DirectorEnterNINOController.onPageLoad(checkMode(mode), index).url)),
      visuallyHiddenLabel = Some(Message("enterNINO.visuallyHidden.text", defaultDirectorName))
    ),
    answerRow(
      label = messages("whyNoNINO.heading", defaultDirectorName),
      answer = Seq(reason),
      changeUrl = Some(Link(DirectorNoNINOReasonController.onPageLoad(checkMode(mode), index).url)),
      visuallyHiddenLabel = Some(Message("whyNoNINO.visuallyHidden.text", defaultDirectorName))
    ),
    answerRow(
      label = messages("hasUTR.heading", defaultDirectorName),
      answer = Seq("site.yes"),
      answerIsMessageKey = true,
      changeUrl = Some(Link(HasDirectorUTRController.onPageLoad(checkMode(mode), index).url)),
      visuallyHiddenLabel = Some(Message("hasUTR.visuallyHidden.text", defaultDirectorName))
    ),
    answerRow(
      label = messages("enterUTR.heading", defaultDirectorName),
      answer = Seq(utr.value),
      changeUrl = Some(Link(DirectorEnterUTRController.onPageLoad(checkMode(mode), index).url)),
      visuallyHiddenLabel = Some(Message("enterUTR.visuallyHidden.text", defaultDirectorName))
    ),
    answerRow(
      label = messages("whyNoUTR.heading", defaultDirectorName),
      answer = Seq(reason),
      changeUrl = Some(Link(DirectorNoUTRReasonController.onPageLoad(checkMode(mode), index).url)),
      visuallyHiddenLabel = Some(Message("whyNoUTR.visuallyHidden.text", defaultDirectorName))
    ),
    answerRow(Message("address.checkYourAnswersLabel", defaultDirectorName),
      Seq(
        address.addressLine1,
        address.addressLine2,
        address.postcode.value,
        address.country
      ),
      answerIsMessageKey = false,
      Some(Link(CompanyDirectorAddressPostCodeLookupController.onPageLoad(checkMode(mode), index).url)),
      visuallyHiddenLabel = Some(Message("address.visuallyHidden.text", defaultDirectorName))
    ),
    answerRow(Message("addressYears.heading", defaultDirectorName),
      Seq(s"common.addressYears.${addressYears.toString}"), answerIsMessageKey = true,
      Some(Link(DirectorAddressYearsController.onPageLoad(checkMode(mode), index).url)),
      visuallyHiddenLabel = Some(Message("addressYears.visuallyHidden.text", defaultDirectorName))
    ),
    answerRow(Message("previousAddress.checkYourAnswersLabel", defaultDirectorName),
      Seq(
        address.addressLine1,
        address.addressLine2,
        address.postcode.value,
        address.country
      ),
      answerIsMessageKey = false,
      Some(Link(DirectorPreviousAddressPostCodeLookupController.onPageLoad(checkMode(mode), index).url)),
      visuallyHiddenLabel = Some(Message("previousAddress.visuallyHidden.text", defaultDirectorName))
    ),
    answerRow(
      label = messages("email.title", defaultDirectorName),
      answer = Seq(email),
      changeUrl = Some(Link(DirectorEmailController.onPageLoad(checkMode(mode), index).url)),
      visuallyHiddenLabel = Some(Message("email.visuallyHidden.text", defaultDirectorName))
    )
    ,
    answerRow(
      label = messages("phone.title", defaultDirectorName),
      answer = Seq(phone),
      changeUrl = Some(Link(DirectorPhoneController.onPageLoad(checkMode(mode), index).url)),
      visuallyHiddenLabel = Some(Message("phone.visuallyHidden.text", defaultDirectorName))
    )
  )
}
