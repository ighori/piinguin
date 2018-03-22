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

import org.specs2._
import org.specs2.matcher.FutureMatchers
import org.specs2.concurrent.ExecutionEnv

class PiinguinClientSpec(implicit ee: ExecutionEnv) extends Specification with FutureMatchers {
  def is = s2"""
  This is a specification for the Piinguin client implementation:

  createPiiRecord return a Future with a SuccessMessage for a successfully created record                $e1
  createPiiRecord return a Future with a FailureMessage when unsuccessful in createing a record          $e2
  deletePiiRecord return a Future wuth a SuccessMessage for successfully deleting record                 $e3
  deletePiiRecord return a Future wuth a FailureMessage when unsuccessful in deleting a record           $e4
  readPiiRecord return a Future with a PiiRecord when successful in reading a record                     $e5
  readPiiRecord return a Future with a FailureMessage when unsuccessful in reading a record              $e6
  """

  // Needed in light of e2e test?
  def e1 = true must_== false
  def e2 = true must_== false
  def e3 = true must_== false
  def e4 = true must_== false
  def e5 = true must_== false
  def e6 = true must_== false

}
