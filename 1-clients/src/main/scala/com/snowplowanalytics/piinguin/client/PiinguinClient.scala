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

package com.snowplowanalytics.piinguin.client

import java.net.URL

import com.snowplowanalytics.piinguin.server.generated.protocols.piinguin.ReadPiiRecordRequest.LawfulBasisForProcessing
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

// Generated
import com.snowplowanalytics.piinguin.server.generated.protocols.piinguin._

object PiinguinClient {
  type PiiRecord = com.snowplowanalytics.piinguin.server.generated.protocols.piinguin.PiiRecord
}

/**
 * Piinguin client implementation following the piinguing GRPC protocol
 * @param url the server URL
 */
class PiinguinClient(url: URL)(implicit ec: ExecutionContext) {
  import PiinguinClient._

  private val logger  = LoggerFactory.getLogger(classOf[PiinguinClient].getName)
  private val channel = ManagedChannelBuilder.forAddress(url.getHost, url.getPort).usePlaintext(true).build()
  private val stub    = PiinguinGrpc.stub(channel)

  /**
   * createPiiRecord creates a single piirecord using a single request asynchronusly
   * @param modifiedValue the modified value of the record
   * @param originalValue the original value for the record
   * @return a future for the execution that returns eitehr a failure message or a success message
   */
  def createPiiRecord(modifiedValue: String, originalValue: String): Future[Either[String, String]] = {
    logger.debug(s"Sending single create record request for modifiedValue: $modifiedValue originalValue: originalValue")
    stub.createPiiRecord(CreatePiiRecordRequest(Some(PiiRecord(modifiedValue, originalValue)))).transform {
      case Success(ChangeRecordResponse(None)) => errSuccess("Failed to get a response")
      case Success(ChangeRecordResponse(Some(error))) =>
        if (error.isSuccess) okSuccess(error.message) else errSuccess(error.message)
      case Failure(t) => errSuccess(t.getMessage)
    }
  }

  /**
   * deletePiiRecord deletes a single piirecord using a single request asynchronusly
   * @param modifiedValue the modified value of the record
   * @return a future for the execution that returns eitehr a failure message or a success message
   */
  def deletePiiRecord(modifiedValue: String): Future[Either[String, String]] = {
    logger.debug(s"Sending single delete record request for modifiedValue: $modifiedValue")
    stub.deletePiiRecord(DeletePiiRecordRequest(modifiedValue)).transform {
      case Success(ChangeRecordResponse(None)) => errSuccess("Failed to get a response")
      case Success(ChangeRecordResponse(Some(error))) =>
        if (error.isSuccess) okSuccess(error.message) else errSuccess(error.message)
      case Failure(t) => errSuccess(t.getMessage)
    }
  }

  /**
   * readPiiRecord reads a single piirecord using a single request asynchronusly
   * @param modifiedValue the modified value of the record
   * @param basisForProcessing the legal basis for reuesting to read this record
   * @return a future for the execution that returns eitehr a failure message or a pii record
   */
  def readPiiRecord(modifiedValue: String,
                    basisForProcessing: LawfulBasisForProcessing): Future[Either[String, PiiRecord]] = {
    logger.debug(
      s"Sending single read record request for modifiedValue: $modifiedValue and basisForProcessing: $basisForProcessing")
    stub.readPiiRecord(ReadPiiRecordRequest(modifiedValue, basisForProcessing)).transform {
      case Success(ReadPiiRecordResponse(_, Some(error))) if (!error.isSuccess) => errSuccess(error.message)
      case Success(ReadPiiRecordResponse(Some(piiRecord), _)) => Success(Right(piiRecord))
      case Failure(t)                                         => errSuccess(t.getMessage)
    }
  }

