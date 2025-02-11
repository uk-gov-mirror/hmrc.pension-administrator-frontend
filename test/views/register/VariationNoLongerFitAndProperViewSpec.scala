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

import models.UpdateMode
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.variationNoLongerFitAndProper

class VariationNoLongerFitAndProperViewSpec extends ViewBehaviours {

  private val messageKeyPrefix = "noLongerFitAndProper"

  val view: variationNoLongerFitAndProper = app.injector.instanceOf[variationNoLongerFitAndProper]

  private def createView: () => HtmlFormat.Appendable = () =>
    view(Some("Mark Wright"), UpdateMode)(fakeRequest, messages)

  "noLongerFitAndProper view" must {

    appRunning()

    behave like normalPage(createView, messageKeyPrefix, expectedGuidanceKeys = "p2")

    behave like pageWithReturnLink(createView, controllers.routes.PsaDetailsController.onPageLoad().url)

    "display the paragraph with psa name" in {
      createView must haveDynamicText("noLongerFitAndProper.p1", "Mark Wright")
    }
  }

}
