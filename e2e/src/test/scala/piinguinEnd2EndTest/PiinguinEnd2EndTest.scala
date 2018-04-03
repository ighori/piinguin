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

// Java
import java.net.{InetSocketAddress, Socket, URL}

// Scalatest
import org.scalatest.{BeforeAndAfterAll, CancelAfterFailure, WordSpec}
import org.scalatest.Matchers._

// DynamoDB
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType._
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException

// Scala
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Right, Success, Try}

// Server main
import com.snowplowanalytics.piinguin.server.Main

// Client
import com.snowplowanalytics.piinguin.client.PiinguinClient
import com.snowplowanalytics.piinguin.client.SuccessMessage

// From servers test
import com.snowplowanalytics.piinguin.server.clients.{DynamoDBTestClient, DynamoDBTestUtils}


class PiinguinEnd2EndTest extends WordSpec with CancelAfterFailure with BeforeAndAfterAll {
  import ExecutionContext.Implicits.global
  val TEST_TABLE_NAME = "stuff"
  val SERVER_PORT     = 8080
  "Piinguin server" can {
    "start" should {
      "be reachable" in {
        assert(retryConnect(500L, 5) == true)
      }
      "be possible to create a record" in {
        val url            = new URL(s"http://localhost:$SERVER_PORT")
        val piinguinClient = new PiinguinClient(url)
        val result         = Await.result(piinguinClient.createPiiRecord("1234", "567"), 10 seconds)
        result should be(Right(SuccessMessage("OK")))
      }
    }
  }

  /**
   * This method is used to see if a connection is possible to the Piingiin Server
   */
  private def connect: Boolean =
    Try {
      val socket = new Socket()
      socket.connect(new InetSocketAddress("localhost", SERVER_PORT), 5)
      socket.close()
    } match {
      case Success(_) => true
      case Failure(_) => false
    }

  /**
   * This retries the connection with the specified delay for the specified number of retires
   */
  private def retryConnect(delay: Long, retries: Int): Boolean = {
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

  /**
   * Start the server and create the table before all tests
   */
  override def beforeAll = {
    val client = DynamoDBTestClient.client
    try { client.deleteTable(TEST_TABLE_NAME) } catch { case r:ResourceNotFoundException => {} } // Make sure that the table does not exist from
    DynamoDBTestUtils.createTable(client)(TEST_TABLE_NAME)('modifiedValue -> S)
    val _      = Future { Main.main(Array("-p", s"$SERVER_PORT", "-t", TEST_TABLE_NAME, "--dynamo-test-endpoint")) }
  }

  /**
   * Delete the table
   */
  override def afterAll = {
    val client = DynamoDBTestClient.client
    client.deleteTable(TEST_TABLE_NAME)
  }
}
