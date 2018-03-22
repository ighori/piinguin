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

package com.snowplowanalytics.piinguin.server

import com.snowplowanalytics.piinguin.server.clients.{DynamoDBClient, DynamoDBTestClient}
import io.grpc.{Server, ServerBuilder}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext

// This project
import implementations.PiinguinImpl

// Generated
import com.snowplowanalytics.piinguin.server.generated.protocols.piinguin.PiinguinGrpc

/**
 * GRPC Piinguin server
 */
class PiinguinServer(port: Int, dynamoTestEndPoint: Boolean)(implicit executionContext: ExecutionContext) { self =>

  private val logger = LoggerFactory.getLogger(classOf[PiinguinServer].getName)

  private val dynamoDbClient = if (dynamoTestEndPoint) DynamoDBTestClient.client else DynamoDBClient.client
  private val server: Server = ServerBuilder
    .forPort(port)
    .addService(
      PiinguinGrpc.bindService(new PiinguinImpl(new DynamoDBClient(dynamoDbClient, "piinguin")), executionContext))
    .build

  def start(): Unit = {
    logger.info(s"Server started on port: $port")
    server.start
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        logger.info(s"Shutdown hook started")
        self.stop()
        logger.info(s"Shutdown hook complete")
      }
    })
  }

  def stop(): Unit = {
    logger.info("Server shutting down")
    server.shutdown
    logger.info("Server shut down complete")
  }

  def blockUntilShutdown(): Unit = server.awaitTermination
}
