/*
 * Copyright 2020 HM Revenue & Customs
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

package viewmodels.address

import models.TolerantAddress
import play.api.mvc.Call
import viewmodels.Message

case class AddressListViewModel(
                                 postCall: Call,
                                 manualInputCall: Call,
                                 addresses: Seq[TolerantAddress],
                                 title: Message,
                                 heading: Message,
                                 selectAddress: Message = Message("select.address.hint.text"),
                                 selectAddressLink: Message = Message("manual.entry.link"),
                                 psaName: Option[String] = None,
                                 selectAddressPostLink: Option[Message] = None
                               )
