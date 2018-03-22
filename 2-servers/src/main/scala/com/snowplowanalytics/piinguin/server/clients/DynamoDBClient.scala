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

package com.snowplowanalytics.piinguin.server.clients

// Scanamo
import com.gu.scanamo._
import com.gu.scanamo.error.DynamoReadError
import com.gu.scanamo.syntax._
import com.gu.scanamo.ops.ScanamoOps

// DynamoDb
import com.amazonaws.services.dynamodbv2.{
  AmazonDynamoDBAsync,
  AmazonDynamoDBAsyncClient,
  AmazonDynamoDBAsyncClientBuilder
}
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.dynamodbv2.model.{BatchWriteItemResult, DeleteItemResult}

// Scala
import scala.concurrent.{ExecutionContext, Future}

// Generated
import com.snowplowanalytics.piinguin.server.generated.protocols.piinguin._

/**
 * Piinguin Client for dynamo
 * @param client the amazon dynamo client to be used
 * @param tableName the name of the table to be used (it must exist)
 */
class DynamoDBClient(val client: AmazonDynamoDBAsync, val tableName: String)(implicit ec: ExecutionContext) {
  private val table: Table[PiiRecord] = Table[PiiRecord](tableName)

  /**
   * Write a single record asynchronously
   * @param record PiiRecord object to be written
   * @return a future for the execution of the request with either an error or the odl record if replaced
   */
  def putRecord(record: PiiRecord): Future[Option[Either[DynamoReadError, PiiRecord]]] = exec(table.put(record))

  /**
   * Get a single record asynchronously
   * @param modifiedValue the value for the table key
   * @return a future for the execution of the request with either an error or the record if found
   */
  def getRecord(modifiedValue: String): Future[Option[Either[DynamoReadError, PiiRecord]]] =
    exec(table.get('modifiedValue -> modifiedValue))

  /**
   * Delete a single record asynchronously
   * @param modifiedValue the value for the table key
   * @return a future for the execution of the request with the result of the deletion
   */
  def deleteRecord(modifiedValue: String): Future[DeleteItemResult] =
    exec(table.delete('modifiedValue -> modifiedValue))

  /**
   * Write mutltiple records asynchronously
   * @param records List of PiiRecord objects to be written
   * @return a future with the result of the batch write
   */
  def putRecords(records: List[PiiRecord]): Future[List[BatchWriteItemResult]] = exec(table.putAll(records.toSet))

  /**
   * Get mutltiple records asynchronously
   * @param records the list of requests containing the modifiedValues to be read
   * @return a future for the execution of the request with a set of either an error or the record if found
   */
  def getRecords(records: List[ReadPiiRecordRequest]): Future[Set[Either[DynamoReadError, PiiRecord]]] =
    exec(table.getAll('modifiedValue -> records.map(_.modifiedValue).toSet))

  /**
   * Delete mutltiple records asynchronously
   * @param records the list of requests containing the modifiedValues to be deleted
   * @return a future for the execution of the request with a list of the results
   */
  def deleteRecords(records: List[DeletePiiRecordRequest]): Future[List[BatchWriteItemResult]] =
    exec(table.deleteAll('modifiedValue -> records.map(_.modifiedValue).toSet))

  private def exec[A](ops: ScanamoOps[A]): Future[A] = ScanamoAsync.exec(client)(ops)

}

/**
 * Normal client for dynamodb
 */
object DynamoDBClient {
  lazy val client = AmazonDynamoDBAsyncClientBuilder.defaultClient()
}

/**
 * Test client for dynamodb using default test end point
 */
object DynamoDBTestClient {
  def client =
    AmazonDynamoDBAsyncClient
      .asyncBuilder()
      .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("dummy", "credentials")))
      .withEndpointConfiguration(new EndpointConfiguration("http://localhost:8000", ""))
      .build()
}
