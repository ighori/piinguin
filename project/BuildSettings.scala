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

// SBT
import sbt._
import Keys._

// Sbt buildinfo plugin
import sbtbuildinfo.BuildInfoPlugin.autoImport._

// Protobuf generator plugin
import sbtprotoc.ProtocPlugin.autoImport.PB

// Scalafmt plugin
import com.lucidchart.sbt.scalafmt.ScalafmtPlugin._
import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport._

object BuildSettings {

  lazy val commonSettings = basicSettings ++ formatting ++ commonDependencies

  lazy val serverSettings = commonSettings ++ serverDependencies

  lazy val protoGenSettings = grpcSources ++ grpcGenDependencies

  lazy val e2eTestSettings = e2eTestDependencies ++ formatting

  lazy val basicSettings = Seq(
    organization := "com.snowplowanalytics",
    version := "0.1.0",
    scalaVersion := "2.12.5",
    scalacOptions := compilerOptions,
    scalacOptions in Test := Seq("-Yrangepos"),
    javacOptions := javaCompilerOptions)

  lazy val compilerOptions = Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:implicitConversions",
    "-unchecked",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused",
    "-Ywarn-unused-import",
    "-Xfuture",
    "-Xlint",
    "-Xfatal-warnings")

  lazy val javaCompilerOptions = Seq(
    "-source", "1.8",
    "-target", "1.8")

  lazy val buildInfo = Seq(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.snowplowanalytics.piinguin",
    buildInfoOptions += BuildInfoOption.BuildTime)

  lazy val commonDependencies = Seq(
    libraryDependencies ++= Seq(
      Dependencies.Libraries.slf4jApi,
      Dependencies.Libraries.slf4jSimple,
      // Test
      Dependencies.Libraries.specs2))

  lazy val serverDependencies = Seq(
    libraryDependencies ++= Seq(
      Dependencies.Libraries.scopt,
      Dependencies.Libraries.grpcNetty,
      Dependencies.Libraries.scanamo))

  lazy val grpcGenDependencies = Seq(
    libraryDependencies ++= Seq(
      Dependencies.Libraries.grpcNetty,
      Dependencies.Libraries.scalaPBRuntimeGrpc))

  lazy val e2eTestDependencies = Seq(
    libraryDependencies ++= Seq(
      Dependencies.Libraries.scalatest
    )
  )

  lazy val grpcSources = Seq(
    PB.protocVersion := "-v351",
    (PB.targets in Compile) := Seq(scalapb.gen() -> (sourceManaged in Compile).value))

  lazy val formatting = Seq(
    scalafmtConfig := file(".scalafmt.conf"),
    scalafmtOnCompile := true,
    scalafmtVersion := "1.3.0")
}
