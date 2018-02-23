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

package views

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.{MatchResult, Matcher}
import play.twirl.api.{Html, HtmlFormat}

trait ViewSpecBase extends SpecBase {

  type View = () => HtmlFormat.Appendable

  def asDocument(html: Html): Document = Jsoup.parse(html.toString())

  def assertEqualsMessage(doc: Document, cssSelector: String, expectedMessageKey: String) =
    assertEqualsValue(doc, cssSelector, messages(expectedMessageKey))

  def assertEqualsValue(doc : Document, cssSelector : String, expectedValue: String) = {
    val elements = doc.select(cssSelector)

    if(elements.isEmpty) throw new IllegalArgumentException(s"CSS Selector $cssSelector wasn't rendered.")

    //<p> HTML elements are rendered out with a carriage return on some pages, so discount for comparison
    assert(elements.first().html().replace("\n", "") == expectedValue)
  }

  def assertPageTitleEqualsMessage(doc: Document, expectedMessageKey: String, args: Any*) = {
    val headers = doc.getElementsByTag("h1")
    headers.size mustBe 1
    headers.first.text.replaceAll("\u00a0", " ") mustBe messages(expectedMessageKey, args:_*).replaceAll("&nbsp;", " ")
  }

  def assertContainsText(doc:Document, text: String) = assert(doc.toString.contains(text), "\n\ntext " + text + " was not rendered on the page.\n")

  def assertContainsMessages(doc: Document, expectedMessageKeys: String*) = {
    for (key <- expectedMessageKeys) assertContainsText(doc, messages(key))
  }

  def assertRenderedById(doc: Document, id: String) = {
    assert(doc.getElementById(id) != null, "\n\nElement " + id + " was not rendered on the page.\n")
  }

  def assertNotRenderedById(doc: Document, id: String) = {
    assert(doc.getElementById(id) == null, "\n\nElement " + id + " was rendered on the page.\n")
  }

  def assertRenderedByIdWithText(doc: Document, id: String, text: String) = {
    val element = doc.getElementById(id)
    assert(element != null, "\n\nElement " + id + " was not rendered on the page.\n")
    assert(element.text().equals(text), s"\n\nElement $id had text '${element.text()}' not '$text'.\n")
  }

  def assertRenderedByForWithText(doc: Document, forElement: String, text: String) = {
    val element = doc.select(s"label[for=$forElement]")
    assert(
      element.size == 1,
      s"\n\nElement for $forElement was not rendered on the page.\n")
    assert(
      element.first.text().equals(text),
      s"\n\nElement for $forElement had text '${element.first.text}' not '$text'.\n"
    )
  }

  def assertRenderedByCssSelector(doc: Document, cssSelector: String) = {
    assert(!doc.select(cssSelector).isEmpty, "Element " + cssSelector + " was not rendered on the page.")
  }

  def assertNotRenderedByCssSelector(doc: Document, cssSelector: String) = {
    assert(doc.select(cssSelector).isEmpty, "\n\nElement " + cssSelector + " was rendered on the page.\n")
  }

  def assertContainsLabel(doc: Document, forElement: String, expectedText: String, expectedHintText: Option[String] = None) = {
    val labels = doc.select(s"label[for=$forElement] span")
    assert(labels.size > 0, s"\n\nLabel for $forElement was not rendered on the page.")
    val label = labels.first
    assert(label.text() == expectedText, s"\n\nLabel for $forElement was not $expectedText")

    if (expectedHintText.isDefined) {
      assert(labels.select(".form-hint").first.text == expectedHintText.get,
        s"\n\nLabel for $forElement did not contain hint text $expectedHintText")
    }
  }

  def assertElementHasClass(doc: Document, id: String, expectedClass: String) = {
    assert(doc.getElementById(id).hasClass(expectedClass), s"\n\nElement $id does not have class $expectedClass")
  }

  def assertContainsRadioButton(doc: Document, id: String, name: String, value: String, isChecked: Boolean) = {
    assertRenderedById(doc, id)
    val radio = doc.getElementById(id)
    assert(radio.attr("name") == name, s"\n\nElement $id does not have name $name")
    assert(radio.attr("value") == value, s"\n\nElement $id does not have value $value")
    isChecked match {
      case true => assert(radio.attr("checked") == "checked", s"\n\nElement $id is not checked")
      case _ => assert(!radio.hasAttr("checked") && radio.attr("checked") != "checked", s"\n\nElement $id is checked")
    }
  }

  def haveDynamicText(messageKey: String, args: Any*): Matcher[View] = Matcher[View] {
    view =>
      val text = messages(messageKey, args:_*)
      MatchResult(
        Jsoup.parse(view().toString).toString.contains(text),
        s"text $text is not rendered on the page",
        s"text $text is rendered on the page"
      )
  }

  def haveLink(url: String, linkId: String): Matcher[View] = Matcher[View] {
    view =>
      val link = Jsoup.parse(view().toString()).select(s"a[id=$linkId]")
      val href = link.attr("href")
      MatchResult(
        href == url,
        s"href $href is not equal to the url $url",
        s"href $href is equal to the url $url"
      )
  }

}
