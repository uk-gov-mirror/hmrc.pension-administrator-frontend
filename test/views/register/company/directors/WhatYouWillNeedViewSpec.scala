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

package views.register.company.directors

import models.NormalMode
import play.twirl.api.Html
import viewmodels.Message
import views.behaviours.ViewBehaviours
import views.html.register.company.directors.whatYouWillNeed

class WhatYouWillNeedViewSpec extends ViewBehaviours {

  private def call = controllers.register.company.directors.routes.DirectorNameController.onPageLoad(NormalMode, 0)

  private val messageKeyPrefix = "whatYouWillNeed.directorsOrPartners"

  val view: whatYouWillNeed = app.injector.instanceOf[whatYouWillNeed]

  private def createView: () => Html = () => view(call)(fakeRequest, messages)

  "WhatYouWillNeed view" must {
    behave like normalPageWithTitle(createView, messageKeyPrefix,
      title = Message("whatYouWillNeed.directorsOrPartners.title"),
      pageHeader = Message("whatYouWillNeed.directors.heading"),
      expectedGuidanceKeys = "body.item1", "body.item2", "body.item3", "body.item4",
      "body.item5", "body.item6", "body.item7", "body.item8")

    "display the correct paragraph" in {
      val doc = asDocument(createView())
      assertContainsText(doc, Message("whatYouWillNeed.directors.body.text"))
    }

    behave like pageWithSubmitButton(createView)

    "have anchor element with correct target and content" in {
      asDocument(createView()) must haveLinkWithUrlAndContent(
        linkId = "submit",
        url = call.url,
        expectedContent = messages("site.continue")
      )
    }
  }

}
