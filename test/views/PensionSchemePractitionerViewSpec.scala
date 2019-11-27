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

package views

import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.pensionSchemePractitioner

class PensionSchemePractitionerViewSpec extends ViewBehaviours {

  import PensionSchemePractitionerViewSpec._

  "pensionSchemePractitioner" must {

    behave like normalPage(createView(), messageKeyPrefix)

    "display the lede text" in {
      createView() must haveDynamicText("pensionSchemePractitioner.lede")
    }

    "display the explanation text" in {
      createView() must haveDynamicText("pensionSchemePractitioner.explanation")
    }

    "display the continue link" in {
      createView() must haveLink(frontendAppConfig.tpssUrl, "continueTpssLink")
    }

  }
  app.stop()
}

object PensionSchemePractitionerViewSpec extends ViewSpecBase {

  val messageKeyPrefix: String = "pensionSchemePractitioner"

  val view: pensionSchemePractitioner = inject[pensionSchemePractitioner]

  def createView(): () => HtmlFormat.Appendable =
    () => view()(fakeRequest, messages)

}
