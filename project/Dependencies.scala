import sbt._
import scalapb.compiler.Version.{ grpcJavaVersion, scalapbVersion, protobufVersion }

object Dependencies {
  object V {
    val slf4jVersion       = "1.7.25"
    val specs2Version      = "4.0.2"
    val scanamoVersion     = "1.0.0-M5"
    val scoptVersion       = "3.7.0"
    val scalatestVersion   = "3.0.5"
  }

  object Libraries {
    // Java
    val grpcNetty          = "io.grpc"              %  "grpc-netty"           % grpcJavaVersion
    val slf4jApi           = "org.slf4j"            %  "slf4j-api"            % V.slf4jVersion
    val slf4jSimple        = "org.slf4j"            %  "slf4j-simple"         % V.slf4jVersion
    // Scala
    val scopt              = "com.github.scopt"     %% "scopt"                % V.scoptVersion
    val scalaPBRuntimeGrpc = "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapbVersion
    val scanamo            = "com.gu"               %% "scanamo"              % V.scanamoVersion
    // Test
    val specs2             = "org.specs2"           %% "specs2-core"          % V.specs2Version % "test"
    val scalatest          = "org.scalatest"        %% "scalatest"            % V.scalatestVersion % "test"
  }
}