  /**
   * createPiiRecords creates a number of piirecords using bidirectional streaming
   * @param kvPairs a list of key-value tuples where the key is the modifiedValue and the value is the originalValue
   * @return a future for the execution that returns either a failure message or a success message
   */
  def createRecords(kvPairs: List[(String, String)]): List[Either[String, String]] = {
    logger.info("Streaming create records started")
    val responseStream = ListBuffer[Either[String, String]]()

    val responseObserver = new StreamObserver[ChangeRecordResponse] {
      override def onError(t: Throwable): Unit = responseStream += err(s"${t.getMessage}")

      override def onCompleted(): Unit = {}

      override def onNext(value: ChangeRecordResponse): Unit = responseStream += {
        value match {
          case ChangeRecordResponse(None)        => err("Failed to get a response")
          case ChangeRecordResponse(Some(error)) => if (error.isSuccess) ok(error.message) else err(error.message)
        }
      }
    }
    val sth: StreamObserver[CreatePiiRecordRequest] = stub.createPiiRecords(responseObserver)
    kvPairs.foreach {
      case (modifiedValue: String, originalValue: String) =>
        sth.onNext(CreatePiiRecordRequest(Some(PiiRecord(modifiedValue, originalValue))))
    }
    sth.onCompleted()
    logger.info("Streaming create records completed")
    responseStream.toList
  }

  /**
   * deletePiiRecords deletes a number of piirecords using bidirectional streaming
   * @param keys a list of modifiedValues idientifying the records to be deleted
   * @return a future for the execution that returns either a failure message or a success message
   */
  def deleteRecords(keys: List[String]): List[Either[String, String]] = {
    logger.info("Streaming delete records started")
    val responseStream = ListBuffer[Either[String, String]]()

    val responseObserver = new StreamObserver[ChangeRecordResponse] {
      override def onError(t: Throwable): Unit = responseStream += err(s"${t.getMessage}")

      override def onCompleted(): Unit = {}

      override def onNext(value: ChangeRecordResponse): Unit = responseStream += {
        value match {
          case ChangeRecordResponse(None)        => err("Failed to get a response")
          case ChangeRecordResponse(Some(error)) => if (error.isSuccess) ok(error.message) else err(error.message)
        }
      }
    }
    val sth: StreamObserver[DeletePiiRecordRequest] = stub.deletePiiRecords(responseObserver)
    keys.foreach {
      case modifiedValue: String => sth.onNext(DeletePiiRecordRequest(modifiedValue))
    }
    sth.onCompleted()
    logger.info("Streaming delete records completed")
    responseStream.toList
  }

  /**
   * readPiiRecords reads a number of piirecords using bidirectional streaming
   * @param keys a list of tuples containgn the modifiedValues along with the lawful basis for processing value for each
   * @return a future for the execution that returns either a failure message or a success message
   */
  def readRecords(keys: List[(String, LawfulBasisForProcessing)]): List[Either[String, PiiRecord]] = {
    logger.info("Streaming read records started")
    val responseStream = ListBuffer[Either[String, PiiRecord]]()

    val responseObserver = new StreamObserver[ReadPiiRecordResponse] {
      override def onError(t: Throwable): Unit = responseStream += err(s"${t.getMessage}")

      override def onCompleted(): Unit = {}

      override def onNext(value: ReadPiiRecordResponse): Unit = responseStream += {
        value match {
          case ReadPiiRecordResponse(Some(_), Some(error)) if (!error.isSuccess) => err(error.message)
          case ReadPiiRecordResponse(Some(piiRecord), _) => Right(piiRecord)
        }
      }
    }
    val sth: StreamObserver[ReadPiiRecordRequest] = stub.readPiiRecords(responseObserver)
    keys.foreach {
      case (modifiedValue: String, lb: LawfulBasisForProcessing) => sth.onNext(ReadPiiRecordRequest(modifiedValue, lb))
    }
    sth.onCompleted()
    logger.info("Streaming records records completed")
    responseStream.toList
  }

  /**
   * Private methods returning the result of the execution
   */
  private def okSuccess(msg: String)  = Success(ok(msg))
  private def errSuccess(msg: String) = Success(err(msg))

  private def ok(msg: String)  = Right(msg)
  private def err(msg: String) = Left(msg)
}
