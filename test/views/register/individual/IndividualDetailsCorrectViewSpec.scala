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

package views.register.individual

import controllers.register.individual.routes
import forms.register.individual.IndividualDetailsCorrectFormProvider
import models.{NormalMode, TolerantAddress, TolerantIndividual}
import play.api.data.Form
import utils.countryOptions.CountryOptions
import views.behaviours.YesNoViewBehaviours
import views.html.register.individual.individualDetailsCorrect

class IndividualDetailsCorrectViewSpec extends YesNoViewBehaviours {

  private val messageKeyPrefix = "individualDetailsCorrect"

  val form = new IndividualDetailsCorrectFormProvider()()

  private val individual = TolerantIndividual(
    Some("John"),
    Some("T"),
    Some("Doe")
  )

  private val address = TolerantAddress(
    Some("Building Name"),
    Some("1 Main Street"),
    Some("Some Village"),
    Some("Some Town"),
    Some("ZZ1 1ZZ"),
    Some("GB")
  )

  private val countryName = "United Kingdom"

  private def createView = () => individualDetailsCorrect(frontendAppConfig, form, NormalMode, individual, address, new CountryOptions(environment, frontendAppConfig))(fakeRequest, messages)

  private def createViewUsingForm = (form: Form[_]) => individualDetailsCorrect(frontendAppConfig, form, NormalMode, individual, address, new CountryOptions(environment, frontendAppConfig))(fakeRequest, messages)

  "IndividualDetailsCorrect view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like yesNoPage(createViewUsingForm, messageKeyPrefix, routes.IndividualDetailsCorrectController.onSubmit(NormalMode).url, s"$messageKeyPrefix.heading")

    behave like pageWithSecondaryHeader(createView, messages("site.secondaryHeader"))

    behave like pageWithSubmitButton(createView)

    "display the individual's name" in {
      val doc = asDocument(createView())
      assertRenderedByIdWithText(doc, "individual-title", messages("individualDetailsCorrect.name"))
      assertRenderedByIdWithText(doc, "individual-value", individual.fullName)
    }

    "display the individual's address" in {
      val doc = asDocument(createView())
      assertRenderedByIdWithText(doc, "address-title", messages("individualDetailsCorrect.address"))
      assertRenderedByIdWithText(doc, "address-value-0", address.addressLine1.value)
      assertRenderedByIdWithText(doc, "address-value-1", address.addressLine2.value)
      assertRenderedByIdWithText(doc, "address-value-2", address.addressLine3.value)
      assertRenderedByIdWithText(doc, "address-value-3", address.addressLine4.value)
      assertRenderedByIdWithText(doc, "address-value-4", countryName)
      assertRenderedByIdWithText(doc, "address-value-5", address.postcode.value)
    }

  }

}
