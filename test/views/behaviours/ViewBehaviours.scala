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

package views.behaviours

import org.jsoup.Jsoup
import play.twirl.api.HtmlFormat
import views.ViewSpecBase

trait ViewBehaviours extends ViewSpecBase {

  def normalPageWithTitle(view: () => HtmlFormat.Appendable,
                          messageKeyPrefix: String,
                          title: String,
                          pageHeader: String,
                          expectedGuidanceKeys: String*): Unit = {
    normalPageWithNoPageTitleCheck(view, messageKeyPrefix, expectedGuidanceKeys: _*)

    "behave like a normal page" when {
      "rendered" must {
        "display the correct browser title" in {
          val doc = asDocument(view())
          assertEqualsMessage(doc, "title", title + " - " + messages("pension.scheme.administrator.title"))
        }

        "display the correct page header" in {
          val doc = asDocument(view())
          assertPageTitleEqualsMessage(doc, pageHeader)
        }
      }
    }

  }

  // TODO: This should be looked at - it IS doing a page title check!
  def normalPageWithPageTitleCheck(view: () => HtmlFormat.Appendable,
                                   messageKeyPrefix: String,
                                   expectedGuidanceKeys: String*): Unit = {
    "behave like a normal page" when {
      "rendered" must {
        "have the correct banner title" in {
          val doc = asDocument(view())
          val nav = doc.getElementById("proposition-menu")
          val span = nav.children.first
          span.text mustBe messages("site.service_name")
        }

        "display the correct browser title" in {
          val doc = asDocument(view())
          assertEqualsMessage(doc, "title", messages(s"$messageKeyPrefix.title")  + " - " + messages("pension.scheme.administrator.title"))
        }

        "display the correct guidance" in {
          val doc = asDocument(view())
          for (key <- expectedGuidanceKeys) assertContainsText(doc, messages(s"$messageKeyPrefix.$key"))
        }
      }
    }
  }

  def normalPageWithNoPageTitleCheck(view: () => HtmlFormat.Appendable,
                                      messageKeyPrefix: String,
                                      expectedGuidanceKeys: String*): Unit = {

    "behave like a normal page" when {
      "rendered" must {
        "have the correct banner title" in {
          val doc = asDocument(view())
          val nav = doc.getElementById("proposition-menu")
          val span = nav.children.first
          span.text mustBe messages("site.service_name")
        }

        "display the correct guidance" in {
          val doc = asDocument(view())
          for (key <- expectedGuidanceKeys) assertContainsText(doc, messages(s"$messageKeyPrefix.$key"))
        }
      }
    }
  }

  def normalPageWithDynamicTitle(view: () => HtmlFormat.Appendable,
                                 messageKeyPrefix: String,
                                 dynamicContent: String,
                                 expectedGuidanceKeys: String*): Unit = {

    "behave like a normal page" when {
      "rendered" must {

        normalPageWithNoPageTitleCheck(view, messageKeyPrefix, expectedGuidanceKeys: _*)

        "display the correct dynamic title" in {
          val doc = asDocument(view())
          assertContainsText(doc, messages(s"$messageKeyPrefix.title", dynamicContent))
        }
      }
    }
  }

  def normalPage(view: () => HtmlFormat.Appendable,
                 messageKeyPrefix: String,
                 expectedGuidanceKeys: String*): Unit = {

    normalPageWithPageTitleCheck(view, messageKeyPrefix, expectedGuidanceKeys: _*)

    "behave like a normal page" when {
      "rendered" must {
        "display the correct heading" in {
          val doc = asDocument(view())
          assertPageTitleEqualsMessage(doc, s"$messageKeyPrefix.heading")
        }
      }
    }
  }

  def pageWithBackLink(view: () => HtmlFormat.Appendable): Unit = {

    "behave like a page with a back link" must {
      "have a back link" in {
        val doc = asDocument(view())
        assertRenderedById(doc, "back-link")
      }
    }
  }

  def pageWithSecondaryHeader(view: () => HtmlFormat.Appendable,
                              heading: String): Unit = {

    "behave like a page with a secondary header" in {
      Jsoup.parse(view().toString()).getElementsByClass("heading-secondary").text() must include(heading)
    }

  }

  def pageWithSubmitButton(view: () => HtmlFormat.Appendable): Unit = {
    "behave like a page with a submit button" in {
      val doc = asDocument(view())
      assertRenderedById(doc, "submit")
    }
  }

  def pageWithContinueButton(view: () => HtmlFormat.Appendable, url: String, id: String): Unit = {
    "behave like a page with a continue button" in {
      val doc = asDocument(view())
      assertRenderedByCssSelector(doc, "a.button")
      assertLink(doc, id, url)
    }
  }

  def pageWithExitToGovUKLink(view: () => HtmlFormat.Appendable, url: String, id: String): Unit = {
    "behave like a page with a exit to gov.uk link" in {
      val doc = asDocument(view())
      assertLink(doc, id, url)
    }
  }

  def pageWithReturnLink(view: () => HtmlFormat.Appendable, url: => String): Unit = {
    "behave like a page with a return link" in {
      val doc = asDocument(view())
      assertLink(doc, "return-link", url)
    }
  }
}
