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

package views.register.company

import controllers.register.company.routes
import models.NormalMode
import play.twirl.api.Html
import views.behaviours.ViewBehaviours
import views.html.register.company.companyReview

class CompanyReviewViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "companyReview"
  val companyName = "test company name"
  val directors: Seq[String] = Seq("director a", "director b", "director c")
  val tenDirectors: Seq[String] = Seq("director a", "director b", "director c", "director d", "director e",
    "director f", "director g", "director h", "director i", "director j")

  val view: companyReview = app.injector.instanceOf[companyReview]


  def createView: () => Html = () => view(companyName, directors)(fakeRequest, messages)

  def createSecView: () => Html = () => view(companyName, tenDirectors)(fakeRequest, messages)

  "CompanyReview view" must {
    behave like normalPage(createView, messageKeyPrefix)
  }

  "display company name" in {
    createView must haveDynamicText(companyName)
  }

  "have link to edit company details" in {
    createView must haveLink(
      routes.CheckYourAnswersController.onPageLoad().url, "edit-company-details"
    )
  }

  "have link to change directors" in {
    createView must haveLink(
      routes.AddCompanyDirectorsController.onPageLoad(NormalMode).url, "edit-director-details"
    )
    createSecView must haveDynamicText("companyReview.directors.changeLink")
  }

  "contain list of directors" in {
    for (director <- directors)
      createView must haveDynamicText(director)
  }

}
