/*
 * Copyright 2016 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package system.packages

import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner
import spray.json._

import common.JsHelpers
import common.TestHelpers
import common.Wsk
import common.WskActorSystem
import common.WskProps
import common.WskTestHelpers

@RunWith(classOf[JUnitRunner])
class KafkaFeedTests
  extends FlatSpec
    with Matchers
    with WskActorSystem
    with BeforeAndAfterAll
    with TestHelpers
    with WskTestHelpers
    with JsHelpers {

  implicit val wskprops = WskProps()
  val wsk = new Wsk()
  val actionFile = "../action/kafkaFeed.js"

  behavior of "Kafka feed action"

  it should "reject invocation when topic argument is missing" in withAssetCleaner(wskprops) {
    (wp, assetHelper) =>
      val actionName = "missingTopicAction"
      val expectedOutput = JsObject(
        "error" -> JsString("You must supply a \"topic\" parameter.")
      )

      assetHelper.withCleaner(wsk.action, actionName) {
        (action, _) => action.create(actionName, Some(actionFile))
      }

      val run = wsk.action.invoke(actionName, parameterFile = Some("dat/missingTopic.json"))

      withActivation(wsk.activation, run) {
        activation =>
          activation.response.result shouldBe Some(expectedOutput)
      }
  }

  it should "reject invocation when brokers argument is missing" in withAssetCleaner(wskprops) {
    (wp, assetHelper) =>
      val actionName = "missingBrokersAction"
      val expectedOutput = JsObject(
        "error" -> JsString("You must supply a \"brokers\" parameter as an array of Kafka brokers.")
      )

      assetHelper.withCleaner(wsk.action, actionName) {
        (action, _) => action.create(actionName, Some(actionFile))
      }

      val run = wsk.action.invoke(actionName, parameterFile = Some("dat/missingBrokers.json"))

      withActivation(wsk.activation, run) {
        activation =>
          activation.response.result shouldBe Some(expectedOutput)
      }
  }


  it should "reject invocation when package_endpoint argument is missing" in withAssetCleaner(wskprops) {
    (wp, assetHelper) =>
      val actionName = "missingPackageEndpointAction"
      val expectedOutput = JsObject(
        "error" -> JsString("Could not find the package_endpoint parameter.")
      )

      assetHelper.withCleaner(wsk.action, actionName) {
        (action, _) => action.create(actionName, Some(actionFile))
      }

      val run = wsk.action.invoke(actionName, parameterFile = Some("dat/missingPackageEndpoint.json"))

      withActivation(wsk.activation, run) {
        activation =>
          activation.response.result shouldBe Some(expectedOutput)
      }
  }

  it should "reject invocation when default namespace is used" in withAssetCleaner(wskprops) {
    (wp, assetHelper) =>
      val actionName = "defaultNamespaceAction"
      val expectedOutput = JsObject(
        "error" -> JsString("You must supply a non-default namespace.")
      )

      assetHelper.withCleaner(wsk.action, actionName) {
        (action, _) => action.create(actionName, Some(actionFile))
      }

      val run = wsk.action.invoke(actionName, parameterFile = Some("dat/defaultNamespace.json"))

      withActivation(wsk.activation, run) {
        activation =>
          activation.response.result shouldBe Some(expectedOutput)
      }
  }

}
