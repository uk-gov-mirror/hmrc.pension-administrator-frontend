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

package models.reads

import models.TolerantAddress
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json._


class TolerantAddressReadsSpec extends WordSpec with MustMatchers with OptionValues {
  "A Postcode Lookup response payload" should {
    "map correctly to a tolerant addres" when {
      val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1"), JsString("line2"), JsString("line3"), JsString("line4"))),
      "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK")))

      "We have line1" in {
        val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)

        result.addressLine1 mustBe tolerantAddressSample.addressLine1
      }

      "We have line2" in {
        val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)

        result.addressLine2 mustBe tolerantAddressSample.addressLine2
      }

      "We have line3" in {
        val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)

        result.addressLine3 mustBe tolerantAddressSample.addressLine3
      }

      "We have line4" in {
        val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)

        result.addressLine4 mustBe tolerantAddressSample.addressLine4
      }

      "we have a postcode" in {
        val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)

        result.postcode mustBe tolerantAddressSample.postcode
      }

      "we have a country code" in {
        val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)

        result.country mustBe tolerantAddressSample.country
      }

      "we have a town" which {
        "maps to line2" in {
          val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1"))),
            "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK"), "town" -> JsString("Tyne & Wear")))
          val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)
          val expectedAddress = tolerantAddressSample.copy(addressLine2 = Some("Tyne and Wear"))

          result.addressLine2 mustBe expectedAddress.addressLine2
        }

        "maps to line3" in {
          val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1"),JsString("line2"))),
            "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK"), "town" -> JsString("Tyne & Wear")))
          val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)
          val expectedAddress = tolerantAddressSample.copy(addressLine3 = Some("Tyne and Wear"))

          result.addressLine3 mustBe expectedAddress.addressLine3
        }

        "maps to line4" in {
          val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1"),JsString("line2"),JsString("line3"))),
            "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK"), "town" -> JsString("Tyne & Wear")))
          val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)
          val expectedAddress = tolerantAddressSample.copy(addressLine4 = Some("Tyne and Wear"))

          result.addressLine4 mustBe expectedAddress.addressLine4
        }
      }

      "we have a county" which {
        "maps to line2" in {
          val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1"))),
            "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK"), "county" -> JsString("Tyne & Wear")))
          val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)
          val expectedAddress = tolerantAddressSample.copy(addressLine2 = Some("Tyne and Wear"))

          result.addressLine2 mustBe expectedAddress.addressLine2
        }

        "maps to line3" in {
          val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1"),JsString("line2"))),
            "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK"), "county" -> JsString("Tyne & Wear")))
          val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)
          val expectedAddress = tolerantAddressSample.copy(addressLine3 = Some("Tyne and Wear"))

          result.addressLine3 mustBe expectedAddress.addressLine3
        }

        "maps to line4" in {
          val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1"),JsString("line2"),JsString("line3"))),
            "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK"), "county" -> JsString("Tyne & Wear")))
          val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)
          val expectedAddress = tolerantAddressSample.copy(addressLine4 = Some("Tyne and Wear"))

          result.addressLine4 mustBe expectedAddress.addressLine4
        }
      }

      

      "we have a list of addresses" in {
        val addresses = JsArray(Seq(payload,payload))

        val result = addresses.as[Seq[TolerantAddress]](TolerantAddress.postCodeLookupReads)

        result.head.country mustBe tolerantAddressSample.country
      }

      "we have a & in the address" in {
        val tolerantAddressSample = TolerantAddress(Some("line1 and line1"), Some("line2 and line2"), Some("line3 and line3"), Some("line4 and line4"),Some("ZZ1 1ZZ"),Some("UK"))
        val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1 & line1"), JsString("line2 & line2"), JsString("line3 & line3"), JsString("line4 & line4"))),
          "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK")))

        val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)

        result.addressLine1 mustBe tolerantAddressSample.addressLine1
        result.addressLine2 mustBe tolerantAddressSample.addressLine2
        result.addressLine3 mustBe tolerantAddressSample.addressLine3
        result.addressLine4 mustBe tolerantAddressSample.addressLine4
      }
    }
  }

  val tolerantAddressSample = TolerantAddress(Some("line1"), Some("line2"), Some("line3"), Some("line4"),Some("ZZ1 1ZZ"),Some("UK"))
}
