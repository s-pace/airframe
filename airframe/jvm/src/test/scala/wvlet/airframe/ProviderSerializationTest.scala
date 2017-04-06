/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wvlet.airframe

import wvlet.airframe.ProviderSerializationExample._
import wvlet.airframe.ProviderVal._
import DesignSerializationTest._
/**
  *
  */
class ProviderSerializationTest extends AirframeSpec {

  "Design" should {
    "serialize design with provider" taggedAs ("ser") in {
      val testBinderDesign =
        providerDesign
        .bind[App].toProvider(provider5 _)

      val b = testBinderDesign.serialize
      val d = deserialize(b)

      val app = d.newSession.build[App]
      app shouldBe App(d1, d2, d3, d4, d5)
    }

    "serialize design with provider1" taggedAs ("ser-p1") in {
      val testBinderDesign =
        providerDesign
        .bind[App].toProvider(provider1 _)

      val b = testBinderDesign.serialize
      val d = deserialize(b)

      val app = d.newSession.build[App]
      app shouldBe App(d1, z2, z3, z4, z5)
    }

  }
}
