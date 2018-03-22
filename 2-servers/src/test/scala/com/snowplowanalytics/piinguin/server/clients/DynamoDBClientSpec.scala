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

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDB, AmazonDynamoDBAsyncClient}
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType._
import com.amazonaws.services.dynamodbv2._
import com.amazonaws.services.dynamodbv2.model.{DeleteItemResult, _}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import org.specs2._
import org.specs2.matcher.{EitherMatchers, FutureMatchers, OptionMatchers}
import org.specs2.specification.{AfterEach, BeforeEach, ForEach}
import com.gu.scanamo.error.DynamoReadError
import com.snowplowanalytics.piinguin.server.generated.protocols.piinguin.PiiRecord
import org.specs2.execute.{AsResult, Result}
import com.gu.scanamo._
import com.gu.scanamo.syntax._

import scala.collection.JavaConverters._
import java.util.UUID.randomUUID

class DynamoDBClientSpec
    extends Specification
    with FutureMatchers
    with OptionMatchers
    with EitherMatchers
    with DynamoDBContext {
  def is = s2"""
  This is a specification for the DynamoDB client implementation:

  putRecord should return a None in the first put                                $e1
  putRecord should return a Some with the old record in the second put           $e2
  getRecord should return a Future[Option[Either[DynamoReadError, PiiRecord]]]   $e3
  deleteRecord should return a Future[DeleteItemResult]                          $e4
  """

  def e1 = { dbc: DynamoDBClient =>
    val put1 = dbc.putRecord(PiiRecord("12", "67"))
    Await.result(put1, 10 seconds) must beNone
  }
  def e2 = { dbc: DynamoDBClient =>
    val put1 = dbc.putRecord(PiiRecord("12", "67"))
    Await.result(put1, 10 seconds)
    val put2 = dbc.putRecord(PiiRecord("12", "77"))
    Await.result(put2, 10 seconds) must beSome.like {
      case (eith: Either[DynamoReadError, PiiRecord]) => eith must beRight(PiiRecord("12", "67"))
    }
  }
  def e3 = { dbc: DynamoDBClient =>
    val put1 = dbc.putRecord(PiiRecord("12", "67"))
    Await.result(put1, 10 seconds)
    val get = dbc.getRecord("12")
    Await.result(get, 10 seconds) must beSome.like {
      case (eith: Either[DynamoReadError, PiiRecord]) => eith must beRight(PiiRecord("12", "67"))
    }
  }
  def e4 = { dbc: DynamoDBClient =>
    val put1 = dbc.putRecord(PiiRecord("12", "67"))
    Await.result(put1, 10 seconds)
    val delete = dbc.deleteRecord("12")
    Await.result(delete, 10 seconds) must beLike {
      case (dir: DeleteItemResult) => dir must haveClass[DeleteItemResult]
    }
  }
}

trait DynamoDBContext extends ForEach[DynamoDBClient] {
  val DYNAMO_DB_TABLE = "piitest"
  def foreach[R: AsResult](test: DynamoDBClient => R): Result = {
    val tableName = s"$DYNAMO_DB_TABLE-${randomUUID().toString}"
    val client    = DynamoDBTestClient.client
    DynamoDBTestUtils.createTable(client)(tableName)('modifiedValue -> S)
    val dbc = new DynamoDBClient(client, tableName)(ExecutionContext.global)
    try AsResult(test(dbc))
    finally {
      val tables = client.listTables.getTableNames
      if (tables.contains(tableName)) client.deleteTable(tableName)
    }
  }

}

object DynamoDBTestUtils {

  def createTable(client: AmazonDynamoDB)(tableName: String)(attributes: (Symbol, ScalarAttributeType)*) =
    client.createTable(
      attributeDefinitions(attributes),
      tableName,
      keySchema(attributes),
      arbitraryThroughputThatIsIgnoredByDynamoDBLocal
    )

  private def attributeDefinitions(attributes: Seq[(Symbol, ScalarAttributeType)]) =
    attributes.map { case (symbol, attributeType) => new AttributeDefinition(symbol.name, attributeType) }.asJava

  private def keySchema(attributes: Seq[(Symbol, ScalarAttributeType)]) = {
    val hashKeyWithType :: rangeKeyWithType = attributes.toList
    val keySchemas                          = hashKeyWithType._1 -> KeyType.HASH :: rangeKeyWithType.map(_._1 -> KeyType.RANGE)
    keySchemas.map { case (symbol, keyType) => new KeySchemaElement(symbol.name, keyType) }.asJava
  }

  private val arbitraryThroughputThatIsIgnoredByDynamoDBLocal = new ProvisionedThroughput(1L, 1L)
}
