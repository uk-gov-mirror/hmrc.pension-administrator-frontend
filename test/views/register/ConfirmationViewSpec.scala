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

package views.register

import models.requests.DataRequest
import models.{PSAUser, UserType}
import play.twirl.api.Html
import utils.UserAnswers
import views.behaviours.ViewBehaviours
import views.html.register.confirmation

class ConfirmationViewSpec extends ViewBehaviours {

  val psaId: String = "A1234567"
  private val psaName: String = "psa name"
  private val psaEmail: String = "test@test.com"
  val view: confirmation = inject[confirmation]

  "Confirmation view where user is existing PSA" must {
    val messageKeyPrefix = "confirmation.existingPSA"
    val psaUser = PSAUser(UserType.Individual, None, isExistingPSA = true, None, None, "")
    val request = DataRequest(fakeRequest, "cacheId", psaUser, UserAnswers())

    def createView(): () => Html = () => view(psaId, psaName, psaEmail)(request, messages)

    behave like normalPageWithPageTitleCheck(createView(), messageKeyPrefix)


    "behave like a normal page" when {
      "rendered" must {
        "display the correct heading" in {
          val doc = asDocument(createView()())
          assertPageTitleEqualsMessage(doc,
            messages(s"$messageKeyPrefix.heading", psaName))
        }
      }
    }


    "display the PSA ID number text" in {
      createView mustNot haveDynamicText("confirmation.psaIdNumber")
    }

    "display the PSA ID number" in {
      createView mustNot haveDynamicText(psaId)
    }

    "display the 'what you need to know' heading " in {
      createView must haveDynamicText("confirmation.whatYouNeedToKnow.heading")
    }

    "display the 'email notification' detail " in {
      createView must haveDynamicText("confirmation.email")
    }

    "display the 'what you need to know' detail " in {
      createView must haveDynamicText(
        "confirmation.whatYouNeedToKnow.schemeLink"
      )
    }

    "have a link to register scheme" in {
      createView must haveLink(frontendAppConfig.registerSchemeUrl, "register-scheme-link")
    }

    behave like pageWithSubmitButton(createView())

    "have a link to 'print this screen'" in {
      createView must haveLinkOnClick("window.print();return false;", "print-this-page-link")
    }

  }

  "Confirmation view where user is new PSA" must {

    val messageKeyPrefix = "confirmation.newPSA"
    val psaUser = PSAUser(UserType.Individual, None, isExistingPSA = false, None, None, "")

    def createView(): () => Html = () => view(psaId, psaName, psaEmail)(DataRequest(fakeRequest, "cacheId", psaUser, UserAnswers()), messages)

    behave like normalPageWithPageTitleCheck(createView(), messageKeyPrefix)

    "behave like a normal page" when {
      "rendered" must {
        "display the correct heading" in {
          val doc = asDocument(createView()())
          assertPageTitleEqualsMessage(doc,
            messages(s"$messageKeyPrefix.heading", psaName))
        }
      }
    }

    "display the PSA ID number text" in {
      createView must haveDynamicText("confirmation.psaIdNumber")
    }

    "display the PSA ID number" in {
      createView must haveDynamicText(psaId)
    }

    "display the 'what you need to know' heading " in {
      createView must haveDynamicText("confirmation.whatYouNeedToKnow.heading")
    }

    "display the psa email " in {
      createView must haveDynamicText(psaEmail)
    }

    "display the 'what you need to know' detail " in {
      createView must haveDynamicText(
        "what-you-need-to-know",
        "confirmation.whatYouNeedToKnow.newPSA.detail",
        messages("confirmation.whatYouNeedToKnow.schemeLink")
      )
    }

    "have a link to register scheme" in {
      createView must haveLink(frontendAppConfig.registerSchemeUrl, "register-scheme-link")
    }

    behave like pageWithSubmitButton(createView())

    "have a link to 'print this screen'" in {
      createView must haveLinkOnClick("window.print();return false;", "print-this-page-link")
    }

  }

}
