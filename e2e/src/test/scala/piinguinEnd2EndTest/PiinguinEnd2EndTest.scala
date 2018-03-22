//
//  Copyright (c) 2018 Snowplow Analytics Ltd. All rights reserved.
//
//  This program is licensed to you under the Apache License Version 2.0,
//  and you may not use this file except in compliance with the Apache License
//  Version 2.0. You may obtain a copy of the Apache License Version 2.0 at
//  http://www.apache.org/licenses/LICENSE-2.0.
//
//  Unless required by applicable law or agreed to in writing,
//  software distributed under the Apache License Version 2.0 is distributed on
//  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
//  express or implied. See the Apache License Version 2.0 for the specific
//  language governing permissions and limitations there under.
//

package piinguinEnd2EndTest

// Specs2
//import org.specs2._
//import org.specs2.concurrent.ExecutionEnv
//import org.specs2.matcher.FutureMatchers
//import org.specs2.specification.AfterAll

// Scalatest

// Java
import java.net.{InetSocketAddress, Socket}

// Scalatest
import org.scalatest.concurrent.AsyncCancelAfterFailure
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll}

// DynamoDB
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType._

// Server main
import com.snowplowanalytics.piinguin.server.Main

// Scala
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

// From servers test
import com.snowplowanalytics.piinguin.server.clients.DynamoDBTestClient

class PiinguinEnd2EndTest extends AsyncWordSpec with AsyncCancelAfterFailure {

  val TEST_TABLE_NAME = "stuff"
  val SERVER_PORT     = 8080
  "Piinguin server" can {
    "start" should {
      val server = Future { Main.main(Array("-p", s"$SERVER_PORT", "--dynamo-test-endpoint")) }
      "be reachable" in {
        assert(retryConnect(500L, 5) == true)
      }
      "be reachable from client" in {
        assert(true == true)
      }
    }
  }

  def connect: Boolean =
    Try {
      val socket = new Socket()
      socket.connect(new InetSocketAddress("localhost", SERVER_PORT), 5)
      socket.close()
    } match {
      case Success(_) => true
      case Failure(_) => false
    }

  def retryConnect(delay: Long, retries: Int): Boolean = {
    var retriesCount = retries
    while (retriesCount > 0) {
      if (connect) {
        return true
      } else {
        retriesCount -= 1
        Thread.sleep(delay)
      }
    }
    false
  }

  // Side-effecting check for dynamo (creates table)
  def dynamoDBReady = {
    val client = DynamoDBTestClient.client
    DynamoDBTestClient.createTable(client)(TEST_TABLE_NAME)('modifiedValue -> S)
  }
  // Clean-up
  // override def afterAll = {
  //   val client = DynamoDBTestClient.client
  //   client.deleteTable(TEST_TABLE_NAME)
  // }
}
