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

package views.deregister

import views.behaviours.ViewBehaviours
import views.html.deregister.unableToStopBeingPsa

class UnableToStopBeingPsaViewSpec extends ViewBehaviours {

  private val messageKeyPrefix = "unableToStopBeingPsa"

  private val unableToStopBeingPsaView = injector.instanceOf[unableToStopBeingPsa]

  private def createView() =
    () => unableToStopBeingPsaView(
    )(fakeRequest, messages)

  "Unable to Stop Being Psa view" must {
    "have link to return to your pension schemes" in {
      createView() must haveLink(frontendAppConfig.managePensionsYourPensionSchemesUrl, linkId = "return-link")
    }

    behave like normalPage(createView(), messageKeyPrefix)

  }
}
