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

package views.behaviours

import models.Person
import views.ViewSpecBase

trait PeopleListBehaviours {
  this: ViewSpecBase =>

  def peopleList(emptyView: View, nonEmptyView: View, people: Seq[(Int, Person)]): Unit = {
    "behave like a list of people" must {
      "not show the list if there are no people" in {
        val doc = asDocument(emptyView())
        doc.select("ul#people").size() mustBe 0
      }

      "show the list when there are one or more people" in {
        val doc = asDocument(nonEmptyView())
        doc.select("ul#people").size() mustBe 1
      }

      "display the correct number of people in the list" in {
        val doc = asDocument(nonEmptyView())
        doc.select("#people > li").size() mustBe people.size
      }

      "display the correct details for each person" in {
        val doc = asDocument(nonEmptyView())
        people.foreach { case (index, person) =>
          val name = doc.select(s"#${Person.id(index)}")
          name.size mustBe 1
          name.first.text mustBe person.name
        }
      }

      "display the delete link for each person" in {
        val doc = asDocument(nonEmptyView())
        people.foreach { case (index, person) =>
          val link = doc.select(s"#${Person.deleteLinkId(index)}")
          link.size mustBe 1
          link.first.text mustBe messages("site.delete")
          link.first.attr("href") mustBe person.deleteLink
        }
        withClue("Delete page has not been developed") {
          pending
        }
      }

      "display the edit link for each person" in {
        val doc = asDocument(nonEmptyView())
        people.foreach { case (index, person) =>
          val link = doc.select(s"#${Person.editLinkId(index)}")
          link.size mustBe 1
          link.first.text mustBe messages("site.edit")
          link.first.attr("href") mustBe person.editLink
        }
        withClue("Edit page has not been developed") {
          pending
        }
      }
    }
  }

}
