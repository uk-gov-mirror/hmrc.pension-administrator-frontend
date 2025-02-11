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

import forms.register.DeclarationWorkingKnowledgeFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.{Html, HtmlFormat}
import views.behaviours.YesNoViewBehaviours
import views.html.register.declarationWorkingKnowledge

class DeclarationWorkingKnowledgeViewSpec extends YesNoViewBehaviours {

  private val messageKeyPrefix = "declarationWorkingKnowledge"

  val form = new DeclarationWorkingKnowledgeFormProvider()()

  val view: declarationWorkingKnowledge = app.injector.instanceOf[declarationWorkingKnowledge]

  private def createView: () => HtmlFormat.Appendable = () => view(form, NormalMode)(fakeRequest, messages)

  private def createViewUsingForm: Form[_] => Html = (form: Form[_]) =>
    view(form, NormalMode)(fakeRequest, messages)

  "DeclarationWorkingKnowledge view" must {

    behave like normalPage(createView, messageKeyPrefix, "p1", "p2", "p3")

    behave like yesNoPage(createViewUsingForm, messageKeyPrefix,
      controllers.register.routes.DeclarationWorkingKnowledgeController.onSubmit(NormalMode).url,s"$messageKeyPrefix.heading")
  }

}
