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

package views.register.company

import play.api.data.Form
import controllers.register.company.routes
import forms.register.company.CompanyDirectorAddressPostCodeLookupFormProvider
import models.NormalMode
import play.twirl.api.HtmlFormat
import views.behaviours.StringViewBehaviours
import views.html.register.company.companyDirectorAddressPostCodeLookup

class CompanyDirectorAddressPostCodeLookupViewSpec extends StringViewBehaviours {

  val messageKeyPrefix = "companyDirectorAddressPostCodeLookup"

  val form = new CompanyDirectorAddressPostCodeLookupFormProvider()()

  def createView: () => HtmlFormat.Appendable =
    () => companyDirectorAddressPostCodeLookup(
      frontendAppConfig,
      form,
      NormalMode)(fakeRequest, messages)

  def createViewUsingForm: Form[String] => HtmlFormat.Appendable =
    (form: Form[String]) => companyDirectorAddressPostCodeLookup(
      frontendAppConfig,
      form,
      NormalMode)(fakeRequest, messages)

  "CompanyPreviousAddressPostCodeLookup view" must {
    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, messages("site.secondaryHeader"))

    behave like stringPage(
      createViewUsingForm,
      messageKeyPrefix,
      controllers.register.company.routes.CompanyDirectorAddressPostCodeLookupController.onSubmit(NormalMode).url,
      Some(s"$messageKeyPrefix.postalCode.hint"),
      "postalCode"
    )

    "display lede text" in {
      createView must haveDynamicText(s"$messageKeyPrefix.lede")
    }

    "display enter address manually link" in {
      createView must haveLink(routes.CompanyDirectorAddressController.onPageLoad(NormalMode).url, "manual-address-link")
    }
  }
}
