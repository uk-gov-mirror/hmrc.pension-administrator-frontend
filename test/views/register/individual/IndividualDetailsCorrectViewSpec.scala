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

package views.register.individual

import controllers.register.individual.routes
import forms.register.individual.IndividualDetailsCorrectFormProvider
import models.{NormalMode, TolerantAddress, TolerantIndividual}
import play.api.data.Form
import play.twirl.api.Html
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

  val view: individualDetailsCorrect = app.injector.instanceOf[individualDetailsCorrect]

  private def createView: () => Html = () => view(form, NormalMode, individual, address, new CountryOptions(environment, frontendAppConfig))(fakeRequest, messages)

  private def createViewUsingForm: Form[_] => Html = (form: Form[_]) => view(form, NormalMode, individual, address, new CountryOptions(environment, frontendAppConfig))(fakeRequest, messages)

  "IndividualDetailsCorrect view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like yesNoPage(createViewUsingForm, messageKeyPrefix, routes.IndividualDetailsCorrectController.onSubmit(NormalMode).url, s"$messageKeyPrefix.heading")

    behave like pageWithSubmitButton(createView)

    "display the individual's name" in {
      val doc = asDocument(createView())
      assertRenderedByIdWithText(doc, "individual-value", individual.fullName)
    }

    "display the individual's address" in {
      val doc = asDocument(createView())
      assertRenderedByIdWithText(doc, "address-value-0", address.addressLine1.value)
      assertRenderedByIdWithText(doc, "address-value-1", address.addressLine2.value)
      assertRenderedByIdWithText(doc, "address-value-2", address.addressLine3.value)
      assertRenderedByIdWithText(doc, "address-value-3", address.addressLine4.value)
      assertRenderedByIdWithText(doc, "address-value-4", address.postcode.value)
      assertRenderedByIdWithText(doc, "address-value-5", countryName)
    }

  }

}
