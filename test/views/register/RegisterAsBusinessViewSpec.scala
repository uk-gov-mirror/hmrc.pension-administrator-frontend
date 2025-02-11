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

import forms.register.RegisterAsBusinessFormProvider
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.ViewSpecBase
import views.behaviours.YesNoViewBehaviours
import views.html.register.registerAsBusiness

class RegisterAsBusinessViewSpec extends YesNoViewBehaviours {

  import RegisterAsBusinessViewSpec._

  override val form: Form[Boolean] = new RegisterAsBusinessFormProvider().apply()

  "Register as Business view" must {

    behave like normalPage(createView(form), messageKeyPrefix)

    behave like yesNoPage(
      createViewUsingForm(),
      messageKeyPrefix,
      controllers.register.routes.RegisterAsBusinessController.onSubmit().url,
      s"$messageKeyPrefix.heading"
    )

    "display the correct label for no" in {
      createView(form) must haveLabel("value-no", messages("registerAsBusiness.no.label"))
    }

    behave like pageWithSubmitButton(createView(form))

  }

}

object RegisterAsBusinessViewSpec extends ViewSpecBase {

  val messageKeyPrefix: String = "registerAsBusiness"

  val view: registerAsBusiness = app.injector.instanceOf[registerAsBusiness]

  def createView(form: Form[Boolean]): () => HtmlFormat.Appendable = () =>
      view(
        form
      )(fakeRequest, messages)

  def createViewUsingForm(): Form[Boolean] => HtmlFormat.Appendable =
    form =>
      createView(form).apply()
}
